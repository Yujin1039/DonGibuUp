package kr.spring.cart.vo;

import javax.validation.constraints.NotBlank;

import kr.spring.goods.vo.GoodsVO;
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
	private Long cart_quantity;  //장바구니 상품 수량
	private long purchase_num; // 구매 식별번호 추가
    private int price; // 가격 필드 추가
	private GoodsVO goods;	
	
	public Long getItem_num() {
        return item_num;
    }

    public void setItem_num(Long item_num) {
        this.item_num = item_num;
    }

    public Long getPurchase_num() {
        return purchase_num;
    }

    public void setPurchase_num(Long purchase_num) {
        this.purchase_num = purchase_num;
    }

    public Long getCart_quantity() {
        return cart_quantity;
    }

    public void setCart_quantity(Long cart_quantity) {
        this.cart_quantity = cart_quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    // Getter for item_price
    public int getItem_price() {
        return price;
    }
	}


