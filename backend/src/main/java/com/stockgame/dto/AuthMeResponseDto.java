package com.stockgame.dto;

import com.stockgame.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthMeResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private Long points;

    public static AuthMeResponseDto from(User user) {
        return AuthMeResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .points(user.getPoints())
                .build();
    }
}

