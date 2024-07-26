package kr.spring.refund.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import kr.spring.refund.vo.RefundVO;

@Mapper
public interface RefundMapper {
	//환불 신청
	public void insertRefund(RefundVO refundVO);
	
	//관리자 환불신청 목록 조회
	public List<RefundVO> getRefundList(Map<String,Object> map);
	
	//사용자 환불신청 목록 조회
	public List<RefundVO> getRefundListByMemnum(Map<String,Object> map);
	
	//환불 취소(삭제)
	@Delete("DELETE FROM refund WHERE refund_num=#{refund_num}")
	public void deleteRefund(long refund_num);
	
	//환불 승인 및 반려
	@Update("UPDATE refund SET refund_status=#{refund_status}, refund_status=SYSDATE WHERE refund_num=#{refund_num}")
	public void updateRefundStatus(long refund_num, int status);
}