package com.zbw.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zbw.domain.Admin;
import com.zbw.domain.BookCategory;
import com.zbw.domain.User;
import com.zbw.domain.Vo.BookVo;
import com.zbw.service.IAdminService;
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

    @RequestMapping("/isAdminExist")
    @ResponseBody
    public String adminIsExist(@RequestParam("adminName") String adminName) {
        boolean b = adminService.adminIsExist(adminName);
        return b ? "true" : "false";
    }

    @PostMapping("/adminLogin")
    public String adminLogin(@RequestParam("userName") String userName,
                             @RequestParam("password") String password,
                             HttpServletRequest request) {
        Admin admin = adminService.adminLogin(userName, password);
        if (admin == null) {
            request.getSession().setAttribute("flag", 1);
            return "index";
        }
        request.getSession().setAttribute("flag", 0);
        request.getSession().setAttribute("admin", admin);
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
    public String returnAdminIndexPage() {
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
}