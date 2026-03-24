package com.stockgame.security.oauth2;

import java.util.Map;

/** registrationId("google", "kakao")에 맞는 {@link OAuth2UserInfo} 구현체를 반환합니다. */
public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {}

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId,
                                                   Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao"  -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 소셜 로그인 제공자: " + registrationId);
        };
    }
}

