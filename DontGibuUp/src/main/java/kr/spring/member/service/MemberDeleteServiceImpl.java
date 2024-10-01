package kr.spring.member.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;

import kr.spring.cart.dao.CartMapper;
import kr.spring.challenge.dao.ChallengeMapper;
import kr.spring.challenge.vo.ChallengePaymentVO;
import kr.spring.challenge.vo.ChallengeVO;
import kr.spring.dbox.service.DboxService;
import kr.spring.dbox.vo.DboxDonationVO;
import kr.spring.delete.dao.DeleteMapper;
import kr.spring.member.dao.MemberMapper;
import kr.spring.member.vo.MemberVO;
import kr.spring.notify.service.NotifyService;
import kr.spring.notify.vo.NotifyVO;
import kr.spring.point.service.PointService;
import kr.spring.point.vo.PointVO;
import kr.spring.refund.vo.RefundVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class MemberDeleteServiceImpl implements MemberDeleteService {
	@Autowired
	MemberMapper memberMapper;
	@Autowired
	PointService pointService;
	@Autowired
	NotifyService notifyService;
	@Autowired
	PasswordEncoder pwEncoder;
	@Autowired
	CartMapper cartMapper;
	@Autowired
	ChallengeMapper challengeMapper;
	@Autowired
	DeleteMapper deleteMapper;
	@Autowired
	MemberService memberService;
	@Autowired
	DboxService dboxService;
	
	//IamportClient 초기화 하기
	private IamportClient impClient; 

	@Value("${iamport.chal_apiKey}")
	private String apiKey;
	
	@Value("${iamport.chal_secretKey}")
	private String secretKey;

	@PostConstruct
	public void initImp() {
		this.impClient = new IamportClient(apiKey,secretKey);
	}
	
	
	//회원탈퇴
	@Override
	public void deleteAccount(long mem_num) throws IamportResponseException, IOException {

		//===================단순삭제======================//

		// 카트 삭제
		cartMapper.deleteCartsByMember(mem_num);
		// 챌린지 채팅 읽은 목록 삭제
		deleteMapper.deleteChatReadsByMember(mem_num);
		//챌린지 좋아요 삭제
		challengeMapper.deleteChalFavsByMember(mem_num);
		//챌린지 인증 신고 삭제
		deleteMapper.deleteVerifyRptsByMember(mem_num);
		//챌린지 인증 삭제
		deleteMapper.deleteVerifiesByMember(mem_num);

		//문의 삭제
		deleteMapper.deleteInquiriesByMember(mem_num);
		//알림 로그 삭제
		deleteMapper.deleteNotifyLogsByMember(mem_num);
		//포인트 로그 삭제
		deleteMapper.deletePointLogsByMember(mem_num);
		//환불신청 삭제
		deleteMapper.deleteRefundsByMember(mem_num);
		//신고 삭제(mem_num) reported_mem_num 일때는 일단 그대로 두고 보여줄 때 null이면 이미 탈퇴한 회원이라고 알려주기
		deleteMapper.deleteReportsByMember(mem_num);

		//===================수정======================//
		//정기기부 중단
		deleteMapper.cancelSubscriptionByMember(mem_num);

		//챌린지 리더일 때
		// 리더인 챌린지 목록 조회
		List<Long> leaderChallengeNums = deleteMapper.selectChallengesByMember(mem_num);

		//신청한 챌린지가 없을 때
		if (!leaderChallengeNums.isEmpty()) {
			// 리더인 모든 챌린지에 대해 모든 참여자 환불 처리
			for (long chal_num : leaderChallengeNums) {
				//모든 결제 내역 불러오기
				List<ChallengePaymentVO> payList = deleteMapper.selectChalPayListByChalNum(chal_num);

				//모든 결제 내역 취소하기
				for (ChallengePaymentVO payVO : payList) {
					CancelData cancelData = new CancelData(payVO.getOd_imp_uid(), true);
					impClient.cancelPaymentByImpUid(cancelData);

					//챌린지 결제 상태 - 취소
					challengeMapper.updateChalPaymentStatus(payVO.getChal_joi_num());
					//챌린지 참가 상태 - 취소
					challengeMapper.updateChallengeJoinStatus(payVO.getChal_joi_num());

					//사용 포인트 복구
					ChallengePaymentVO chalPayVO = challengeMapper.selectChallengePayment(payVO.getChal_joi_num());
					int chal_point = chalPayVO.getChal_point();
					if (chal_point > 0) {
						//포인트 로그 작성
						PointVO pointVO = new PointVO(30, chalPayVO.getChal_point(), chalPayVO.getMem_num());
						pointService.insertPointLog(pointVO);

						//회원 포인트 업데이트
						memberService.updateMemPoint(pointVO);
					}

					//챌린지 삭제 알림
					NotifyVO notifyVO = new NotifyVO();
					notifyVO.setMem_num(payVO.getMem_num()); //알림 받을 회원 번호
					notifyVO.setNotify_type(37); //알림 타입
					notifyVO.setNot_url("/member/myPage/payment"); //알림을 누르면 반환할 url

					Map<String, String> dynamicValues = new HashMap<String, String>();
					ChallengeVO challenge = challengeMapper.selectChallenge(chal_num);
					dynamicValues.put("chalTitle", challenge.getChal_title());

					//NotifyService 호출
					notifyService.insertNotifyLog(notifyVO, dynamicValues); //알림 로그 찍기
				}

				//챌린지 톡방 환영 메시지 삭제
				challengeMapper.deleteChallengeChat(chal_num);

				//챌린지 상태 - 취소
				challengeMapper.updateChallengeStatus(chal_num);
			}
		} else {
			// 일반 회원에 대한 환불 처리

			//모든 결제 내역 불러오기
			List<ChallengePaymentVO> payList = deleteMapper.selectNonLeaderChallengesByMember(mem_num);

			if (!payList.isEmpty()) {
				//모든 결제 내역 취소하기
				for (ChallengePaymentVO payVO : payList) {
					CancelData cancelData = new CancelData(payVO.getOd_imp_uid(), true);
					impClient.cancelPaymentByImpUid(cancelData);

					//챌린지 결제 상태 - 취소
					challengeMapper.updateChalPaymentStatus(payVO.getChal_joi_num());
					//챌린지 참가 상태 - 취소
					challengeMapper.updateChallengeJoinStatus(payVO.getChal_joi_num());

					//리더에게 회원의 챌린지 취소 알림
					NotifyVO notifyVO = new NotifyVO();

					//챌린지 정보
					ChallengeVO challengeVO = challengeMapper.selectChallenge(payVO.getChal_num());

					notifyVO.setMem_num(challengeVO.getMem_num()); //알림 받을 회원 번호
					notifyVO.setNotify_type(35); //알림 타입
					notifyVO.setNot_url("/challenge/detail?chal_num=" + challengeVO.getChal_num()); //알림을 누르면 반환할 url

					Map<String, String> dynamicValues = new HashMap<String, String>();
					MemberVO participant = memberMapper.selectMemberDetail(payVO.getMem_num());
					String memNick = participant.getMem_nick();

					dynamicValues.put("memNick", memNick);
					dynamicValues.put("chalTitle", challengeVO.getChal_title());

					//NotifyService 호출
					notifyService.insertNotifyLog(notifyVO, dynamicValues); //알림 로그 찍기

				}
			}
		}
		
		//진행중인 기부박스 체크
		List<DboxDonationVO> dboxList = deleteMapper.selectOngoingDboxByMember(mem_num);
		
		for(DboxDonationVO dboxdonationVO : dboxList) {
			//refund
			RefundVO refundVO = new RefundVO();
			refundVO.setMem_num(dboxdonationVO.getMem_num());
			refundVO.setImp_uid(dboxdonationVO.getDbox_imp_uid());
			refundVO.setPayment_type(1);
			refundVO.setReason(4);
			refundVO.setReturn_point(dboxdonationVO.getDbox_do_point());
			refundVO.setAmount((int)dboxdonationVO.getDbox_do_price());
			dboxService.refund(refundVO,dboxdonationVO);
			//결제상태 변경
			dboxService.updatePayStatus(dboxdonationVO.getDbox_do_num(), 2);   				
		}
		
		//진행 이전 기부박스 종료
		deleteMapper.updateDboxStatusByMember(mem_num);
		
		//delete detail 삭제
		memberMapper.deleteMemberDetail(mem_num);

		//status 업데이트, 비밀번호 삭제
		memberMapper.deleteMember(mem_num);
		
		log.debug("<<회원 탈퇴 완료>>");

	}
}
