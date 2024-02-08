package com.dft.prestashop;

import com.dft.prestashop.model.accesscredential.PrestashopAccessCredentials;
import com.dft.prestashop.model.shop.ShopWrapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;

import static com.dft.prestashop.constantcodes.ConstantCode.SHOP_ENDPOINT;

public class ShopAPI extends PrestashopSDK {

    public ShopAPI(PrestashopAccessCredentials accessCredentials) {
        super(accessCredentials);
    }

    public ShopWrapper getShops(HashMap<String, String> params) {
        URI uri = addParameters(baseUrl(SHOP_ENDPOINT), params);
        HttpRequest request = get(uri);
        return getRequestWrapped(request, ShopWrapper.class);
    }
}