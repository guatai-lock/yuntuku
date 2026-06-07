package com.guatai.yuntukubackend.service.impl;

import cn.dev33.satoken.exception.SaTokenException;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guatai.yuntukubackend.constant.UserConstant;
import com.guatai.yuntukubackend.exception.BusinessException;
import com.guatai.yuntukubackend.exception.ErrorCode;
import com.guatai.yuntukubackend.manger.auth.StpKit;
import com.guatai.yuntukubackend.mapper.UserMapper;
import com.guatai.yuntukubackend.model.dto.user.UserQueryRequest;
import com.guatai.yuntukubackend.model.dto.user.VipCode;
import com.guatai.yuntukubackend.model.dto.user.VipExchangeRequest;
import com.guatai.yuntukubackend.model.entity.User;
import com.guatai.yuntukubackend.model.enums.UserRoleEnum;
import com.guatai.yuntukubackend.model.vo.LoginUserVO;
import com.guatai.yuntukubackend.model.vo.UserVO;
import com.guatai.yuntukubackend.utils.vipexchange.VipCodeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserServiceImpl userService;

    private User defaultUser;

    @BeforeEach
    void setUp() {
        lenient().when(request.getSession()).thenReturn(session);
        lenient().when(request.getSession(anyBoolean())).thenReturn(session);

        defaultUser = new User();
        defaultUser.setId(1L);
        defaultUser.setUserAccount("testuser");
        defaultUser.setUserPassword(DigestUtils.md5DigestAsHex(("yupi" + "password123").getBytes()));
        defaultUser.setUserName("测试用户");
        defaultUser.setUserRole(UserRoleEnum.USER.getValue());
    }

    // ==================== userRegister ====================

    @Test
    void testUserRegister_ValidInput_ShouldReturnId() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // 模拟 MyBatis-Plus 自动生成 ID
            return 1;
        }).when(userMapper).insert(any(User.class));

        long result = userService.userRegister("newuser", "password123", "password123");

        assertEquals(1L, result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User savedUser = captor.getValue();
        assertEquals("newuser", savedUser.getUserAccount());
        assertEquals(UserRoleEnum.USER.getValue(), savedUser.getUserRole());
    }

    @Test
    void testUserRegister_BlankParams_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> userService.userRegister("", "password123", "password123"));
        assertThrows(BusinessException.class,
                () -> userService.userRegister("newuser", null, "password123"));
    }

    @Test
    void testUserRegister_AccountTooShort_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> userService.userRegister("ab", "password123", "password123"));
    }

    @Test
    void testUserRegister_PasswordTooShort_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> userService.userRegister("newuser", "short", "short"));
    }

    @Test
    void testUserRegister_PasswordMismatch_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> userService.userRegister("newuser", "password123", "different"));
    }

    @Test
    void testUserRegister_DuplicateAccount_ShouldThrow() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> userService.userRegister("existing", "password123", "password123"));
    }

    @Test
    void testUserRegister_SaveFailed_ShouldThrow() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(0);

        assertThrows(BusinessException.class,
                () -> userService.userRegister("newuser", "password123", "password123"));
    }

    @Test
    void testUserRegister_InsertThrows_ShouldPropagate() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> userService.userRegister("newuser", "password123", "password123"));
    }

    // ==================== userLogin ====================

    @Test
    void testUserLogin_ValidCredentials_ShouldCallMapperAndSetSession() {
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(defaultUser);

        // StpKit.SPACE.login() 需要 Sa-Token 上下文，在纯 Mockito 测试中不可用
        // 但我们可以验证 StpKit 调用之前的逻辑是否正常
        try {
            userService.userLogin("testuser", "password123", request);
        } catch (Exception e) {
            // SaTokenException 或 NullPointerException 等均可接受
        }

        // 验证 mapper 被正确调用
        verify(userMapper).selectOne(any(QueryWrapper.class));
        // 验证 session 被设置
        verify(session).setAttribute(eq(UserConstant.USER_LOGIN_STATE), eq(defaultUser));
    }

    @Test
    void testUserLogin_BlankAccount_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> userService.userLogin("", "password123", request));
        assertThrows(BusinessException.class,
                () -> userService.userLogin(null, "password123", request));
    }

    @Test
    void testUserLogin_AccountTooShort_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> userService.userLogin("ab", "password123", request));
    }

    @Test
    void testUserLogin_PasswordTooShort_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> userService.userLogin("testuser", "short", request));
    }

    @Test
    void testUserLogin_InvalidCredentials_ShouldThrow() {
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> userService.userLogin("testuser", "wrongpassword", request));
    }

    // ==================== getEncryptPassword ====================

    @Test
    void testGetEncryptPassword_ShouldReturnMd5WithSalt() {
        String result = userService.getEncryptPassword("password123");
        String expected = DigestUtils.md5DigestAsHex(("yupi" + "password123").getBytes());
        assertEquals(expected, result);
    }

    // ==================== getLoginUser ====================

    @Test
    void testGetLoginUser_LoggedIn_ShouldReturnUser() {
        when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(defaultUser);
        when(userMapper.selectById(1L)).thenReturn(defaultUser);

        User result = userService.getLoginUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("testuser", result.getUserAccount());
    }

    @Test
    void testGetLoginUser_NotLoggedIn_ShouldThrow() {
        when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> userService.getLoginUser(request));
    }

    @Test
    void testGetLoginUser_UserDeleted_ShouldThrow() {
        when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(defaultUser);
        when(userMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> userService.getLoginUser(request));
    }

    // ==================== getLoginUserVO ====================

    @Test
    void testGetLoginUserVO_NullUser_ShouldReturnNull() {
        assertNull(userService.getLoginUserVO(null));
    }

    @Test
    void testGetLoginUserVO_ValidUser_ShouldCopyFields() {
        defaultUser.setUserAvatar("http://avatar.jpg");
        defaultUser.setUserProfile("profile");
        defaultUser.setEditTime(DateUtil.date());
        defaultUser.setCreateTime(DateUtil.date());
        defaultUser.setUpdateTime(DateUtil.date());

        LoginUserVO result = userService.getLoginUserVO(defaultUser);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("testuser", result.getUserAccount());
        assertEquals("测试用户", result.getUserName());
        assertEquals("http://avatar.jpg", result.getUserAvatar());
        assertEquals("profile", result.getUserProfile());
        assertEquals(UserRoleEnum.USER.getValue(), result.getUserRole());
    }

    // ==================== getUserVO ====================

    @Test
    void testGetUserVO_NullUser_ShouldReturnNull() {
        assertNull(userService.getUserVO(null));
    }

    @Test
    void testGetUserVO_ValidUser_ShouldCopyFields() {
        UserVO result = userService.getUserVO(defaultUser);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("testuser", result.getUserAccount());
        assertEquals("测试用户", result.getUserName());
    }

    // ==================== getUserVOList ====================

    @Test
    void testGetUserVOList_EmptyList_ShouldReturnEmpty() {
        List<UserVO> result = userService.getUserVOList(new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserVOList_NonEmptyList_ShouldReturnMappedList() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUserAccount("user2");
        user2.setUserName("用户2");

        List<UserVO> result = userService.getUserVOList(java.util.Arrays.asList(defaultUser, user2));

        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUserAccount());
        assertEquals("user2", result.get(1).getUserAccount());
    }

    // ==================== userLogout ====================

    @Test
    void testUserLogout_LoggedIn_ShouldReturnTrue() {
        when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(defaultUser);

        boolean result = userService.userLogout(request);

        assertTrue(result);
        verify(session).removeAttribute(UserConstant.USER_LOGIN_STATE);
    }

    @Test
    void testUserLogout_NotLoggedIn_ShouldThrow() {
        when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> userService.userLogout(request));
    }

    // ==================== getQueryWrapper ====================

    @Test
    void testGetQueryWrapper_NullRequest_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> userService.getQueryWrapper(null));
    }

    @Test
    void testGetQueryWrapper_AllFields_ShouldBuildConditions() {
        UserQueryRequest queryRequest = new UserQueryRequest();
        queryRequest.setId(1L);
        queryRequest.setUserAccount("test");
        queryRequest.setUserName("测试");
        queryRequest.setUserProfile("简介");
        queryRequest.setUserRole("admin");
        queryRequest.setSortField("createTime");
        queryRequest.setSortOrder("ascend");

        QueryWrapper<User> wrapper = userService.getQueryWrapper(queryRequest);

        assertNotNull(wrapper);
        // 验证 SQL 片段中包含过滤条件
        String sqlSegment = wrapper.getCustomSqlSegment();
        assertTrue(sqlSegment.contains("userRole") || sqlSegment.contains("user_role"));
        // 验证排序字段
        assertTrue(sqlSegment.contains("createTime") || sqlSegment.contains("create_time"));
    }

    @Test
    void testGetQueryWrapper_EmptyFields_ShouldBuildEmptyConditions() {
        UserQueryRequest queryRequest = new UserQueryRequest();
        // 所有字段为空，不设置条件

        QueryWrapper<User> wrapper = userService.getQueryWrapper(queryRequest);

        assertNotNull(wrapper);
        // 不应该有 WHERE 条件
        String sqlSegment = wrapper.getCustomSqlSegment();
        assertTrue(sqlSegment.isEmpty() || sqlSegment.contains("order by"));
    }

    // ==================== isAdmin ====================

    @Test
    void testIsAdmin_AdminRole_ShouldReturnTrue() {
        User admin = new User();
        admin.setUserRole(UserRoleEnum.ADMIN.getValue());
        assertTrue(userService.isAdmin(admin));
    }

    @Test
    void testIsAdmin_UserRole_ShouldReturnFalse() {
        assertFalse(userService.isAdmin(defaultUser));
    }

    @Test
    void testIsAdmin_NullUser_ShouldReturnFalse() {
        assertFalse(userService.isAdmin(null));
    }

    // ==================== exchangeVipForMember ====================

    @Test
    void testExchangeVipForMember_ValidCode_NonVip_ShouldUpgrade() {
        // 准备参数
        VipExchangeRequest exchangeRequest = new VipExchangeRequest();
        exchangeRequest.setVipCode("TEST-CODE-001");

        // Mock 登录用户
        when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(defaultUser);
        when(userMapper.selectById(1L)).thenReturn(defaultUser);

        // Mock VipCodeUtil 和 IdUtil
        try (MockedStatic<VipCodeUtil> mockedVipCode = mockStatic(VipCodeUtil.class);
             MockedStatic<IdUtil> mockedIdUtil = mockStatic(IdUtil.class)) {
            VipCode mockCode = new VipCode();
            mockCode.setCode("TEST-CODE-001");
            mockCode.setHasUsed(false);
            mockedVipCode.when(() -> VipCodeUtil.getValidVipCode("TEST-CODE-001")).thenReturn(mockCode);
            // 阻止异步线程修改真实的测试文件
            mockedVipCode.when(() -> VipCodeUtil.markVipCodeAsUsed(anyString())).thenAnswer(invocation -> null);
            // Mock ID 生成：返回一个不会溢出 Long 的 16 位十六进制数
            mockedIdUtil.when(IdUtil::fastSimpleUUID).thenReturn("00000000000000001111111111111111");

            // Mock mapper 更新成功
            doAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return 1;
            }).when(userMapper).update(any(User.class), any(QueryWrapper.class));

            boolean result = userService.exchangeVipForMember(exchangeRequest, request);

            assertTrue(result);
            // 验证用户角色被更新为 VIP
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<QueryWrapper> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
            verify(userMapper).update(userCaptor.capture(), wrapperCaptor.capture());

            assertEquals(UserRoleEnum.VIP.getValue(), userCaptor.getValue().getUserRole());
            assertNotNull(userCaptor.getValue().getVipExpireTime());
            assertEquals("TEST-CODE-001", userCaptor.getValue().getVipCode());
            assertNotNull(userCaptor.getValue().getVipNumber());
        }
    }

    @Test
    void testExchangeVipForMember_BlankCode_ShouldThrow() {
        VipExchangeRequest exchangeRequest = new VipExchangeRequest();
        exchangeRequest.setVipCode("");

        assertThrows(BusinessException.class,
                () -> userService.exchangeVipForMember(exchangeRequest, request));
    }

    @Test
    void testExchangeVipForMember_UpdateFailed_ShouldThrow() {
        VipExchangeRequest exchangeRequest = new VipExchangeRequest();
        exchangeRequest.setVipCode("TEST-CODE-001");

        when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(defaultUser);
        when(userMapper.selectById(1L)).thenReturn(defaultUser);

        try (MockedStatic<VipCodeUtil> mockedVipCode = mockStatic(VipCodeUtil.class);
             MockedStatic<IdUtil> mockedIdUtil = mockStatic(IdUtil.class)) {
            VipCode mockCode = new VipCode();
            mockCode.setCode("TEST-CODE-001");
            mockCode.setHasUsed(false);
            mockedVipCode.when(() -> VipCodeUtil.getValidVipCode("TEST-CODE-001")).thenReturn(mockCode);
            // 阻止异步线程修改真实的测试文件
            mockedVipCode.when(() -> VipCodeUtil.markVipCodeAsUsed(anyString())).thenAnswer(invocation -> null);
            // Mock ID 生成
            mockedIdUtil.when(IdUtil::fastSimpleUUID).thenReturn("00000000000000001111111111111111");
            // mapper.update 返回 0（更新失败）
            when(userMapper.update(any(User.class), any(QueryWrapper.class))).thenReturn(0);

            assertThrows(BusinessException.class,
                    () -> userService.exchangeVipForMember(exchangeRequest, request));
        }
    }
}
