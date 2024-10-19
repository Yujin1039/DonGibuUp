package kr.spring.challenge.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import kr.spring.category.service.CategoryService;
import kr.spring.challenge.service.ChallengeService;
import kr.spring.category.vo.ChallengeCategoryVO;
import kr.spring.challenge.vo.ChallengeChatVO;
import kr.spring.challenge.vo.ChallengeFavVO;
import kr.spring.challenge.vo.ChallengeJoinVO;
import kr.spring.challenge.vo.ChallengePaymentVO;
import kr.spring.challenge.vo.ChallengeVO;
import kr.spring.challenge.vo.ChallengeVerifyRptVO;
import kr.spring.challenge.vo.ChallengeVerifyVO;
import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import kr.spring.point.vo.PointVO;
import kr.spring.util.FileUtil;
import kr.spring.util.PagingUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ChallengeAjaxController {
	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private MemberService memberService;

	/*==========================
	 *  챌린지 목록
	 *==========================*/
	@GetMapping("/challenge/addlist")
	@ResponseBody
	public Map<String,Object> getList(@RequestParam(defaultValue="1") int pageNum,
			@RequestParam(defaultValue="1") int rowCount,
			@RequestParam(defaultValue="0") int order,
			String chal_type,
			@RequestParam(defaultValue="") String freqOrder,
			@RequestParam(defaultValue="") String keyword,
			String chal_sdate,
			String chal_edate,
			HttpSession session) {
		log.debug("order : " + order);
		Map<String,Object> map = new HashMap<>();
		//map에 검색할 자기계발 카테고리 넣기
		List<ChallengeCategoryVO> categories = categoryService.selectChalCateList();
		map.put("categories", categories);

		map.put("chal_type", chal_type);
		map.put("freqOrder", freqOrder);
		map.put("keyword", keyword);
		map.put("order", order);
		map.put("chal_sdate", chal_sdate);
		map.put("chal_edate", chal_edate);

		//총 챌린지 개수
		int count = challengeService.selectRowCount(map);

		//페이지 처리
		PagingUtil page = new PagingUtil(pageNum, count, rowCount);
		map.put("start", page.getStartRow());
		map.put("end", page.getEndRow());

		log.debug("keyword : " + keyword);
		log.debug("order : " + order);
		log.debug("start : " + page.getStartRow());
		log.debug("end : " + page.getEndRow());

		List<ChallengeVO> list = null;
		if (count > 0) {
			list = challengeService.selectList(map);
		} else {
			list = Collections.emptyList();
		}

		MemberVO member = (MemberVO) session.getAttribute("user");
		Map<String, Object> mapJson = new HashMap<>();

		mapJson.put("freqOrder", freqOrder);
		mapJson.put("keyword", keyword);
		mapJson.put("order", order);
		mapJson.put("count", count);
		mapJson.put("list", list);

		return mapJson;
	}

	/*==========================
	 *  챌린지 참가
	 *==========================*/
	//챌린지 참가 회원 목록
	@GetMapping("/challenge/verify/joinMemberList")
	@ResponseBody
	public Map<String,Object> joinMemberList(@RequestParam(defaultValue="1") int pageNum,
			long chal_num,long chal_joi_num,int rowCount){
		Map<String,Object> map = new HashMap<>();
		map.put("chal_num", chal_num);
		map.put("chal_joi_num", chal_joi_num);

		//총 챌린지 참가 멤버수 불러오기
		int memberCount = challengeService.selectJoinMemberRowCount(map);

		//페이지 처리
		PagingUtil page = new PagingUtil(pageNum,memberCount,rowCount);

		int start = page.getStartRow();
		int end = page.getEndRow();
		map.put("start", start);
		map.put("end", end);

		log.debug("<<start>> : "+start);
		log.debug("<<end>> : "+end);

		List<ChallengeJoinVO> joinList = challengeService.selectJoinMemberList(map);

		Map<String,Object> mapJson = new HashMap<>();
		mapJson.put("list", joinList);
		mapJson.put("count", memberCount);				

		return mapJson;
	}

	/*==========================
	 *  챌린지 인증
	 *==========================*/
	//챌린지 인증 현황 불러오기
	@GetMapping("/challenge/verify/verifyMemberList")
	@ResponseBody
	public Map<String,Object> verifyMemberList(@RequestParam(defaultValue="1") int pageNum,
			long chal_joi_num,long chal_num,int rowCount,HttpSession session){
		Map<String,Object> map = new HashMap<>();
		map.put("chal_joi_num", chal_joi_num);				

		//총 챌린지 인증 개수
		int count = challengeService.selectChallengeVerifyListRowCount(map);

		//페이지 처리
		PagingUtil page = new PagingUtil(pageNum,count,rowCount);

		int start = page.getStartRow();
		int end = page.getEndRow();
		map.put("start", start);
		map.put("end", end);

		List<ChallengeVerifyVO> verifyList = challengeService.selectChallengeVerifyList(map);

		Map<String,Object> mapJson = new HashMap<>();

		//챌린지 종료 날짜 정보
		ChallengeVO challenge = challengeService.selectChallenge(chal_num);
		mapJson.put("chal_edate", challenge.getChal_edate());

		//회원의 참가 번호가 로그인한 사람의 참가 번호와 같은지 확인		
		ChallengeJoinVO challengeJoin = challengeService.selectChallengeJoin(chal_joi_num); 
		MemberVO user = (MemberVO) session.getAttribute("user"); 
		long mem_num = user.getMem_num();		
		if(challengeJoin.getMem_num() == mem_num) { 
			mapJson.put("isUser", true);
		}else { 
			mapJson.put("isUser", false);
			//회원 프로필 사진,닉네임
			MemberVO userInfo = memberService.selectMemberDetail(challengeJoin.getMem_num());
			mapJson.put("member", userInfo);
		}

		mapJson.put("list", verifyList);
		mapJson.put("count", count);

		return mapJson;
	}

	//회원의 챌린지 인증 제보
	@PostMapping("challenge/verify/reportVerify")
	@ResponseBody
	public Map<String,Object> reportChallengeVerify(@RequestBody Map<String, Long> requestData,HttpSession session){
		Long chal_ver_num = requestData.get("chal_ver_num");
		Long reported_joi_num = requestData.get("reported_joi_num");
		MemberVO user = (MemberVO) session.getAttribute("user");

		Map<String,Object> mapJson = new HashMap<>();

		if(user == null) {
			mapJson.put("result", "logout");
		}else {
			long report_mem_num = user.getMem_num();
			ChallengeVerifyRptVO verifyRptVO = new ChallengeVerifyRptVO();

			verifyRptVO.setChal_ver_num(chal_ver_num);
			verifyRptVO.setReported_joi_num(reported_joi_num);
			verifyRptVO.setReport_mem_num(report_mem_num);

			//챌린지 제보에 데이터 insert
			challengeService.insertVerifyReport(verifyRptVO);

			mapJson.put("result", "success");
		}

		return mapJson;
	}

	//리더의 챌린지 인증 취소 조치
	@PostMapping("/challenge/verify/cancelVerify")
	@ResponseBody
	public Map<String,Object> cancelChallengeVerify(@RequestBody Map<String, Long> requestData,HttpSession session){
		Long chal_ver_num = requestData.get("chal_ver_num");
		Long chal_joi_num = requestData.get("chal_joi_num");
		Long chal_num = requestData.get("chal_num");
		log.debug("chal_ver_num >> "+chal_ver_num);
		log.debug("chal_joi_num >> "+chal_joi_num);
		Map<String,Object> mapJson = new HashMap<>();
		MemberVO user = (MemberVO) session.getAttribute("user");
		long leader_joi_num = challengeService.selectLeaderJoiNum(chal_num);


		if(user == null) {
			mapJson.put("result", "logout");
		}else if(leader_joi_num != chal_joi_num) {
			mapJson.put("result", "wrongAccess");
		}else {
			Map<String,Long> map = new HashMap<>();
			map.put("chal_ver_num", chal_ver_num);
			map.put("chal_ver_status", (long) 1);
			challengeService.updateVerifyStatus(map);
			mapJson.put("result", "success");
		}
		return mapJson;
	}

	//리더의 챌린지 인증 취소 조치 복구
	@PostMapping("/challenge/verify/recoverVerify")
	@ResponseBody
	public Map<String,Object> recoverChallengeVerify(@RequestBody Map<String, Long> requestData,HttpSession session){
		Long chal_ver_num = requestData.get("chal_ver_num");
		Long chal_joi_num = requestData.get("chal_joi_num");
		Long chal_num = requestData.get("chal_num");
		log.debug("chal_ver_num >> "+chal_ver_num);
		log.debug("chal_joi_num >> "+chal_joi_num);
		Map<String,Object> mapJson = new HashMap<>();
		MemberVO user = (MemberVO) session.getAttribute("user");
		long leader_joi_num = challengeService.selectLeaderJoiNum(chal_num);

		if(user == null) {
			mapJson.put("result", "logout");
		}else if(leader_joi_num != chal_joi_num) {
			mapJson.put("result", "wrongAccess");
		}else {
			Map<String,Long> map = new HashMap<>();
			map.put("chal_ver_num", chal_ver_num);
			map.put("chal_ver_status", (long) 0);
			challengeService.updateVerifyStatus(map);
			mapJson.put("result", "success");
		}
		return mapJson;
	}

	/*==========================
	 *  챌린지 결제
	 *==========================*/
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

	// 결제 정보 검증하기
	@PostMapping("/challenge/paymentVerifyWrite/{imp_uid}")
	@ResponseBody
	public IamportResponse<Payment> validateIamportWrite(@PathVariable String imp_uid, 
			long chal_num,HttpSession session)
			throws IamportResponseException, IOException {
		log.debug("chal_num >> "+chal_num);
		
		IamportResponse<Payment> payment = impClient.paymentByImpUid(imp_uid);

		//로그인 여부 확인하기
		MemberVO member = (MemberVO) session.getAttribute("user");
		
		//챌린지 참여 금액
		int chalFee = challengeService.selectChallenge(chal_num).getChal_fee();
		
		//실 결제 금액
		int paidAmount = payment.getResponse().getAmount().intValue();
		//사용된 포인트
		String usedPointsJSON = payment.getResponse().getCustomData();
		int usedPoints = 0;
		
		JSONObject usedPointsObject = new JSONObject(usedPointsJSON);
		usedPoints = usedPointsObject.getInt("usedPoints");
		
		if(chalFee - usedPoints != paidAmount || member == null) {
			//결제 취소 요청하기
			CancelData cancelData = new CancelData(imp_uid, true);
			impClient.cancelPaymentByImpUid(cancelData);
		}
		
		log.debug("payment: " + payment);

		return payment;
	}

	//챌린지 참가 및 결제 정보 저장
	@PostMapping("/challenge/payAndEnrollWrite")
	@ResponseBody
	public Map<String, String> saveChallengeInfoWrite(@RequestBody Map<String, Object> data, HttpSession session, HttpServletRequest request)
			throws IllegalStateException, IOException {
		int chalPayPrice = (Integer) data.get("chal_pay_price");
		log.debug("chalPayPrice : " + chalPayPrice);
		
		int chalPoint = (Integer) data.get("chal_point");
		log.debug("chalPoint : " + chalPoint);
		
		int dcateNum = (Integer) data.get("dcate_num");
		log.debug("dcateNum : " + dcateNum);
		
		long chalNum = Long.parseLong( (String) data.get("chal_num"));		
		log.debug("chalNum : " + chalNum);

		Map<String, String> mapJson = new HashMap<>();

		//세션 데이터 가져오기
		MemberVO member = (MemberVO) session.getAttribute("user");

		if (member == null) {
			mapJson.put("result", "logout");
		} else {
			//챌린지 참가 정보 저장
			ChallengeJoinVO challengeJoinVO = new ChallengeJoinVO();
			challengeJoinVO.setMem_num(member.getMem_num());
			challengeJoinVO.setDcate_num(dcateNum);
			challengeJoinVO.setChal_num(chalNum);
			challengeJoinVO.setChal_joi_ip(request.getRemoteAddr());

			//챌린지 결제 정보 저장
			ChallengePaymentVO challengePaymentVO = new ChallengePaymentVO();
			challengePaymentVO.setMem_num(member.getMem_num());
			challengePaymentVO.setChal_pay_price(chalPayPrice);
			challengePaymentVO.setChal_point(chalPoint);
			if(chalPayPrice > 0) {
				String odImpUid = (String) data.get("od_imp_uid");
				challengePaymentVO.setOd_imp_uid(odImpUid);
				log.debug("odImpUid : "+odImpUid);
			}		

			try {
				challengeService.insertChallengeJoin(challengeJoinVO, challengePaymentVO);
				
				//포인트 사용시 반영
				if(chalPoint > 0) {
					//세션 포인트 업데이트
					member.setMem_point(member.getMem_point()-chalPoint);
				}
				
				mapJson.put("result", "success");
			} catch (Exception e) {
				log.error("챌린지 참가 및 결제 정보 저장 중 오류 발생", e);
				mapJson.put("result", "error");
				mapJson.put("message", "챌린지 참가 및 결제 정보 저장 중 오류가 발생했습니다. 관리자에게 문의하세요.");
			}
		}

		return mapJson;
	}

	//결제 정보 검증 (리더)
	@PostMapping("/challenge/paymentVerify/{imp_uid}")
	@ResponseBody
	public IamportResponse<Payment> validateIamport(@PathVariable String imp_uid,HttpSession session,
			HttpServletRequest request) throws IamportResponseException, IOException{        
		IamportResponse<Payment> payment = impClient.paymentByImpUid(imp_uid);

		//로그인 여부 확인하기
		MemberVO member = (MemberVO) session.getAttribute("user");

		//세션에 저장된 결제 금액 가져오기
		ChallengeVO challengeVO = (ChallengeVO) session.getAttribute("challengeVO");
		int expectedAmount = challengeVO.getChal_fee();

		//실 결제 금액
		int paidAmount = payment.getResponse().getAmount().intValue();
		//사용 포인트
		String usedPointsJSON = payment.getResponse().getCustomData();
		int usedPoints = 0;
		
		JSONObject usedPointsObject = new JSONObject(usedPointsJSON);
		usedPoints = usedPointsObject.getInt("usedPoints");
		
		log.debug("usedPoints >> "+usedPoints);

		//예정 결제 금액과 실 결제 금액 비교하기
		if(expectedAmount-usedPoints != paidAmount || member == null) {
			//결제 취소 요청하기
			CancelData cancelData = new CancelData(imp_uid, true);
			impClient.cancelPaymentByImpUid(cancelData);
		}
		log.debug("payment"+payment);
		return payment;
	}

	//챌린지 개설, 참가, 리더 결제 정보 저장
	@PostMapping("/challenge/payAndEnroll")
	@ResponseBody
	public Map<String,String> saveChallengeInfo(@RequestBody Map<String, Object> data, 
			HttpSession session, HttpServletRequest request) throws IllegalStateException, IOException{
		int chalPayPrice = (Integer) data.get("chal_pay_price");					
		int chalPoint = (Integer) data.get("chal_point");
		int dcateNum = (Integer) data.get("dcate_num");	
		
		log.debug("chalPayPrice : "+chalPayPrice);
		log.debug("chalPoint : "+chalPoint);
		log.debug("dcateNum : "+dcateNum);

		Map<String,String> mapJson = new HashMap<>();

		//세션 데이터 가져오기
		MemberVO member = (MemberVO) session.getAttribute("user");
		ChallengeVO challenge = (ChallengeVO) session.getAttribute("challengeVO");

		if(member == null) {
			mapJson.put("result", "logout");
		}else {			
			//챌린지 참가 정보 저장
			ChallengeJoinVO challengeJoinVO = new ChallengeJoinVO();
			challengeJoinVO.setMem_num(member.getMem_num());
			challengeJoinVO.setDcate_num(dcateNum);
			challengeJoinVO.setChal_joi_ip(request.getRemoteAddr());

			//챌린지 결제 정보 저장
			ChallengePaymentVO challengePaymentVO = new ChallengePaymentVO();
			challengePaymentVO.setMem_num(member.getMem_num());
			challengePaymentVO.setChal_pay_price(chalPayPrice);
			challengePaymentVO.setChal_point(chalPoint);
			if(chalPayPrice > 0) {
				String odImpUid = (String) data.get("od_imp_uid");
				challengePaymentVO.setOd_imp_uid(odImpUid);
				log.debug("odImpUid : "+odImpUid);
			}
			
			//챌린지 시작 채팅 메시지 설정
			ChallengeChatVO chatVO = new ChallengeChatVO();
			chatVO.setChat_content("챌린지가 시작되었습니다! @{common}");
			chatVO.setMem_num(member.getMem_num());
			
			challengeService.insertChallenge(challenge,challengeJoinVO,challengePaymentVO,chatVO);

			//포인트 사용시 반영
			if(chalPoint > 0) {
				//세션 포인트 업데이트
				member.setMem_point(member.getMem_point()-chalPoint);
			}
			
			String sdate = challenge.getChal_sdate();

			session.removeAttribute("challengeVO");

			mapJson.put("result", "success");
			mapJson.put("sdate", sdate);
		}

		return mapJson;
	}

	//챌린지 참가 창 벗어날시 이미지 및 개설 데이터 삭제(리더)
	@PostMapping("/challenge/deleteImage")
	public void deleteImg(HttpSession session, HttpServletRequest request) {		
		//세션에 저장된 파일 이름 가져오기
		ChallengeVO challenge = (ChallengeVO) session.getAttribute("challengeVO");		
		//파일 삭제
		FileUtil.removeFile(request, challenge.getChal_photo());

		//챌린지 개설 정보 삭제
		session.removeAttribute("challengeVO");
	}

	/*==========================
	 *  챌린지 단체 채팅
	 *==========================*/
	//chal_num 데이터 세션에 저장
	@PostMapping("/challenge/join/joinChal_chat")
	@ResponseBody
	public Map<String,Object> joinChallengeChat(Long chal_num,HttpSession session) {	
		Map<String,Object> mapJson = new HashMap<>();
		MemberVO user = (MemberVO) session.getAttribute("user");
		if(user == null) {
			mapJson.put("result","logout");
		}else {
			mapJson.put("result", "success");
			session.setAttribute("chal_num", chal_num);
		}			
		return mapJson;
	}

	//채팅 작성하기
	@PostMapping("/challenge/join/chalWriteChat")
	@ResponseBody
	public Map<String,String> writeChallengeChat(long chal_num,String chat_content,
			@RequestParam("upload") MultipartFile upload,HttpSession session,HttpServletRequest request) throws IllegalStateException, IOException{
		log.debug("chalWriteChat 메서드 진입");
		Map<String,String> mapJson = new HashMap<>();

		MemberVO user = (MemberVO) session.getAttribute("user");
		if(user == null) {
			mapJson.put("result", "logout");
		}else {
			ChallengeChatVO chatVO = new ChallengeChatVO();
			chatVO.setChal_num(chal_num);
			chatVO.setChat_content(chat_content);
			chatVO.setChat_filename(FileUtil.createFile(request, upload));
			chatVO.setMem_num(user.getMem_num());

			challengeService.insertChallengeChat(chatVO);

			mapJson.put("result", "success");
		}
		return mapJson;
	}

	//채팅 메시지 읽어오기
	@PostMapping("/challenge/join/chalReadChat")
	@ResponseBody
	public Map<String,Object> readChallengeChat(long chal_num,HttpSession session){
		Map<String,Object> mapJson = new HashMap<>();

		MemberVO user = (MemberVO) session.getAttribute("user");

		if(user == null) {
			mapJson.put("result", "logout");
		}else {
			Map<String,Object> map = new HashMap<>();
			map.put("chal_num", chal_num);
			map.put("mem_num", user.getMem_num());
			List<ChallengeChatVO> chatList = challengeService.selectChallengeChat(map);

			mapJson.put("result", "success");
			mapJson.put("chatList", chatList);
			mapJson.put("mem_num", user.getMem_num());
		}

		return mapJson;
	}

	/*==========================
	 *  챌린지 좋아요
	 *==========================*/
	//챌린지 좋아요 읽기
	@GetMapping("/challenge/getFav")
	@ResponseBody
	public Map<String,Object> getFav(ChallengeFavVO fav, HttpSession session){
		log.debug("<<챌린지 좋아요 - ChallengeFavVO>> : " + fav);

		Map<String,Object> mapJson = new HashMap<>();

		MemberVO user = (MemberVO)session.getAttribute("user");
		if(user == null) {
			mapJson.put("status", "noFav");
		}else {
			// 로그인된 회원번호 셋팅
			fav.setMem_num(user.getMem_num());
			ChallengeFavVO challengeFav = challengeService.selectFav(fav);

			if(challengeFav != null) {
				mapJson.put("status", "yesFav");
			}else {
				mapJson.put("status", "noFav");
			}
		}
		mapJson.put("count", challengeService.selectFavCount(fav.getChal_num()));

		return mapJson;
	}

	//챌린지 좋아요 등록/삭제
	@PostMapping("/challenge/writeFav")
	@ResponseBody
	public Map<String,Object> writeFav(ChallengeFavVO fav, HttpSession session){
		log.debug("<<챌린지 좋아요 - 등록/삭제>> : " + fav);

		Map<String,Object> mapJson = new HashMap<>();

		MemberVO user = (MemberVO)session.getAttribute("user");
		if(user == null) {
			mapJson.put("result", "logout");
		}else {
			// 로그인된 회원번호 셋팅
			fav.setMem_num(user.getMem_num());
			ChallengeFavVO challengeFav = challengeService.selectFav(fav);
			if(challengeFav != null) {
				// 등록 -> 삭제
				challengeService.deleteFav(fav);
				mapJson.put("status", "noFav");
			}else {
				// 등록
				challengeService.insertFav(fav);
				mapJson.put("status", "yesFav");
			}
			mapJson.put("result", "success");
			mapJson.put("count", challengeService.selectFavCount(fav.getChal_num()));
		}

		return mapJson;
	}
	
	//챌린지 참가 취소(챌린지 삭제)
	@PostMapping("/challenge/join/delete")
	public ResponseEntity<String> deleteChallengeJoin(@RequestBody Map<String,Object> requestData, HttpSession session) {
		try {
			long chal_joi_num = ((Integer) requestData.get("chal_joi_num")).longValue();
			boolean isLeader = Boolean.parseBoolean(requestData.get("isLeader").toString());	
			
			MemberVO member = (MemberVO) session.getAttribute("user");
			ChallengeJoinVO challengeJoin = challengeService.selectChallengeJoin(chal_joi_num);

			//참가 정보가 없거나 회원 정보가 일치하지 않는 경우 처리
			if (challengeJoin == null || challengeJoin.getMem_num() != member.getMem_num()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("챌린지 참가 정보가 없거나 권한이 없습니다.");
			}
			
			long chal_num = challengeJoin.getChal_num();
			
			if(isLeader) {
				//토스페이먼츠사 다건 결제 취소(외부 api)
				//모든 결제 내역 불러오기
				List<ChallengePaymentVO> payList = challengeService.selectChallengePaymentList(chal_num);

				//모든 결제 내역 취소하기
				for(ChallengePaymentVO payVO : payList) {
					CancelData cancelData = new CancelData(payVO.getOd_imp_uid(), true);
					impClient.cancelPaymentByImpUid(cancelData);
				}
				Map<String,Object> data = new HashMap<>();
				data.put("chal_num", chal_num);
				data.put("payList", payList);
								
				//결제,참가 상태 및 포인트 변경,챌린지 톡방 환영 메시지 삭제,챌린지 취소
				challengeService.cancelChallenge(data);				
			}else {
				//토스페이먼츠사 단건 결제 취소(외부 api)
				ChallengePaymentVO payVO = challengeService.selectChallengePayment(chal_joi_num);	
				String od_imp_uid = payVO.getOd_imp_uid();
				
				CancelData cancelData = new CancelData(od_imp_uid, true);
				impClient.cancelPaymentByImpUid(cancelData);
				
				//결제,참가 상태 및 포인트 변경
				challengeService.cancelChallengeJoin(chal_joi_num,chal_num);
			}	
			//세션에 포인트 반영
			ChallengePaymentVO payVO = challengeService.selectChallengePayment(chal_joi_num);
			int chal_point = payVO.getChal_point();				
			member.setMem_point(member.getMem_point()+chal_point);
			
			return ResponseEntity.ok("챌린지가 취소되었습니다.");
		} catch (Exception e) {
			log.error("챌린지 취소 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("챌린지 취소 중 오류가 발생했습니다.");
		}
	}
	
	//관리자의 챌린지 중단
	@PostMapping("/admin/cancelChallengeByAdmin")
	@ResponseBody
	public Map<String,String> cancelChallengeByAdmin(@RequestBody Map<String,Object> data, HttpSession session) throws IamportResponseException, IOException {		
		Map<String,String> mapJson = new HashMap<>();
		
		MemberVO user = (MemberVO) session.getAttribute("user");
		
		if(user == null) {
			mapJson.put("result", "logout");
		}else if(user.getMem_status() != 9) {
			mapJson.put("result", "noAuthority");
		}else {
			//토스페이먼츠사 다건 결제 취소(외부 api)
			//모든 결제 내역 불러오기
			long chal_num = (Long) data.get("chal_num");
			List<ChallengePaymentVO> payList = challengeService.selectChallengePaymentList(chal_num);

			//모든 결제 내역 취소하기
			for(ChallengePaymentVO payVO : payList) {
				CancelData cancelData = new CancelData(payVO.getOd_imp_uid(), true);
				impClient.cancelPaymentByImpUid(cancelData);
			}
			data.put("payList", payList);
			challengeService.cancelChallengeByAdmin(data);
			mapJson.put("result", "success");
		}		
		return mapJson;
	}
}