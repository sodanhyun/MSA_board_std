package kuke.board.comment.api;


import kuke.board.comment.entity.Comment;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my comment1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my comment2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my comment3", response1.getCommentId(), 1L));

        log.info("commentId = {}",  response1.getCommentId());
        log.info("commentId = {}",  response2.getCommentId());
        log.info("commentId = {}",  response3.getCommentId());
    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 237488069889052672L)
                .retrieve()
                .body(CommentResponse.class);
        log.info("response = {}", response);
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);
        log.info("response.getCommentCount() = {}", response.getCommentCount());
        for(CommentResponse comment : response.getComments()) {
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                log.info("\tcomment.getCommentId() = " + comment.getCommentId());
            }
            log.info("comment.getCommentId() = " + comment.getCommentId());
        }

//        comment.getCommentId() = 237490263577939968
//        	comment.getCommentId() = 237490263645048838
//        comment.getCommentId() = 237490263645048838
//        comment.getCommentId() = 237490263577939969
//        	comment.getCommentId() = 237490263645048834
//        comment.getCommentId() = 237490263645048834
//        comment.getCommentId() = 237490263577939970
//        	comment.getCommentId() = 237490263645048839
//        comment.getCommentId() = 237490263645048839
//        comment.getCommentId() = 237490263577939971
//        	comment.getCommentId() = 237490263645048832
//        comment.getCommentId() = 237490263645048832
//        comment.getCommentId() = 237490263577939972
//        	comment.getCommentId() = 237490263645048836
//        comment.getCommentId() = 237490263645048836
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        log.info("firstPage");
        for(CommentResponse comment : response1) {
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                log.info("\tcomment.getCommentId() = " + comment.getCommentId());
            }
            log.info("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastParentCommentId = response1.getLast().getParentCommentId();
        Long lastCommentId = response1.getLast().getCommentId();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&lastParentCommentId=%s&lastCommentId=%s&pageSize=5"
                        .formatted(lastParentCommentId, lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        log.info("secondPage");
        for(CommentResponse comment : response1) {
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                log.info("\tcomment.getCommentId() = " + comment.getCommentId());
            }
            log.info("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    @Test
    void delete() {
//        commentId = 237488069889052672 - x
//        commentId = 237488070551752704 - x
//        commentId = 237488070627250176 - x
        restClient.delete()
                .uri("/v1/comments/{commentId}", 237488070627250176L)
                .retrieve();
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
