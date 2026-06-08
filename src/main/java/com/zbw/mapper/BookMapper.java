package com.zbw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbw.domain.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
