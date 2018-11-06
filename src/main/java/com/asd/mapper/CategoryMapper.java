package com.asd.mapper;

import com.asd.pojo.Category;
import com.asd.pojo.Item;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

public interface CategoryMapper {

    public Long saveCategory(@Param("category") Category category);
}
