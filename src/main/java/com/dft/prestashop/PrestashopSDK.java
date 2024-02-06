package com.dft.prestashop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.dft.prestashop.constantcodes.ConstantCode.AUTHORIZATION;
import static com.dft.prestashop.constantcodes.ConstantCode.BASE_ENDPOINT;
import static com.dft.prestashop.constantcodes.ConstantCode.BASIC;
import static com.dft.prestashop.constantcodes.ConstantCode.HTTP;

public class PrestashopSDK {

    private final HttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PrestashopAccessCredentials prestashopAccessCredentials;
    int MAX_ATTEMPTS = 50;
    int TIME_OUT_DURATION = 60000;

    public PrestashopSDK(String domain, String webServiceKey) {
        prestashopAccessCredentials = new PrestashopAccessCredentials(domain, encodeToBase64(webServiceKey));
        client = HttpClient.newHttpClient();
    }


    @SneakyThrows
    protected URI baseUrl(String path) {
        return new URI(new StringBuilder()
                .append(HTTP)
                .append(prestashopAccessCredentials.getDomain())
                .append(BASE_ENDPOINT)
                .append(path)
                .toString());
    }

    @SneakyThrows
    protected URI addParameters(URI uri, HashMap<String, String> params) {

        if (params == null) return uri;
        String query = uri.getQuery();
        StringBuilder builder = new StringBuilder();
        if (query != null)
            builder.append(query);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String keyValueParam = entry.getKey() + "=" + entry.getValue();
            if (!builder.toString().isEmpty())
                builder.append("&");
            builder.append(keyValueParam);
        }
        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), builder.toString(), uri.getFragment());
    }

    @SneakyThrows
    protected HttpRequest get(URI uri) {
        return HttpRequest.newBuilder(uri)
                .header(AUTHORIZATION, String.format("%s %s",BASIC, prestashopAccessCredentials.getWebServiceKey()))
                .GET()
                .build();
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
    public <T> CompletableFuture<HttpResponse<T>> tryResend(HttpClient client,
                                                            HttpRequest request,
                                                            HttpResponse.BodyHandler<T> handler,
                                                            HttpResponse<T> resp, int count) {
        if (resp.statusCode() == 429 && count < MAX_ATTEMPTS) {
            Thread.sleep(TIME_OUT_DURATION);
            return client.sendAsync(request, handler)
                .thenComposeAsync(response -> tryResend(client, request, handler, response, count + 1));
        }
        return CompletableFuture.completedFuture(resp);
    }

    public static String encodeToBase64(String input) {
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes(StandardCharsets.UTF_8));
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }
}
