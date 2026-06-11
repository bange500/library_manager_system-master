package com.zbw.service;

import com.zbw.domain.Book;
import com.zbw.domain.Vo.BookVo;
import com.zbw.utils.page.Page;

import java.util.List;

public interface IBookService {

    List<BookVo> selectBooksByBookPartInfo(String partInfo);

    Page<BookVo> findBooksByKeyword(String keyword, int pageNum);

    Page<BookVo> findBooksByCategoryId(int categoryId, int pageNum);

    List<Book> findBooksByCategoryId(int categoryId);

    boolean isBookBorrowed(int bookId);

    Book getBookDetailById(Integer bookId);

    int getBorrowedCountByBookId(Integer bookId);

    List<Book> findByIsbn(String isbn);

    /**
     * 获取推荐图书（同类别随机，排除同名书 + 当前用户借阅中）
     */
    List<Book> getRecommendBooks(int categoryId, int excludeBookId, int limit, Integer userId);
}
