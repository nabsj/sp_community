package com.example.community.post;

import com.example.community.board.Board;
import com.example.community.board.BoardService;
import com.example.community.member.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardService boardService;
    private final PostRecommendationRepository postRecommendationRepository;

    public PostService(PostRepository postRepository,
                       BoardService boardService,
                       PostRecommendationRepository postRecommendationRepository) {
        this.postRepository = postRepository;
        this.boardService = boardService;
        this.postRecommendationRepository = postRecommendationRepository;
    }

    /**
     * 특정 게시판의 전체 글 목록 (최신 순)
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByBoard(Long boardId) {
        Board board = boardService.getBoard(boardId);
        return postRepository.findByBoardOrderByIdDesc(board);
    }

    /**
     * 글 상세 조회 + 조회수 증가
     */
    @Transactional
    public Post getPostAndIncreaseViewCount(Long boardId, Long postId) {
        Board board = boardService.getBoard(boardId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다. id=" + postId));

        if (!post.getBoard().getId().equals(board.getId())) {
            throw new IllegalArgumentException("게시판과 게시글 정보가 일치하지 않습니다.");
        }

        post.increaseViewCount();
        return post;
    }

    /**
     * 추천용 단순 조회 (조회수 증가 X)
     */
    @Transactional(readOnly = true)
    public Post getPost(Long boardId, Long postId) {
        Board board = boardService.getBoard(boardId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다. id=" + postId));

        if (!post.getBoard().getId().equals(board.getId())) {
            throw new IllegalArgumentException("게시판과 게시글 정보가 일치하지 않습니다.");
        }

        return post;
    }

    /**
     * 새 글 작성
     * writer : 화면에 보여줄 작성자 이름(회원 닉네임 / 비회원 닉네임)
     */
    @Transactional
    public Post createPost(Board board,
                           String writer,
                           String title,
                           String content) {

        Post post = new Post();
        post.setBoard(board);
        post.setTitle(title);
        post.setContent(content);
        post.setWriter(writer);
        post.setViewCount(0L);
        post.setRecommendCount(0L);

        // createdAt, updatedAt 은 Post 엔티티의 @PrePersist / @PreUpdate 에서 자동 설정
        return postRepository.save(post);
    }

    /**
     * 이미 이 회원이 이 글을 추천했는지 여부
     */
    @Transactional(readOnly = true)
    public boolean hasUserRecommended(Post post, Member member) {
        if (post == null || member == null) return false;
        return postRecommendationRepository.existsByPostAndMember(post, member);
    }

    /**
     * 추천 처리
     * - 비회원은 컨트롤러에서 이미 막음
     * - 한 계정당 한 번만 추천
     */
    @Transactional
    public void recommendPost(Post post, Member member) {
        if (post == null || member == null) {
            return;
        }

        boolean already = postRecommendationRepository.existsByPostAndMember(post, member);
        if (already) {
            return;
        }

        PostRecommendation rec = new PostRecommendation(post, member);
        postRecommendationRepository.save(rec);

        post.increaseRecommendCount();
        // 영속 상태라 Dirty Checking 으로 자동 UPDATE
    }
}
