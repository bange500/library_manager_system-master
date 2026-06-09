package com.zbw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbw.domain.*;
import com.zbw.mapper.AdminMapper;
import com.zbw.mapper.BookCategoryMapper;
import com.zbw.mapper.BookMapper;
import com.zbw.service.IAdminService;
import com.zbw.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class AdminServiceImpl implements IAdminService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private BookMapper bookMapper;

    @Resource
    private BookCategoryMapper bookCategoryMapper;

    @Override
    public boolean adminIsExist(String name) {
        List<Admin> admin = adminMapper.selectList(
            new LambdaQueryWrapper<Admin>().eq(Admin::getAdminName, name));
        if (null == admin || admin.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public Admin adminLogin(String name, String password) {
        List<Admin> admin = adminMapper.selectList(
            new LambdaQueryWrapper<Admin>().eq(Admin::getAdminName, name));

        if (null == admin) {
            return null;
        }
        for (Admin a : admin) {
            // BCrypt 验证，兼容旧明文密码
            if (PasswordUtil.matches(password, a.getAdminPwd())) {
                // 若数据库中是旧明文密码，登录成功后自动升级为 BCrypt
                if (PasswordUtil.needsUpgrade(a.getAdminPwd())) {
                    a.setAdminPwd(PasswordUtil.encode(password));
                    adminMapper.updateById(a);
                }
                return a;
            }
        }
        return null;
    }

    @Override
    public boolean addBook(Book book) {
        int n = bookMapper.insert(book);
        return n > 0;
    }

    @Override
    public int batchAddBooks(List<Book> books) {
        int successCount = 0;
        for (Book book : books) {
            int n = bookMapper.insert(book);
            if (n > 0) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public boolean deleteBookById(int bookId) {
        int n = bookMapper.deleteById(bookId);
        return n > 0;
    }

    @Override
    public List<BookCategory> getBookCategories() {
        return bookCategoryMapper.selectList(null);
    }

    @Override
    public boolean addBookCategory(BookCategory bookCategory) {
        int n = bookCategoryMapper.insert(bookCategory);
        return n > 0;
    }

    @Override
    public boolean updateAdmin(Admin admin, HttpServletRequest request) {
        Admin sessionAdmin = (Admin) request.getSession().getAttribute("admin");
        admin.setAdminId(sessionAdmin.getAdminId());

        // 如果修改了密码且不是 BCrypt 密文，则加密
        if (admin.getAdminPwd() != null && !admin.getAdminPwd().isEmpty()
            && !PasswordUtil.isBcryptHash(admin.getAdminPwd())) {
            admin.setAdminPwd(PasswordUtil.encode(admin.getAdminPwd()));
        }

        int n = adminMapper.updateById(admin);

        if (n > 0) {
            Admin newAdmin = adminMapper.selectById(admin.getAdminId());
            request.getSession().setAttribute("admin", newAdmin);
            return true;
        }
        return false;
    }
}
