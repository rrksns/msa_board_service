package com.example.boardservice.dto;

public class AddActivitiyScoreRequestDto {
    private Long userId;
    private int score;

    public AddActivitiyScoreRequestDto(Long userId, int score) {
        this.userId = userId;
        this.score = score;
    }

    public Long getUserId() {
        return userId;
    }

    public int getScore() {
        return score;
    }
}
