package kr.spring.challenge.vo;

import java.sql.Date;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ChallengeJoinVO {
	
    private long chal_joi_num; 			//챌린지 참가번호
    @NotNull
    private long chal_num; 				//챌린지 번호
    @NotNull
    private long mem_num; 				//회원 번호
    
    @NotNull
    private Integer dcate_num; 			//기부 카테고리
    
    private Double chal_joi_rate; 		//최종 달성률
    private Double chal_joi_total; 		//최종 기부액
    private Integer chal_joi_success; 	//성공 여부
    private Double chal_joi_refund; 	//환급액
    private Integer chal_joi_status; 	//참가 상태
    
    private Date chal_joi_date; 		//챌린지 참가일
    private String chal_joi_ip;			//챌린지 참가자 ip
    
    // 추가된 속성
    private String chal_title;
    private String chal_sdate;
    private String chal_edate;
    private Long chal_fee;
    private String dcate_charity;
    private int chal_freq;
    private String mem_nick;	//회원 닉네임
    private String mem_photo;   //회원 프로필
    private int reported_num;	//회원별 제보된 인증 수
    private boolean isHost;  	//챌린지 개설자 여부
    private boolean isPub;
}