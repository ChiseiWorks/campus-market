package com.campus.market.controller;

import com.campus.market.common.BizException;
import com.campus.market.common.Result;
import com.campus.market.entity.Category;
import com.campus.market.entity.Notice;
import com.campus.market.entity.SchoolLocation;
import com.campus.market.service.CommonService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 基础数据与文件接口（接口文档 5.3）
 * 分类 / 地点 / 公告为开放接口；上传需登录
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /** 允许的图片后缀 */
    private static final Set<String> ALLOW_EXT = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    /** 分类列表：type=1 闲置 / 2 跑腿，不传返回全部启用 */
    @GetMapping("/api/category/list")
    public Result<List<Category>> categoryList(@RequestParam(required = false) Integer type) {
        return Result.ok(commonService.categoryList(type));
    }

    /** 校内地点库（预置数据，只读） */
    @GetMapping("/api/location/list")
    public Result<List<SchoolLocation>> locationList() {
        return Result.ok(commonService.locationList());
    }

    /** 系统公告（status=1） */
    @GetMapping("/api/notice/list")
    public Result<List<Notice>> noticeList() {
        return Result.ok(commonService.noticeList());
    }

    /**
     * 图片上传（multipart，字段名 file）
     * 存本地 uploads/ 目录（WebMvcConfig 已映射 /uploads/** 静态资源），返回可访问 URL
     * TODO: 生产环境建议改对象存储（OSS/COS），本地磁盘不适合多实例部署
     */
    @PostMapping("/api/file/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        if (!ALLOW_EXT.contains(ext)) {
            throw new BizException("仅支持 jpg/jpeg/png/gif/webp 图片");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(dir);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }
        // 返回完整可访问 URL（前端 common.js upload 取 data.url）
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        Map<String, String> data = new HashMap<>();
        data.put("url", baseUrl + "/uploads/" + filename);
        log.info("文件上传成功：{}", filename);
        return Result.ok(data);
    }
}
