package kr.spring.challenge.service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siot.IamportRestClient.IamportClient;

import kr.spring.challenge.dao.ChallengeMapper;
import kr.spring.challenge.exception.MaxParticipantsExceededException;
import kr.spring.challenge.vo.ChallengeChatVO;
import kr.spring.challenge.vo.ChallengeFavVO;
import kr.spring.challenge.vo.ChallengeJoinVO;
import kr.spring.challenge.vo.ChallengePaymentVO;
import kr.spring.challenge.vo.ChallengeReviewVO;
import kr.spring.challenge.vo.ChallengeVO;
import kr.spring.challenge.vo.ChallengeVerifyRptVO;
import kr.spring.challenge.vo.ChallengeVerifyVO;
import kr.spring.member.dao.MemberMapper;
import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import kr.spring.notify.service.NotifyService;
import kr.spring.notify.vo.NotifyVO;
import kr.spring.point.service.PointService;
import kr.spring.point.vo.PointVO;
import kr.spring.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ChallengeServiceImpl implements ChallengeService{

	@Autowired
	ChallengeMapper challengeMapper;
	@Autowired
	MemberMapper memberMapper;
	@Autowired
	PointService pointService;
	@Autowired
	MemberService memberService;
	@Autowired
	NotifyService notifyService;
	@Autowired
	private ServletContext servletContext;

	//*챌린지 개설*//
	@Override
	public void insertChallenge(ChallengeVO chalVO,ChallengeJoinVO joinVO,ChallengePaymentVO payVO,ChallengeChatVO chatVO) {
		//1. 챌린지 기본 정보 삽입
		chalVO.setChal_num(challengeMapper.selectChal_num());
		challengeMapper.insertChallenge(chalVO);

		//2. 챌린지 참가 정보 삽입
		joinVO.setChal_num(chalVO.getChal_num());
		joinVO.setChal_joi_num(challengeMapper.selectChal_joi_num());
		challengeMapper.insertChallengeJoin(joinVO);

		//3. 챌린지 결제 정보 삽입
		payVO.setChal_joi_num(joinVO.getChal_joi_num());
		challengeMapper.insertChallengePayment(payVO);

		//4. 결제시 포인트 변동사항 기록 
		if(payVO.getChal_point() > 0) {
			//포인트 로그 작성
			PointVO pointVO = new PointVO(20,-payVO.getChal_point(),payVO.getMem_num());
			pointService.insertPointLog(pointVO);

			//회원 포인트 업데이트
			memberService.updateMemPoint(pointVO);
		}

		//5. 챌린지 채팅방 정보 삽입 (공개 챌린지일 경우만)
		if(chalVO.getChal_public() == 0) {
			chatVO.setChal_num(chalVO.getChal_num());	
			long chat_id = challengeMapper.selectChat_id();
			chatVO.setChat_id(chat_id);
			chatVO.setChat_date(chalVO.getChal_sdate());			
			challengeMapper.insertChallengeChat(chatVO);
			//채팅 기록 로그에 추가
			Map<String,Long> map = new HashMap<>();
			map.put("chal_num", chalVO.getChal_num());
			map.put("last_chat_id",chat_id);
			map.put("mem_num", payVO.getMem_num());
			challengeMapper.insertChatLog(map);
		}

		//6. 챌린지 개설 알림
		NotifyVO notifyVO = new NotifyVO();
		notifyVO.setMem_num(chalVO.getMem_num()); //챌린지 개설자에게 알림
		notifyVO.setNotify_type(3); //알림 타입: 챌린지 개설 알림
		notifyVO.setNot_url("/challenge/detail?chal_num=" + chalVO.getChal_num()); //알림 클릭 시 이동할 URL

		//동적 데이터 매핑
		Map<String, String> dynamicValues = new HashMap<>();
		dynamicValues.put("chalTitle", chalVO.getChal_title()); //알림 템플릿에서 사용할 동적 데이터

		//알림 서비스 호출
		notifyService.insertNotifyLog(notifyVO, dynamicValues);
	}	

	@Override
	public List<ChallengeVO> selectList(Map<String, Object> map) {
		return challengeMapper.selectList(map);
	}

	@Override
	public Integer selectRowCount(Map<String, Object> map) {
		return challengeMapper.selectRowCount(map);
	}

	@Override
	public ChallengeVO selectChallenge(Long chal_num) {
		return challengeMapper.selectChallenge(chal_num);
	}

	@Override
	public void updateChallenge(Long chal_num) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteChalPhoto(Long chal_num) {
		challengeMapper.deleteChalPhoto(chal_num);
	}

	//*챌린지 참가*//
	@Override
	public void insertChallengeJoin(ChallengeJoinVO chalJoinVO, ChallengePaymentVO chalPayVO) {
		//챌린지 참가인원 업데이트
		long chal_num = chalJoinVO.getChal_num();
		isMaxParticipants(chal_num);
		
		//챌린지 참가 정보 저장
		chalJoinVO.setChal_joi_num(challengeMapper.selectChal_joi_num());
		challengeMapper.insertChallengeJoin(chalJoinVO);

		//챌린지 결제 정보 저장
		chalPayVO.setChal_joi_num(chalJoinVO.getChal_joi_num());
		challengeMapper.insertChallengePayment(chalPayVO);
		
		//채팅 기록 로그에 추가
		Map<String,Long> map = new HashMap<>();
		map.put("chal_num", chal_num);
		map.put("mem_num", chalJoinVO.getMem_num());
		map.put("last_chat_id",challengeMapper.selectFirstChat_id(chal_num));
		challengeMapper.insertChatLog(map);

		//결제시 포인트 변동사항 기록 
		if(chalPayVO.getChal_point() > 0) {
			//포인트 로그 작성
			PointVO pointVO = new PointVO(20,-chalPayVO.getChal_point(),chalPayVO.getMem_num());
			pointService.insertPointLog(pointVO);

			//회원 포인트 업데이트
			memberService.updateMemPoint(pointVO);
		}

		//참가한 회원 정보 가져오기
		MemberVO participant = memberMapper.selectMemberDetail(chalJoinVO.getMem_num());
		String memNick = participant.getMem_nick();

		//챌린지 개설자 정보 가져오기
		ChallengeVO challenge = challengeMapper.selectChallenge(chalJoinVO.getChal_num());
		long creatorMemNum = challenge.getMem_num();
		String chalTitle = challenge.getChal_title();

		//개설자에게 알림 보내기
		NotifyVO notifyVO = new NotifyVO();
		notifyVO.setMem_num(creatorMemNum); //알림 받을 개설자 회원 번호
		notifyVO.setNotify_type(4); //알림 타입 (챌린지 참가)
		notifyVO.setNot_url("/challenge/detail?chal_num=" + chalJoinVO.getChal_num()); //알림을 누르면 반환할 URL

		//동적 데이터 매핑
		Map<String, String> dynamicValues = new HashMap<>();
		dynamicValues.put("memNick", memNick);
		dynamicValues.put("chalTitle", chalTitle);

		//알림 로그 찍기
		notifyService.insertNotifyLog(notifyVO, dynamicValues);
	}

	@Override
	public void isMaxParticipants(Long chal_num) {
		//락 설정
		ChallengeVO challenge = challengeMapper.selectChallengeForUpdate(chal_num);
		
		//챌린지 참가인원 업데이트
		if(challenge.getChal_join() < challenge.getChal_max()) {
			challengeMapper.addChallengeJoinNum(chal_num);			
		}else {
			throw new MaxParticipantsExceededException("모집이 마감되었습니다.");
		}
	}

	@Override
	public List<ChallengeJoinVO> selectChallengeJoinList(Map<String,Object> map) {
		return challengeMapper.selectChallengeJoinList(map);
	}

	@Override public ChallengeJoinVO selectChallengeJoin(Long chal_joi_num) {
		return challengeMapper.selectChallengeJoin(chal_joi_num); 
	}


	//챌린지 ID로 챌린지 참가 데이터 삭제
	@Override
	public void deleteChallengeJoinsByChallengeId(Long chal_num) {
		challengeMapper.deleteChallengeJoinsByChallengeId(chal_num);
	}

	//리더 여부 확인
	@Override
	public boolean isChallengeLeader(Long chal_num, Long mem_num) {
		ChallengeVO challenge = challengeMapper.selectChallenge(chal_num);
		return challenge != null && challenge.getMem_num() == mem_num;
	}
	//리더 chal_joi_num 확인하기
	@Override
	public Long selectLeaderJoiNum(Long chal_num) {
		return challengeMapper.selectLeaderJoiNum(chal_num);
	}

	//후기 작성 여부
	@Override
	public ChallengeReviewVO selectChallengeReviewByMemberAndChallenge(Map<String, Object> map) {
		return challengeMapper.selectChallengeReviewByMemberAndChallenge(map);
	}

	//*챌린지 결제*//
	@Override
	public void insertChallengePayment(ChallengePaymentVO chalPayVO) {
		challengeMapper.insertChallengePayment(chalPayVO);
	}

	//동일 챌린지의 모든 결제 내역 불러오기
	@Override
	public List<ChallengePaymentVO> selectChallengePaymentList(Long chal_num) {
		Map<String,Object> map = new HashMap<>();
		map.put("chal_num", chal_num);
		List<ChallengeJoinVO> joinList = challengeMapper.selectJoinMemberList(map);
		List<ChallengePaymentVO> payList = new ArrayList<>();

		for(ChallengeJoinVO joinVO : joinList) {
			payList.add(challengeMapper.selectChallengePayment(joinVO.getChal_joi_num()));
		}
		return payList;
	}

	//*챌린지 인증*//
	@Override
	public void insertChallengeVerify(ChallengeVerifyVO chalVerifyVO) {
		challengeMapper.insertChallengeVerify(chalVerifyVO);
	}

	@Override
	public Integer selectChallengeVerifyListRowCount(Map<String, Object> map) {
		return challengeMapper.selectChallengeVerifyListRowCount(map);
	}

	@Override
	public List<ChallengeVerifyVO> selectChallengeVerifyList(Map<String, Object> map) {
		return challengeMapper.selectChallengeVerifyList(map);
	}

	@Override 
	public ChallengeVerifyVO selectChallengeVerify(Long chal_ver_num) {
		return challengeMapper.selectChallengeVerify(chal_ver_num); 
	}

	@Override
	public void updateChallengeVerify(ChallengeVerifyVO challengeVerify) {
		challengeMapper.updateChallengeVerify(challengeVerify);
	}

	@Override
	public void deleteChallengeVerify(Long chal_ver_num) {
		challengeMapper.deleteChallengeVerify(chal_ver_num);
	}

	//주별 인증 횟수 확인
	@Override
	public int countWeeklyVerify(Long chal_joi_num, LocalDate startDate, int weekNumber) {
		Map<String, Object> params = new HashMap<>();
		params.put("chal_joi_num", chal_joi_num);
		params.put("startDate", startDate.toString());
		params.put("weekNumber", weekNumber);
		return challengeMapper.countWeeklyVerify(params);
	}

	@Override
	public Integer countTodayVerify(Long chal_joi_num) {
		return challengeMapper.countTodayVerify(chal_joi_num);
	}

	@Override
	public List<ChallengeJoinVO> selectJoinMemberList(Map<String,Object> map) {
		return challengeMapper.selectJoinMemberList(map);
	}

	@Override
	public Integer selectJoinMemberRowCount(Map<String, Object> map) {
		return challengeMapper.selectJoinMemberRowCount(map);
	}

	//*챌린지 후기*//
	@Override
	public void insertChallengeReview(ChallengeReviewVO chalReviewVO) {
		chalReviewVO.setChal_rev_num(challengeMapper.selectChal_rev_num());
		challengeMapper.insertChallengeReview(chalReviewVO);

		//포인트 지급
		givePointsForReview(chalReviewVO);

		//1. 챌린지 개설자에게 참가 알림 전송
		//참가자 정보 가져오기
		MemberVO participant = memberMapper.selectMemberDetail(chalReviewVO.getMem_num());

		//챌린지 개설자 정보 가져오기
		ChallengeVO challenge = challengeMapper.selectChallenge(chalReviewVO.getChal_num());
		long creatorMemNum = challenge.getMem_num();
		String chalTitle = challenge.getChal_title();

		//개설자에게 알림 보내기
		NotifyVO notifyToCreator = new NotifyVO();
		notifyToCreator.setMem_num(creatorMemNum); //알림 받을 개설자 회원 번호
		notifyToCreator.setNotify_type(5); //알림 타입 (챌린지 후기 등록)
		notifyToCreator.setNot_url("/challenge/detail?chal_num=" + chalReviewVO.getChal_num()); //알림을 누르면 반환할 URL

		//동적 데이터 매핑
		Map<String, String> dynamicValuesForCreator = new HashMap<>();
		dynamicValuesForCreator.put("chalTitle", chalTitle);

		//알림 로그 찍기
		notifyService.insertNotifyLog(notifyToCreator, dynamicValuesForCreator);

		//2. 챌린지 참가자에게 포인트 알림 전송
		//포인트 적립 알림
		NotifyVO notifyToParticipant = new NotifyVO();
		notifyToParticipant.setMem_num(participant.getMem_num()); //알림 받을 회원 번호
		notifyToParticipant.setNotify_type(22); //알림 타입 (포인트 적립)

		//알림을 누르면 반환할 URL
		notifyToParticipant.setNot_url("/member/myPage"); //예시: 마이페이지로 이동

		//동적 데이터 매핑
		Map<String, String> dynamicValuesForParticipant = new HashMap<>();
		dynamicValuesForParticipant.put("pointAmount", "500"); //포인트 금액
		dynamicValuesForParticipant.put("peventDetail", "챌린지 후기 작성"); //포인트 적립 이벤트 상세 설명

		//알림 로그 찍기
		notifyService.insertNotifyLog(notifyToParticipant, dynamicValuesForParticipant);
	}

	@Override
	public List<ChallengeReviewVO> selectChallengeReviewList(Long chal_num) {
		return challengeMapper.selectChallengeReviewList(chal_num);
	}   

	@Override
	public ChallengeReviewVO selectChallengeReview(Long chal_rev_num) {
		return challengeMapper.selectChallengeReview(chal_rev_num);
	}   

	@Override
	public void updateChallengeReview(ChallengeReviewVO chalReviewVO) {
		challengeMapper.updateChallengeReview(chalReviewVO);
	}

	@Override
	public void deleteChallengeReview(Long chal_rev_num) {
		challengeMapper.deleteChallengeReview(chal_rev_num);
	}

	@Override
	public Integer selectChallengeJoinListRowCount(Map<String, Object> map) {
		return challengeMapper.selectChallengeJoinListRowCount(map);
	}

	//개별 챌린지 취소
	@Override
	public void cancelChallengeJoin(Long chal_joi_num,Long chal_num) {
		ChallengePaymentVO payVO = selectChallengePayment(chal_joi_num);	

		//챌린지 결제 상태 - 취소
		challengeMapper.updateChalPaymentStatus(payVO.getChal_joi_num());
		//챌린지 참가 상태 - 취소
		challengeMapper.updateChallengeJoinStatus(payVO.getChal_joi_num());
		//챌린지 참가인원 변경
		challengeMapper.deleteChallengeJoinNum(chal_num);

		//사용 포인트 복구
		int chal_point = payVO.getChal_point();		
		if(chal_point > 0) {
			//포인트 로그 작성
			PointVO pointVO = new PointVO(30,payVO.getChal_point(),payVO.getMem_num());
			pointService.insertPointLog(pointVO);

			//회원 포인트 업데이트
			memberService.updateMemPoint(pointVO);
		}

		//리더에게 회원의 챌린지 취소 알림
		NotifyVO notifyVO = new NotifyVO();

		//챌린지 정보
		ChallengeVO challengeVO = challengeMapper.selectChallenge(chal_num);

		notifyVO.setMem_num(challengeVO.getMem_num()); //알림 받을 회원 번호
		notifyVO.setNotify_type(35);             	   //알림 타입
		notifyVO.setNot_url("/challenge/detail?chal_num="+chal_num); //알림을 누르면 반환할 url

		Map<String, String> dynamicValues = new HashMap<String, String>();
		MemberVO participant = memberMapper.selectMemberDetail(payVO.getMem_num());
		String memNick = participant.getMem_nick();

		dynamicValues.put("memNick", memNick);
		dynamicValues.put("chalTitle", challengeVO.getChal_title());

		//NotifyService 호출
		notifyService.insertNotifyLog(notifyVO, dynamicValues); //알림 로그 찍기
	}

	//챌린지의 모든 참가 내역 및 결제 취소(리더 취소시)
	@Override
	public void cancelChallenge(Map<String,Object> data) {
		List<ChallengePaymentVO> payList = (List) data.get("payList");
		long chal_num = (Long) data.get("chal_num");

		//모든 결제 내역 취소하기
		for(ChallengePaymentVO payVO : payList) {
			//챌린지 결제 상태 - 취소
			challengeMapper.updateChalPaymentStatus(payVO.getChal_joi_num());
			//챌린지 참가 상태 - 취소
			challengeMapper.updateChallengeJoinStatus(payVO.getChal_joi_num());

			//사용 포인트 복구
			ChallengePaymentVO chalPayVO = challengeMapper.selectChallengePayment(payVO.getChal_joi_num());
			int chal_point = chalPayVO.getChal_point();		
			if(chal_point > 0) {
				//포인트 로그 작성
				PointVO pointVO = new PointVO(30,chalPayVO.getChal_point(),chalPayVO.getMem_num());
				pointService.insertPointLog(pointVO);

				//회원 포인트 업데이트
				memberService.updateMemPoint(pointVO);
			}			

			//챌린지 삭제 알림
			NotifyVO notifyVO = new NotifyVO();
			notifyVO.setMem_num(payVO.getMem_num()); //알림 받을 회원 번호
			notifyVO.setNotify_type(33);             //알림 타입
			notifyVO.setNot_url("/member/myPage/payment"); //알림을 누르면 반환할 url

			Map<String, String> dynamicValues = new HashMap<String, String>();
			ChallengeVO challenge = challengeMapper.selectChallenge(chal_num);
			dynamicValues.put("chalTitle", challenge.getChal_title());

			//NotifyService 호출
			notifyService.insertNotifyLog(notifyVO, dynamicValues); //알림 로그 찍기
		}		

		//챌린지 톡방 환영 메시지 삭제
		challengeMapper.deleteChallengeChat(chal_num);

		//챌린지 좋아요 삭제
		challengeMapper.deleteAllFav(chal_num);

		//챌린지 상태 - 취소
		challengeMapper.updateChallengeStatus(chal_num);
	}

	//후기 작성 시 포인트 지급
	public void givePointsForReview(ChallengeReviewVO review) {
		int pointAmount = 500;//지급할 포인트 양
		long memNum = review.getMem_num();
		long chalNum = review.getChal_num();

		//포인트 로그 추가
		PointVO pointVO = new PointVO(13, pointAmount, memNum); //(이벤트 타입, 포인트 양, 회원 번호)
		pointService.insertPointLog(pointVO);

		//회원 포인트 업데이트
		memberService.updateMemPoint(pointVO);

		log.info("회원 ID {}에게 챌린지 ID {}에 대한 후기 작성으로 {} 포인트를 지급했습니다.", memNum, chalNum, pointAmount);
	}

	//*챌린지 채팅*//
	//챌린지 채팅 메시지 넣기
	@Override
	public void insertChallengeChat(ChallengeChatVO chatVO,Set<Long> memList) {
		//chal_chat 테이블에 레코드 삽입
		long chat_id = challengeMapper.selectChat_id();
		chatVO.setChat_id(chat_id);
		challengeMapper.insertChallengeChat(chatVO);
		//chal_chat_read 테이블 업데이트
		Map<String,Object> map = new HashMap<>();
		map.put("chal_num", chatVO.getChal_num());
		for(long mem_num:memList) {			
			map.put("mem_num", mem_num);
			map.put("last_chat_id", chat_id);
			challengeMapper.updateChatRead(map);
		}		
	}

	//챌린지 채팅 전체 메시지 불러오기
	@Override
	public List<ChallengeChatVO> selectChallengeChat(Map<String, Object> map) {
		log.debug("map = {}",map);
		//최근 읽은 채팅 업데이트 하기
		long latest_chat_id = challengeMapper.selectLatestChat_id((Long) map.get("chal_num"));
		map.put("last_chat_id", latest_chat_id);
		challengeMapper.updateChatRead(map);
		return challengeMapper.selectChallengeChat(map);
	}

	//챌린지 채팅방 삭제
	@Override
	public void deleteChallengeChat(Long chal_num) {
		Map<String,Object> map = new HashMap<>();
		map.put("chal_num", chal_num);
		List<ChallengeChatVO> chatList = challengeMapper.selectChallengeChat(map);
		for(ChallengeChatVO chat : chatList) {
			removeFile(chat.getChat_filename());
		}
		//챌린지 채팅 읽기 기록 삭제
		challengeMapper.deleteChalChatRead(chal_num);
		//챌린지 채팅 전체 삭제
		challengeMapper.deleteChallengeChat(chal_num);		
	}

	//채팅방 사진 삭제
	private void removeFile(String filename) {
		if (filename != null) {
			// 컨텍스트 루트상의 절대 경로 구하기
			String path = servletContext.getRealPath("/upload");
			File file = new File(path, filename);
			if (file.exists() && file.delete()) {
				log.info("파일 삭제 성공: {}", filename);
			} else {
				log.warn("파일 삭제 실패 또는 파일이 존재하지 않음: {}", filename);
			}
		}
	}

	//*챌린지 좋아요*//
	@Override
	public ChallengeFavVO selectFav(ChallengeFavVO fav) {
		return challengeMapper.selectFav(fav);
	}

	@Override
	public Integer selectFavCount(Long chal_num) {
		return challengeMapper.selectFavCount(chal_num);
	}

	@Override
	public void insertFav(ChallengeFavVO fav) {
		challengeMapper.insertFav(fav);
	}

	@Override
	public void deleteFav(ChallengeFavVO fav) {
		challengeMapper.deleteFav(fav);
	}

	@Override
	public void updateVerifyStatus(Map<String,Long> map) {
		challengeMapper.updateVerifyStatus(map);		
	}

	@Override
	public void insertVerifyReport(ChallengeVerifyRptVO chalVerifyRptVO) {
		challengeMapper.insertVerifyReport(chalVerifyRptVO);
		challengeMapper.updateReportStatus(chalVerifyRptVO.getChal_ver_num());
	}

	//*챌린지 스케줄러*//
	@Override
	public void processTodayExpiredChallenges() {
		try {
			LocalDate today = LocalDate.now();
			List<ChallengeVO> todayExpiredChallenges = challengeMapper.getTodayExpiredChallenges(today);

			for (ChallengeVO challenge : todayExpiredChallenges) {
				try {
					processChallenge(challenge);
				} catch (Exception e) {
					// 개별 챌린지 처리 중 예외가 발생한 경우
					log.error("챌린지 ID {} 처리 중 오류 발생: {}", challenge.getChal_num(), e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			// 메서드 전체에서 예외가 발생한 경우
			log.error("오늘 종료된 챌린지 처리 중 오류 발생: {}", e.getMessage(), e);
		}
	}

	private void processChallenge(ChallengeVO challenge) {
		try {
			//각 챌린지에 대한 종료 작업
			long chal_num = challenge.getChal_num();

			//1. 참가상태 업데이트
			challengeMapper.updateChallengeJoinStatusToCompleted(chal_num);

			//2. 참가자에게 환급 포인트 지급
			refundPointsToUsers(chal_num);

			//3. 단체 채팅방 삭제			
			deleteChallengeChat(chal_num);

			//4. 챌린지 종료 알림 전송
			Map<String, Object> params = new HashMap<>();
			params.put("chal_num", chal_num);
			List<ChallengeJoinVO> participants = challengeMapper.selectJoinMemberList(params);

			for (ChallengeJoinVO participant : participants) {
				NotifyVO notifyVO = new NotifyVO();
				notifyVO.setMem_num(participant.getMem_num()); //알림 받을 회원 번호
				notifyVO.setNotify_type(2); //알림 타입 (챌린지 종료)

				//알림을 누르면 반환할 URL (루트 컨텍스트 다음 부분만)
				notifyVO.setNot_url("/challenge/detail?chal_num=" + chal_num);

				//동적 데이터 매핑
				Map<String, String> dynamicValues = new HashMap<>();
				dynamicValues.put("chalTitle", challenge.getChal_title());

				//알림 로그 찍기
				notifyService.insertNotifyLog(notifyVO, dynamicValues);
			}
		} catch (Exception e) {
			log.error("챌린지 ID {} 처리 중 예외 발생: {}", challenge.getChal_num(), e.getMessage(), e);
		}
	}

	//챌린지 종료시 환급 포인트 지급
	@Override
	public void refundPointsToUsers(Long chal_num) {
		Map<String, Object> map = new HashMap<>();
		map.put("chal_num", chal_num);
		List<ChallengeJoinVO> joinList = challengeMapper.selectJoinMemberList(map);

		for (ChallengeJoinVO join : joinList) {
			ChallengeVO challenge = challengeMapper.selectChallenge(join.getChal_num());
			join.setChal_sdate(challenge.getChal_sdate());
			join.setChal_edate(challenge.getChal_edate());
			join.setChal_fee(challenge.getChal_fee().longValue());
			join.setChal_freq(challenge.getChal_freq());

			int returnPoint = calculateReturnPoint(join);

			challengeMapper.insertRefundPoints(join.getMem_num(), 14, returnPoint);//이벤트 타입 (챌린지 환급)

			//환급 포인트 알림 전송
			NotifyVO notifyVO = new NotifyVO();
			notifyVO.setMem_num(join.getMem_num()); //알림 받을 회원 번호
			notifyVO.setNotify_type(22); //알림 타입 (포인트 적립)

			//알림을 누르면 반환할 URL
			notifyVO.setNot_url("/member/myPage"); //예시: 마이페이지로 이동

			//동적 데이터 매핑
			Map<String, String> dynamicValues = new HashMap<>();
			dynamicValues.put("pointAmount", String.valueOf(returnPoint)); //포인트 금액
			dynamicValues.put("peventDetail", "챌린지 환급"); //포인트 적립 이벤트 상세 설명

			// 알림 로그 찍기
			notifyService.insertNotifyLog(notifyVO, dynamicValues);
		}
	}

	private int calculateReturnPoint(ChallengeJoinVO challengeJoin) {
		Long chal_fee = challengeJoin.getChal_fee();
		int achieveRate = calculateAchieveRate(challengeJoin);

		if (achieveRate == 100) {
			return (int) (chal_fee * 0.95);
		} else {
			return (int) (achieveRate / 100.0 * chal_fee);
		}
	}

	private int calculateAchieveRate(ChallengeJoinVO challengeJoin) {
		Long chal_joi_num = challengeJoin.getChal_joi_num();
		int chal_freq = challengeJoin.getChal_freq();

		Map<String, Object> verifyMap = new HashMap<>();
		verifyMap.put("chal_joi_num", chal_joi_num);
		List<ChallengeVerifyVO> verifyList = challengeMapper.selectChallengeVerifyList(verifyMap);

		long successCount = verifyList.stream().filter(v -> v.getChal_ver_status() == 0).count();

		String sdate = challengeJoin.getChal_sdate();
		String edate = challengeJoin.getChal_edate();

		LocalDate startDate = LocalDate.parse(sdate, DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate endDate = LocalDate.parse(edate, DateTimeFormatter.ISO_LOCAL_DATE);
		long totalWeeks = ChronoUnit.WEEKS.between(startDate, endDate) + 1;

		long totalCount = totalWeeks * chal_freq;

		return totalCount > 0 ? (int) ((double) successCount / totalCount * 100) : 0;
	}

	//인증 요청 알림 실행
	/*
	 * @Override public void processTodayVerificationRequest() { Map<String, Object>
	 * paramMap = new HashMap<>(); paramMap.put("status", "ongoing"); // 진행 중인 챌린지
	 * 필터 List<ChallengeVO> ongoingChallenges =
	 * challengeMapper.selectList(paramMap);
	 * 
	 * // 현재 진행 중인 챌린지 로그 log.debug("Ongoing Challenges: {}", ongoingChallenges);
	 * 
	 * for (ChallengeVO challenge : ongoingChallenges) { Map<String, Object>
	 * joinMemberMap = new HashMap<>(); joinMemberMap.put("chal_num",
	 * challenge.getChal_num()); List<ChallengeJoinVO> participants =
	 * challengeMapper.selectJoinMemberList(joinMemberMap); LocalDate currentDate =
	 * LocalDate.now();
	 * 
	 * // 각 챌린지의 참가자 목록 로그 log.debug("Participants for Challenge {}: {}",
	 * challenge.getChal_num(), participants);
	 * 
	 * for (ChallengeJoinVO participant : participants) { LocalDate
	 * challengeStartDate = LocalDate.parse(participant.getChal_sdate()); LocalDate
	 * challengeEndDate = LocalDate.parse(participant.getChal_edate());
	 * 
	 * // 챌린지 시작일과 종료일 로그 log.debug("Challenge Start Date: {}, End Date: {}",
	 * challengeStartDate, challengeEndDate);
	 * 
	 * int chalFreq = challenge.getChal_freq(); // 인증 빈도 List<LocalDate> deadlines =
	 * calculateWeeklyDeadlines(challengeStartDate, challengeEndDate, chalFreq);
	 * 
	 * // 마감일 목록 로그 log.debug("Deadlines: {}", deadlines);
	 * 
	 * for (LocalDate deadline : deadlines) { if
	 * (currentDate.equals(deadline.minusDays(1))) { int completedVerifications =
	 * countWeeklyVerifications(participant.getChal_joi_num(), deadline);
	 * 
	 * // 완료된 인증 횟수 로그 log.debug("Completed Verifications: {} for Participant: {}",
	 * completedVerifications, participant.getChal_joi_num());
	 * 
	 * if (completedVerifications < chalFreq) { // 알림 전송 로그
	 * log.debug("Sending notification for Participant: {} in Challenge: {}",
	 * participant.getMem_num(), challenge.getChal_num());
	 * 
	 * // 알림 전송 NotifyVO notifyVO = new NotifyVO();
	 * notifyVO.setMem_num(participant.getMem_num()); // 알림 받을 참가자 회원 번호
	 * notifyVO.setNotify_type(1); // 알림 타입 (챌린지 인증 마감 알림)
	 * notifyVO.setNot_url("/challenge/detail?chal_num=" + challenge.getChal_num());
	 * // 알림을 누르면 반환할 URL
	 * 
	 * Map<String, String> dynamicValues = new HashMap<>();
	 * dynamicValues.put("chalTitle", challenge.getChal_title());
	 * 
	 * notifyService.insertNotifyLog(notifyVO, dynamicValues); } } } } } }
	 * 
	 * private List<LocalDate> calculateWeeklyDeadlines(LocalDate startDate,
	 * LocalDate endDate, int chalFreq) { List<LocalDate> deadlines = new
	 * ArrayList<>(); LocalDate current = startDate;
	 * 
	 * while (!current.isAfter(endDate)) { for (int i = 0; i < chalFreq; i++) { if
	 * (!current.isAfter(endDate)) { deadlines.add(current); } current =
	 * current.plusDays(1); } current = current.plusDays(7 - chalFreq); // 나머지 일수
	 * 건너뛰기 }
	 * 
	 * return deadlines; }
	 * 
	 * private int countWeeklyVerifications(long chalJoiNum, LocalDate deadline) {
	 * Map<String, Object> params = new HashMap<>(); params.put("chal_joi_num",
	 * chalJoiNum); params.put("week_start", deadline.minusDays(7)); // 해당 주 시작일
	 * params.put("week_end", deadline); // 해당 주 종료일
	 * 
	 * return challengeMapper.countWeeklyVerify(params); }
	 */    

	//*챌린지 관리자*//
	//챌린지 목록
	@Override
	public List<ChallengeVO> selectChallengeList(Map<String, Object> map) {
		return challengeMapper.selectChallengeList(map);
	}

	//챌린지 레코드 수
	@Override
	public int selectChallengeCount(Map<String, Object> map) {
		return challengeMapper.selectChallengeCount(map);
	}

	@Override
	public ChallengePaymentVO selectChallengePayment(Long chal_joi_num) {
		return challengeMapper.selectChallengePayment(chal_joi_num);
	}

	//챌린지 중단
	@Override
	public void cancelChallengeByAdmin(Map<String, Object> map) {
		Long chal_num = (Long) map.get("chal_num");
		int chal_phase = (Integer) map.get("chal_phase");		

		//모든 결제 내역 불러오기
		List<ChallengePaymentVO> payList = (List) map.get("payList");

		//모든 결제 내역 취소하기
		for(ChallengePaymentVO payVO : payList) {
			//챌린지 결제 상태 - 취소
			challengeMapper.updateChalPaymentStatus(payVO.getChal_joi_num());
			//챌린지 참가 상태 - 취소
			challengeMapper.updateChallengeJoinStatus(payVO.getChal_joi_num());

			//사용 포인트 복구
			ChallengePaymentVO chalPayVO = challengeMapper.selectChallengePayment(payVO.getChal_joi_num());
			int chal_point = chalPayVO.getChal_point();		
			if(chal_point > 0) {
				//포인트 로그 작성
				PointVO pointVO = new PointVO(30,chalPayVO.getChal_point(),chalPayVO.getMem_num());
				pointService.insertPointLog(pointVO);

				//회원 포인트 업데이트
				memberService.updateMemPoint(pointVO);
			}			

			//챌린지 삭제 알림
			NotifyVO notifyVO = new NotifyVO();
			notifyVO.setMem_num(payVO.getMem_num()); //알림 받을 회원 번호
			notifyVO.setNotify_type(34);             //알림 타입
			notifyVO.setNot_url("/member/myPage/payment"); //알림을 누르면 반환할 url

			Map<String, String> dynamicValues = new HashMap<String, String>();
			ChallengeVO challenge = challengeMapper.selectChallenge(chal_num);
			dynamicValues.put("chalTitle", challenge.getChal_title());

			//NotifyService 호출
			notifyService.insertNotifyLog(notifyVO, dynamicValues); //알림 로그 찍기
		}

		if(chal_phase == 0) {//시작 전 챌린지 중단
			//챌린지 톡방 환영 메시지 삭제
			challengeMapper.deleteChallengeChat(chal_num);
		}else if(chal_phase == 1){//진행 중인 챌린지 중단
			//챌린지 채팅방 삭제
			deleteChallengeChat(chal_num);

			//챌린지 인증 신고 & 기록 삭제
			Map<String, Object> rptMap = new HashMap<>();
			rptMap.put("chal_num", chal_num);
			List<ChallengeJoinVO> joinList = selectJoinMemberList(rptMap);

			for(ChallengeJoinVO join : joinList) {
				challengeMapper.deleteVerifyReport(join.getMem_num()); 	
				challengeMapper.deleteChallengeVerifyByChalJoiNum(join.getChal_joi_num());
			}			
		}
		//챌린지 좋아요 삭제
		challengeMapper.deleteAllFav(chal_num);

		//챌린지 상태 - 취소
		challengeMapper.updateChallengeStatus(chal_num);
	}	

	//*챌린지 메인*//
	//인기 챌린지
	@Override
	public List<ChallengeVO> getPopularChallenges() {
		return challengeMapper.getPopularChallenges();
	}
	//운동 챌린지
	@Override
	public List<ChallengeVO> getExerciseChallenges() {
		return challengeMapper.getExerciseChallenges();
	}

	@Override
	public Long selectLastChat_id(Long chal_num, Long mem_num) {
		return challengeMapper.selectLastChat_id(chal_num,mem_num);
	}
}