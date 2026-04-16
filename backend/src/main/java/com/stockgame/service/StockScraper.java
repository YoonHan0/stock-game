package com.stockgame.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class StockScraper {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public BigDecimal getStockPrice(String stockCode) {
        String url = "https://finance.naver.com/item/main.naver?code=" + stockCode;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Document doc = Jsoup.parse(response.body());

            Elements priceElements = doc.select(".no_today .blind");

            if (!priceElements.isEmpty()) {
                String priceStr = priceElements.get(0).text().replace(",", "");
                return new BigDecimal(priceStr);
            }

            throw new RuntimeException("가격 정보를 파싱할 수 없습니다. stockCode=" + stockCode);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("주가 스크래핑 실패. stockCode=" + stockCode, e);
        }
    }
}