package com.zbw.service;

import com.zbw.domain.Book;
import com.zbw.domain.Vo.BookVo;
import com.zbw.utils.page.Page;

import java.util.List;
import java.util.Map;

public interface IBookService {

    /**
     * 根据 书籍的部分信息 去数据库中查找书籍
     *
     * @param partInfo
     * @return
     */
    List<BookVo> selectBooksByBookPartInfo(String partInfo);

    /**
     * 根据书籍名称关键字分页查找
     *
     * @param keyword
     * @param pageNum
     * @return
     */
    Page<BookVo> findBooksByKeyword(String keyword, int pageNum);

    /**
     * 根据书籍种类id查找书籍,分页查找
     *
     * @param categoryId
     * @return
     */
    Page<BookVo> findBooksByCategoryId(int categoryId, int pageNum);

    /**
     * 根据书籍种类id查找所有书籍（不分页）
     *
     * @param categoryId
     * @return
     */
    List<Book> findBooksByCategoryId(int categoryId);

    /**
     * 检查图书是否正在被借阅
     *
     * @param bookId
     * @return true=被借阅中, false=可借
     */
    boolean isBookBorrowed(int bookId);

    /**
     * 根据书籍id查询单本完整详情（含扩展信息）
     *
     * @param bookId
     * @return
     */
    Book getBookDetailById(Integer bookId);

    /**
     * 根据书籍id查询当前被借出的数量
     *
     * @param bookId
     * @return
     */
    int getBorrowedCountByBookId(Integer bookId);

    /**
     * 根据ISBN查询书籍
     *
     * @param isbn
     * @return
     */
    List<Book> findByIsbn(String isbn);

    /**
     * 根据类别ID获取推荐图书（同类别下排除当前书籍，从 Redis 缓存随机读取）
     * 同时排除指定用户正在借阅的书
     *
     * @param categoryId     书籍类别ID
     * @param excludeBookId  排除的书籍ID（当前查看的书）
     * @param limit          返回数量
     * @param userId         当前用户ID（可为null，管理员场景不传）
     * @return
     */
    List<Book> getRecommendBooks(int categoryId, int excludeBookId, int limit, Integer userId);

    /**
     * 全量刷新推荐缓存（管理员手动触发）
     * 查询所有图书 → 按分类分组 → 写入 Redis SET → 清理旧数据
     *
     * @return 同步结果摘要（总分类数、总图书数等）
     */
    Map<String, Object> refreshRecommendCache();
}
