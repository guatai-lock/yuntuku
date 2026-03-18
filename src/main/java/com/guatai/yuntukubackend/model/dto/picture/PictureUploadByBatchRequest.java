package com.guatai.yuntukubackend.model.dto.picture;

import lombok.Data;

/**
 * ClassName: PictureUploadByBatchRequest
 * Package: com.guatai.yuntukubackend.model.dto.picture
 * Description:
 *
 */
@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;
    /**
     * 名称前缀
     */
    private String namePrefix;

}

