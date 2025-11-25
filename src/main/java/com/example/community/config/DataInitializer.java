package com.example.community.config;

import com.example.community.board.Board;
import com.example.community.board.BoardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BoardRepository boardRepository;

    public DataInitializer(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public void run(String... args) {

        if (boardRepository.count() == 0) {
            boardRepository.save(new Board("자유게시판", "잡담, 일상 무엇이든 자유롭게"));
            boardRepository.save(new Board("볼링갤러리", "볼링 이야기, 장비, 구질, 영상 공유"));
            boardRepository.save(new Board("개발질문", "자바, 스프링, 코딩 질문 전용"));
        }
    }
}
