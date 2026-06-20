package com.zbw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("reservation")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer bookId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date reserveTime;

    /** 0=排队中 1=待借阅(已通知) 2=已借出 3=已取消 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date notifyTime;

    /** 用户名 — JOIN 查询填充 */
    @TableField(exist = false)
    private String userName;

    /** 书名 — JOIN 查询填充 */
    @TableField(exist = false)
    private String bookName;
}
