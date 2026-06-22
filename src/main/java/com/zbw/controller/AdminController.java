package com.zbw.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zbw.domain.Admin;
import com.zbw.domain.Announcement;
import com.zbw.domain.BookCategory;
import com.zbw.domain.User;
import com.zbw.domain.Vo.BookVo;
import com.zbw.service.IAdminService;
import com.zbw.service.IAnnouncementService;
import com.zbw.service.IBookCategoryService;
import com.zbw.service.IUserService;
import com.zbw.utils.page.Page;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AdminController {

    @Resource
    private IAdminService adminService;
    @Resource
    private IBookCategoryService bookCategoryService;
    @Resource
    private IUserService userService;
    @Resource
    private IAnnouncementService announcementService;

    @RequestMapping("/isAdminExist")
    @ResponseBody
    public String adminIsExist(@RequestParam("adminName") String adminName) {
        boolean b = adminService.adminIsExist(adminName);
        return b ? "true" : "false";
    }

    @PostMapping("/adminLogin")
    public String adminLogin(@RequestParam("userName") String userName,
                             @RequestParam("password") String password,
                             HttpServletRequest request,
                             Model model) {
        Admin admin = adminService.adminLogin(userName, password);
        if (admin == null) {
            request.getSession().setAttribute("flag", 1);
            return "index";
        }
        request.getSession().setAttribute("flag", 0);
        request.getSession().setAttribute("admin", admin);
        model.addAttribute("carouselList", announcementService.getCarouselAnnouncements());
        model.addAttribute("announcementList", announcementService.getNormalAnnouncements());
        return "admin/index";
    }

    @RequestMapping("/addBookPage")
    public String addBookPage() {
        return "admin/addBook";
    }

    @RequestMapping("/addCategoryPage")
    public String addCategoryPage(@RequestParam("pageNum") int pageNum, Model model) {
        Page<BookCategory> page = bookCategoryService.selectBookCategoryByPageNum(pageNum);
        model.addAttribute("page", page);
        return "admin/addCategory";
    }

    @RequestMapping("/showStausPage")
    public String showStatusPage() {
        return "admin/showStaus";
    }

    @RequestMapping("/adminIndex")
    public String returnAdminIndexPage(Model model) {
        List<Announcement> carouselList = announcementService.getCarouselAnnouncements();
        List<Announcement> announcementList = announcementService.getNormalAnnouncements();
        model.addAttribute("carouselList", carouselList);
        model.addAttribute("announcementList", announcementList);
        return "admin/index";
    }

    @RequestMapping("/showUsersPage")
    public String showUsersPage(Model model, @RequestParam("pageNum") int pageNum) {
        Page<User> page = userService.findUserByPage(pageNum);
        model.addAttribute("page", page);
        return "admin/showUsers";
    }

    @RequestMapping("/showBooksPage")
    public String showBooksPage(Model model) {
        Page<BookVo> page = new Page<>();
        page.setPageCount(1);
        page.setPageNum(1);
        model.addAttribute("page", page);
        return "admin/showBooks";
    }

    @RequestMapping("/adminLogOut")
    public String userLogOut(HttpServletRequest request) {
        request.getSession().invalidate();
        return "index";
    }

    @RequestMapping("/addUserPage")
    public String addUserPage() {
        return "admin/addUser";
    }

    @RequestMapping("/importBooksPage")
    public String importBooksPage() {
        return "admin/importBooks";
    }

    @RequestMapping("/importUsersPage")
    public String importUsersPage() {
        return "admin/importUsers";
    }

    @RequestMapping("/adminInfoPage")
    public String adminInfo() {
        return "admin/adminInfo";
    }

    @RequestMapping("/adminBasicInfoPage")
    public String adminBasicInfo() {
        return "admin/adminBasicInfo";
    }

    @RequestMapping("/adminSecuritySettingsPage")
    public String adminSecuritySettings() {
        return "admin/adminSecuritySettings";
    }

    @RequestMapping("/updateAdmin")
    @ResponseBody
    public boolean updateAdmin(Admin admin, HttpServletRequest request) {
        return adminService.updateAdmin(admin, request);
    }

    @RequestMapping("/updateAdminPwd")
    @ResponseBody
    public Map<String, Object> updateAdminPwd(@RequestParam("oldPwd") String oldPwd,
                                              @RequestParam("newPwd") String newPwd,
                                              HttpServletRequest request) {
        return adminService.updateAdminPwd(oldPwd, newPwd, request);
    }

    // ==================== 公告/活动管理 ====================

    /**
     * 返回公告管理页面
     */
    @RequestMapping("/manageAnnouncementPage")
    public String manageAnnouncementPage(@RequestParam("pageNum") int pageNum, Model model) {
        Page<Announcement> page = announcementService.getAnnouncementsByPage(pageNum);
        model.addAttribute("page", page);
        return "admin/manageAnnouncement";
    }

    /**
     * 新增公告/活动
     */
    @RequestMapping("/addAnnouncement")
    @ResponseBody
    public Map<String, Object> addAnnouncement(Announcement announcement, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin != null) {
            announcement.setPublisherId(admin.getAdminId());
        }
        boolean success = announcementService.saveAnnouncement(announcement);
        result.put("success", success);
        result.put("msg", success ? "添加成功" : "添加失败");
        return result;
    }

    /**
     * 更新公告/活动
     */
    @RequestMapping("/updateAnnouncement")
    @ResponseBody
    public Map<String, Object> updateAnnouncement(Announcement announcement) {
        Map<String, Object> result = new HashMap<>();
        boolean success = announcementService.updateAnnouncement(announcement);
        result.put("success", success);
        result.put("msg", success ? "更新成功" : "更新失败");
        return result;
    }

    /**
     * 获取公告/活动JSON数据（供编辑弹窗使用）
     */
    @RequestMapping("/getAnnouncementJson")
    @ResponseBody
    public Announcement getAnnouncementJson(@RequestParam("id") int id) {
        return announcementService.getById(id);
    }

    /**
     * 删除公告/活动
     */
    @RequestMapping("/deleteAnnouncement")
    @ResponseBody
    public Map<String, Object> deleteAnnouncement(@RequestParam("id") int id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = announcementService.deleteAnnouncement(id);
        result.put("success", success);
        result.put("msg", success ? "删除成功" : "删除失败");
        return result;
    }
}