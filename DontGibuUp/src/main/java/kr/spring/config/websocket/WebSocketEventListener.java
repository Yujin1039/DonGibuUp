package kr.spring.config.websocket;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketEventListener {
    // 채팅방 별로 접속한 사용자들을 관리하는 맵
    private final static ConcurrentHashMap<Long, Set<Long>> users = new ConcurrentHashMap<>();
    //

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long memNum = (Long) headerAccessor.getSessionAttributes().get("mem_num"); // 회원 ID
        Long chalNum = (Long) headerAccessor.getSessionAttributes().get("chal_num"); // 채팅방 ID
        log.debug("chalNum={}, memNum={}",chalNum,memNum);
        users.compute(chalNum, (key, existingValue) -> {
            if (existingValue == null) {
                // 키가 없으면 새 HashSet을 생성하고 memNum을 추가
                Set<Long> newUserSet = new HashSet<>();
                newUserSet.add(memNum);
                return newUserSet;
            } else {
                // 키가 존재하면 existingValue에 memNum을 추가
                existingValue.add(memNum);
                return existingValue;
            }
        });
        log.debug("현재 접속자 = {}",users);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long memNum = (Long) headerAccessor.getSessionAttributes().get("mem_num"); // 회원 ID
        Long chalNum = (Long) headerAccessor.getSessionAttributes().get("chal_num"); // 채팅방 ID
        
        Set<Long> connectedUsers = users.get(chalNum);        
        if (connectedUsers != null) {
            connectedUsers.remove(memNum); // 채팅방에서 사용자 제거
            if (connectedUsers.isEmpty()) {
                users.remove(chalNum); // 채팅방에 사용자가 없으면 방 제거
            }
        }
        log.debug("현재 접속자 = {}",users);
    }
    
    // 현재 채팅방에 접속한 사용자 목록을 가져오는 메서드
    public static Set<Long> getUsersInChatRoom(Long chalNum) {
        return users.getOrDefault(chalNum, new HashSet<>());
    }
    
    // 현재 채팅방에 접속한 사용자 수를 가져오는 메서드
    public static int getUserNumInChatRoom(Long chalNum) {
        log.debug("챌린지 참여: {}",users.get(chalNum).size());
    	return users.get(chalNum).size();
    }
}
