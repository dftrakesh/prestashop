package com.dft.prestashop.model.common;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

@Data
public class Language {

    @JacksonXmlProperty(isAttribute = true)
    private int id;

    @JacksonXmlText
    private String value;
}