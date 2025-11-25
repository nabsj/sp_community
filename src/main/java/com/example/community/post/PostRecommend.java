package com.example.community.post;

import com.example.community.member.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_recommends",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "member_id"})
)
public class PostRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글에 대한 추천인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 누가 추천했는지 (회원)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PostRecommend() {
    }

    public PostRecommend(Post post, Member member) {
        this.post = post;
        this.member = member;
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

    public Member getMember() {
        return member;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
