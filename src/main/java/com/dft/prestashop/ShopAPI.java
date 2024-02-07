package com.dft.prestashop;

import com.dft.prestashop.handler.XmlBodyHandler;
import com.dft.prestashop.model.shop.ShopWrapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class ShopAPI extends PrestashopSDK {

    public ShopAPI(PrestashopAccessCredentials accessCredentials) {
        super(accessCredentials);
    }

    public ShopWrapper getShops(HashMap<String, String> params) {
        URI uri = addParameters(baseUrl("/shops"), params);
        HttpRequest request = get(uri);
        HttpResponse.BodyHandler<ShopWrapper> handler = new XmlBodyHandler<>(ShopWrapper.class);
        System.out.println("handler = " + handler);

        return getRequestWrapped(request, handler);
    }

}