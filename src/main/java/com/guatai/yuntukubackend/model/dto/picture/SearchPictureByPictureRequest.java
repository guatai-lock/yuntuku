package com.guatai.yuntukubackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: SearchPictureBypictureRequest
 * Package: com.guatai.yuntukubackend.model.dto.picture
 * Description:
 *
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}

