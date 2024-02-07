package com.dft.prestashop.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class XmlBodyHandler<T> implements HttpResponse.BodyHandler<T> {

    private Class<T> targetType;

    public XmlBodyHandler(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return asXml(targetType);
    }

    public static <T> HttpResponse.BodySubscriber<T> asXml(Class<T> targetType) {
        HttpResponse.BodySubscriber<String> upstream = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);

        return HttpResponse.BodySubscribers.mapping(
                upstream,
                (String body) -> {
                    try {
                        XmlMapper xmlMapper = new XmlMapper();
                        System.out.println("body = " + body);
                        String cleanedBody = body.substring(body.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));

                        return xmlMapper.readValue(cleanedBody, targetType);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }
}
