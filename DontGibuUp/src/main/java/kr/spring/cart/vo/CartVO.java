package kr.spring.cart.vo;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class CartVO {
	
	@NotBlank
	private long cart_num; //장바구니 식별번호
	@NotBlank
	private long item_num; //상품번호
	@NotBlank
	private long mem_num;	//회원 식별 번호
	@NotBlank
	private long cart_quantity;  //장바구니 상품 수량
	
		
	}


