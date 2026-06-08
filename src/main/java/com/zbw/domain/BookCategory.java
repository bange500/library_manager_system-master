package com.zbw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("book_category")
public class BookCategory {
    @TableId(type = IdType.AUTO)
    private Integer categoryId;

    private String categoryName;
}