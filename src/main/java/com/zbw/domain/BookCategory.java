package com.zbw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@TableName("book_category")
public class BookCategory {
    @TableId(type = IdType.AUTO)
    private Integer categoryId;

    @NotBlank(message = "类别名称不能为空")
    @Size(max = 50, message = "类别名称不能超过50个字符")
    private String categoryName;
}
