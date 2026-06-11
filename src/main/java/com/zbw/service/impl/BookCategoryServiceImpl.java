package com.zbw.service.impl;

import com.zbw.domain.BookCategory;
import com.zbw.mapper.BookCategoryMapper;
import com.zbw.service.IBookCategoryService;
import com.zbw.utils.page.Page;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class BookCategoryServiceImpl implements IBookCategoryService {

    @Resource
    private BookCategoryMapper bookCategoryMapper;

    @Override
    public Page<BookCategory> selectBookCategoryByPageNum(int pageNum) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BookCategory> mpPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, 10);
        mpPage = bookCategoryMapper.selectPage(mpPage, null);

        Page<BookCategory> page = new Page<>();
        page.setList(mpPage.getRecords());
        page.setPageNum((int) mpPage.getCurrent());
        page.setPageSize((int) mpPage.getSize());
        page.setPageCount((int) mpPage.getPages());
        return page;
    }

    @Override
    public int deleteBookCategoryById(int bookCategoryId) {
        return bookCategoryMapper.deleteById(bookCategoryId);
    }

    @Override
    public BookCategory getCategoryById(Integer categoryId) {
        return bookCategoryMapper.selectById(categoryId);
    }
}
