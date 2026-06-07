package com.guatai.yuntukubackend.utils.vipexchange;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;
import com.guatai.yuntukubackend.constant.UserConstant;
import com.guatai.yuntukubackend.exception.BusinessException;
import com.guatai.yuntukubackend.model.dto.user.VipCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VipCodeUtil 单元测试
 * 注意：markVipCodeAsUsed 会修改文件，所以测试前后需要备份和恢复
 */
class VipCodeUtilTest {

    private String backupContent;

    @BeforeEach
    void setUp() {
        // 备份测试文件，以便在测试后恢复
        ClassPathResource resource = new ClassPathResource(UserConstant.VIP_CODE_FILE_PATH);
        backupContent = FileUtil.readUtf8String(resource.getFile());
    }

    @AfterEach
    void tearDown() {
        // 恢复测试文件到原始状态
        ClassPathResource resource = new ClassPathResource(UserConstant.VIP_CODE_FILE_PATH);
        FileUtil.writeUtf8String(backupContent, resource.getFile());
    }

    @Test
    void testGetAllVipCodes_ShouldReturnList() {
        List<VipCode> codes = VipCodeUtil.getAllVipCodes();
        assertNotNull(codes);
        assertEquals(3, codes.size());
    }

    @Test
    void testGetAllVipCodes_FileContent() {
        List<VipCode> codes = VipCodeUtil.getAllVipCodes();
        assertEquals("TEST-CODE-001", codes.get(0).getCode());
        assertFalse(codes.get(0).isHasUsed());
        assertEquals("TEST-CODE-003", codes.get(2).getCode());
        assertTrue(codes.get(2).isHasUsed());
    }

    @Test
    void testIsValidVipCode_WithUnusedCode_ShouldReturnTrue() {
        assertTrue(VipCodeUtil.isValidVipCode("TEST-CODE-001"));
    }

    @Test
    void testIsValidVipCode_WithUsedCode_ShouldReturnFalse() {
        assertFalse(VipCodeUtil.isValidVipCode("TEST-CODE-003"));
    }

    @Test
    void testIsValidVipCode_WithNonExistentCode_ShouldReturnFalse() {
        assertFalse(VipCodeUtil.isValidVipCode("NON-EXISTENT"));
    }

    @Test
    void testIsValidVipCode_WithBlankCode_ShouldReturnFalse() {
        assertFalse(VipCodeUtil.isValidVipCode(""));
        assertFalse(VipCodeUtil.isValidVipCode(null));
        assertFalse(VipCodeUtil.isValidVipCode("   "));
    }

    @Test
    void testMarkVipCodeAsUsed_ShouldMarkAsUsed() {
        VipCodeUtil.markVipCodeAsUsed("TEST-CODE-002");
        assertFalse(VipCodeUtil.isValidVipCode("TEST-CODE-002"));
    }

    @Test
    void testMarkVipCodeAsUsed_WithNonExistentCode_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> VipCodeUtil.markVipCodeAsUsed("NON-EXISTENT"));
    }

    @Test
    void testGetValidVipCode_WithValidCode_ShouldReturnCode() {
        VipCode code = VipCodeUtil.getValidVipCode("TEST-CODE-001");
        assertNotNull(code);
        assertEquals("TEST-CODE-001", code.getCode());
        assertFalse(code.isHasUsed());
    }

    @Test
    void testGetValidVipCode_WithUsedCode_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> VipCodeUtil.getValidVipCode("TEST-CODE-003"));
    }

    @Test
    void testGetValidVipCode_WithBlankCode_ShouldThrow() {
        assertThrows(BusinessException.class,
                () -> VipCodeUtil.getValidVipCode(""));
        assertThrows(BusinessException.class,
                () -> VipCodeUtil.getValidVipCode(null));
    }

    @Test
    void testSaveVipCodesToFile_ShouldPersist() {
        // 读取当前列表，修改后保存
        List<VipCode> codes = VipCodeUtil.getAllVipCodes();
        assertEquals(3, codes.size());

        // 保存回文件（内容不变）
        VipCodeUtil.saveVipCodesToFile(codes);

        // 验证文件内容不变
        List<VipCode> reloaded = VipCodeUtil.getAllVipCodes();
        assertEquals(3, reloaded.size());
    }
}
