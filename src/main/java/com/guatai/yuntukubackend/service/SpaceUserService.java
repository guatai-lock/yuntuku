package com.guatai.yuntukubackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guatai.yuntukubackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.guatai.yuntukubackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.guatai.yuntukubackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guatai.yuntukubackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 30019713
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2026-04-12 13:40:07
*/
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 添加空间成员
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验添加空间成员
     * @param spaceUser
     * @param add
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 将查询请求对象装换为查询封装对象,查询单个
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间成员包装类,关联查询用户和空间信息
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类,关联查询列表
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
