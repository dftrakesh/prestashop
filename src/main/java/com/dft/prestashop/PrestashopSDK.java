package com.dft.prestashop;

import lombok.SneakyThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.dft.prestashop.constantcodes.ConstantCode.ACCEPT;
import static com.dft.prestashop.constantcodes.ConstantCode.ACCEPT_VALUE;
import static com.dft.prestashop.constantcodes.ConstantCode.AUTHORIZATION;
import static com.dft.prestashop.constantcodes.ConstantCode.BASE_ENDPOINT;
import static com.dft.prestashop.constantcodes.ConstantCode.BASIC;
import static com.dft.prestashop.constantcodes.ConstantCode.CONTENT_TYPE;
import static com.dft.prestashop.constantcodes.ConstantCode.CONTENT_TYPE_VALUE;
import static com.dft.prestashop.constantcodes.ConstantCode.HTTP;

public class PrestashopSDK {

    private final HttpClient client;
    private final PrestashopAccessCredentials prestashopAccessCredentials;

    int MAX_ATTEMPTS = 50;
    int TIME_OUT_DURATION = 60000;

    public PrestashopSDK(PrestashopAccessCredentials accessCredentials) {
        prestashopAccessCredentials = accessCredentials;
        client = HttpClient.newHttpClient();
    }

    protected HttpRequest get(URI uri) {
        String base64EncodedWebServiceKey = Base64.getEncoder().encodeToString(prestashopAccessCredentials.getWebServiceKey().getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder(uri)
            .header(AUTHORIZATION, String.format("%s%s", BASIC, base64EncodedWebServiceKey))
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT, ACCEPT_VALUE)
            .GET()
            .build();
    }

    protected URI baseUrl(String path) {

        return URI.create(String.format("%s%s%s%s", HTTP, prestashopAccessCredentials.getDomain(), BASE_ENDPOINT, path));
    }

    protected URI addParameters(URI uri, HashMap<String, String> params) {

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String keyValueParam = String.format("%s=%s", entry.getKey(), entry.getValue());
            if (!builder.toString().isEmpty()) {
                builder.append("&");
            }
            builder.append(keyValueParam);
        }

        return URI.create(uri + "?" + builder);
    }


    @SneakyThrows
    public <T> T getRequestWrapped(HttpRequest request, HttpResponse.BodyHandler<T> handler) {

        return client
            .sendAsync(request, handler)
            .thenComposeAsync(response -> tryResend(client, request, handler, response, 1))
            .get()
            .body();
    }

    @SneakyThrows
    public <T> CompletableFuture<HttpResponse<T>> tryResend(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<T> handler, HttpResponse<T> resp, int count) {
        if (resp.statusCode() == 429 && count < MAX_ATTEMPTS) {
            Thread.sleep(TIME_OUT_DURATION);

            return client.sendAsync(request, handler)
                .thenComposeAsync(response -> tryResend(client, request, handler, response, count + 1));
        }

        return CompletableFuture.completedFuture(resp);
    }
}
