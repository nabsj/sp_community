package com.example.community.post;

import com.example.community.member.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_recommendations",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"post_id", "member_id"})
       })
public class PostRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 글을 추천했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 어떤 회원이 추천했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime recommendedAt;

    protected PostRecommendation() {
    }

    public PostRecommendation(Post post, Member member) {
        this.post = post;
        this.member = member;
        this.recommendedAt = LocalDateTime.now();
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

    public LocalDateTime getRecommendedAt() {
        return recommendedAt;
    }
}
