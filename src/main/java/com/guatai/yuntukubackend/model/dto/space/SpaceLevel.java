package com.guatai.yuntukubackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ClassName: SpaceLevel
 * Package: com.guatai.yuntukubackend.model.dto.space
 * Description:
 *给前端展示空间信息
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}

