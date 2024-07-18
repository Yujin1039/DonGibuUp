package kr.spring.cs.service;

import java.util.List;
import java.util.Map;

import kr.spring.cs.vo.FaqVO;
import kr.spring.cs.vo.InquiryVO;

public interface CSService {
	/*******************
	 	   1:1 문의
	 ******************/
	//문의 작성
	public void insertInquiry(InquiryVO inquiryVO);
	//문의 상세
	public InquiryVO selectInquiryDetail(long inquiry_num);
	//문의 목록
	public List<InquiryVO> selectInquiryList(Map<String, Object> map);
	//문의 개수
	public int selectInquiryListCount(Map<String, Object> map);
	//회원별 문의 목록
	public List<InquiryVO> selectInquiryListByMemNum(long mem_num);
	//문의 수정
	public void updateInquiry(InquiryVO inquiryVO);
	//문의 삭제
	public void deleteInquiry(long inquiry_num);
	//문의 답변/답변 수정(관리자)
	public void replyInquiry(InquiryVO inquiryVO);
	
	/*******************
	   	자주 하는 질문
	 ******************/
	//faq 목록
	public List<FaqVO> selectFaqList(Map<String, Object> map);
	//faq 개수
	public int selectFaqCount(Map<String, Object> map);
	//faq 등록
	public void insertFaq(FaqVO faqVO);
	//faq 수정
	public void updateFaq(FaqVO faqVO);
	//faq 삭제
	public void deleteFaq(long faq_num);
}
