package com.stockgame.security.oauth2;

import java.util.Map;

/**
 * Google OAuth2 응답 attributes 매핑.
 * <pre>
 * {
 *   "sub":     "1234567890",   ← providerId
 *   "email":   "user@gmail.com",
 *   "name":    "홍길동"
 * }
 * </pre>
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("name");
    }
}

