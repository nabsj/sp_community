package com.example.community.post;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 게시글의 댓글인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 화면에 표시할 작성자 이름 (회원 닉네임 or 비회원이 입력한 닉네임)
    @Column(nullable = false, length = 50)
    private String writer;

    // 비회원 댓글 삭제용 비밀번호 (회원은 null)
    @Column(length = 100)
    private String guestPassword;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Comment() {
    }

    public Comment(Post post, String writer, String guestPassword, String content) {
        this.post = post;
        this.writer = writer;
        this.guestPassword = guestPassword;
        this.content = content;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public String getWriter() {
        return writer;
    }

    public String getGuestPassword() {
        return guestPassword;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
