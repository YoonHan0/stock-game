package com.stockgame.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class VoteRequestDto {

    private Long quizId;
    private String prediction; // "UP" 또는 "DOWN"
//    private UUID userId;       // 나중엔 인증 정보에서 가져오겠지만, 지금은 임시로 받음
    private String userId;
}
