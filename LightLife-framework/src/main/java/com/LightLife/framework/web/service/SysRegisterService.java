package com.LightLife.framework.web.service;

import com.LightLife.common.core.domain.entity.SysRole;
import com.LightLife.common.utils.ip.IpUtils;
import com.LightLife.common.utils.uuid.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.LightLife.common.constant.CacheConstants;
import com.LightLife.common.constant.Constants;
import com.LightLife.common.constant.UserConstants;
import com.LightLife.common.core.domain.entity.SysUser;
import com.LightLife.common.core.domain.model.RegisterBody;
import com.LightLife.common.core.redis.RedisCache;
import com.LightLife.common.exception.user.CaptchaException;
import com.LightLife.common.exception.user.CaptchaExpireException;
import com.LightLife.common.utils.MessageUtils;
import com.LightLife.common.utils.SecurityUtils;
import com.LightLife.common.utils.StringUtils;
import com.LightLife.framework.manager.AsyncManager;
import com.LightLife.framework.manager.factory.AsyncFactory;
import com.LightLife.system.service.ISysConfigService;
import com.LightLife.system.service.ISysUserService;

import java.util.Date;
import java.util.List;

/**
 * 注册校验方法
 *
 * @author LightLife
 */
@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;
    @Value("${wx.password}")
    private String password;
    /**
     * 注册
     */
    public String register(RegisterBody registerBody)
    {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = "账户长度必须在2到20个字符之间";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        else if (!userService.checkUserNameUnique(sysUser))
        {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            sysUser.setNickName(username);
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag)
            {
                msg = "注册失败,请联系系统管理人员";
            }
            else
            {
                Long[] roleIds = new Long[1];
                roleIds[0]=2L;
                userService.insertUserAuth(sysUser.getUserId(),roleIds);
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }

    public boolean wxregister(String username)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setNickName("游客");
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        boolean regFlag = userService.registerUser(sysUser);
        if (!regFlag)
        {
            return false;
        }
        else
        {
            Long[] roleIds = new Long[1];
            roleIds[0]=2L;
            userService.insertUserAuth(sysUser.getUserId(),roleIds);
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
        }
        return true;
    }
}
