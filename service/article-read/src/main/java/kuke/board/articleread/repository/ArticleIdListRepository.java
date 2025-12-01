package kuke.board.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleIdListRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::board::{boardId}::article-list
    private static final String KEY_FORMAT = "article-read::board::%s::article-list";

    public void add(Long boardId, Long articleId, Long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(boardId);
            // sorted set 값을 추가(zAdd)
            // sorted set 은 score 를 기준으로 정렬된 데이터를 저장함
            // 0으로 고정한 이유 : articleId(Long) value를 score(double) 매개변수에 할당하면
            // double과 long의 수를 표현하는 bit 수는 같으나 부동 소수점을 표현하지 않는 long의
            // 표현가능한 수의 크기가 더 크므로 타입 변환 시 값이 부정확하여 순서가 꼬일 수 있음
            // sorted set 은 score가 동일한 경우 value 값을 기준으로 정렬함
            conn.zAdd(key, 0, toPaddedString(articleId));
            // 정렬된 데이터 중 상위 limit 개의 데이터만 남기도록 설정
            conn.zRemRange(key, 0, - limit - 1);
            return null;
        });
    }

    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddedString(articleId));
    }

    public List<Long> readAll(Long boardId, Long offset, Long limit) {
        return redisTemplate.opsForZSet()
                .reverseRange(generateKey(boardId), offset, offset + limit - 1)
                .stream().map(Long::valueOf).toList();
    }

    public List<Long> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long limit) {
        return redisTemplate.opsForZSet().reverseRangeByLex(
                generateKey(boardId),
                lastArticleId == null ?
                        Range.unbounded() :
                        Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastArticleId))),
                Limit.limit().count(limit.intValue())
        ).stream().map(Long::valueOf).toList();
    }

    private String toPaddedString(Long articleId) {
        return "%019d".formatted(articleId);
        // 1234 -> 0000000000000001234
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}
