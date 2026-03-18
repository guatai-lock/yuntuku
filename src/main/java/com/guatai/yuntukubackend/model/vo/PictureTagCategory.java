package com.guatai.yuntukubackend.model.vo;

/**
 * ClassName: PictureTagCategory
 * Package: com.guatai.yuntukubackend.model.vo
 * Description:
 *
 */

import lombok.Data;

import java.util.List;

/**
 * 图片标签分类列表视图
 */
@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}
