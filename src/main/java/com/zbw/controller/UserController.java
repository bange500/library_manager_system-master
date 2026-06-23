package com.zbw.controller;


import com.zbw.domain.Announcement;
import com.zbw.domain.Department;
import com.zbw.domain.Reservation;
import com.zbw.domain.User;
import com.zbw.domain.Vo.BorrowingBooksVo;
import com.zbw.service.IAnnouncementService;
import com.zbw.service.IBookService;
import com.zbw.service.IBorrowingBooksRecordService;
import com.zbw.service.IReservationService;
import com.zbw.service.IUserService;
import com.zbw.utils.ExcelImportUtil;
import com.zbw.utils.page.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IBorrowingBooksRecordService borrowingBooksRecordService;

    @Resource
    private IBookService bookService;

    @Resource
    private IAnnouncementService announcementService;

    @Resource
    private IReservationService reservationService;

    /**
     * 用户登录
     *
     * @param userName
     * @return
     */
    @PostMapping("/userLogin")
    public String userLogin(@Param("userId") int userId,
                            @Param("password") String password,
                            HttpServletRequest request,
                            Model model) {
        User user = userService.userLogin(userId, password);

        if (null != user) {
            // flag = 0 表示用户名密码校验成功  【用于前端校验】
            request.getSession().setAttribute("flag", 0);

            request.getSession().setAttribute("user", user);
            model.addAttribute("carouselList", announcementService.getCarouselAnnouncements());
            model.addAttribute("announcementList", announcementService.getNormalAnnouncements());
            return "user/index";
        }

        // flag 为 1 表示 登录失败 【用于前端校验】
        request.getSession().setAttribute("flag", 1);
        return "index";
    }

    /**
     * //验证用户是否存在
     *
     * @param userName
     * @return
     */
    @RequestMapping("/isUserExist")
    @ResponseBody
    public String isUserExist(@Param("userName") String userName) {
        List<User> users = userService.findUserByUserName(userName);
        if (null == users) {
            return "false";
        }
        if (users.size() < 1) {
            return "false";
        }
        return "true";
    }

    /**
     * 查找所有部门
     */
    @RequestMapping("/getDepts")
    @ResponseBody
    public List<Department> getDepts() {
        List<Department> depts = userService.findAllDepts();
        return depts;
    }


    /**
     * 返回用户借书记录页面
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping("/userBorrowBookRecord")
    public String userBorrowBookRecord(Model model, HttpServletRequest request) {
        ArrayList<BorrowingBooksVo> res = borrowingBooksRecordService.selectAllBorrowRecord(request);
        model.addAttribute("borrowingBooksList", res);

        return "user/borrowingBooksRecord";
    }

    /**
     * 返回还书页面
     */
    @RequestMapping("/userReturnBooksPage")
    public String userReturnBooksPage() {
        return "user/returnBooks";
    }

    /**
     * 返回个人信息页面
     */
    @RequestMapping("/userMessagePage")
    public String userMessagePage(Model model, HttpServletRequest request) {
        User session_user = (User) request.getSession().getAttribute("user");
        User user = userService.findUserById(session_user.getUserId());
        model.addAttribute("message_user", user);
        return "user/userMessage";
    }

    /**
     * 返回借书页面
     */
    @RequestMapping("/borrowingPage")
    public String borrowing() {
        return "user/borrowingBooks";
    }

    /**
     * 返回用户首页
     */
    @RequestMapping("/userIndex")
    public String userIndex(Model model) {
        List<Announcement> carouselList = announcementService.getCarouselAnnouncements();
        List<Announcement> announcementList = announcementService.getNormalAnnouncements();
        model.addAttribute("carouselList", carouselList);
        model.addAttribute("announcementList", announcementList);
        return "user/index";
    }

    /**
     * @param user
     * @param request
     * @return
     * @author zbw
     * 更新用户信息
     */
    @RequestMapping("/updateUser")
    @ResponseBody
    public boolean updateUser(User user, HttpServletRequest request) {
        return userService.updateUser(user, request);
    }

    /**
     * 用户还书
     *
     * @param bookId
     * @param request
     * @return
     */
    @RequestMapping("/userReturnBook")
    @ResponseBody
    public boolean returnBook(int bookId, HttpServletRequest request) {
        return userService.userReturnBook(bookId, request);
    }

    /**
     * 用户借书
     *
     * @param bookId
     * @param request
     * @return
     */
    @RequestMapping("/userBorrowingBook")
    @ResponseBody
    public boolean borrowingBook(int bookId, HttpServletRequest request) {
        System.out.println(bookId);
        return userService.userBorrowingBook(bookId, request);
    }

    /**
     * 返回管理员登陆界面
     */
    @RequestMapping("/adminLoginPage")
    public String adminLoginPage() {
        return "adminLogin";
    }

    /**
     * 用户退出登陆
     *
     * @param request
     * @return
     */
    @RequestMapping("/userLogOut")
    public String userLogOut(HttpServletRequest request) {
        request.getSession().invalidate();
        return "index";
    }

    /**
     * 返回用户索书页面
     */
    @RequestMapping("/findBookPage")
    public String findBookPage(Model model) {
        Page<com.zbw.domain.Vo.BookVo> page = new Page<>();
        page.setPageCount(1);
        page.setPageNum(1);
        model.addAttribute("page", page);
        return "user/findBook";
    }


    /**
     * 根据用户id删除用户
     *
     * @param userId
     * @return
     */
    @RequestMapping("/deleteUser")
    @ResponseBody
    public String deleteUserByUserId(@RequestParam("userId") int userId) {
        int res = userService.deleteUserById(userId);
        if (res > 0) {
            return "true";
        }
        return "false";
    }

    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    @RequestMapping("/addUser")
    @ResponseBody
    public String addUser(@Valid User user) {
        int res = userService.insertUser(user);
        if (res > 0) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Excel 批量导入用户
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果 JSON
     */
    @RequestMapping("/importUsersByExcel")
    @ResponseBody
    public Map<String, Object> importUsersByExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("msg", "请选择要上传的Excel文件");
            return result;
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls") && !fileName.endsWith(".csv"))) {
            result.put("success", false);
            result.put("msg", "文件格式不正确，请上传 .xlsx、.xls 或 .csv 文件");
            return result;
        }

        try {
            List<User> users;
            if (fileName.endsWith(".csv")) {
                users = ExcelImportUtil.parseUsersFromCsv(file);
            } else {
                users = ExcelImportUtil.parseUsersFromExcel(file);
            }
            if (users.isEmpty()) {
                result.put("success", false);
                result.put("msg", "Excel文件中没有有效的用户数据，请检查文件内容");
                return result;
            }

            int successCount = userService.batchAddUsers(users);
            result.put("success", true);
            result.put("msg", "成功导入 " + successCount + " 个用户，共读取 " + users.size() + " 条数据");
            result.put("total", users.size());
            result.put("imported", successCount);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", "导入失败：" + e.getMessage());
        }
        return result;
    }

    @RequestMapping("/userBasicInfoPage")
    public String userBasicInfoPage() {
        return "user/userBasicInfo";
    }

    @RequestMapping("/userSecuritySettingsPage")
    public String userSecuritySettingsPage() {
        return "user/userSecuritySettings";
    }

    @RequestMapping("/updateUserPwd")
    @ResponseBody
    public Map<String, Object> updateUserPwd(@RequestParam("oldPwd") String oldPwd,
                                              @RequestParam("newPwd") String newPwd,
                                              HttpServletRequest request) {
        return userService.updateUserPwd(oldPwd, newPwd, request);
    }

    /**
     * 公告/活动详情页
     */
    @RequestMapping("/announcementDetail")
    public String announcementDetail(@RequestParam("id") int id, Model model) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return "redirect:/userIndex";
        }
        model.addAttribute("announcement", announcement);
        return "announcement/detail";
    }

    // ==================== 图书预约 ====================

    /**
     * 预约图书
     */
    @RequestMapping("/reserveBook")
    @ResponseBody
    public Map<String, Object> reserveBook(@RequestParam("bookId") int bookId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }
        String errMsg = reservationService.reserveBook(sessionUser.getUserId(), bookId);
        result.put("success", errMsg == null);
        result.put("msg", errMsg != null ? errMsg : "预约成功，图书归还后将通知您");
        return result;
    }

    /**
     * 获取通知数（header红点）
     */
    @RequestMapping("/getNotificationCount")
    @ResponseBody
    public int getNotificationCount(HttpServletRequest request) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) return 0;
        return reservationService.getNotificationCount(sessionUser.getUserId());
    }

    /**
     * 用户预约列表页
     */
    @RequestMapping("/userReservationsPage")
    public String userReservationsPage(Model model, HttpServletRequest request) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/userIndex";
        }
        List<Reservation> list = reservationService.getUserReservations(sessionUser.getUserId());
        model.addAttribute("reservations", list);
        return "user/reservations";
    }

    /**
     * 取消预约
     */
    @RequestMapping("/cancelReservation")
    @ResponseBody
    public Map<String, Object> cancelReservation(@RequestParam("id") int id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }
        boolean ok = reservationService.cancelReservation(id, sessionUser.getUserId());
        result.put("success", ok);
        result.put("msg", ok ? "已取消预约" : "取消失败");
        return result;
    }
}
