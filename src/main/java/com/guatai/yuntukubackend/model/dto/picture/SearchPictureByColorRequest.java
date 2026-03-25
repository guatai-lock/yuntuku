package com.guatai.yuntukubackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: picture
 * Package: com.guatai.yuntukubackend.model.dto.picture
 * Description:
 *
 */
@Data
public class SearchPictureByColorRequest implements Serializable {

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}

