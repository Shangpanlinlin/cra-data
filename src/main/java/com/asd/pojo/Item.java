package com.asd.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Item {

    private Integer id ;  //primary auto_increment
    private String image;  //url
    private String name;
    private String SKU;  //
    private String packSize;
    private double discountedPrice;
    private double retailPrice;
    private double savePercent;
    private String  inStock;
    private String barcode;
    private String brand;

    List<Category> categories = new ArrayList<>();


}
