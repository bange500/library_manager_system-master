package com.zbw;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbw.domain.Book;
import com.zbw.domain.Vo.BookVo;
import com.zbw.mapper.BookMapper;
import com.zbw.service.IBookService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

@SpringBootTest
public class BookServiceTest {

    @Resource
    private BookMapper bookMapper;

    @Resource
    private IBookService bookService;

    @Test
    public void testSelectBookByName() {
        List<BookVo> bookVoList = bookService.selectBooksByBookPartInfo("平凡的世界");
        if (bookVoList != null) {
            for (BookVo bookVo : bookVoList) {
                System.out.println(bookVo.getBookName() + " " + bookVo.getIsExist());
            }
        }
    }

    @Test
    public void testSelectByCategoryId() {
        com.zbw.utils.page.Page<BookVo> page = bookService.findBooksByCategoryId(1, 1);
        if (page != null) {
            for (BookVo bookVo : page.getList()) {
                System.out.println(bookVo.getBookName() + " " + bookVo.getIsExist());
            }
            System.out.println(page.getPageCount());
        }
    }

    @Test
    public void testSelectByCategoryAndPage() {
        Page<Book> mpPage = new Page<>(1, 2);
        mpPage = bookMapper.selectPage(mpPage,
            new LambdaQueryWrapper<Book>().eq(Book::getBookCategory, 1));
        List<Book> books = mpPage.getRecords();
        if (books != null) {
            for (Book b : books) {
                System.out.println(b.getBookId() + " " + b.getBookName() + " " + b.getBookCategory());
            }
        }
    }

    @Test
    public void testFindAllBookCountByCategoryId() {
        long n = bookMapper.selectCount(
            new LambdaQueryWrapper<Book>().eq(Book::getBookCategory, 1));
        System.out.println(n);
    }
}
