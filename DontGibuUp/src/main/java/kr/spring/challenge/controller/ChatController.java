package kr.spring.challenge.controller;

import kr.spring.challenge.vo.ChallengeChatVO;
import kr.spring.config.websocket.WebSocketEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	/*
	

    
    // 현재 채팅방 접속자 수
	public int getRoomUsersNum(Long chal_num) {
		Set<String> users = roomUsers.get(chal_num);
	    return (users != null) ? users.size() : 0;
	}
    */

    // 채팅 메시지를 전송하는 엔드포인트
    //@SendTo("/challenge/")
	@MessageMapping("/sendChat")
    public void sendMessage(ChallengeChatVO chatMessage) {		
		log.debug("sendMessage 동작");
		int num = WebSocketEventListener.getUserNumInChatRoom(chatMessage.getChal_num());
		
		Map<String,Object> payload = new HashMap<>(); 
		payload.put("mem_num", chatMessage.getMem_num());
		payload.put("mem_photo", chatMessage.getMem_photo());
		payload.put("mem_nick", chatMessage.getMem_nick());
		payload.put("chat_content", chatMessage.getChat_content());
		payload.put("chat_filename", chatMessage.getChat_filename());
		payload.put("chat_date", chatMessage.getChat_date());
		payload.put("chat_readCount", num);
		log.debug("메시지 내용 = {}",payload);
		log.debug("url: /sub/challenge/{}",chatMessage.getChal_num());
		messagingTemplate.convertAndSend("/sub/challenge/"+chatMessage.getChal_num(), payload);
    }
	
	@MessageMapping("/update-unread/{chal_num}/{mem_num}/{lastChatId}/{latest_chat_id}")
	@SendTo("/sub/update/{chal_num}")
	public Map<String,Long> readStatus(@DestinationVariable Long chal_num,@DestinationVariable Long mem_num,
			@DestinationVariable Long lastChatId,@DestinationVariable Long latest_chat_id) {
	    Map<String,Long> info = new HashMap<>();
	    info.put("mem_num",mem_num);
	    info.put("last_chat_id",lastChatId);
	    info.put("latest_chat_id", latest_chat_id);

	    return info;
	}
}
