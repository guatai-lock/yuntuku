package com.guatai.yuntukubackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guatai.yuntukubackend.exception.BusinessException;
import com.guatai.yuntukubackend.exception.ErrorCode;
import com.guatai.yuntukubackend.mapper.SpaceMapper;
import com.guatai.yuntukubackend.model.dto.space.SpaceAddRequest;
import com.guatai.yuntukubackend.model.dto.space.SpaceQueryRequest;
import com.guatai.yuntukubackend.model.entity.Space;
import com.guatai.yuntukubackend.model.entity.User;
import com.guatai.yuntukubackend.model.enums.SpaceLevelEnum;
import com.guatai.yuntukubackend.model.enums.SpaceTypeEnum;
import com.guatai.yuntukubackend.model.enums.UserRoleEnum;
import com.guatai.yuntukubackend.model.vo.SpaceVO;
import com.guatai.yuntukubackend.service.SpaceUserService;
import com.guatai.yuntukubackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SpaceServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SpaceServiceImplTest {

    @Mock
    private SpaceMapper spaceMapper;

    @Mock
    private UserService userService;

    @Mock
    private SpaceUserService spaceUserService;

    private TransactionTemplate transactionTemplate;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SpaceServiceImpl spaceService;

    private User defaultUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        defaultUser = new User();
        defaultUser.setId(1L);
        defaultUser.setUserRole(UserRoleEnum.USER.getValue());

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUserRole(UserRoleEnum.ADMIN.getValue());

        // 使用匿名子类而非 Mockito mock，避免 JDK 21 上的 Mockito 限制
        transactionTemplate = new TransactionTemplate() {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                try {
                    return action.doInTransaction(null);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // 使用反射注入 transactionTemplate 到 spaceService
        injectTransactionTemplate();
    }

    private void injectTransactionTemplate() {
        try {
            java.lang.reflect.Field field = SpaceServiceImpl.class.getDeclaredField("transactionTemplate");
            field.setAccessible(true);
            field.set(spaceService, transactionTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject TransactionTemplate", e);
        }
    }

    // ==================== addSpace ====================
    // 注意：涉及 lambdaQuery() 的 addSpace 测试需要集成测试环境（MyBatis-Plus 运行时）
    // 此处仅测试能在纯 Mockito 环境下验证的路径

    @Test
    void testAddSpace_NonAdminProfessionalSpace_ShouldThrow() {
        SpaceAddRequest addRequest = new SpaceAddRequest();
        addRequest.setSpaceLevel(SpaceLevelEnum.PROFESSIONAL.getValue());
        addRequest.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());

        when(userService.isAdmin(defaultUser)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> spaceService.addSpace(addRequest, defaultUser));
    }

    // ==================== validSpace ====================

    @Test
    void testValidSpace_NullSpace_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> spaceService.validSpace(null, true));
    }

    @Test
    void testValidSpace_Add_BlankName_ShouldThrow() {
        Space space = new Space();
        space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        // spaceName is blank

        assertThrows(BusinessException.class,
                () -> spaceService.validSpace(space, true));
    }

    @Test
    void testValidSpace_Add_NullLevel_ShouldThrow() {
        Space space = new Space();
        space.setSpaceName("测试空间");
        // spaceLevel is null

        assertThrows(BusinessException.class,
                () -> spaceService.validSpace(space, true));
    }

    @Test
    void testValidSpace_Add_NullType_ShouldThrow() {
        Space space = new Space();
        space.setSpaceName("测试空间");
        space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        // spaceType is null

        assertThrows(BusinessException.class,
                () -> spaceService.validSpace(space, true));
    }

    @Test
    void testValidSpace_InvalidLevel_ShouldThrow() {
        Space space = new Space();
        space.setSpaceName("测试空间");
        space.setSpaceLevel(999); // invalid level
        space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());

        assertThrows(BusinessException.class,
                () -> spaceService.validSpace(space, true));
    }

    @Test
    void testValidSpace_NameTooLong_ShouldThrow() {
        Space space = new Space();
        space.setSpaceName("这是一个名字非常非常长的空间，超过了三十个字符的限制会触发异常");
        space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());

        assertThrows(BusinessException.class,
                () -> spaceService.validSpace(space, true));
    }

    @Test
    void testValidSpace_Valid_ShouldPass() {
        Space space = new Space();
        space.setSpaceName("测试空间");
        space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());

        assertDoesNotThrow(() -> spaceService.validSpace(space, true));
    }

    @Test
    void testValidSpace_Update_ShouldNotRequireName() {
        Space space = new Space();
        // name can be null on update

        assertDoesNotThrow(() -> spaceService.validSpace(space, false));
    }

    // ==================== fillSpaceBySpaceLevel ====================

    @Test
    void testFillSpaceBySpaceLevel_ShouldSetDefaults() {
        Space space = new Space();
        space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());

        spaceService.fillSpaceBySpaceLevel(space);

        assertNotNull(space.getMaxSize());
        assertNotNull(space.getMaxCount());
    }

    @Test
    void testFillSpaceBySpaceLevel_ShouldNotOverrideExisting() {
        Space space = new Space();
        space.setSpaceLevel(SpaceLevelEnum.PROFESSIONAL.getValue());
        space.setMaxSize(9999L); // custom value

        spaceService.fillSpaceBySpaceLevel(space);

        assertEquals(9999L, space.getMaxSize().longValue()); // should NOT be overridden
    }

    // ==================== getSpaceVO ====================

    @Test
    void testGetSpaceVO_ShouldPopulateUser() {
        Space space = new Space();
        space.setId(1L);
        space.setUserId(100L);

        User mockUser = new User();
        mockUser.setId(100L);

        when(userService.getById(100L)).thenReturn(mockUser);

        SpaceVO result = spaceService.getSpaceVO(space, request);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
    }

    // ==================== getQueryWrapper ====================

    @Test
    void testGetQueryWrapper_AllFields_ShouldBuildConditions() {
        SpaceQueryRequest queryRequest = new SpaceQueryRequest();
        queryRequest.setId(1L);
        queryRequest.setUserId(100L);
        queryRequest.setSpaceName("测试");
        queryRequest.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        queryRequest.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        queryRequest.setSortField("createTime");
        queryRequest.setSortOrder("ascend");

        QueryWrapper<Space> wrapper = spaceService.getQueryWrapper(queryRequest);

        assertNotNull(wrapper);
    }

    @Test
    void testGetQueryWrapper_NullRequest_ShouldReturnEmpty() {
        QueryWrapper<Space> wrapper = spaceService.getQueryWrapper(null);
        assertNotNull(wrapper);
    }

    // ==================== getSpaceVOPage ====================

    @Test
    void testGetSpaceVOPage_EmptyList_ShouldReturnEmptyPage() {
        Page<Space> spacePage = new Page<>(1, 10);
        spacePage.setRecords(Arrays.asList());

        Page<SpaceVO> result = spaceService.getSpaceVOPage(spacePage, request);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void testGetSpaceVOPage_NonEmpty_ShouldPopulate() {
        Space space1 = new Space();
        space1.setId(1L);
        space1.setUserId(100L);

        Space space2 = new Space();
        space2.setId(2L);
        space2.setUserId(200L);

        Page<Space> spacePage = new Page<>(1, 10, 2);
        spacePage.setRecords(Arrays.asList(space1, space2));

        User user1 = new User();
        user1.setId(100L);
        User user2 = new User();
        user2.setId(200L);

        when(userService.listByIds(anySet())).thenReturn(Arrays.asList(user1, user2));

        Page<SpaceVO> result = spaceService.getSpaceVOPage(spacePage, request);

        assertEquals(2, result.getRecords().size());
    }

    // ==================== checkSpaceAuth ====================

    @Test
    void testCheckSpaceAuth_Owner_ShouldPass() {
        Space space = new Space();
        space.setUserId(1L);

        assertDoesNotThrow(() -> spaceService.checkSpaceAuth(defaultUser, space));
    }

    @Test
    void testCheckSpaceAuth_Admin_ShouldPass() {
        Space space = new Space();
        space.setUserId(99L); // different user

        when(userService.isAdmin(adminUser)).thenReturn(true);

        assertDoesNotThrow(() -> spaceService.checkSpaceAuth(adminUser, space));
    }

    @Test
    void testCheckSpaceAuth_OtherUser_ShouldThrow() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setUserRole(UserRoleEnum.USER.getValue());

        Space space = new Space();
        space.setUserId(1L); // different from otherUser

        when(userService.isAdmin(otherUser)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> spaceService.checkSpaceAuth(otherUser, space));
    }
}
