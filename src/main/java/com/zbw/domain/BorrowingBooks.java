package com.zbw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("borrowingbooks")
public class BorrowingBooks {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer bookId;

    private Date date;
}