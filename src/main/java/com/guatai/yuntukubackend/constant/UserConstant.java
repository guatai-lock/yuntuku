package com.guatai.yuntukubackend.constant;

/**
 * ClassName: UserConstant
 * Package: com.guatai.yuntukubackend.constant
 * Description:
 *
 */
/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";
    /**
     * VIP角色
     */
    String VIP_ROLE = "vip";

    // endregion

    /**
     * VIP兑换码文件路径
     */
    String VIP_CODE_FILE_PATH = "biz/vipcode.json";

    /**
     * VIP兑换码文件锁
     */
    String VIP_CODE_FILE_LOCK = "vip_code_file_lock";
    // endregion
}
