package com.guatai.yuntukubackend.api.imagesearch;

import com.guatai.yuntukubackend.api.imagesearch.model.ImageSearchResult;
import com.guatai.yuntukubackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.guatai.yuntukubackend.api.imagesearch.sub.GetImageListApi;
import com.guatai.yuntukubackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * ClassName: ImageSearchApiFacade
 * Package: com.guatai.yuntukubackend.api.imagesearch
 * Description:
 *门面模式提供统一接口给其他服务调用
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://guatai1-1409680085.cos.ap-guangzhou.myqcloud.com/public/2030604651874672641/2026-03-16_FocJB26E5D5OSuD1.webp";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}

