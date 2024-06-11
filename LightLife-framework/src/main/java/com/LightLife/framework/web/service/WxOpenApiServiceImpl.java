package com.LightLife.framework.web.service;

import com.alibaba.fastjson2.JSONObject;
import com.LightLife.common.constant.CacheConstants;
import com.LightLife.common.constant.Constants;
import com.LightLife.common.core.domain.AjaxResult;
import com.LightLife.common.core.domain.entity.SysUser;
import com.LightLife.common.core.domain.model.LoginUser;
import com.LightLife.common.core.redis.RedisCache;
import com.LightLife.common.exception.ApiException;
import com.LightLife.system.mapper.SysUserMapper;
import com.LightLife.system.service.ISysAuthUserService;
import com.LightLife.system.service.ISysConfigService;
import com.LightLife.system.service.ISysUserService;
import com.LightLife.system.service.IWxOpenApiService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.LightLife.common.core.domain.AjaxResult.error;
import static com.LightLife.common.utils.SecurityUtils.getLoginUser;

@Service
public class WxOpenApiServiceImpl implements IWxOpenApiService {
    @Value("${wx.appid}")
    private String appid;
    @Value("${wx.secret}")
    private String secret;
    // 令牌秘钥
    @Value("${token.secret}")
    private String prosecret;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ISysAuthUserService authUserService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private SysRegisterService registerService;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysLoginService loginService;
    @Autowired
    private TokenService tokenService;
    /**
     * 获取 OpenId 和 SessionKey
     *
     * @param code 登录凭证code
     * @return OpenId 和 SessionKey
     * @throws ApiException 调用异常
     */
    public SysUser getOpenIdAndSessionKey(String code,String token) throws ApiException {
        if (StringUtils.isNotEmpty(token)){
            try
            {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                LoginUser user = redisCache.getCacheObject(userKey);
                if (user!=null){
                    //token有效继续使用
                    user.getUser().setWxUserToken(token);
                    return user.getUser();
                }
            }
            catch (Exception e)
            {
            }
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        String response = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject.containsKey("errcode")) {
            throw new ApiException(jsonObject.getInteger("errcode"), jsonObject.getString("errmsg"));
        }
        String openid = jsonObject.getString("openid");
        String sessionKey = jsonObject.getString("session_key");

        // redis缓存中不存在wx_accessToken，需要存储
        Map<String,Object> wx_open_message = new HashMap<>();
        wx_open_message.put("openid",openid);
        wx_open_message.put("sessionKey",sessionKey);
        // 存储到redis并设置过期时间
        redisCache.setCacheObject("wx_sessionKey_"+openid, wx_open_message, Constants.WX_ACCESS_TOKEN, TimeUnit.SECONDS);

        //登录
        SysUser user = userService.selectUserByUserName(openid);
        if (user!=null){
            String tokens = wxlogin(openid);
            user.setWxUserToken(tokens);
        }else {
            wxregister(openid);
            user = userService.selectUserByUserName(openid);
            String tokens = wxlogin(openid);
            user.setWxUserToken(tokens);
        }
        return user;
    }
    /**
     * 获取 AccessToken
     *
     * @param  获取 Access Token 的方式
     * @return AccessToken 信息
     * @throws ApiException 调用异常
     */
    public String getAccessToken() throws ApiException {
        String wxAccessToken = redisCache.getCacheObject("wx_accessToken");
        if (StringUtils.isNotBlank(wxAccessToken)) { // redis缓存中存在wx_accessToken
            // 判断过期时间是否小于10分钟
            Long expireTime = redisCache.getExpire("wx_accessToken");
            if (expireTime < 600L) {
                return refreshAccessToken();
            } else {
                return wxAccessToken;
            }
        } else {
            // redis缓存中不存在wx_accessToken，需要调用接口获取
            return refreshAccessToken();
        }
    }

    // 刷新access_token并存入redis缓存
    private String refreshAccessToken() throws ApiException {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" + "&appid=" + appid + "&secret=" + secret;
        String response = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject.containsKey("errcode")) {
            throw new ApiException(jsonObject.getInteger("errcode"), jsonObject.getString("errmsg"));
        }
        String accessToken = jsonObject.getString("access_token");
        redisCache.setCacheObject("wx_accessToken", accessToken, Constants.WX_ACCESS_TOKEN, TimeUnit.SECONDS); // 存储到redis并设置过期时间
        return accessToken;
    }
    public AjaxResult getPhoneNumber(String code) throws ApiException {
        RestTemplate restTemplate = new RestTemplate();
        String accessToken = getAccessToken();
        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject params = new JSONObject();
        params.put("code", code);
        HttpEntity<String> requestEntity = new HttpEntity<>(params.toJSONString(), headers);
        String response = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject.getIntValue("errcode") != 0) {
            throw new ApiException(jsonObject.getInteger("errcode"), jsonObject.getString("errmsg"));
        }
        String phoneNumber = jsonObject.getJSONObject("phone_info").getString("phoneNumber");
        SysUser user = getLoginUser().getUser();
        user.setPhonenumber(phoneNumber);
        return updateUserProfile(user);
    }

    /**
     * 微信登录 返回token
     * @param openid
     * @return
     */
    public String wxlogin(String openid){
        return loginService.wxlogin(openid);
    }
    /**
     * 微信注册 返回token
     * @param openid
     * @return
     */
    public boolean wxregister(String openid){
        return registerService.wxregister(openid);
    }
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(prosecret)
                .parseClaimsJws(token)
                .getBody();
    }
    private String getTokenKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }

    public AjaxResult updateUserProfile(SysUser user)
    {
        LoginUser loginUser = getLoginUser();
        SysUser sysUser = loginUser.getUser();
        user.setUserName(sysUser.getUserName());
        user.setUserId(sysUser.getUserId());
        user.setPassword(null);
        user.setAvatar(sysUser.getAvatar());
        if (userService.updateUserProfile(user) > 0)
        {
            // 更新缓存用户信息
            sysUser.setNickName(user.getNickName());
            sysUser.setPhonenumber(user.getPhonenumber());
            sysUser.setEmail(user.getEmail());
            sysUser.setSex(user.getSex());
            tokenService.setLoginUser(loginUser);
            AjaxResult ajaxResult = new AjaxResult();
            ajaxResult.put("loginUser",loginUser);
            return ajaxResult;
        }else {
            return error("修改个人信息异常，请联系管理员");
        }
    }
}