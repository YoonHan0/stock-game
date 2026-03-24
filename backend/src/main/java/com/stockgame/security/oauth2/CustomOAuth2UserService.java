package com.stockgame.security.oauth2;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.enums.AuthProvider;
import com.stockgame.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 로그인 시 사용자 정보를 users 테이블과 동기화합니다.
 * - email 기준으로 없으면 신규 가입
 * - 있으면 닉네임/소셜 식별자 정보 업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"google".equalsIgnoreCase(registrationId)) {
            OAuth2Error error = new OAuth2Error("unsupported_provider",
                    "지원하지 않는 OAuth2 provider 입니다: " + registrationId, null);
            throw new OAuth2AuthenticationException(error);
        }

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oAuth2User.getAttributes()
        );

        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname(); // 정책: 구글 profile name 그대로 사용
        String providerId = userInfo.getProviderId();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info", "Google email이 없습니다.", null));
        }
        if (nickname == null || nickname.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info", "Google profile name이 없습니다.", null));
        }

        userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.updateSocialInfo(AuthProvider.GOOGLE, providerId, nickname);
                    log.debug("기존 사용자 업데이트 - email: {}", email);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = User.ofSocial(email, nickname, AuthProvider.GOOGLE, providerId);
                    log.info("신규 소셜 사용자 가입 - email: {}", email);
                    return userRepository.save(newUser);
                });

        return oAuth2User;
    }
}

