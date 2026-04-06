package com.guatai.yuntukubackend.service;

import com.guatai.yuntukubackend.model.dto.space.analyze.*;
import com.guatai.yuntukubackend.model.entity.Space;
import com.guatai.yuntukubackend.model.entity.User;
import com.guatai.yuntukubackend.model.vo.space.analyze.*;

import java.util.List;

/**
 * ClassName: a
 * Package: com.guatai.yuntukubackend.service
 * Description:
 *
 */
public interface SpaceAnalyzeService {
    //个人/公共空间资源分析
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);
    //空间分析权限校验
    void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);
    //空间图片分类分析
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);
    //空间图片标签分析
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);
    //图片大小分析
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);
    //用户上传行为分析
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);
    //空间使用排行分析
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
