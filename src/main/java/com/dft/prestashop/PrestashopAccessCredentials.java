package com.dft.prestashop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestashopAccessCredentials {

    private String domain;
    private String webServiceKey;
}
