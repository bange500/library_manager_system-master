package com.zbw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.zbw.domain.Announcement;
import com.zbw.mapper.AnnouncementMapper;
import com.zbw.service.IAnnouncementService;
import com.zbw.utils.page.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements IAnnouncementService {

    @Resource
    private AnnouncementMapper announcementMapper;

    private static final int PAGE_SIZE = 10;

    @Override
    public List<Announcement> getCarouselAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getIsCarousel, 1)
               .orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public List<Announcement> getNormalAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getIsCarousel, 0)
               .orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public Page<Announcement> getAnnouncementsByPage(int pageNum) {
        IPage<Announcement> iPage = new PageDTO<>(pageNum, PAGE_SIZE);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getCreateTime);

        IPage<Announcement> result = announcementMapper.selectPage(iPage, wrapper);

        Page<Announcement> page = new Page<>();
        page.setList(result.getRecords());
        page.setPageNum(pageNum);
        page.setPageSize(PAGE_SIZE);
        page.setPageCount((int) result.getPages());
        return page;
    }

    @Override
    public Announcement getById(int id) {
        return announcementMapper.selectById(id);
    }

    @Override
    public boolean saveAnnouncement(Announcement announcement) {
        int n = announcementMapper.insert(announcement);
        return n > 0;
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        // 保留发布人ID，防止被表单提交的 null 覆盖
        Announcement existing = announcementMapper.selectById(announcement.getId());
        if (existing != null && existing.getPublisherId() != null) {
            announcement.setPublisherId(existing.getPublisherId());
        }
        int n = announcementMapper.updateById(announcement);
        return n > 0;
    }

    @Override
    public boolean deleteAnnouncement(int id) {
        int n = announcementMapper.deleteById(id);
        return n > 0;
    }
}
