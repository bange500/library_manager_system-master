package com.zbw.service;

import java.util.List;
import java.util.Map;

import com.zbw.domain.Admin;
import com.zbw.domain.Book;
import com.zbw.domain.BookCategory;

import jakarta.servlet.http.HttpServletRequest;

public interface IAdminService {

    //验证用户是否存在
    boolean adminIsExist(String name);
    
    //管理员登陆
    Admin adminLogin(String name, String password);

    //&emsp;&emsp;录入新书
    boolean addBook(Book book);

    // 批量导入图书
    int batchAddBooks(List<Book> books);

    // 根据id删除图书
    boolean deleteBookById(int bookId);

    // 修改图书信息
    boolean updateBook(Book book);

    //获取所有图书类别
    List<BookCategory> getBookCategories();

    //增加图书类别
    boolean addBookCategory(BookCategory bookCategory);

    // 更新管理员信息
    boolean updateAdmin(Admin admin, HttpServletRequest request);

    // 更新管理员密码
    Map<String, Object> updateAdminPwd(String oldPwd, String newPwd, HttpServletRequest request);
}
