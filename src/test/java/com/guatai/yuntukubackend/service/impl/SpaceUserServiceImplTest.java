package com.guatai.yuntukubackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guatai.yuntukubackend.exception.BusinessException;
import com.guatai.yuntukubackend.exception.ErrorCode;
import com.guatai.yuntukubackend.mapper.SpaceUserMapper;
import com.guatai.yuntukubackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.guatai.yuntukubackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.guatai.yuntukubackend.model.entity.Space;
import com.guatai.yuntukubackend.model.entity.SpaceUser;
import com.guatai.yuntukubackend.model.entity.User;
import com.guatai.yuntukubackend.model.vo.SpaceUserVO;
import com.guatai.yuntukubackend.model.vo.UserVO;
import com.guatai.yuntukubackend.service.SpaceService;
import com.guatai.yuntukubackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SpaceUserServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SpaceUserServiceImplTest {

    @Mock
    private SpaceUserMapper spaceUserMapper;

    @Mock
    private UserService userService;

    @Mock
    private SpaceService spaceService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SpaceUserServiceImpl spaceUserService;

    private SpaceUser defaultSpaceUser;

    @BeforeEach
    void setUp() {
        defaultSpaceUser = new SpaceUser();
        defaultSpaceUser.setId(1L);
        defaultSpaceUser.setSpaceId(10L);
        defaultSpaceUser.setUserId(100L);
        defaultSpaceUser.setSpaceRole("editor");
    }

    // ==================== addSpaceUser ====================

    @Test
    void testAddSpaceUser_ValidRequest_ShouldReturnId() {
        SpaceUserAddRequest addRequest = new SpaceUserAddRequest();
        addRequest.setSpaceId(10L);
        addRequest.setUserId(100L);
        addRequest.setSpaceRole("editor");

        when(userService.getById(100L)).thenReturn(new User());
        when(spaceService.getById(10L)).thenReturn(new Space());
        doAnswer(invocation -> {
            SpaceUser su = invocation.getArgument(0);
            su.setId(1L);
            return 1;
        }).when(spaceUserMapper).insert(any(SpaceUser.class));

        long result = spaceUserService.addSpaceUser(addRequest);

        assertEquals(1L, result);
        ArgumentCaptor<SpaceUser> captor = ArgumentCaptor.forClass(SpaceUser.class);
        verify(spaceUserMapper).insert(captor.capture());
        assertEquals(10L, captor.getValue().getSpaceId().longValue());
        assertEquals("editor", captor.getValue().getSpaceRole());
    }

    @Test
    void testAddSpaceUser_NullRequest_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> spaceUserService.addSpaceUser(null));
    }

    @Test
    void testAddSpaceUser_SaveFailed_ShouldThrow() {
        SpaceUserAddRequest addRequest = new SpaceUserAddRequest();
        addRequest.setSpaceId(10L);
        addRequest.setUserId(100L);
        addRequest.setSpaceRole("editor");

        when(userService.getById(100L)).thenReturn(new User());
        when(spaceService.getById(10L)).thenReturn(new Space());
        when(spaceUserMapper.insert(any(SpaceUser.class))).thenReturn(0);

        assertThrows(BusinessException.class,
                () -> spaceUserService.addSpaceUser(addRequest));
    }

    // ==================== validSpaceUser ====================

    @Test
    void testValidSpaceUser_Null_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> spaceUserService.validSpaceUser(null, true));
    }

    @Test
    void testValidSpaceUser_Add_MissingFields_ShouldThrow() {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceRole("editor");
        // spaceId 和 userId 未设置

        assertThrows(BusinessException.class,
                () -> spaceUserService.validSpaceUser(spaceUser, true));
    }

    @Test
    void testValidSpaceUser_Add_UserNotFound_ShouldThrow() {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(10L);
        spaceUser.setUserId(999L);
        spaceUser.setSpaceRole("editor");

        when(userService.getById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> spaceUserService.validSpaceUser(spaceUser, true));
    }

    @Test
    void testValidSpaceUser_Add_SpaceNotFound_ShouldThrow() {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(999L);
        spaceUser.setUserId(100L);
        spaceUser.setSpaceRole("editor");

        when(userService.getById(100L)).thenReturn(new User());
        when(spaceService.getById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> spaceUserService.validSpaceUser(spaceUser, true));
    }

    @Test
    void testValidSpaceUser_InvalidRole_ShouldThrow() {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceRole("invalid_role");

        assertThrows(BusinessException.class,
                () -> spaceUserService.validSpaceUser(spaceUser, true));
    }

    @Test
    void testValidSpaceUser_Valid_ShouldPass() {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(10L);
        spaceUser.setUserId(100L);
        spaceUser.setSpaceRole("editor");

        when(userService.getById(100L)).thenReturn(new User());
        when(spaceService.getById(10L)).thenReturn(new Space());

        assertDoesNotThrow(() -> spaceUserService.validSpaceUser(spaceUser, true));
    }

    @Test
    void testValidSpaceUser_Update_NullRole_ShouldPass() {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setId(1L);
        // spaceRole is null -> skip role validation
        // add=false -> skip user/space existence check

        assertDoesNotThrow(() -> spaceUserService.validSpaceUser(spaceUser, false));
    }

    // ==================== getQueryWrapper ====================

    @Test
    void testGetQueryWrapper_AllFields_ShouldBuildConditions() {
        SpaceUserQueryRequest queryRequest = new SpaceUserQueryRequest();
        queryRequest.setId(1L);
        queryRequest.setSpaceId(10L);
        queryRequest.setUserId(100L);
        queryRequest.setSpaceRole("admin");

        QueryWrapper<SpaceUser> wrapper = spaceUserService.getQueryWrapper(queryRequest);

        assertNotNull(wrapper);
        String sql = wrapper.getCustomSqlSegment();
        assertTrue(sql.contains("spaceId") || sql.contains("space_id"));
    }

    @Test
    void testGetQueryWrapper_NullRequest_ShouldReturnEmptyWrapper() {
        QueryWrapper<SpaceUser> wrapper = spaceUserService.getQueryWrapper(null);
        assertNotNull(wrapper);
    }

    // ==================== getSpaceUserVO ====================

    @Test
    void testGetSpaceUserVO_ShouldPopulateUserAndSpace() {
        User mockUser = new User();
        mockUser.setId(100L);
        mockUser.setUserName("测试用户");

        Space mockSpace = new Space();
        mockSpace.setId(10L);
        mockSpace.setSpaceName("测试空间");

        when(userService.getById(100L)).thenReturn(mockUser);
        when(userService.getUserVO(any())).thenReturn(new UserVO());
        when(spaceService.getById(10L)).thenReturn(mockSpace);
        when(spaceService.getSpaceVO(any(), any())).thenReturn(null);

        SpaceUserVO result = spaceUserService.getSpaceUserVO(defaultSpaceUser, request);

        assertNotNull(result);
        assertEquals(10L, result.getSpaceId().longValue());
        assertEquals(100L, result.getUserId().longValue());
        assertEquals("editor", result.getSpaceRole());
        assertNotNull(result.getUser());
    }

    // ==================== getSpaceUserVOList ====================

    @Test
    void testGetSpaceUserVOList_EmptyList_ShouldReturnEmpty() {
        List<SpaceUserVO> result = spaceUserService.getSpaceUserVOList(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSpaceUserVOList_NonEmptyList_ShouldReturnPopulatedList() {
        SpaceUser su1 = new SpaceUser();
        su1.setId(1L);
        su1.setSpaceId(10L);
        su1.setUserId(100L);

        SpaceUser su2 = new SpaceUser();
        su2.setId(2L);
        su2.setSpaceId(20L);
        su2.setUserId(200L);

        User user1 = new User();
        user1.setId(100L);
        User user2 = new User();
        user2.setId(200L);

        Space space1 = new Space();
        space1.setId(10L);
        Space space2 = new Space();
        space2.setId(20L);

        when(userService.listByIds(anySet())).thenReturn(Arrays.asList(user1, user2));
        when(userService.getUserVO(any())).thenReturn(new UserVO());
        when(spaceService.listByIds(anySet())).thenReturn(Arrays.asList(space1, space2));

        List<SpaceUserVO> result = spaceUserService.getSpaceUserVOList(Arrays.asList(su1, su2));

        assertEquals(2, result.size());
        verify(userService).listByIds(anySet());
        verify(spaceService).listByIds(anySet());
    }
}
