package kr.spring.config.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {
    // 채팅방 별로 접속한 사용자들 관리
    private final RedisTemplate<String, Long> redisTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String chalNum = String.valueOf(headerAccessor.getSessionAttributes().get("chal_num")); // 채팅방 ID 
        long memNum = (Long) headerAccessor.getSessionAttributes().get("mem_num"); // 회원 ID
        log.debug("chalNum={}, memNum={}",chalNum,memNum);
        
        redisTemplate.opsForSet().add(chalNum, memNum); // Redis에 추가

        log.debug("Redis 상태: {}", redisTemplate.opsForSet().members(chalNum));
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String chalNum = String.valueOf(headerAccessor.getSessionAttributes().get("chal_num")); // 채팅방 ID 
        long memNum = (Long) headerAccessor.getSessionAttributes().get("mem_num"); // 회원 ID
        log.debug("chalNum={}, memNum={}",chalNum,memNum);
        
        redisTemplate.opsForSet().remove(chalNum, memNum); // Redis에서 제거
        if (redisTemplate.opsForSet().size(chalNum) == 0) {
            redisTemplate.delete(chalNum); // 채팅방 삭제
        }
        log.debug("Redis 상태: {}", redisTemplate.opsForSet().members(chalNum));
    }
}
