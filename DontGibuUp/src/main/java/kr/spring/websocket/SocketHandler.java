package kr.spring.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketHandler extends TextWebSocketHandler {

	private Map<String, WebSocketSession> users = new ConcurrentHashMap<String, WebSocketSession>();
	private ObjectMapper objectMapper = new ObjectMapper();

	/*
	 * 클라이언트가 연결되면, 클라이언트와 관련된 WebSocketSession을 users 맵에 저장한다. 이 users 맵은
	 * 채팅 메시지를 연결된 전체 클라이언트에 전달할 때 사용
	 */
	@Override
	public void afterConnectionEstablished(
			WebSocketSession session) throws Exception {
		log.debug("{} 연결 됨",session.getId());
		users.put(session.getId(), session);
	}

	/*
	 * 클라이언트와의 연결이 종료되면, 클라이언트에 해당하는 WebSocketSession을 users 맵에서 제거한다.
	 */
	@Override
	public void afterConnectionClosed(
			WebSocketSession session, CloseStatus status) throws Exception {
		log.debug(session.getId() + " 연결 종료됨");
		users.remove(session.getId());
	}

	/*
	 * 클라이언트가 전송한 메시지를 users 맵에 보관한 전체 WebSocketSession에 다시 전달한다. 클라이언트는
	 * 메시지를 수신하면 채팅 영역에 보여주도록 구현, 특정 클라이언트가 채팅 메시지를 서버에 보내면 전체 클라이언트는
	 * 다시 그 메시지를 받아서 화면에 보여주게 된다.
	 */
	@Override
	protected void handleTextMessage(
			WebSocketSession session, TextMessage message) throws Exception {
		log.debug(session.getId() + "로부터 메시지 수신: " + message.getPayload());
		
		JsonNode jsonNode = objectMapper.readTree(message.getPayload());
        String type = jsonNode.get("type").asText();

        if ("chatMessage".equals(type)) {
            handleChatMessage(session, jsonNode);
        } else if ("updateReadCount".equals(type)) {
            handleUpdateReadCount(jsonNode);
        }
	}
	
	private void handleChatMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
        log.debug("[접속 user 수 : " + users.values().size() + "]");
        for (WebSocketSession s : users.values()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(jsonNode.toString()));
                log.debug(s.getId() + "에 메시지 발송: " + jsonNode.toString());
            }
        }
    }

    private void handleUpdateReadCount(JsonNode jsonNode) throws Exception {
        Long messageId = jsonNode.get("messageId").asLong();
        int readCount = jsonNode.get("readCount").asInt();

        String updateMessage = String.format(
                "{\"type\": \"updateReadCount\", \"messageId\": %d, \"readCount\": %d}",
                messageId, readCount
        );

        for (WebSocketSession s : users.values()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(updateMessage));
                log.debug(s.getId() + "에 읽은 사람 수 업데이트 메시지 발송: " + updateMessage);
            }
        }
    }
	
	@Override
	public void handleTransportError(
			WebSocketSession session, Throwable exception) throws Exception {
		log.debug(session.getId() + " 익셉션 발생: " + exception.toString());
	}
}
