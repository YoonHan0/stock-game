package com.stockgame.security.oauth2;

import java.util.Map;

/**
 * 소셜 제공자마다 다른 attributes 구조를 추상화한 클래스.
 * 각 제공자 구현체가 이 인터페이스를 통해 일관된 방식으로 사용자 정보를 제공합니다.
 */
public abstract class OAuth2UserInfo {

    protected final Map<String, Object> attributes;

    protected OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /** 소셜 제공자가 발급한 고유 사용자 ID */
    public abstract String getProviderId();

    /** 소셜 계정 이메일 */
    public abstract String getEmail();

    /** 소셜 프로필 닉네임 */
    public abstract String getNickname();
}

