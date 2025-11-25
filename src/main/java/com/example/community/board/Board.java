package com.example.community.board;

import jakarta.persistence.*;

@Entity
@Table(name = "boards")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 커뮤니티 이름 (예: 자유게시판, 볼링갤러리)
    @Column(nullable = false, unique = true)
    private String name;

    // 설명
    @Column(length = 500)
    private String description;

    public Board() {
    }

    public Board(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // getter / setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
