package com.guatai.yuntukubackend.api.imagesearch.model;

import lombok.Data;

/**
 * ClassName: ImageSearchResult
 * Package: com.guatai.yuntukubackend.api.model
 * Description:
 *
 */
@Data
public class ImageSearchResult {

    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;
}

