package com.zbw;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbw.domain.BookCategory;
import com.zbw.mapper.BookCategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

@SpringBootTest
public class BookCategoryMapperTest {

    @Resource
    private BookCategoryMapper bookCategoryMapper;

    @Test
    public void testSelectByPageNum() {
        Page<BookCategory> mpPage = new Page<>(1, 5);
        mpPage = bookCategoryMapper.selectPage(mpPage, null);
        List<BookCategory> list = mpPage.getRecords();
        if (list != null) {
            for (BookCategory b : list) {
                System.out.println(b.getCategoryId() + " " + b.getCategoryName());
            }
        }
    }

    @Test
    public void testSelectAllCount() {
        long n = bookCategoryMapper.selectCount(null);
        System.out.println(n);
    }
}
