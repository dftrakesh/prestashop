package com.dft.prestashop.model.shop;

import lombok.Data;

@Data
public class Shop {
    private Integer id;
    private Integer id_shop_group;
    private Integer id_category;
    private Integer id_theme;
    private Integer active;
    private Integer deleted;
    private String name;
}
