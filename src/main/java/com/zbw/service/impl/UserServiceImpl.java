package com.zbw.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbw.domain.Book;
import com.zbw.domain.BorrowingBooks;
import com.zbw.domain.Department;
import com.zbw.domain.User;
import com.zbw.domain.Vo.BorrowingBooksVo;
import com.zbw.mapper.BookMapper;
import com.zbw.mapper.BorrowingBooksMapper;
import com.zbw.mapper.DepartmentMapper;
import com.zbw.mapper.UserMapper;
import com.zbw.service.IReservationService;
import com.zbw.service.IUserService;
import com.zbw.utils.PasswordUtil;
import com.zbw.utils.page.Page;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

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
    @Resource
    private IReservationService reservationService;

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
        if (n > 0) {
            // 归还成功后，通知预约排队用户
            reservationService.processReturnNotification(bookId);
            return true;
        }
        return false;
    }

    @Override
    public boolean userBorrowingBook(int bookId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");

        // 检查该书是否可借：已借出数量 < 总库存
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            return false;
        }
        int totalStock = (book.getTotalStock() == null) ? 0 : book.getTotalStock();
        List<BorrowingBooks> list = borrowingBooksMapper.selectList(
                new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, bookId));
        int borrowedCount = (list == null) ? 0 : list.size();
        if (borrowedCount >= totalStock) {
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

    @Override
    public Map<String, Object> updateUserPwd(String oldPwd, String newPwd, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User sessionUser = (User) request.getSession().getAttribute("user");
        User user = userMapper.selectById(sessionUser.getUserId());

        // 验证原密码
        if (!PasswordUtil.matches(oldPwd, user.getUserPwd())) {
            result.put("code", 1);
            result.put("msg", "原密码错误");
            return result;
        }

        // 设置新密码
        user.setUserPwd(PasswordUtil.encode(newPwd));
        int n = userMapper.updateById(user);

        if (n > 0) {
            User newUser = userMapper.selectById(user.getUserId());
            request.getSession().setAttribute("user", newUser);
            result.put("code", 0);
            result.put("msg", "密码修改成功");
            return result;
        }

        result.put("code", 1);
        result.put("msg", "密码修改失败");
        return result;
    }
}
