package com.zbw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbw.domain.Book;
import com.zbw.domain.BorrowingBooks;
import com.zbw.domain.User;
import com.zbw.domain.Vo.BorrowingBooksVo;
import com.zbw.mapper.BookMapper;
import com.zbw.mapper.BorrowingBooksMapper;
import com.zbw.mapper.UserMapper;
import com.zbw.service.IBorrowingBooksRecordService;
import com.zbw.utils.page.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BorrowingBooksRecordServiceImpl implements IBorrowingBooksRecordService {
    @Resource
    private BorrowingBooksMapper borrowingBooksMapper;

    @Resource
    private BookMapper bookMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Page<BorrowingBooksVo> selectAllByPage(int pageNum) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BorrowingBooks> mpPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, 10);
        mpPage = borrowingBooksMapper.selectPage(mpPage, null);

        List<BorrowingBooksVo> borrowingBooksVos = new LinkedList<>();
        for (BorrowingBooks b : mpPage.getRecords()) {
            User user = userMapper.selectById(b.getUserId());
            Book book = bookMapper.selectById(b.getBookId());
            BorrowingBooksVo borrowingBooksVo = new BorrowingBooksVo();

            borrowingBooksVo.setId(b.getId());
            borrowingBooksVo.setUser(user);
            borrowingBooksVo.setBook(book);

            Date date1 = b.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateOfBorrowing = sdf.format(date1);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            calendar.add(Calendar.MONTH, 2);
            Date date2 = calendar.getTime();
            String dateOfReturn = sdf.format(date2);

            borrowingBooksVo.setDateOfBorrowing(dateOfBorrowing);
            borrowingBooksVo.setDateOfReturn(dateOfReturn);
            borrowingBooksVos.add(borrowingBooksVo);
        }

        Page<BorrowingBooksVo> page = new Page<>();
        page.setList(borrowingBooksVos);
        page.setPageNum((int) mpPage.getCurrent());
        page.setPageSize((int) mpPage.getSize());
        page.setPageCount((int) mpPage.getPages());
        return page;
    }

    @Override
    public ArrayList<BorrowingBooksVo> selectAllBorrowRecord(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (null == user) {
            return null;
        }

        List<BorrowingBooks> list = borrowingBooksMapper.selectList(
            new LambdaQueryWrapper<BorrowingBooks>()
                .eq(BorrowingBooks::getUserId, user.getUserId())
                .orderByAsc(BorrowingBooks::getDate));

        if (null == list) {
            return null;
        }

        ArrayList<BorrowingBooksVo> borrowingBooksVos = new ArrayList<>();
        for (BorrowingBooks b : list) {
            Book book = bookMapper.selectById(b.getBookId());
            BorrowingBooksVo borrowingBooksVo = new BorrowingBooksVo();

            borrowingBooksVo.setId(b.getId());
            borrowingBooksVo.setBook(book);

            Date date1 = b.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateOfBorrowing = sdf.format(date1);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            calendar.add(Calendar.MONTH, 2);
            Date date2 = calendar.getTime();
            String dateOfReturn = sdf.format(date2);

            borrowingBooksVo.setDateOfBorrowing(dateOfBorrowing);
            borrowingBooksVo.setDateOfReturn(dateOfReturn);

            borrowingBooksVos.add(borrowingBooksVo);
        }
        return borrowingBooksVos;
    }

    /**
     * 管理员根据借阅记录id删除借阅记录
     * @param id 借阅记录id
     * @return 删除结果
     */
    @Override
    public boolean deleteBorrowingRecordById(int id) {
        int n = borrowingBooksMapper.deleteById(id);
        return n > 0;
    }
}
