package com.guatai.yuntukubackend.controller;

import com.guatai.yuntukubackend.common.BaseResponse;
import com.guatai.yuntukubackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: MainController
 * Package: com.guatai.yuntukubackend.controller
 * Description:
 *
 */
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
