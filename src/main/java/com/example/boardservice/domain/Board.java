package com.example.boardservice.domain;

import jakarta.persistence.*;

@Entity
@Table(name="boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;
    private String titile;
    private String content;
    private Long userId;

    public Board() {
    }

    public Board(String titile, String content, Long userId) {
        this.titile = titile;
        this.content = content;
        this.userId = userId;
    }

    public Long getBoardId() {
        return boardId;
    }

    public String getTitile() {
        return titile;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}
