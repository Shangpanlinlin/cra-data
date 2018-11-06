package com.asd.mapper;

import com.asd.pojo.Item;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;


public interface ItemMapper {

    public Long saveItems(@Param("items") Collection<Item> items);
}
