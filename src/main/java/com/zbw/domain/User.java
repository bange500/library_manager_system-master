package com.zbw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度需在2-50个字符之间")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 4, max = 24, message = "密码长度需在4-24个字符之间")
    private String userPwd;

    @Email(message = "邮箱格式不正确")
    private String userEmail;

}
