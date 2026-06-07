package com.guatai.yuntukubackend.aop;

import com.guatai.yuntukubackend.annotation.AuthCheck;
import com.guatai.yuntukubackend.exception.BusinessException;
import com.guatai.yuntukubackend.exception.ErrorCode;
import com.guatai.yuntukubackend.model.entity.User;
import com.guatai.yuntukubackend.model.enums.UserRoleEnum;
import com.guatai.yuntukubackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthInterceptor 单元测试
 * 注意：需要在测试前后手动管理 RequestContextHolder
 */
@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private UserService userService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testDoInterceptor_NoRequiredRole_ShouldProceed() throws Throwable {
        AuthCheck authCheck = createAuthCheck(null);
        when(joinPoint.proceed()).thenReturn("success");

        Object result = authInterceptor.doInterceptor(joinPoint, authCheck);

        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void testDoInterceptor_AdminRole_WithAdminUser_ShouldProceed() throws Throwable {
        User adminUser = new User();
        adminUser.setUserRole(UserRoleEnum.ADMIN.getValue());

        AuthCheck authCheck = createAuthCheck(UserRoleEnum.ADMIN.getValue());
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(adminUser);
        when(joinPoint.proceed()).thenReturn("success");

        Object result = authInterceptor.doInterceptor(joinPoint, authCheck);

        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void testDoInterceptor_AdminRole_WithUserRole_ShouldThrow() throws Throwable {
        User normalUser = new User();
        normalUser.setUserRole(UserRoleEnum.USER.getValue());

        AuthCheck authCheck = createAuthCheck(UserRoleEnum.ADMIN.getValue());
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(normalUser);

        assertThrows(BusinessException.class,
                () -> authInterceptor.doInterceptor(joinPoint, authCheck));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void testDoInterceptor_UserNotLoggedIn_ShouldThrow() throws Throwable {
        AuthCheck authCheck = createAuthCheck(UserRoleEnum.ADMIN.getValue());
        when(userService.getLoginUser(any(HttpServletRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.NOT_LOGIN_ERROR));

        assertThrows(BusinessException.class,
                () -> authInterceptor.doInterceptor(joinPoint, authCheck));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void testDoInterceptor_UserHasNullRole_ShouldThrow() throws Throwable {
        User userWithNullRole = new User();
        userWithNullRole.setUserRole(null); // null role

        AuthCheck authCheck = createAuthCheck(UserRoleEnum.ADMIN.getValue());
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(userWithNullRole);

        assertThrows(BusinessException.class,
                () -> authInterceptor.doInterceptor(joinPoint, authCheck));
        verify(joinPoint, never()).proceed();
    }

    /**
     * 创建 AuthCheck 匿名实现（避免 Mockito mock 注解类在 JDK 21 上的问题）
     */
    private AuthCheck createAuthCheck(String mustRole) {
        return new AuthCheck() {
            @Override
            public String mustRole() {
                return mustRole;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return AuthCheck.class;
            }
        };
    }
}
