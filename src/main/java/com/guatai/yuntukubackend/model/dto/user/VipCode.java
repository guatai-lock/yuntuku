package com.guatai.yuntukubackend.model.dto.user;

/**
 * ClassName: VipCode
 * Package: com.guatai.yuntukubackend.model.dto.user
 * Description:
 *
 */

import lombok.Data;

/**
 * VIP兑换码实体
 */
@Data
public class VipCode {
    private String code;
    private boolean hasUsed;
}
