package com.guatai.yuntukubackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guatai.yuntukubackend.model.dto.space.SpaceAddRequest;
import com.guatai.yuntukubackend.model.dto.space.SpaceQueryRequest;
import com.guatai.yuntukubackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guatai.yuntukubackend.model.entity.User;
import com.guatai.yuntukubackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 30019713
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2026-03-23 20:16:50
*/
public interface SpaceService extends IService<Space> {
    /**
     * 创建空间
     * @param spaceAddRequest 创建空间请求体
     * @param loginUser 登录用户，用于判定是否是管理员，非管理员仅可创建普通空间，同时关联创建用户信息
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 用于校验空间数据
     * @param space 空间实体对象
     * @param add 是否是创建空间（区分创建空间和编辑空间，校验的参数不一样）
     */
    void validSpace(Space space,boolean add);

    /**
     * 创建或跟新空间时，自动填充限额数据
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 获取脱敏SpaceVO
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 拼接查询条件
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取分页SpaceVO
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /***
     * 空间权限校验
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
