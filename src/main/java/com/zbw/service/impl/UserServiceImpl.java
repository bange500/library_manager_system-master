package com.zbw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbw.domain.*;
import com.zbw.domain.Vo.BorrowingBooksVo;
import com.zbw.mapper.BookMapper;
import com.zbw.mapper.BorrowingBooksMapper;
import com.zbw.mapper.DepartmentMapper;
import com.zbw.mapper.UserMapper;
import com.zbw.service.IUserService;
import com.zbw.utils.PasswordUtil;
import com.zbw.utils.page.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private DepartmentMapper departmentMapper;
    @Resource
    private BorrowingBooksMapper borrowingBooksMapper;
    @Resource
    private BookMapper bookMapper;

    @Override
    public List<User> findUserByUserName(String userName) {
        return userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getUserName, userName));
    }

    @Override
    public List<Department> findAllDepts() {
        return departmentMapper.selectList(null);
    }

    @Override
    public User userLogin(int userId, String password) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        // BCrypt 验证，兼容旧明文密码
        if (PasswordUtil.matches(password, user.getUserPwd())) {
            // 若数据库中是旧明文密码，登录成功后自动升级为 BCrypt
            if (PasswordUtil.needsUpgrade(user.getUserPwd())) {
                user.setUserPwd(PasswordUtil.encode(password));
                userMapper.updateById(user);
            }
            return user;
        }
        return null;
    }

    @Override
    public boolean updateUser(User user, HttpServletRequest request) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        user.setUserId(sessionUser.getUserId());

        // 如果修改了密码且不是 BCrypt 密文，则加密
        if (user.getUserPwd() != null && !user.getUserPwd().isEmpty()
            && !PasswordUtil.isBcryptHash(user.getUserPwd())) {
            user.setUserPwd(PasswordUtil.encode(user.getUserPwd()));
        }

        int n = userMapper.updateById(user);

        if (n > 0) {
            User newUser = userMapper.selectById(user.getUserId());
            request.getSession().setAttribute("user", newUser);
            return true;
        }
        return false;
    }

    @Override
    public List<BorrowingBooksVo> findAllBorrowingBooks(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");

        List<BorrowingBooks> borrowingBooksList = borrowingBooksMapper.selectList(
            new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getUserId, user.getUserId()));

        if (null == borrowingBooksList) {
            return null;
        }

        List<BorrowingBooksVo> res = new LinkedList<>();
        for (BorrowingBooks borrowingBooks : borrowingBooksList) {
            Book book = bookMapper.selectById(borrowingBooks.getBookId());
            BorrowingBooksVo borrowingBooksVo = new BorrowingBooksVo();
            borrowingBooksVo.setBook(book);

            Date date1 = borrowingBooks.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateOfBorrowing = sdf.format(date1);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            calendar.add(Calendar.MONTH, 2);
            Date date2 = calendar.getTime();
            String dateOfReturn = sdf.format(date2);

            borrowingBooksVo.setDateOfBorrowing(dateOfBorrowing);
            borrowingBooksVo.setDateOfReturn(dateOfReturn);
            res.add(borrowingBooksVo);
        }
        return res;
    }

    @Override
    public boolean userReturnBook(int bookId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        int n = borrowingBooksMapper.delete(
            new LambdaQueryWrapper<BorrowingBooks>()
                .eq(BorrowingBooks::getUserId, user.getUserId())
                .eq(BorrowingBooks::getBookId, bookId));
        return n > 0;
    }

    @Override
    public boolean userBorrowingBook(int bookId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");

        // 检查该书是否可借
        List<BorrowingBooks> list = borrowingBooksMapper.selectList(
            new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, bookId));
        if (list.size() > 0) {
            return false;
        }

        BorrowingBooks borrowingBooks = new BorrowingBooks();
        borrowingBooks.setBookId(bookId);
        borrowingBooks.setUserId(user.getUserId());
        borrowingBooks.setDate(new Date());

        try {
            int n = borrowingBooksMapper.insert(borrowingBooks);
            return n > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public Page<User> findUserByPage(int pageNum) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> mpPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, 10);
        mpPage = userMapper.selectPage(mpPage, null);

        Page<User> page = new Page<>();
        page.setList(mpPage.getRecords());
        page.setPageNum((int) mpPage.getCurrent());
        page.setPageSize((int) mpPage.getSize());
        page.setPageCount((int) mpPage.getPages());
        return page;
    }

    @Override
    public int insertUser(User user) {
        // 密码加密存储
        if (user.getUserPwd() != null && !PasswordUtil.isBcryptHash(user.getUserPwd())) {
            user.setUserPwd(PasswordUtil.encode(user.getUserPwd()));
        }
        return userMapper.insert(user);
    }

    @Override
    public int batchAddUsers(List<User> users) {
        int successCount = 0;
        for (User user : users) {
            // 密码加密存储
            if (user.getUserPwd() != null && !PasswordUtil.isBcryptHash(user.getUserPwd())) {
                user.setUserPwd(PasswordUtil.encode(user.getUserPwd()));
            }
            int n = userMapper.insert(user);
            if (n > 0) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public int deleteUserById(int userId) {
        return userMapper.deleteById(userId);
    }
}
