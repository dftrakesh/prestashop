package com.dft.prestashop.model.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JacksonXmlRootElement(localName = "shop")
public class Shop {
    private Integer id;
    private Integer idShopGroup;
    private Integer idCategory;
    private Integer idTheme;
    private Integer active;
    private Integer deleted;
    private String name;
}
