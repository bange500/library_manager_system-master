package com.zbw;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbw.domain.Department;
import com.zbw.domain.User;
import com.zbw.domain.Vo.BorrowingBooksVo;
import com.zbw.mapper.UserMapper;
import com.zbw.service.IBorrowingBooksRecordService;
import com.zbw.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

@SpringBootTest
public class UserTest {

    @Resource
    private IUserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private IBorrowingBooksRecordService borrowingBooksRecordService;

    @Test
    public void findUsersByName() {
        List<User> users = userService.findUserByUserName("yxc");
        if (users != null) {
            for (User u : users) {
                System.out.println(u.getUserName());
            }
        } else {
            System.out.println("null");
        }
    }

    @Test
    public void findAllDepts() {
        List<Department> depts = userService.findAllDepts();
        if (depts == null) {
            System.out.println("null");
        } else {
            for (Department d : depts) {
                System.out.println(d.getDeptName());
            }
        }
    }

    @Test
    public void updateUserTest() {
        User user = new User();
        // 你的更新逻辑
    }

    @Test
    public void selectByPage() {
        Page<User> mpPage = new Page<>(1, 5);
        mpPage = userMapper.selectPage(mpPage, null);
        List<User> users = mpPage.getRecords();
        if (users != null) {
            for (User u : users) {
                System.out.println(u.getUserId() + " " + u.getUserName());
            }
        }
    }

    @Test
    public void testSelectCount() {
        long n = userMapper.selectCount(null);
        System.out.println(n);
    }

    @Test
    public void testSelectAllBorrowingBooksByPageNum() {
        com.zbw.utils.page.Page<BorrowingBooksVo> page = borrowingBooksRecordService.selectAllByPage(1);
        if (page != null) {
            for (BorrowingBooksVo b : page.getList()) {
                System.out.println(b.getUser().getUserName() + " " + b.getBook().getBookName());
            }
        }
    }
}
