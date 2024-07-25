package kr.spring.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import kr.spring.cart.vo.CartVO;
import kr.spring.goods.vo.PurchaseVO;
import kr.spring.goods.vo.RefundVO;
@Mapper
public interface PurchaseMapper {
	 void insertPurchase(PurchaseVO purchaseVO);
	    void insertRefund(RefundVO refundVO);
	    List<PurchaseVO> getPurchaseListByMember(long memNum);
	    void updateRefundStatus(String impUid, int status);
	    List<PurchaseVO> getAllPurchases();
	    void updateDeliveryStatus(int purchaseNum, String deliveryStatus);
	    Long insertPurchaseForCart(PurchaseVO purchaseVO);
	    Long getNextPurchaseNum();
	    List<CartVO> getPurchaseItems(long purchaseNum);
	    void insertPurchaseWithCartItems(PurchaseVO purchaseVO);
	    void insertPurchaseItems(CartVO cartItem);
	    Long getLatestPurchaseNum(long memNum);
	    @Select("SELECT purchase_seq.nextval FROM dual")
	    Long getSeq();
	    void updateDeliveryStatusByImpUid(@Param("impUid") String impUid, @Param("deliveryStatus") String deliveryStatus);
	   

}