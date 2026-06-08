package com.zbw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbw.domain.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
