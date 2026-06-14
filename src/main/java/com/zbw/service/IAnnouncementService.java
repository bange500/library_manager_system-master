package com.zbw.service;

import com.zbw.domain.Announcement;
import com.zbw.utils.page.Page;

import java.util.List;

public interface IAnnouncementService {

    /**
     * 获取轮播图公告（is_carousel=1），按创建时间倒序
     */
    List<Announcement> getCarouselAnnouncements();

    /**
     * 获取非轮播的普通公告列表（is_carousel=0），按创建时间倒序
     */
    List<Announcement> getNormalAnnouncements();

    /**
     * 分页查询所有公告/活动
     */
    Page<Announcement> getAnnouncementsByPage(int pageNum);

    /**
     * 按 ID 查询详情
     */
    Announcement getById(int id);

    /**
     * 新增公告/活动
     */
    boolean saveAnnouncement(Announcement announcement);

    /**
     * 更新公告/活动
     */
    boolean updateAnnouncement(Announcement announcement);

    /**
     * 删除公告/活动
     */
    boolean deleteAnnouncement(int id);
}
