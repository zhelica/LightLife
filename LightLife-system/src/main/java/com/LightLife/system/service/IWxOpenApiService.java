package com.LightLife.system.service;

import com.LightLife.common.core.domain.AjaxResult;
import com.LightLife.common.core.domain.entity.SysUser;
import com.LightLife.common.exception.ApiException;

/**
 * 获取微信开放平台开放接口
 */
public interface IWxOpenApiService {
    /**
     * 微信小程序登录
     * @param code
     * @return
     * @throws ApiException
     */
    public SysUser getOpenIdAndSessionKey(String code,String token) throws ApiException;

    /**
     * 获取token用于请求用户手机号
     * @return
     * @throws ApiException
     */
    public String  getAccessToken() throws ApiException;

    /**
     * 获取用户手机号
     * @param code
     * @param openid
     * @return
     * @throws ApiException
     */
    public AjaxResult getPhoneNumber(String code) throws ApiException;

    /**
     * 微信登录认证
     * @param openid
     * @return
     */
    public String wxlogin(String openid);

    /**
     * 微信小程序注册
     * @param openid
     * @return
     */
    public boolean wxregister(String openid);

    public AjaxResult updateUserProfile(SysUser user);
}
