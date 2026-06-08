package com.zbw.controller;

import com.zbw.domain.Book;
import com.zbw.domain.BookCategory;
import com.zbw.domain.Vo.BookVo;
import com.zbw.service.IAdminService;
import com.zbw.service.IBookCategoryService;
import com.zbw.service.IBookService;
import com.zbw.utils.ExcelImportUtil;
import com.zbw.utils.page.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BookController {
    @Resource
    private IAdminService adminService;
    @Resource
    private IBookService bookService;
    @Resource
    private IBookCategoryService bookCategoryService;

    /**
     * 管理员&emsp;&emsp;录入新书
     *
     * @param book
     * @return
     */
    @RequestMapping("/addBook")
    @ResponseBody
    public String addBook(Book book) {
        boolean res = adminService.addBook(book);
        if (res) {
            return "true";
        }
        return "false";
    }

    /**
     * 返回&emsp;&emsp;查询书籍结果页
     *
     * @param pageNum
     * @param model
     * @return
     */
    @RequestMapping("/showBooksResultPageByCategoryId")
    public String showBooksResultPageByCategoryId(@RequestParam("pageNum") int pageNum, @RequestParam("bookCategory") int bookCategory, Model model) {
        Page<BookVo> page = bookService.findBooksByCategoryId(bookCategory, pageNum);
        model.addAttribute("page", page);
        model.addAttribute("bookCategory", bookCategory);
        return "admin/showBooks";
    }

    /**
     * 返回用户&emsp;&emsp;查询书籍结果页
     *
     * @param bookPartInfo
     * @return
     */
    @RequestMapping("/findBookByBookPartInfo")
    public String findBooksResultPage(@RequestParam("bookPartInfo") String bookPartInfo, Model model) {
        
        List<BookVo> bookVos = bookService.selectBooksByBookPartInfo(bookPartInfo);

        model.addAttribute("bookList", bookVos);
        return "user/findBook";
    }

    /**
     * 查询所有书籍种类
     *
     * @return
     */
    @RequestMapping("/findAllBookCategory")
    @ResponseBody
    public List<BookCategory> findAllBookCategory() {
        return adminService.getBookCategories();
    }

    /**
     * 新建书籍种类
     *
     * @param bookCategory
     * @return
     */
    @RequestMapping("/addBookCategory")
    @ResponseBody
    public String addBookCategory(BookCategory bookCategory) {
        boolean b = adminService.addBookCategory(bookCategory);
        if (b) {
            return "true";
        }
        return "false";
    }

    /**
     * 根据书籍种类id查找该类别下所有图书
     *
     * @param bookCategoryId
     * @return
     */
    @RequestMapping("/findBooksByCategoryId")
    @ResponseBody
    public Map<String, Object> findBooksByCategoryId(@RequestParam("bookCategoryId") int bookCategoryId) {
        Map<String, Object> result = new HashMap<>();
        List<Book> books = bookService.findBooksByCategoryId(bookCategoryId);
        result.put("count", books.size());
        result.put("books", books);
        return result;
    }

    /**
     * 检查图书状态（是否被借阅中）
     *
     * @param bookId
     * @return
     */
    @RequestMapping("/checkBookStatus")
    @ResponseBody
    public Map<String, Object> checkBookStatus(@RequestParam("bookId") int bookId) {
        Map<String, Object> result = new HashMap<>();
        boolean isBorrowed = bookService.isBookBorrowed(bookId);
        result.put("isBorrowed", isBorrowed);
        result.put("msg", isBorrowed ? "该图书正在被借阅中，无法删除" : "该图书可安全删除");
        return result;
    }

    /**
     * 根据图书id删除图书
     *
     * @param bookId
     * @return
     */
    @RequestMapping("/deleteBook")
    @ResponseBody
    public String deleteBookById(@RequestParam("bookId") int bookId) {
        // 安全检查：如果图书正在被借阅，不能删除
        if (bookService.isBookBorrowed(bookId)) {
            return "borrowed";
        }
        boolean res = adminService.deleteBookById(bookId);
        return res ? "true" : "false";
    }

    /**
     * 根据书籍种类id删除种类
     *
     * @param bookCategoryId
     * @return
     */
    @RequestMapping("/deleteCategory")
    @ResponseBody
    public String deleteBookCategoryById(@RequestParam("bookCategoryId") int bookCategoryId) {
        int res = bookCategoryService.deleteBookCategoryById(bookCategoryId);
        if (res > 0) {
            return "true";
        }
        return "false";
    }

    /**
     * Excel 批量导入图书
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果 JSON
     */
    @RequestMapping("/importBooksByExcel")
    @ResponseBody
    public Map<String, Object> importBooksByExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("msg", "请选择要上传的Excel文件");
            return result;
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            result.put("success", false);
            result.put("msg", "文件格式不正确，请上传 .xlsx 或 .xls 文件");
            return result;
        }

        try {
            List<Book> books = ExcelImportUtil.parseBooksFromExcel(file);
            if (books.isEmpty()) {
                result.put("success", false);
                result.put("msg", "Excel文件中没有有效的图书数据，请检查文件内容");
                return result;
            }

            int successCount = adminService.batchAddBooks(books);
            result.put("success", true);
            result.put("msg", "成功导入 " + successCount + " 本图书，共读取 " + books.size() + " 条数据");
            result.put("total", books.size());
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

}
