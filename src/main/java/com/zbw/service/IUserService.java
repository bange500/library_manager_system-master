package com.zbw.service;

import java.util.List;
import java.util.Map;

import com.zbw.domain.Department;
import com.zbw.domain.User;
import com.zbw.domain.Vo.BorrowingBooksVo;
import com.zbw.utils.page.Page;

import jakarta.servlet.http.HttpServletRequest;

public interface IUserService {

    // 查询用户名 为"userName"的所有用户
    List<User> findUserByUserName(String userName);

    //查询所有部门
    List<Department> findAllDepts();

    //用户登录（通过id）
    User userLogin(int userId, String password);

    //更新用户信息
    boolean updateUser(User user, HttpServletRequest request);

    //查询用户借书记录
    List<BorrowingBooksVo> findAllBorrowingBooks(HttpServletRequest request);

    //用户还书
    boolean userReturnBook(int bookId, HttpServletRequest request);

    //用户借书 
    boolean userBorrowingBook(int bookId, HttpServletRequest request);

    //通过id查找用户
    User findUserById(int id);

    //分页查询用户
    Page<User> findUserByPage(int pageNum);

    //添加用户
    int insertUser(User user);

    // 批量导入用户
    int batchAddUsers(List<User> users);

    //根据用户id删除用户
    int deleteUserById(int userId);

    // 更新用户密码
    Map<String, Object> updateUserPwd(String oldPwd, String newPwd, HttpServletRequest request);
}
