package com.guatai.yuntukubackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: a
 * Package: com.guatai.yuntukubackend.model.dto.spaceuser
 * Description:
 *
 */
@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}

