package kr.spring.challenge.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.siot.IamportRestClient.exception.IamportResponseException;

import kr.spring.challenge.vo.ChallengeChatVO;
import kr.spring.challenge.vo.ChallengeFavVO;
import kr.spring.challenge.vo.ChallengeJoinVO;
import kr.spring.challenge.vo.ChallengePaymentVO;
import kr.spring.challenge.vo.ChallengeReviewVO;
import kr.spring.challenge.vo.ChallengeVO;
import kr.spring.challenge.vo.ChallengeVerifyRptVO;
import kr.spring.challenge.vo.ChallengeVerifyVO;

public interface ChallengeService {
	
	//*챌린지 개설*//
	public void insertChallenge(ChallengeVO chalVO,ChallengeJoinVO joinVO,ChallengePaymentVO payVO,ChallengeChatVO chatVO);
	public List<ChallengeVO> selectList(Map<String,Object> map);
	public Integer selectRowCount(Map<String,Object> map);
	public ChallengeVO selectChallenge(Long chal_num);
	public void updateChallenge(Long chal_num);
	//동일 챌린지의 모든 결제,참가 취소 및 챌린지 삭제
    public void cancelChallenge(Long chal_num) throws IamportResponseException, IOException;
	public void deleteChalPhoto(Long chal_num);
	//참가 인원수 조회
	public int countCurrentParticipants(long chal_num);
	
	//*챌린지 참가*//
	public void insertChallengeJoin(ChallengeJoinVO chalJoinVO, ChallengePaymentVO chalPayVO);
	public Integer selectChallengeJoinListRowCount(Map<String,Object> map);
	public List<ChallengeJoinVO> selectChallengeJoinList(Map<String,Object> map);
	public ChallengeJoinVO selectChallengeJoin(Long chal_joi_num);
	public Integer selectJoinMemberRowCount(Map<String,Object> map);
	public List<ChallengeJoinVO> selectJoinMemberList(Map<String,Object> map);
	public void deleteChallengeJoinsByChallengeId(Long chal_num);
	//단건 참가 취소
	public void cancelChallengeJoin(Long chal_joi_num,Long chal_num) throws IamportResponseException, IOException;    
    //리더 여부 확인
	public boolean isChallengeLeader(Long chal_num, Long mem_num);
	//리더 chal_joi_num 확인하기
	public Long selectLeaderJoiNum(Long chal_num);
	
	//후기 작성 여부
	public ChallengeReviewVO selectChallengeReviewByMemberAndChallenge(Map<String, Object> map);
	
	//*챌린지 결제*//
    public void insertChallengePayment(ChallengePaymentVO chalPayVO);
    //단건 결제 내역 불러오기
    public ChallengePaymentVO selectChallengePayment(Long chal_joi_num);
    //동일 챌린지의 모든 결제 내역 불러오기
    public List<ChallengePaymentVO> selectChallengePaymentList(Long chal_num);
    
	//*챌린지 인증*//
    public void insertChallengeVerify(ChallengeVerifyVO chalVerifyVO);
    public Integer selectChallengeVerifyListRowCount(Map<String,Object> map);
    public List<ChallengeVerifyVO> selectChallengeVerifyList(Map<String, Object> map);    
    public ChallengeVerifyVO selectChallengeVerify(Long chal_ver_num);
    public void updateChallengeVerify(ChallengeVerifyVO challengeVerify);
    public void deleteChallengeVerify(Long chal_ver_num);
    public int countWeeklyVerify(Long chal_joi_num, LocalDate startDate, int weekNumber);//Integer?
    public Integer countTodayVerify(Long chal_joi_num);
    //리더의 챌린지 인증 취소
    public void updateVerifyStatus(Map<String,Long> map);
    //회원의 챌린지 인증 제보
    public void insertVerifyReport(ChallengeVerifyRptVO chalVerifyRptVO);
    
	//*챌린지 후기*//
    public void insertChallengeReview(ChallengeReviewVO chalReviewVO);
    public List<ChallengeReviewVO> selectChallengeReviewList(Long chal_num);     
    public ChallengeReviewVO selectChallengeReview(Long chal_rev_num);
    public void updateChallengeReview(ChallengeReviewVO chalReviewVO);
    public void deleteChallengeReview(Long chal_rev_num);

    //*챌린지 채팅*//
    //채팅 메시지 등록
    public void insertChallengeChat(ChallengeChatVO chalChatVO);
    //채팅 메시지 읽기
    public List<ChallengeChatVO> selectChallengeChat(Map<String,Object> map);
    //챌린지 종료시 채팅기록 삭제
    public void deleteChallengeChat(Long chal_num);
	
    //*챌린지 좋아요*//
    public ChallengeFavVO selectFav(ChallengeFavVO fav);
    public Integer selectFavCount(Long chal_num);
    public void insertFav(ChallengeFavVO fav);
    public void deleteFav(ChallengeFavVO fav);
    
    //*챌린지 스케줄러*//
    public void processTodayExpiredChallenges();
    //챌린지 종료시 환급 포인트 지급
    public void refundPointsToUsers(Long chal_num);
    //챌린지 인증 요청 알림 실행
    //public void processTodayVerificationRequest();
    
    //*챌린지 관리자*//
    //챌린지 목록
    public List<ChallengeVO> selectChallengeList(Map<String, Object> map);
    //챌린지 레코드 수
    public int selectChallengeCount(Map<String, Object> map);
    //챌린지 중단
    public void cancelChallengeByAdmin(Map<String, Long> map) throws IamportResponseException, IOException;

    //*챌린지 메인*//
    //인기 챌린지
    public List<ChallengeVO> getPopularChallenges();
    //운동 챌린지
    public List<ChallengeVO> getExerciseChallenges();
}