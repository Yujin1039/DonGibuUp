package kr.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.google.gson.Gson;

import kr.spring.interceptor.LoginCheckInterceptor;
import kr.spring.interceptor.MemStatusCheckInterceptor;
import kr.spring.interceptor.WriterCheckInterceptor;
import kr.spring.config.websocket.SocketHandler;

//자바코드 기반 설정 클래스
@Configuration
public class AppConfig implements WebMvcConfigurer {
	private LoginCheckInterceptor loginCheck;
	private WriterCheckInterceptor writerCheck;
	private MemStatusCheckInterceptor memStatusCheck;
	
	@Bean
	public LoginCheckInterceptor interceptor2() {
		loginCheck = new LoginCheckInterceptor();
		return loginCheck;
	}
	@Bean
	public MemStatusCheckInterceptor interceptor3() {
		memStatusCheck = new MemStatusCheckInterceptor();
		return memStatusCheck;
	}
	@Bean
	public WriterCheckInterceptor interceptor4() {
		writerCheck = new WriterCheckInterceptor();
		return writerCheck;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		//LoginCheckInterceptor 설정
		registry.addInterceptor(loginCheck)
		        .addPathPatterns("/member/myPage/**")
		        .addPathPatterns("/board/write")
		        .addPathPatterns("/board/update")
		        .addPathPatterns("/board/delete")
		        .addPathPatterns("/dbox/propose/end")
		        .addPathPatterns("/dbox/**/example")
		        .addPathPatterns("/dbox/myPage/**")
		        .addPathPatterns("/category/insertCategory")
		        .addPathPatterns("/category/deleteCategory")
		        .addPathPatterns("/category/updateCategory")
		        .addPathPatterns("/subscription/subscriptionDetail")
		        .addPathPatterns("/subscription/subscriptionList")
		        .addPathPatterns("/subscription/paymentHistory")
				.addPathPatterns("/challenge/write")
				.addPathPatterns("/challenge/join/**")
		        .addPathPatterns("/challenge/verify/**")
		        .addPathPatterns("/challenge/review/**")
		        .addPathPatterns("/cs/inquiry")
		        .addPathPatterns("/cs/report")
				.addPathPatterns("/admin/**");
		registry.addInterceptor(memStatusCheck)
				.addPathPatterns("/member/myPage/**")  
				.addPathPatterns("/admin/**");
		registry.addInterceptor(writerCheck)
				.addPathPatterns("/member/myPage/inquiry/**");
	}  
	
	@Bean
	public TilesConfigurer tilesConfigurer() {
		final TilesConfigurer configurer = 
				                  new TilesConfigurer();
		//XML 설정 파일 경로 지정
		configurer.setDefinitions(new String[] {
				"/WEB-INF/tiles-def/main.xml",
				"/WEB-INF/tiles-def/jsy.xml",
				"/WEB-INF/tiles-def/kbr.xml",
				"/WEB-INF/tiles-def/khc.xml",
				"/WEB-INF/tiles-def/lsy.xml",
				"/WEB-INF/tiles-def/syj.xml",
				"/WEB-INF/tiles-def/yhl.xml"
		});
		configurer.setCheckRefresh(true);
		return configurer;
	}
	@Bean
	public TilesViewResolver tilesViewResolver() {
		final TilesViewResolver tilesViewResolver = 
				                  new TilesViewResolver();
		tilesViewResolver.setViewClass(TilesView.class);
		return tilesViewResolver;
	}
	
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // 예: 풀 사이즈 설정
        return scheduler;
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}