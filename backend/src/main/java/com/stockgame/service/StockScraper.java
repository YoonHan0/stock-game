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

    // Java 21의 HttpClient 활용
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public BigDecimal getStockPrice(String stockCode) {
        String url = "https://finance.naver.com/item/main.naver?code=" + stockCode;

        try {
            // 1. HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0") // 차단 방지용 헤더
                    .GET()
                    .build();

            // 2. 응답 받기
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 3. Jsoup으로 HTML 파싱
            Document doc = Jsoup.parse(response.body());

            // 네이버 금융의 현재가 위치 (클래스명: no_today)
            Elements priceElements = doc.select(".no_today .blind");

            if (!priceElements.isEmpty()) {
                String priceStr = priceElements.get(0).text().replace(",", "");
                return new BigDecimal(priceStr);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}