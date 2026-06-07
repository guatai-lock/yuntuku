package com.guatai.yuntukubackend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.guatai.yuntukubackend.common.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GlobalExceptionHandler 单元测试
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testBusinessExceptionHandler_ShouldReturnErrorResponse() {
        BusinessException exception = new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");

        BaseResponse<?> response = handler.businessExceptionHandler(exception);

        assertNotNull(response);
        assertEquals(40000, response.getCode());
        assertEquals("参数错误", response.getMessage());
    }

    @Test
    void testBusinessExceptionHandler_WithDefaultMessage_ShouldReturnErrorResponse() {
        BusinessException exception = new BusinessException(ErrorCode.SYSTEM_ERROR);

        BaseResponse<?> response = handler.businessExceptionHandler(exception);

        assertNotNull(response);
        assertEquals(50000, response.getCode());
        assertEquals(ErrorCode.SYSTEM_ERROR.getMessage(), response.getMessage());
    }

    @Test
    void testRuntimeExceptionHandler_ShouldReturnSystemError() {
        RuntimeException exception = new RuntimeException("未知错误");

        BaseResponse<?> response = handler.businessExceptionHandler(exception);

        assertNotNull(response);
        assertEquals(50000, response.getCode());
        assertEquals("系统错误", response.getMessage());
    }

    @Test
    void testNotLoginException_ShouldReturnNotLoginError() {
        NotLoginException exception = new NotLoginException("未登录", "login", null);

        BaseResponse<?> response = handler.notLoginException(exception);

        assertNotNull(response);
        assertEquals(40100, response.getCode());
    }

    @Test
    void testNotPermissionException_ShouldReturnNoAuthError() {
        NotPermissionException exception = new NotPermissionException("无权限访问");

        BaseResponse<?> response = handler.notPermissionExceptionHandler(exception);

        assertNotNull(response);
        assertEquals(40101, response.getCode());
    }
}
