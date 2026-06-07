package com.guatai.yuntukubackend.manger.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.guatai.yuntukubackend.manger.auth.model.SpaceUserAuthConfig;
import com.guatai.yuntukubackend.manger.auth.model.SpaceUserRole;
import com.guatai.yuntukubackend.model.entity.Space;
import com.guatai.yuntukubackend.model.entity.SpaceUser;
import com.guatai.yuntukubackend.model.entity.User;
import com.guatai.yuntukubackend.model.enums.SpaceRoleEnum;
import com.guatai.yuntukubackend.model.enums.SpaceTypeEnum;
import com.guatai.yuntukubackend.service.SpaceUserService;
import com.guatai.yuntukubackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: a
 * Package: com.guatai.yuntukubackend.manger.auth
 * Description:
 * 加载配置文件到对象,根据角色获得权限列表，返回权限列表给前端
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        // 找到匹配的角色
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> spaceUserRole.equals(r.getKey()))
                .findFirst()
                .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

    /**
     * 此方法将用户具有的权限列表返回给前端，
     * 方便前端展示对应权限相关操作按钮（与自定义权限校验逻辑的getpermissionlist不同）
     * @param space 空间对象
     * @param space
     * @param loginUser
     * @return
     */
    public List<String> getPermissionListForWebsite(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 定义管理员权限常量
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }
}

