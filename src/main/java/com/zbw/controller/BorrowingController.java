package com.zbw.controller;

import com.zbw.domain.Vo.BorrowingBooksVo;
import com.zbw.service.IBorrowingBooksRecordService;
import com.zbw.utils.page.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.Resource;

@Controller
public class BorrowingController {

    @Resource
    private IBorrowingBooksRecordService borrowingBooksRecordService;

    /**
     * 返回所有用户借书记录页面
     *
     * @return
     */
    @RequestMapping("/allBorrowBooksRecordPage")
    public String allBorrowingBooksRecordPage(Model model, @RequestParam("pageNum") int pageNum) {
        Page<BorrowingBooksVo> page = borrowingBooksRecordService.selectAllByPage(pageNum);
        model.addAttribute("page", page);
        return "admin/allBorrowingBooksRecord";
    }

    /**
     * 管理员删除借阅记录
     *
     * @param id 借阅记录id
     * @return
     */
    @RequestMapping("/deleteBorrowingRecord")
    @ResponseBody
    public String deleteBorrowingRecord(@RequestParam("id") int id) {
        boolean res = borrowingBooksRecordService.deleteBorrowingRecordById(id);
        return res ? "true" : "false";
    }
}
