package com.zbw;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbw.domain.BorrowingBooks;
import com.zbw.mapper.BorrowingBooksMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

@SpringBootTest
public class BorrowingBooksMapperTest {

    @Resource
    private BorrowingBooksMapper borrowingBooksMapper;

    @Test
    public void testSelectAllRecordCount() {
        long count = borrowingBooksMapper.selectCount(
            new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getUserId, 1));
        System.out.println(count);
    }

    @Test
    public void testSelectAllByPageNum() {
        Page<BorrowingBooks> mpPage = new Page<>(1, 5);
        mpPage = borrowingBooksMapper.selectPage(mpPage, null);
        List<BorrowingBooks> list = mpPage.getRecords();
        long count = borrowingBooksMapper.selectCount(null);
        System.out.println("count: " + count);
        if (list != null) {
            for (BorrowingBooks b : list) {
                System.out.println(b.getId() + " " + b.getBookId());
            }
        }
    }
}
