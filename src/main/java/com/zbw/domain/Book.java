package com.zbw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
@TableName("book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Integer bookId;

    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名不能超过200个字符")
    private String bookName;

    @Size(max = 100, message = "作者名不能超过100个字符")
    private String bookAuthor;

    @Size(max = 100, message = "出版社名不能超过100个字符")
    private String bookPublish;

    private Integer bookCategory;

    @DecimalMin(value = "0.0", message = "价格不能为负数")
    private Double bookPrice;

    @Size(max = 2000, message = "简介不能超过2000个字符")
    private String bookIntroduction;

    @NotBlank(message = "ISBN不能为空")
    @Size(max = 20, message = "ISBN不能超过20个字符")
    private String isbn;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishDate;

    private Integer totalStock;

    /**
     * 前端表单接收出版日期字符串，非数据库字段
     */
    @TableField(exist = false)
    private String publishDateStr;

}
