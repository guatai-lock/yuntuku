package com.guatai.yuntukubackend.utils.vipexchange;

/**
 * ClassName: a
 * Package: com.guatai.yuntukubackend.utils
 * Description:
 *
 */
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.guatai.yuntukubackend.constant.UserConstant;
import com.guatai.yuntukubackend.exception.BusinessException;
import com.guatai.yuntukubackend.exception.ErrorCode;
import com.guatai.yuntukubackend.model.dto.user.VipCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * VIP兑换工具类（精简版）
 */
@Slf4j
public class VipCodeUtil {

    // 读写锁：读共享，写互斥
    private static final ReentrantReadWriteLock RW_LOCK = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock READ_LOCK = RW_LOCK.readLock();
    private static final ReentrantReadWriteLock.WriteLock WRITE_LOCK = RW_LOCK.writeLock();

    /**
     * 获取所有VIP兑换码
     */
    public static List<VipCode> getAllVipCodes() {
        READ_LOCK.lock();
        try {
            // 使用 Hutool 的 ResourceUtil 读取 classpath 资源
            String vipCodeJson = ResourceUtil.readUtf8Str(UserConstant.VIP_CODE_FILE_PATH);
            if (StrUtil.isBlank(vipCodeJson)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "VIP兑换码文件不存在或内容为空");
            }
            return JSONUtil.toList(vipCodeJson, VipCode.class);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("读取VIP兑换码文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取兑换码文件失败");
        } finally {
            READ_LOCK.unlock();
        }
    }

    /**
     * 保存VIP兑换码到文件
     *  生产环境无法写入文件,改为数据库实现
     */
    public static void saveVipCodesToFile(List<VipCode> vipCodes) {
        Assert.notEmpty(vipCodes, "VIP兑换码列表不能为空");

        WRITE_LOCK.lock();
        try {
            ClassPathResource resource = new ClassPathResource(UserConstant.VIP_CODE_FILE_PATH);
            FileUtil.writeUtf8String(JSONUtil.toJsonStr(vipCodes), resource.getFile());
        } catch (Exception e) {
            log.error("保存VIP兑换码文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存兑换码文件失败");
        } finally {
            WRITE_LOCK.unlock();
        }
    }
    /**
     * 检查兑换码是否有效
     */
    public static boolean isValidVipCode(String vipCode) {
        if (StrUtil.isBlank(vipCode)) return false;

        try {
            List<VipCode> vipCodes = getAllVipCodes();
            return vipCodes.stream()
                    .anyMatch(code -> vipCode.equals(code.getCode()) && !code.isHasUsed());
        } catch (Exception e) {
            log.error("检查兑换码有效性失败", e);
            return false;
        }
    }

    /**
     * 标记兑换码为已使用
     */
    public static void markVipCodeAsUsed(String vipCode) {
        WRITE_LOCK.lock();
        try {
            List<VipCode> vipCodes = getAllVipCodes();
            boolean found = vipCodes.stream()
                    .filter(code -> vipCode.equals(code.getCode()))
                    .findFirst()
                    .map(code -> {
                        code.setHasUsed(true);
                        return true;
                    })
                    .orElse(false);

            if (!found) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "兑换码不存在");
            }

            saveVipCodesToFile(vipCodes);
        } finally {
            WRITE_LOCK.unlock();
        }
    }
    /**
     * 获取有效的兑换码
     */
    public static VipCode getValidVipCode(String vipCode) {
        if (StrUtil.isBlank(vipCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "兑换码不能为空");
        }
        List<VipCode> vipCodes = getAllVipCodes();
        return vipCodes.stream()
                .filter(code -> vipCode.equals(code.getCode()) && !code.isHasUsed())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "兑换码无效或已被使用"));
    }
}
