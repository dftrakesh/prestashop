package com.dft.prestashop;

import com.dft.prestashop.model.accesscredential.PrestashopAccessCredentials;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
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
    private final XmlMapper xmlMapper;
    private final PrestashopAccessCredentials prestashopAccessCredentials;

    int MAX_ATTEMPTS = 50;
    int TIME_OUT_DURATION = 15000;

    public PrestashopSDK(PrestashopAccessCredentials accessCredentials) {
        prestashopAccessCredentials = accessCredentials;
        client = HttpClient.newHttpClient();
        xmlMapper = new XmlMapper();
    }

    protected HttpRequest get(URI uri) {
        String base64EncodedWebServiceKey = Base64.getEncoder().encodeToString((prestashopAccessCredentials.getWebServiceKey() + ":").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder(uri)
            .header(AUTHORIZATION, String.format("%s%s", BASIC, base64EncodedWebServiceKey))
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT, ACCEPT_VALUE)
            .GET()
            .build();
    }

    protected HttpRequest post(URI uri, String xmlBody) {
        String base64EncodedWebServiceKey = Base64.getEncoder().encodeToString(prestashopAccessCredentials.getWebServiceKey().getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder(uri)
            .header(AUTHORIZATION, String.format("%s%s", BASIC, base64EncodedWebServiceKey))
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT, ACCEPT_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(xmlBody))
            .build();
    }

    protected HttpRequest put(URI uri, String xmlBody) {
        String base64EncodedWebServiceKey = Base64.getEncoder().encodeToString(prestashopAccessCredentials.getWebServiceKey().getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder(uri)
            .header(AUTHORIZATION, String.format("%s%s", BASIC, base64EncodedWebServiceKey))
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT, ACCEPT_VALUE)
            .PUT(HttpRequest.BodyPublishers.ofString(xmlBody))
            .build();
    }

    protected HttpRequest delete(URI uri) {
        String base64EncodedWebServiceKey = Base64.getEncoder().encodeToString(prestashopAccessCredentials.getWebServiceKey().getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder(uri)
            .header(AUTHORIZATION, String.format("%s%s", BASIC, base64EncodedWebServiceKey))
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT, ACCEPT_VALUE)
            .DELETE()
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
    public <T> T getRequestWrapped(HttpRequest request, Class<T> responseType) {
        HttpResponse<String> response = client
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenComposeAsync(resp -> tryResend(client, request, resp, 1))
            .get();

        return xmlToObject(response.body(), responseType);
    }

    @SneakyThrows
    public <T> CompletableFuture<HttpResponse<String>> tryResend(HttpClient client, HttpRequest request, HttpResponse<String> resp, int count) {
        if (resp.statusCode() == 429 && count < MAX_ATTEMPTS) {
            Thread.sleep(TIME_OUT_DURATION);

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenComposeAsync(response -> tryResend(client, request, resp, count + 1));
        }

        return CompletableFuture.completedFuture(resp);
    }

    @SneakyThrows
    public String objectToXml(Object t) {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        return xmlMapper.writeValueAsString(t);
    }

    @SneakyThrows
    public <T> T xmlToObject(String response, Class<T> var) {
        return xmlMapper.readValue(response, var);
    }
}
