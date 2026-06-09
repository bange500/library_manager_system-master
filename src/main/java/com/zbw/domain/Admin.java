package com.zbw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@TableName("admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Integer adminId;

    @NotBlank(message = "管理员用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度需在2-50个字符之间")
    private String adminName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 4, max = 24, message = "密码长度需在4-24个字符之间")
    private String adminPwd;

    @Email(message = "邮箱格式不正确")
    private String adminEmail;
}
