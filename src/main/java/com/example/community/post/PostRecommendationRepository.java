package com.example.community.post;

import com.example.community.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRecommendationRepository extends JpaRepository<PostRecommendation, Long> {

    boolean existsByPostAndMember(Post post, Member member);
}
