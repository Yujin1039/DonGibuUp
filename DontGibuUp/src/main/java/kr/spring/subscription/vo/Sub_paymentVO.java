package kr.spring.subscription.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Sub_paymentVO {
	private long sub_pay_num;
	private long mem_num;
	private long sub_num;
	private int sub_price;
	private String sub_pay_date;
	private int sub_pay_status;
}