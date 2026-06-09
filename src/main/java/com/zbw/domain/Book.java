package com.zbw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

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

    @Size(max = 500, message = "简介不能超过500个字符")
    private String bookIntroduction;

}
