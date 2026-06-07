package com.guatai.yuntukubackend.service.impl;

import com.guatai.yuntukubackend.exception.BusinessException;
import com.guatai.yuntukubackend.exception.ErrorCode;
import com.guatai.yuntukubackend.mapper.PictureMapper;
import com.guatai.yuntukubackend.model.dto.space.analyze.*;
import com.guatai.yuntukubackend.model.entity.Space;
import com.guatai.yuntukubackend.model.entity.User;
import com.guatai.yuntukubackend.model.enums.UserRoleEnum;
import com.guatai.yuntukubackend.model.vo.space.analyze.*;
import com.guatai.yuntukubackend.service.PictureService;
import com.guatai.yuntukubackend.service.SpaceService;
import com.guatai.yuntukubackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SpaceAnalyzeServiceimpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SpaceAnalyzeServiceImplTest {

    @Mock
    private SpaceService spaceService;

    @Mock
    private UserService userService;

    @Mock
    private PictureService pictureService;

    @Mock
    private PictureMapper pictureMapper;

    @InjectMocks
    private SpaceAnalyzeServiceimpl spaceAnalyzeService;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUserRole(UserRoleEnum.ADMIN.getValue());

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setUserRole(UserRoleEnum.USER.getValue());

        lenient().when(pictureService.getBaseMapper()).thenReturn(pictureMapper);
    }

    // ==================== getSpaceUsageAnalyze ====================

    @Test
    void testGetSpaceUsageAnalyze_Public_AsAdmin_ShouldReturnStats() {
        SpaceUsageAnalyzeRequest request = new SpaceUsageAnalyzeRequest();
        request.setQueryPublic(true);

        when(userService.isAdmin(adminUser)).thenReturn(true);
        when(pictureMapper.selectObjs(any())).thenReturn(Arrays.asList(1024L, 2048L));

        SpaceUsageAnalyzeResponse result = spaceAnalyzeService.getSpaceUsageAnalyze(request, adminUser);

        assertNotNull(result);
        assertEquals(3072L, result.getUsedSize()); // 1024 + 2048
        assertEquals(2, result.getUsedCount());
        assertNull(result.getMaxSize());
    }

    @Test
    void testGetSpaceUsageAnalyze_QueryAll_AsAdmin_ShouldReturnStats() {
        SpaceUsageAnalyzeRequest request = new SpaceUsageAnalyzeRequest();
        request.setQueryAll(true);

        when(userService.isAdmin(adminUser)).thenReturn(true);
        when(pictureMapper.selectObjs(any())).thenReturn(Collections.singletonList(500L));

        SpaceUsageAnalyzeResponse result = spaceAnalyzeService.getSpaceUsageAnalyze(request, adminUser);

        assertNotNull(result);
        assertEquals(500L, result.getUsedSize());
        assertEquals(1, result.getUsedCount());
    }

    @Test
    void testGetSpaceUsageAnalyze_QueryAll_AsNonAdmin_ShouldThrow() {
        SpaceUsageAnalyzeRequest request = new SpaceUsageAnalyzeRequest();
        request.setQueryAll(true);

        when(userService.isAdmin(normalUser)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> spaceAnalyzeService.getSpaceUsageAnalyze(request, normalUser));
    }

    @Test
    void testGetSpaceUsageAnalyze_SpecificSpace_ShouldReturnUsage() {
        SpaceUsageAnalyzeRequest request = new SpaceUsageAnalyzeRequest();
        request.setSpaceId(10L);

        Space space = new Space();
        space.setId(10L);
        space.setUserId(2L); // owned by normalUser
        space.setTotalSize(5000L);
        space.setMaxSize(100000L);
        space.setTotalCount(50L);
        space.setMaxCount(1000L);

        when(spaceService.getById(10L)).thenReturn(space);

        SpaceUsageAnalyzeResponse result = spaceAnalyzeService.getSpaceUsageAnalyze(request, normalUser);

        assertNotNull(result);
        assertEquals(5000L, result.getUsedSize());
        assertEquals(100000L, result.getMaxSize());
        assertEquals(50, result.getUsedCount());
        assertEquals(1000, result.getMaxCount());
    }

    // ==================== getSpaceCategoryAnalyze ====================

    @Test
    void testGetSpaceCategoryAnalyze_ShouldReturnCategories() {
        SpaceCategoryAnalyzeRequest request = new SpaceCategoryAnalyzeRequest();
        request.setSpaceId(10L);

        Space space = new Space();
        space.setId(10L);
        space.setUserId(2L);
        when(spaceService.getById(10L)).thenReturn(space);

        // Mock selectMaps to return category data
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "风景");
        row1.put("count", 10L);
        row1.put("totalSize", 10000L);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", "人物");
        row2.put("count", 5L);
        row2.put("totalSize", 5000L);

        when(pictureMapper.selectMaps(any())).thenReturn(Arrays.asList(row1, row2));

        List<SpaceCategoryAnalyzeResponse> result = spaceAnalyzeService.getSpaceCategoryAnalyze(request, normalUser);

        assertEquals(2, result.size());
        assertEquals("风景", result.get(0).getCategory());
        assertEquals(10L, result.get(0).getCount());
        assertEquals(10000L, result.get(0).getTotalSize());
    }

    // ==================== getSpaceTagAnalyze ====================

    @Test
    void testGetSpaceTagAnalyze_ShouldReturnTagCounts() {
        SpaceTagAnalyzeRequest request = new SpaceTagAnalyzeRequest();
        request.setSpaceId(10L);

        Space space = new Space();
        space.setId(10L);
        space.setUserId(2L);
        when(spaceService.getById(10L)).thenReturn(space);

        // Mock selectObjs to return tags JSON
        when(pictureMapper.selectObjs(any())).thenReturn(Arrays.asList(
                "[\"风景\",\"高清\"]",
                "[\"人物\",\"高清\"]",
                "[\"风景\"]"
        ));

        List<SpaceTagAnalyzeResponse> result = spaceAnalyzeService.getSpaceTagAnalyze(request, normalUser);

        assertFalse(result.isEmpty());
        // "风景" appears 2 times, should be first (sorted by count desc)
        assertEquals("风景", result.get(0).getTag());
        assertEquals(2L, result.get(0).getCount());
    }

    // ==================== getSpaceSizeAnalyze ====================

    @Test
    void testGetSpaceSizeAnalyze_ShouldReturnSizeRanges() {
        SpaceSizeAnalyzeRequest request = new SpaceSizeAnalyzeRequest();
        request.setSpaceId(10L);

        Space space = new Space();
        space.setId(10L);
        space.setUserId(2L);
        when(spaceService.getById(10L)).thenReturn(space);

        // Mock selectObjs: <100KB, 100-500KB, 500KB-1MB, >1MB
        when(pictureMapper.selectObjs(any())).thenReturn(Arrays.asList(
                50000L,    // <100KB
                200000L,   // 100-500KB
                700000L,   // 500KB-1MB
                2000000L   // >1MB
        ));

        List<SpaceSizeAnalyzeResponse> result = spaceAnalyzeService.getSpaceSizeAnalyze(request, normalUser);

        assertEquals(4, result.size());
        assertEquals("<100KB", result.get(0).getSizeRange());
        assertEquals(1L, result.get(0).getCount());
        assertEquals("100KB-500KB", result.get(1).getSizeRange());
        assertEquals(1L, result.get(1).getCount());
    }

    // ==================== getSpaceUserAnalyze ====================

    @Test
    void testGetSpaceUserAnalyze_DayDimension_ShouldReturnPeriods() {
        SpaceUserAnalyzeRequest request = new SpaceUserAnalyzeRequest();
        request.setSpaceId(10L);
        request.setTimeDimension("day");

        Space space = new Space();
        space.setId(10L);
        space.setUserId(2L);
        when(spaceService.getById(10L)).thenReturn(space);

        Map<String, Object> row1 = new HashMap<>();
        row1.put("period", "2026-01-01");
        row1.put("count", 5L);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("period", "2026-01-02");
        row2.put("count", 3L);

        when(pictureMapper.selectMaps(any())).thenReturn(Arrays.asList(row1, row2));

        List<SpaceUserAnalyzeResponse> result = spaceAnalyzeService.getSpaceUserAnalyze(request, normalUser);

        assertEquals(2, result.size());
        assertEquals("2026-01-01", result.get(0).getPeriod());
        assertEquals(5L, result.get(0).getCount());
    }

    @Test
    void testGetSpaceUserAnalyze_InvalidDimension_ShouldThrow() {
        SpaceUserAnalyzeRequest request = new SpaceUserAnalyzeRequest();
        request.setSpaceId(10L);
        request.setTimeDimension("invalid");

        Space space = new Space();
        space.setId(10L);
        space.setUserId(2L);
        when(spaceService.getById(10L)).thenReturn(space);

        assertThrows(BusinessException.class,
                () -> spaceAnalyzeService.getSpaceUserAnalyze(request, normalUser));
    }

    // ==================== getSpaceRankAnalyze ====================

    @Test
    void testGetSpaceRankAnalyze_AsAdmin_ShouldReturnRankedSpaces() {
        SpaceRankAnalyzeRequest request = new SpaceRankAnalyzeRequest();
        request.setTopN(5);

        when(userService.isAdmin(adminUser)).thenReturn(true);

        Space space1 = new Space();
        space1.setId(1L);
        Space space2 = new Space();
        space2.setId(2L);
        when(spaceService.list(any(QueryWrapper.class))).thenReturn(Arrays.asList(space1, space2));

        List<Space> result = spaceAnalyzeService.getSpaceRankAnalyze(request, adminUser);

        assertEquals(2, result.size());
    }

    @Test
    void testGetSpaceRankAnalyze_AsNonAdmin_ShouldThrow() {
        SpaceRankAnalyzeRequest request = new SpaceRankAnalyzeRequest();
        request.setTopN(5);

        when(userService.isAdmin(normalUser)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> spaceAnalyzeService.getSpaceRankAnalyze(request, normalUser));
    }

    // ==================== checkSpaceAnalyzeAuth ====================

    @Test
    void testCheckSpaceAnalyzeAuth_QueryAll_AsAdmin_ShouldPass() {
        SpaceAnalyzeRequest request = new SpaceAnalyzeRequest() {};
        request.setQueryAll(true);

        when(userService.isAdmin(adminUser)).thenReturn(true);

        assertDoesNotThrow(() -> spaceAnalyzeService.checkSpaceAnalyzeAuth(request, adminUser));
    }

    @Test
    void testCheckSpaceAnalyzeAuth_QueryAll_AsNonAdmin_ShouldThrow() {
        SpaceAnalyzeRequest request = new SpaceAnalyzeRequest() {};
        request.setQueryAll(true);

        when(userService.isAdmin(normalUser)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> spaceAnalyzeService.checkSpaceAnalyzeAuth(request, normalUser));
    }

    @Test
    void testCheckSpaceAnalyzeAuth_SpecificSpace_AsOwner_ShouldPass() {
        SpaceAnalyzeRequest request = new SpaceAnalyzeRequest() {};
        request.setSpaceId(10L);

        Space space = new Space();
        space.setId(10L);
        space.setUserId(2L);

        when(spaceService.getById(10L)).thenReturn(space);

        assertDoesNotThrow(() -> spaceAnalyzeService.checkSpaceAnalyzeAuth(request, normalUser));
    }
}
