package com.LightLife.system.service;

import com.LightLife.system.domain.SysAuthUser;

import java.util.List;

/**
 * 第三方授权Service接口
 * 
 * @author ruoyi
 * @date 2023-05-06
 */
public interface ISysAuthUserService 
{
    /**
     * 查询第三方授权
     * 
     * @param authId 第三方授权主键
     * @return 第三方授权
     */
    public SysAuthUser selectSysAuthUserByAuthId(Long authId);

    /**
     * 查询第三方授权列表
     * 
     * @param sysAuthUser 第三方授权
     * @return 第三方授权集合
     */
    public List<SysAuthUser> selectSysAuthUserList(SysAuthUser sysAuthUser);

    /**
     * 新增第三方授权
     * 
     * @param sysAuthUser 第三方授权
     * @return 结果
     */
    public int insertSysAuthUser(SysAuthUser sysAuthUser);

    /**
     * 修改第三方授权
     * 
     * @param sysAuthUser 第三方授权
     * @return 结果
     */
    public int updateSysAuthUser(SysAuthUser sysAuthUser);

    /**
     * 批量删除第三方授权
     * 
     * @param authIds 需要删除的第三方授权主键集合
     * @return 结果
     */
    public int deleteSysAuthUserByAuthIds(Long[] authIds);

    /**
     * 删除第三方授权信息
     * 
     * @param authId 第三方授权主键
     * @return 结果
     */
    public int deleteSysAuthUserByAuthId(Long authId);
}
