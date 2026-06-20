package com.zbw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbw.domain.Book;
import com.zbw.domain.BorrowingBooks;
import com.zbw.domain.Vo.BookVo;
import com.zbw.mapper.BookMapper;
import com.zbw.mapper.BorrowingBooksMapper;
import com.zbw.service.IBookService;
import com.zbw.utils.page.Page;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class BookServiceImpl implements IBookService {

    @Resource
    private BookMapper bookMapper;
    @Resource
    private BorrowingBooksMapper borrowingBooksMapper;

    @Override
    public List<BookVo> selectBooksByBookPartInfo(String partInfo) {
        List<BookVo> bookVos = new LinkedList<>();
        List<Book> books = bookMapper.selectList(
            new LambdaQueryWrapper<Book>().like(Book::getBookName, partInfo));

        if (null == books) {
            return bookVos;
        }

        for (Book b : books) {
            BookVo bookVo = new BookVo();
            bookVo.setBookId(b.getBookId());
            bookVo.setBookName(b.getBookName());
            bookVo.setBookAuthor(b.getBookAuthor());
            bookVo.setBookPublish(b.getBookPublish());

            List<BorrowingBooks> borrowingBooks = borrowingBooksMapper.selectList(
                new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, b.getBookId()));

            if (borrowingBooks == null || borrowingBooks.isEmpty()) {
                bookVo.setIsExist("可借");
            } else {
                bookVo.setIsExist("不可借");
            }
            bookVos.add(bookVo);
        }
        return bookVos;
    }

    @Override
    public Page<BookVo> findBooksByCategoryId(int categoryId, int pageNum) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Book> mpPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, 10);
        mpPage = bookMapper.selectPage(mpPage,
            new LambdaQueryWrapper<Book>().eq(Book::getBookCategory, categoryId));

        List<BookVo> bookVos = new LinkedList<>();
        for (Book b : mpPage.getRecords()) {
            BookVo bookVo = new BookVo();
            bookVo.setBookId(b.getBookId());
            bookVo.setBookName(b.getBookName());
            bookVo.setBookAuthor(b.getBookAuthor());
            bookVo.setBookPublish(b.getBookPublish());

            List<BorrowingBooks> borrowingBooks = borrowingBooksMapper.selectList(
                    new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, b.getBookId()));

            int borrowedCount = (borrowingBooks == null) ? 0 : borrowingBooks.size();
            int totalStock = (b.getTotalStock() == null) ? 0 : b.getTotalStock();
            if (borrowedCount < totalStock) {
                bookVo.setIsExist("可借");
            } else {
                bookVo.setIsExist("不可借");
            }
            bookVos.add(bookVo);
        }

        Page<BookVo> page = new Page<>();
        page.setList(bookVos);
        page.setPageNum((int) mpPage.getCurrent());
        page.setPageSize((int) mpPage.getSize());
        page.setPageCount((int) mpPage.getPages());
        if (mpPage.getTotal() == 0) {
            page.setPageCount(1);
        }
        return page;
    }

    @Override
    public List<Book> findBooksByCategoryId(int categoryId) {
        return bookMapper.selectList(
            new LambdaQueryWrapper<Book>().eq(Book::getBookCategory, categoryId));
    }

    @Override
    public Page<BookVo> findBooksByKeyword(String keyword, int pageNum) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Book> mpPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, 10);
        mpPage = bookMapper.selectPage(mpPage,
            new LambdaQueryWrapper<Book>().like(Book::getBookName, keyword));

        List<BookVo> bookVos = new LinkedList<>();
        for (Book b : mpPage.getRecords()) {
            BookVo bookVo = new BookVo();
            bookVo.setBookId(b.getBookId());
            bookVo.setBookName(b.getBookName());
            bookVo.setBookAuthor(b.getBookAuthor());
            bookVo.setBookPublish(b.getBookPublish());

            List<BorrowingBooks> borrowingBooks = borrowingBooksMapper.selectList(
                new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, b.getBookId()));

            if (borrowingBooks == null || borrowingBooks.isEmpty()) {
                bookVo.setIsExist("可借");
            } else {
                bookVo.setIsExist("不可借");
            }
            bookVos.add(bookVo);
        }

        Page<BookVo> page = new Page<>();
        page.setList(bookVos);
        page.setPageNum((int) mpPage.getCurrent());
        page.setPageSize((int) mpPage.getSize());
        page.setPageCount((int) mpPage.getPages());
        if (mpPage.getTotal() == 0) {
            page.setPageCount(1);
        }
        return page;
    }

    @Override
    public boolean isBookBorrowed(int bookId) {
        List<BorrowingBooks> list = borrowingBooksMapper.selectList(
            new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, bookId));
        return list != null && !list.isEmpty();
    }

    @Override
    public Book getBookDetailById(Integer bookId) {
        return bookMapper.selectById(bookId);
    }

    @Override
    public int getBorrowedCountByBookId(Integer bookId) {
        List<BorrowingBooks> list = borrowingBooksMapper.selectList(
            new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, bookId));
        return (list != null) ? list.size() : 0;
    }

    @Override
    public List<Book> findByIsbn(String isbn) {
        return bookMapper.selectList(
            new LambdaQueryWrapper<Book>().eq(Book::getIsbn, isbn));
    }

    @Override
    public List<Book> getRecommendBooks(int categoryId, int excludeBookId, int limit, Integer userId) {
        // 1. 查出当前书的书名，用于按书名排除
        String excludeBookName = null;
        Book excludeBook = bookMapper.selectById(excludeBookId);
        if (excludeBook != null) {
            excludeBookName = excludeBook.getBookName();
        }

        // 2. 查出该用户正在借阅的 bookId
        Set<Integer> userBorrowedBookIds = new HashSet<>();
        if (userId != null) {
            List<BorrowingBooks> userBorrowing = borrowingBooksMapper.selectList(
                    new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getUserId, userId));
            for (BorrowingBooks bb : userBorrowing) {
                userBorrowedBookIds.add(bb.getBookId());
            }
        }

        // 3. 查询该分类全部图书，内存随机打乱
        List<Book> allInCategory = bookMapper.selectList(
                new LambdaQueryWrapper<Book>().eq(Book::getBookCategory, categoryId));
        Collections.shuffle(allInCategory);

        // 4. 过滤：排除同名书、用户借阅中、同名去重
        List<Book> result = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        for (Book book : allInCategory) {
            if (result.size() >= limit) {
                break;
            }
            if (userBorrowedBookIds.contains(book.getBookId())) {
                continue;
            }
            if (excludeBookName != null && excludeBookName.equals(book.getBookName())) {
                continue;
            }
            if (!seenNames.add(book.getBookName())) {
                continue;
            }
            result.add(book);
        }
        return result;
    }
}
