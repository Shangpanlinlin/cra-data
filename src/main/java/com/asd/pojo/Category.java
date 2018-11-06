package com.asd.pojo;

import lombok.Data;

import java.util.List;

@Data
public class Category {
    private Integer catagoryId; // auto in increment ;
    private String url;
    private String catagoryName;
    private List<Item> items;
}
