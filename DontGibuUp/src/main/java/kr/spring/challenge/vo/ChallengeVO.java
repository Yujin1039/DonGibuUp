package kr.spring.challenge.vo;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ChallengeVO {
	private long chal_num;
	private long mem_num;
	@NotNull
	private int chal_public;//0:공개,1:비공개
	@NotNull
	private int chal_type;
	@NotBlank
	private String chal_title;
	private String chal_content;
	private MultipartFile upload;//파일
	private String chal_photo;//파일명
	@NotBlank
	private String chal_verify;
	@NotNull
	private Integer chal_freq;
	@NotEmpty
	private String chal_sdate;
	@NotNull
	private Integer chal_period;
	@Range(min=1000,max=200000)
	private Integer chal_fee;
	@Range(min=1)
	private Integer chal_max;
	private Date chal_rdate;
	private String chal_ip;
	private Integer chal_status;
	
	private String chal_edate;
	private String mem_nick;
	private String mem_photo;
	private String categoryName;
	private Integer chal_phase;   //0:시작 전,1:진행 중,2:완료
	private Integer fav;		  //좋아요 수
	private Integer attendance;
	
	public void calculateChalEdate() {
		LocalDate sdate = LocalDate.parse(chal_sdate,DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate edate = sdate.plusDays(chal_period * 7 - 1);
		this.chal_edate = edate.toString();
	}
}
