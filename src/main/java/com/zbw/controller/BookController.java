package com.zbw.controller;

import com.zbw.domain.Book;
import com.zbw.domain.BookCategory;
import com.zbw.domain.User;
import com.zbw.domain.Vo.BookVo;
import com.zbw.service.IAdminService;
import com.zbw.service.IBookCategoryService;
import com.zbw.service.IBookService;
import com.zbw.service.IReservationService;
import com.zbw.utils.ExcelImportUtil;
import com.zbw.utils.page.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
    @Resource
    private IReservationService reservationService;
    @Resource
    private HttpServletRequest request;

    /**
     * 管理员&emsp;&emsp;录入新书（含新增字段：ISBN/出版日期/总库存校验）
     *
     * @param book
     * @return
     */
    @RequestMapping("/addBook")
    @ResponseBody
    public String addBook(@Valid Book book) {
        // 1. ISBN唯一性校验
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            book.setIsbn(book.getIsbn().trim());
            List<Book> existBooks = bookService.findByIsbn(book.getIsbn());
            if (existBooks != null && !existBooks.isEmpty()) {
                return "ISBN已存在，请更换";
            }
        }

        // 2. 出版日期字符串转Date
        if (book.getPublishDateStr() != null && !book.getPublishDateStr().trim().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                book.setPublishDate(sdf.parse(book.getPublishDateStr().trim()));
            } catch (Exception e) {
                return "出版日期格式不正确，应为 yyyy-MM-dd";
            }
        }

        // 3. 总库存校验
        if (book.getTotalStock() == null || book.getTotalStock() < 0) {
            return "库存必须为非负整数";
        }

        boolean res = adminService.addBook(book);
        if (res) {
            return "true";
        }
        return "添加失败，请重试";
    }

    /**
     * 校验ISBN是否唯一（供前端AJAX调用）
     *
     * @param isbn
     * @return true=唯一可用, false=已存在
     */
    @RequestMapping("/checkIsbnUnique")
    @ResponseBody
    public String checkIsbnUnique(@RequestParam("isbn") String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return "false";
        }
        List<Book> existBooks = bookService.findByIsbn(isbn.trim());
        if (existBooks != null && !existBooks.isEmpty()) {
            return "false";
        }
        return "true";
    }

    /**
     * 返回编辑图书页面
     */
    @RequestMapping("/editBookPage")
    public String editBookPage(@RequestParam("bookId") int bookId, Model model) {
        Book book = bookService.getBookDetailById(bookId);
        if (book == null) {
            return "redirect:/showBooksPage";
        }
        // 格式化出版日期字符串
        if (book.getPublishDate() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            book.setPublishDateStr(sdf.format(book.getPublishDate()));
        }
        model.addAttribute("book", book);
        return "admin/editBook";
    }

    /**
     * 管理员&emsp;&emsp;修改图书信息
     */
    @RequestMapping("/updateBook")
    @ResponseBody
    public String updateBook(@Valid Book book) {
        // 1. ISBN唯一性校验（排除自身）
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            book.setIsbn(book.getIsbn().trim());
            List<Book> existBooks = bookService.findByIsbn(book.getIsbn());
            if (existBooks != null && !existBooks.isEmpty()) {
                for (Book b : existBooks) {
                    if (!b.getBookId().equals(book.getBookId())) {
                        return "ISBN已被其他图书使用，请更换";
                    }
                }
            }
        }

        // 2. 出版日期字符串转Date
        if (book.getPublishDateStr() != null && !book.getPublishDateStr().trim().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                book.setPublishDate(sdf.parse(book.getPublishDateStr().trim()));
            } catch (Exception e) {
                return "出版日期格式不正确，应为 yyyy-MM-dd";
            }
        }

        // 3. 总库存校验
        if (book.getTotalStock() == null || book.getTotalStock() < 0) {
            return "库存必须为非负整数";
        }

        boolean res = adminService.updateBook(book);
        if (res) {
            return "true";
        }
        return "修改失败，请重试";
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
        model.addAttribute("searchType", "category");
        return "admin/showBooks";
    }

    /**
     * 管理员按书名关键字查询图书
     */
    @RequestMapping("/adminFindBooksByKeyword")
    public String adminFindBooksByKeyword(@RequestParam("bookPartInfo") String bookPartInfo,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "bookCategory", defaultValue = "0") int bookCategory,
                                          Model model) {
        Page<BookVo> page = bookService.findBooksByKeyword(bookPartInfo, pageNum);
        model.addAttribute("page", page);
        model.addAttribute("keyword", bookPartInfo);
        model.addAttribute("bookCategory", bookCategory);
        model.addAttribute("searchType", "keyword");
        return "admin/showBooks";
    }

    /**
     * 返回用户&emsp;&emsp;按类别分页查询书籍结果页
     *
     * @param pageNum
     * @param bookCategory
     * @param model
     * @return
     */
    @RequestMapping("/userShowBooksByCategory")
    public String userShowBooksByCategory(@RequestParam("pageNum") int pageNum,
                                          @RequestParam("bookCategory") int bookCategory, Model model) {
        Page<BookVo> page = bookService.findBooksByCategoryId(bookCategory, pageNum);
        model.addAttribute("page", page);
        model.addAttribute("bookCategory", bookCategory);
        return "user/findBook";
    }

    /**
     * 返回用户&emsp;&emsp;查询书籍结果页（按书名关键字）
     *
     * @param bookPartInfo
     * @return
     */
    @RequestMapping("/userFindBooksByKeyword")
    public String userFindBooksByKeyword(@RequestParam("bookPartInfo") String bookPartInfo,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "bookCategory", defaultValue = "0") int bookCategory,
                                         Model model) {
        Page<BookVo> page = bookService.findBooksByKeyword(bookPartInfo, pageNum);
        model.addAttribute("page", page);
        model.addAttribute("keyword", bookPartInfo);
        model.addAttribute("bookCategory", bookCategory);
        model.addAttribute("searchType", "keyword");
        return "user/findBook";
    }

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
    public String addBookCategory(@Valid BookCategory bookCategory) {
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
     * 根据图书id查询图书完整详情（含分类名、库存、借阅状态等）
     * 所有字段均返回非null值，前端无需额外判空
     *
     * @param bookId
     * @return
     */
    @RequestMapping("/getBookDetail")
    @ResponseBody
    public Map<String, Object> getBookDetail(@RequestParam("bookId") int bookId) {
        Map<String, Object> result = new HashMap<>();

        // 无论如何先填充默认值，防止前端收到 null
        result.put("success", false);
        result.put("msg", "未知错误");
        result.put("bookId", bookId);
        result.put("bookName", "");
        result.put("bookAuthor", "");
        result.put("bookPublish", "");
        result.put("isExist", "未知");
        result.put("isbn", "");
        result.put("categoryName", "");
        result.put("categoryId", 0);
        result.put("publishDate", "");
        result.put("bookIntroduction", "");
        result.put("totalStock", 0);
        result.put("availableCount", 0);

        Book book = bookService.getBookDetailById(bookId);
        if (book == null) {
            result.put("msg", "书籍不存在");
            return result;
        }

        // 查询分类名
        BookCategory category = bookCategoryService.getCategoryById(book.getBookCategory());

        // 查询借阅状态
        boolean isBorrowed = bookService.isBookBorrowed(bookId);

        // 查询已借出数量
        int borrowedCount = bookService.getBorrowedCountByBookId(bookId);
        int totalStock = (book.getTotalStock() != null) ? book.getTotalStock() : 0;
        int availableCount = Math.max(totalStock - borrowedCount, 0);

        // 格式化出版日期
        String publishDateStr = "";
        if (book.getPublishDate() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            publishDateStr = sdf.format(book.getPublishDate());
        }

        result.put("success", true);
        result.put("msg", "查询成功");
        result.put("bookId", book.getBookId());
        result.put("bookName", nullToEmpty(book.getBookName()));
        result.put("bookAuthor", nullToEmpty(book.getBookAuthor()));
        result.put("bookPublish", nullToEmpty(book.getBookPublish()));
        result.put("isExist", (availableCount > 0) ? "可借" : "不可借");
        result.put("isbn", nullToEmpty(book.getIsbn()));
        result.put("categoryName", (category != null) ? nullToEmpty(category.getCategoryName()) : "未分类");
        result.put("categoryId", (book.getBookCategory() != null) ? book.getBookCategory() : 0);
        result.put("publishDate", publishDateStr);
        result.put("bookIntroduction", nullToEmpty(book.getBookIntroduction()));
        result.put("totalStock", totalStock);
        result.put("availableCount", availableCount);

        // 预约信息
        result.put("reservationCount", reservationService.getQueueCount(bookId));
        User sessionUser = (User) request.getSession().getAttribute("user");
        boolean userHasReserved = false;
        if (sessionUser != null) {
            userHasReserved = reservationService.hasUserReserved(sessionUser.getUserId(), bookId);
        }
        result.put("userHasReserved", userHasReserved);

        return result;
    }

    /**
     * null转空字符串工具方法
     */
    private String nullToEmpty(String str) {
        return (str != null) ? str : "";
    }

    /**
     * 获取推荐图书（同类别随机，排除同名书 + 当前用户借阅中）
     * 学生端 + 管理员端共用接口
     */
    @RequestMapping("/getRecommendBooks")
    @ResponseBody
    public Map<String, Object> getRecommendBooks(@RequestParam("categoryId") int categoryId,
                                                  @RequestParam("bookId") int bookId) {
        Map<String, Object> result = new HashMap<>();

        User sessionUser = (User) request.getSession().getAttribute("user");
        Integer userId = (sessionUser != null) ? sessionUser.getUserId() : null;

        List<Book> books = bookService.getRecommendBooks(categoryId, bookId, 3, userId);
        List<Map<String, Object>> list = new ArrayList<>();

        for (Book b : books) {
            Map<String, Object> item = new HashMap<>();
            item.put("bookId", b.getBookId());
            item.put("bookName", nullToEmpty(b.getBookName()));
            item.put("bookAuthor", nullToEmpty(b.getBookAuthor()));
            item.put("bookPublish", nullToEmpty(b.getBookPublish()));
            list.add(item);
        }

        result.put("success", true);
        result.put("data", list);
        result.put("count", list.size());
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
     * Excel 批量导入图书（含新增字段：ISBN/出版日期/总库存 + 批量校验）
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

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls") && !fileName.endsWith(".csv"))) {
            result.put("success", false);
            result.put("msg", "文件格式不正确，请上传 .xlsx、.xls 或 .csv 文件");
            return result;
        }

        try {
            // 1. 根据文件类型解析
            List<ExcelImportUtil.ImportBookResult> parseResults;
            if (fileName.endsWith(".csv")) {
                parseResults = ExcelImportUtil.parseBooksFromCsv(file);
            } else {
                parseResults = ExcelImportUtil.parseBooksFromExcel(file);
            }
            if (parseResults.isEmpty()) {
                result.put("success", false);
                result.put("msg", "Excel文件中没有有效的图书数据，请检查文件内容");
                return result;
            }

            // 2. 分离成功行和失败行
            List<Book> validBooks = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // 用于批内ISBN去重
            Set<String> batchIsbns = new HashSet<>();

            // 用于批量校验库存
            for (ExcelImportUtil.ImportBookResult pr : parseResults) {
                if (!pr.isSuccess()) {
                    errors.add("第" + pr.getRowNum() + "行: " + pr.getError());
                    continue;
                }

                Book book = pr.getBook();

                // 2.1 ISBN 校验
                String isbn = book.getIsbn();
                if (isbn != null && !isbn.trim().isEmpty()) {
                    isbn = isbn.trim();
                    book.setIsbn(isbn);

                    // 批内重复检查
                    if (!batchIsbns.add(isbn)) {
                        errors.add("第" + pr.getRowNum() + "行: ISBN【" + isbn + "】在导入文件中重复");
                        continue;
                    }

                    // 数据库已存在检查
                    List<Book> existBooks = bookService.findByIsbn(isbn);
                    if (existBooks != null && !existBooks.isEmpty()) {
                        errors.add("第" + pr.getRowNum() + "行: ISBN【" + isbn + "】在系统中已存在");
                        continue;
                    }
                }

                // 2.2 库存默认值
                if (book.getTotalStock() == null) {
                    book.setTotalStock(0);
                }

                validBooks.add(book);
            }

            if (validBooks.isEmpty()) {
                result.put("success", false);
                result.put("msg", "没有可导入的有效数据（" + String.join("; ", errors) + "）");
                return result;
            }

            // 3. 批量插入
            int successCount = adminService.batchAddBooks(validBooks);

            // 4. 构建详细结果
            StringBuilder msg = new StringBuilder();
            msg.append("成功导入 ").append(successCount).append(" 本");
            if (!errors.isEmpty()) {
                msg.append("，跳过 ").append(errors.size()).append(" 条");
            }
            result.put("success", true);
            result.put("msg", msg.toString());
            result.put("total", parseResults.size());
            result.put("imported", successCount);
            result.put("skipped", errors.size());
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

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
