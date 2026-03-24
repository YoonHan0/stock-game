package com.stockgame.security.oauth2;

import java.util.Map;

/**
 * Kakao OAuth2 응답 attributes 매핑.
 * <pre>
 * {
 *   "id": 1234567890,                          ← providerId
 *   "kakao_account": {
 *     "email": "user@kakao.com"
 *   },
 *   "properties": {
 *     "nickname": "홍길동"
 *   }
 * }
 * </pre>
 */
@SuppressWarnings("unchecked")
public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) return null;
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getNickname() {
        Map<String, Object> properties =
                (Map<String, Object>) attributes.get("properties");
        if (properties == null) return null;
        return (String) properties.get("nickname");
    }
}

