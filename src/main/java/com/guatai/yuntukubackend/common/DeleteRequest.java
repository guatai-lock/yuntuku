package com.guatai.yuntukubackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: DeleteRequest
 * Package: com.guatai.yuntukubackend.common
 * Description:
 *通用删除请求类
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
