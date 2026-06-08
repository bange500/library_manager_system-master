package com.zbw.service;

import com.zbw.domain.Book;
import com.zbw.domain.Vo.BookVo;
import com.zbw.utils.page.Page;

import java.util.List;

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
}
