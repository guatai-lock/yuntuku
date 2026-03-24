package com.guatai.yuntukubackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: SpaceEditRequest
 * Package: com.guatai.yuntukubackend.model.dto.space
 * Description:
 *空间编辑亲求，给用户使用
 */
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}

