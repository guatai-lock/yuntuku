package com.guatai.yuntukubackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: a
 * Package: com.guatai.yuntukubackend.model.vo.space.analyze
 * Description:
 *公共分析请求类
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {

    /**
     * 空间 ID,仅在queryPublic和queryAll为false时生效
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private boolean queryPublic;

    /**
     * 全空间分析
     */
    private boolean queryAll;

    private static final long serialVersionUID = 1L;
}

