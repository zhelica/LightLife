package com.LightLife.system.domain;

import com.LightLife.common.annotation.Excel;
import com.LightLife.common.core.domain.BaseEntity;

/**
 * 第三方授权对象 sys_auth_user
 *
 * @author ruoyi
 * @date 2023-05-06
 */
public class SysAuthUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 授权ID */
    private Long authId;

    /** 第三方平台用户唯一ID */
    @Excel(name = "第三方平台用户唯一ID")
    private String uuid;

    /** 系统用户ID */
    @Excel(name = "系统用户ID")
    private Long userId;

    private String sessionKey;
    /** 手机号码 */
    @Excel(name = "手机号码")
    private String phoneNumber;

    /** 登录账号 */
    @Excel(name = "登录账号")
    private String loginName;

    /** 用户昵称 */
    @Excel(name = "用户昵称")
    private String userName;

    /** 头像地址 */
    @Excel(name = "头像地址")
    private String avatar;

    /** 用户邮箱 */
    @Excel(name = "用户邮箱")
    private String email;

    /** 用户来源 */
    @Excel(name = "用户来源")
    private String source;

    public void setAuthId(Long authId)
    {
        this.authId = authId;
    }

    public Long getAuthId()
    {
        return authId;
    }
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getUuid()
    {
        return uuid;
    }
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }
    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }
    public void setLoginName(String loginName)
    {
        this.loginName = loginName;
    }

    public String getLoginName()
    {
        return loginName;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }
    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getAvatar()
    {
        return avatar;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getEmail()
    {
        return email;
    }
    public void setSource(String source)
    {
        this.source = source;
    }

    public String getSource()
    {
        return source;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public String toString() {
        return "SysAuthUser{" +
                "authId=" + authId +
                ", uuid='" + uuid + '\'' +
                ", userId=" + userId +
                ", sessionKey='" + sessionKey + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", loginName='" + loginName + '\'' +
                ", userName='" + userName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", email='" + email + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}