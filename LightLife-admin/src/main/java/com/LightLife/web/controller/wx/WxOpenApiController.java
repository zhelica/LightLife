package com.LightLife.web.controller.wx;


import com.LightLife.common.annotation.Log;
import com.LightLife.common.core.controller.BaseController;
import com.LightLife.common.core.domain.AjaxResult;
import com.LightLife.common.core.domain.entity.SysUser;
import com.LightLife.common.enums.BusinessType;
import com.LightLife.common.exception.ApiException;
import com.LightLife.common.utils.SecurityUtils;
import com.LightLife.framework.web.service.SysLoginService;
import com.LightLife.framework.web.service.SysPermissionService;
import com.LightLife.framework.web.service.TokenService;
import com.LightLife.system.service.ISysUserService;
import com.LightLife.system.service.IWxOpenApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/wechat")
public class WxOpenApiController extends BaseController {
    @Autowired
    private IWxOpenApiService wxOpenApiService;
    @Autowired
    private SysLoginService loginService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private SysPermissionService permissionService;
    @Autowired
    private TokenService tokenService;
    /**
     * 微信小程序登录
     * @param code
     * @return
     */
    @ResponseBody
    @PostMapping("/onLogin")
    public AjaxResult onLogin(@RequestBody Map<String,Object> params) {
        try {
            String code = params.get("code")== null ? "" : String.valueOf(params.get("code"));
            String token = params.get("token")== null ? "" : String.valueOf(params.get("token"));
            SysUser user = wxOpenApiService.getOpenIdAndSessionKey(code,token);
            // 角色集合
            Set<String> roles = permissionService.getRolePermission(user);
            // 权限集合
            Set<String> permissions = permissionService.getMenuPermission(user);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("user", user);
            ajax.put("roles", roles);
            ajax.put("permissions", permissions);
            return ajax;
        } catch (ApiException e) {
            return AjaxResult.error(e.getCode(), e.getMsg());
        }
    }

    @ResponseBody
    @GetMapping("/getPhoneNumber")
    public AjaxResult getPhoneNumber(@RequestParam String code) throws ApiException {
        return wxOpenApiService.getPhoneNumber(code);
    }
    @PreAuthorize("@ss.hasPermi('system:wxuser:list')")
    @GetMapping("/getProfile")
    public AjaxResult getProfile()
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        return ajax;
    }
    @PreAuthorize("@ss.hasPermi('system:wxuser:edit')")
    @Log(title = "第三方授权", businessType = BusinessType.UPDATE)
    @PostMapping("/updateProfile")
    public AjaxResult updateProfile(@RequestBody SysUser user)
    {
        return wxOpenApiService.updateUserProfile(user);
    }
}
