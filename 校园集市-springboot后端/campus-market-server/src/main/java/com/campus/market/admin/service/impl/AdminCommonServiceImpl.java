package com.campus.market.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.admin.dto.LocationSaveDTO;
import com.campus.market.admin.dto.NoticeSaveDTO;
import com.campus.market.admin.service.AdminCommonService;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.entity.Notice;
import com.campus.market.entity.SchoolLocation;
import com.campus.market.mapper.NoticeMapper;
import com.campus.market.mapper.SchoolLocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端：基础数据维护（地点 / 公告）
 */
@Service
@RequiredArgsConstructor
public class AdminCommonServiceImpl implements AdminCommonService {

    private final SchoolLocationMapper schoolLocationMapper;
    private final NoticeMapper noticeMapper;

    @Override
    public List<SchoolLocation> locationListAll() {
        // 全量（含停用），用户端接口只查 status=1
        return schoolLocationMapper.selectList(
                new QueryWrapper<SchoolLocation>().orderByAsc("sort", "id"));
    }

    /**
     * 保存地点：有 id 改、无 id 增
     * 注意：school_location 无逻辑删除字段，不提供删除接口（被商品/跑腿单引用，物理删除会破坏数据）
     */
    @Override
    public SchoolLocation saveLocation(LocationSaveDTO dto) {
        if (dto.getId() != null) {
            // 编辑
            SchoolLocation location = requireLocation(dto.getId());
            location.setName(dto.getName());
            location.setType(dto.getType());
            if (dto.getSort() != null) {
                location.setSort(dto.getSort());
            }
            schoolLocationMapper.updateById(location);
            return location;
        }
        // 新增（默认启用）
        SchoolLocation location = new SchoolLocation();
        location.setName(dto.getName());
        location.setType(dto.getType());
        location.setSort(dto.getSort() == null ? 0 : dto.getSort());
        location.setStatus(1);
        schoolLocationMapper.insert(location);
        return location;
    }

    @Override
    public void toggleLocation(Long id) {
        SchoolLocation location = requireLocation(id);
        location.setStatus(location.getStatus() != null && location.getStatus() == 1 ? 0 : 1);
        schoolLocationMapper.updateById(location);
    }

    @Override
    public PageResult<Notice> noticeList(int page, int size) {
        // 全量（含下线），按发布时间倒序
        Page<Notice> p = noticeMapper.selectPage(new Page<>(page, size),
                new QueryWrapper<Notice>().orderByDesc("create_time"));
        return PageResult.of(p);
    }

    @Override
    public Notice publishNotice(NoticeSaveDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setStatus(1); // 发布
        noticeMapper.insert(notice);
        return notice;
    }

    @Override
    public void offlineNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BizException("公告不存在");
        }
        notice.setStatus(0); // 下线
        noticeMapper.updateById(notice);
    }

    private SchoolLocation requireLocation(Long id) {
        SchoolLocation location = schoolLocationMapper.selectById(id);
        if (location == null) {
            throw new BizException("地点不存在");
        }
        return location;
    }
}
