package com.haikou.government.approval.controller;

import com.haikou.government.common.core.utils.MinioUtils;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.common.core.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传接口
 *
 * @author xhl
 * @since 2026-11-26
 */
@Slf4j
@Tag(name = "文件上传", description = "MinIO 文件上传接口")
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private MinioUtils minioUtils;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件访问 URL
     */
    @Operation(summary = "上传文件", description = "上传文件到 MinIO，返回访问 URL")
    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        // 限制 20MB
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过 20MB");
        }
        try {
            String url = minioUtils.upload(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("name", file.getOriginalFilename());
            data.put("size", String.valueOf(file.getSize()));
            return R.ok(data, "上传成功");
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }
}
