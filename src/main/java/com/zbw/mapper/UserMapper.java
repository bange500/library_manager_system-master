package com.zbw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbw.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
