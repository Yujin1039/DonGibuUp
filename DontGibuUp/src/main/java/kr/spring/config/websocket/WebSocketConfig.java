package kr.spring.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import kr.spring.interceptor.SocketInterceptor;

import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
    	config.enableSimpleBroker("/sub"); // 메시지 구독(수신) 경로
    	config.setApplicationDestinationPrefixes("/pub"); // 메시지 발행 경로
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
    	registry.addEndpoint("/ws-chat")
    			.setAllowedOrigins("http://localhost:8000")
    			.addInterceptors(new SocketInterceptor())
    			.withSockJS();
    }
}
