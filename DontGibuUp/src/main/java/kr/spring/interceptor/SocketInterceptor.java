package kr.spring.interceptor;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import kr.spring.member.vo.MemberVO;

public class SocketInterceptor implements HandshakeInterceptor{

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
	    
	    if (request instanceof ServletServerHttpRequest) {
	        HttpSession session = ((ServletServerHttpRequest) request).getServletRequest().getSession(false);
	        if (session != null) {
	            Long chalNum = (Long) session.getAttribute("chal_num");
	            MemberVO member = (MemberVO) session.getAttribute("user");
	            Long memNum = member.getMem_num();

	            attributes.put("chal_num", chalNum);
	            attributes.put("mem_num", memNum);
	        }
	    }

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		// TODO Auto-generated method stub
		
	}

}
