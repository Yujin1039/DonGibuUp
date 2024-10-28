package kr.spring.challenge.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import kr.spring.challenge.vo.ChallengeChatVO;
import kr.spring.challenge.vo.ChallengeFavVO;
import kr.spring.challenge.vo.ChallengeJoinVO;
import kr.spring.challenge.vo.ChallengePaymentVO;
import kr.spring.challenge.vo.ChallengeReviewVO;
import kr.spring.challenge.vo.ChallengeVO;
import kr.spring.challenge.vo.ChallengeVerifyRptVO;
import kr.spring.challenge.vo.ChallengeVerifyVO;

@Mapper
public interface ChallengeMapper {
	
	//*챌린지 개설*//
	@Select("SELECT challenge_seq.nextval FROM dual")
	public Long selectChal_num(); 
	public void insertChallenge(ChallengeVO chalVO);
	public List<ChallengeVO> selectList(Map<String,Object> map);
	public Integer selectRowCount(Map<String,Object> map);
	public ChallengeVO selectChallenge(Long chal_num);
	@Update("UPDATE challenge SET chal_status=1 WHERE chal_num=#{chal_num}")
	public void updateChallengeStatus(Long chal_num);
	public void deleteChalPhoto(Long chal_num);
	
	//*챌린지 참가*//
	@Select("SELECT chal_join_seq.nextval FROM dual")
	public Long selectChal_joi_num();
    public void insertChallengeJoin(ChallengeJoinVO chalJoinVO);
    //참여 락 설정
    @Select("SELECT chal_join FROM challenge WHERE chal_num=#{chal_num} FOR UPDATE")
    public ChallengeVO selectChallengeForUpdate(long chal_num);
    //참가인원 추가
    @Update("UPDATE challenge SET chal_join=chal_join+1 WHERE chal_num=#{chal_num}")
    public void addChallengeJoinNum(Long chal_num);
    //참가인원 감소
    @Update("UPDATE challenge SET chal_join=chal_join-1 WHERE chal_num=#{chal_num}")
    public void deleteChallengeJoinNum(Long chal_num);
    public Integer selectChallengeJoinListRowCount(Map<String,Object> map);
    public List<ChallengeJoinVO> selectChallengeJoinList(Map<String,Object> map);
    public ChallengeJoinVO selectChallengeJoin(Long chal_joi_num);
    //챌린지 참가 회원 목록
    public Integer selectJoinMemberRowCount(Map<String,Object> map);
    public List<ChallengeJoinVO> selectJoinMemberList(Map<String,Object> map);   
    //챌린지 리더 참가 정보 불러오기
    @Select("SELECT MIN(chal_joi_num) FROM chal_join WHERE chal_num=#{chal_num}")
    public Long selectLeaderJoiNum(Long chal_num);
    //챌린지 ID로 챌린지 참가 데이터 삭제
    public void deleteChallengeJoinsByChallengeId(Long chal_num);
    //후기 작성 여부
    @Select("SELECT * FROM CHAL_REVIEW WHERE chal_num = #{chal_num} AND mem_num = #{mem_num}")
    public ChallengeReviewVO selectChallengeReviewByMemberAndChallenge(Map<String, Object> map);
    
    //*챌린지 결제*//
    //결제 데이터 등록
    public void insertChallengePayment(ChallengePaymentVO chalPayVO);
    //단일 결제 정보 불러오기
    @Select("SELECT * FROM chal_payment WHERE chal_joi_num=#{chal_joi_num}")
    public ChallengePaymentVO selectChallengePayment(Long chal_joi_num);
    //단건 결제 취소 상태로 변경
    @Update("UPDATE chal_payment SET chal_pay_status=2 WHERE chal_joi_num=#{chal_joi_num}")
    public void updateChalPaymentStatus(Long chal_joi_num);
    //단건 챌린지 참가 상태 변경
    @Delete("UPDATE chal_join SET chal_joi_status=2 WHERE chal_joi_num = #{chal_joi_num}")
    public void updateChallengeJoinStatus(Long chal_joi_num);
    
    //*챌린지 인증*//
    public void insertChallengeVerify(ChallengeVerifyVO chalVerifyVO);
    @Select("SELECT COUNT(*) FROM chal_verify WHERE chal_joi_num=#{chal_joi_num}")
    public Integer selectChallengeVerifyListRowCount(Map<String,Object> map);
    public List<ChallengeVerifyVO> selectChallengeVerifyList(Map<String, Object> map);
    public List<ChallengeVerifyVO> selectChallengeVerifyListPage(Map<String, Object> map);
    public ChallengeVerifyVO selectChallengeVerify(Long chal_ver_num);
    public void updateChallengeVerify(ChallengeVerifyVO challengeVerify);
    public void deleteChallengeVerify(Long chal_ver_num);
    @Delete("DELETE FROM chal_verify WHERE chal_joi_num=#{chal_joi_num}")
    public void deleteChallengeVerifyByChalJoiNum(Long chal_joi_num);
    //주별 인증 횟수 확인
    public int countWeeklyVerify(Map<String, Object> params);
    //오늘의 인증 개수 확인 
    @Select("SELECT COUNT(*) FROM chal_verify WHERE chal_joi_num=#{chal_joi_num} AND TRUNC(chal_reg_date) = TRUNC(SYSDATE)")
    public Integer countTodayVerify(Long chal_joi_num);
    //챌린지 인증 상태 변경
    @Update("UPDATE chal_verify SET chal_ver_status=#{chal_ver_status} WHERE chal_ver_num=#{chal_ver_num}")
    public void updateVerifyStatus(Map<String,Long> map);
    
    //*챌린지 인증 제보*//
    //챌린지 인증 제보
    @Select("INSERT INTO chal_verify_rpt (report_mem_num,chal_ver_num,reported_joi_num) VALUES (#{report_mem_num},#{chal_ver_num},#{reported_joi_num})")
    public void insertVerifyReport(ChallengeVerifyRptVO chalVerifyRptVO);
    //챌린지 인증 제보상태 변경
    @Update("UPDATE chal_verify SET chal_ver_report=1 WHERE chal_ver_num=#{chal_ver_num}")
    public void updateReportStatus(Long chal_ver_num);
    //각 챌린지의 인증별 제보된 개수
    @Select("SELECT COUNT(*) FROM chal_verify_rpt WHERE chal_ver_num=#{chal_ver_num}")
    public Integer selectReportedVerifyCount(Long chal_ver_num);
    //각 챌린지의 회원별 제보된 총 인증 개수
    @Select("SELECT COUNT(DISTINCT chal_ver_num) FROM chal_verify_rpt WHERE reported_joi_num=#{reported_joi_num}")
    public Integer selectReportedMemberCount(Long chal_joi_num);
    @Delete("DELETE FROM chal_verify_rpt WHERE report_mem_num=#{report_mem_num}")
    public void deleteVerifyReport(Long report_mem_num);
    
    //*챌린지 후기*//
    @Select("SELECT chal_review_seq.nextval FROM dual")
    public Long selectChal_rev_num();
    public void insertChallengeReview(ChallengeReviewVO chalReviewVO);
    public List<ChallengeReviewVO> selectChallengeReviewList(Long chal_num);       
    public ChallengeReviewVO selectChallengeReview(Long chal_rev_num);   
    public void updateChallengeReview(ChallengeReviewVO chalReviewVO);
    public void deleteChallengeReview(Long chal_rev_num);	
    
    //*챌린지 채팅*//
    //채팅 메시지 번호 생성
    @Select("SELECT chal_chat_seq.nextval FROM dual")
    public Long selectChat_id();
    //채팅 메시지 등록
    public void insertChallengeChat(ChallengeChatVO chalChatVO);
    //읽지 않은 채팅 기록 저장
    @Insert("INSERT INTO chal_chat_read (chal_num,chat_id,mem_num) VALUES (#{chal_num},#{chat_id},#{mem_num})")
    public void insertChatRead(Map<String,Object> map);
    //채팅 메시지 읽기
    public List<ChallengeChatVO> selectChallengeChat(Map<String,Object> map);
    //읽은 채팅 기록 삭제
    @Delete("DELETE FROM chal_chat_read WHERE chal_num=#{chal_num} AND mem_num=#{mem_num}")
    public void deleteChatRead(Map<String,Object> map);
    //챌린지 종료시 채팅기록(chal_chat_read) 삭제
    @Delete("DELETE FROM chal_chat_read WHERE chal_num=#{chal_num}")
    public void deleteChalChatRead(Long chal_num);
    //챌린지 종료시 채팅(chal_chat) 삭제
    @Delete("DELETE FROM chal_chat WHERE chal_num=#{chal_num}")
    public void deleteChallengeChat(Long chal_num);
    
    //*챌린지 좋아요*//
    @Select("SELECT * FROM chal_fav WHERE chal_num=#{chal_num} AND mem_num=#{mem_num}")
    public ChallengeFavVO selectFav(ChallengeFavVO fav);
    @Select("SELECT COUNT(*) FROM chal_fav WHERE chal_num=#{chal_num}")
    public Integer selectFavCount(Long chal_num);
    @Insert("INSERT INTO chal_fav (chal_num, mem_num) VALUES (#{chal_num}, #{mem_num})")
    public void insertFav(ChallengeFavVO fav);
    @Delete("DELETE FROM chal_fav WHERE chal_num=#{chal_num} AND mem_num=#{mem_num}")
    public void deleteFav(ChallengeFavVO fav);
    //챌린지에 해당하는 모든 좋아요 삭제
    @Delete("DELETE FROM chal_Fav WHERE chal_num=#{chal_num}")
    public void deleteAllFav(Long chal_num);
    
    //*챌린지 스케줄러*//
    //오늘 종료된 챌린지 조회
    @Select("SELECT * FROM challenge WHERE chal_edate = #{today}")
    public List<ChallengeVO> getTodayExpiredChallenges(LocalDate today);
    //참가상태 업데이트
    @Update("UPDATE CHAL_JOIN SET chal_joi_status = 1 WHERE chal_num = #{chal_num}")
    public void updateChallengeJoinStatusToCompleted(@Param("chal_num") long chal_num);
    //챌린지 종료시 환급 포인트 지급
    @Insert("INSERT INTO point_log (point_num, mem_num, pevent_type, point_amount, point_date) VALUES (point_log_seq.NEXTVAL, #{mem_num}, #{pevent_type}, #{pevent_amount}, SYSDATE)")
    public void insertRefundPoints(@Param("mem_num") Long mem_num, @Param("pevent_type") int pevent_type, @Param("pevent_amount") int pevent_amount);

    //*챌린지 관리자*//
    //챌린지 목록
    public List<ChallengeVO> selectChallengeList(Map<String, Object> map);
    //챌린지 레코드 수
    public int selectChallengeCount(Map<String, Object> map);
    
    //*챌린지 메인*//
    //인기 챌린지
    public List<ChallengeVO> getPopularChallenges();
    //운동 챌린지
    public List<ChallengeVO> getExerciseChallenges();
    
    //회원탈퇴
    //챌린지 좋아요 삭제
    @Delete("DELETE FROM chal_fav WHERE mem_num=#{mem_num}")
    public void deleteChalFavsByMember(long mem_num);
}