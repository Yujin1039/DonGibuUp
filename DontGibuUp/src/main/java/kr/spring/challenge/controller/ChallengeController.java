package kr.spring.challenge.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import kr.spring.category.service.CategoryService;
import kr.spring.category.vo.ChallengeCategoryVO;
import kr.spring.category.vo.DonationCategoryVO;
import kr.spring.challenge.service.ChallengeService;
import kr.spring.challenge.vo.ChallengeChatVO;
import kr.spring.challenge.vo.ChallengeJoinVO;
import kr.spring.challenge.vo.ChallengeReviewVO;
import kr.spring.challenge.vo.ChallengeVO;
import kr.spring.challenge.vo.ChallengeVerifyVO;
import kr.spring.config.websocket.SocketHandler;
import kr.spring.member.vo.MemberVO;
import kr.spring.util.FileUtil;
import kr.spring.util.PagingUtil;
import kr.spring.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ChallengeController {

	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private CategoryService categoryService;

	@ModelAttribute("challengeVO")
	public ChallengeVO initChallengeVO() {
		return new ChallengeVO();
	}

	@ModelAttribute("challengeJoinVO")
	public ChallengeJoinVO initChallengeJoinVO() {
		return new ChallengeJoinVO();
	}

	@ModelAttribute("challengeReviewVO")
	public ChallengeReviewVO initChallengeReviewVO() {
		return new ChallengeReviewVO();
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}

	/*
	 * ========================== 챌린지 개설 ==========================
	 */
	// 챌린지 개설 폼
	@GetMapping("/challenge/write")
	public String form() {
		return "challengeWrite";
	}

	// 챌린지 개설 (챌린지 개설 유효성 검사 확인 후, 세션에 저장)
	@PostMapping("/challenge/write")
	public String checkValidation(@Valid ChallengeVO challengeVO, BindingResult result, HttpServletRequest request,
			HttpSession session, Model model) throws IllegalStateException, IOException {
		log.debug("<<챌린지 개설 정보 확인>> : " + challengeVO);

		// 유효성 체크
		if (result.hasErrors()) {
			return form();
		}

		// 회원번호
		MemberVO member = (MemberVO) session.getAttribute("user");
		challengeVO.setMem_num(member.getMem_num());
		// ip
		challengeVO.setChal_ip(request.getRemoteAddr());
		// 챌린지 종료일 계산
		challengeVO.calculateChalEdate();
		// 대표 사진 업로드 및 파일 저장
		challengeVO.setChal_photo(FileUtil.createFile(request, challengeVO.getUpload()));
		//챌린지 작성시 줄바꿈 허용,기타 html 비허용
		challengeVO.setChal_title(StringUtil.useNoHTML(challengeVO.getChal_title()));	
		challengeVO.setChal_content(StringUtil.useBrNoHTML(challengeVO.getChal_content()));
		challengeVO.setChal_verify(StringUtil.useBrNoHTML(challengeVO.getChal_verify()));
		
		session.setAttribute("challengeVO", challengeVO);

		return "redirect:/challenge/leaderJoin";
	}

	// 챌린지 개설 목록
	@GetMapping("/challenge/list")
	public String list(Model model) {
		List<ChallengeCategoryVO> categories = categoryService.selectChalCateList();
		model.addAttribute("categories", categories);
		return "challengeList";
	}

	@GetMapping("/challenge/pastList")
	public String pastList(Model model) {
		List<ChallengeCategoryVO> categories = categoryService.selectChalCateList();
		model.addAttribute("categories", categories);
		return "challengePastList";
	}

	// 챌린지 개설 상세
	@GetMapping("/challenge/detail")
	public ModelAndView chalDetail(long chal_num, HttpSession session) {
		ChallengeVO challenge = challengeService.selectChallenge(chal_num);
		MemberVO member = (MemberVO) session.getAttribute("user");

		boolean isJoined = false;
		if (member != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("chal_num", chal_num);
			map.put("mem_num", member.getMem_num());
			List<ChallengeJoinVO> joinList = challengeService.selectChallengeJoinList(map);
			isJoined = joinList.stream().anyMatch(join -> join.getChal_num() == chal_num);
		}

		// 챌린지 리뷰 가져오기
		List<ChallengeReviewVO> reviewList = challengeService.selectChallengeReviewList(chal_num);
		double averageRating = reviewList.stream().mapToInt(ChallengeReviewVO::getChal_rev_grade).average().orElse(0.0);
		averageRating = Math.round(averageRating * 10) / 10.0; // 소수점 첫째 자리까지만 표시
		int reviewCount = reviewList.size();

		ModelAndView mav = new ModelAndView("challengeView");
		mav.addObject("challenge", challenge);
		mav.addObject("isJoined", isJoined);
		mav.addObject("reviewList", reviewList);
		mav.addObject("averageRating", averageRating);
		mav.addObject("reviewCount", reviewCount);

		return mav;
	}

	/*
	 * ========================== 챌린지 참가 ==========================
	 */
	// 챌린지 참가 폼
	@GetMapping("/challenge/join/write")
	public String joinForm(@RequestParam("chal_num") long chal_num, HttpSession session, Model model) {
		log.info("chal_num = {}",chal_num);
		ChallengeVO challengeVO = challengeService.selectChallenge(chal_num);
		List<DonationCategoryVO> categories = categoryService.selectListNoPage();

		model.addAttribute("categories", categories);
		model.addAttribute("challengeVO", challengeVO);

		ChallengeJoinVO challengeJoinVO = new ChallengeJoinVO();
		challengeJoinVO.setChal_num(chal_num);
		model.addAttribute("challengeJoinVO", challengeJoinVO);

		// 회원의 포인트 정보
		MemberVO member = (MemberVO) session.getAttribute("user");
		model.addAttribute("mem_point", member.getMem_point());

		return "challengeJoinWrite";
	}

	// 챌린지 참가 폼 (리더)
	@GetMapping("/challenge/leaderJoin")
	public String joinForm(Model model, HttpSession session) {
		List<DonationCategoryVO> categories = categoryService.selectListNoPage();
		model.addAttribute("categories", categories);

		ChallengeVO vo = (ChallengeVO) session.getAttribute("challengeVO");
		model.addAttribute("challenge", vo);

		// 회원의 포인트 정보
		MemberVO member = (MemberVO) session.getAttribute("user");
		model.addAttribute("mem_point", member.getMem_point());

		return "leaderJoinForm";
	}

	// 챌린지 참가 목록
	@GetMapping("/challenge/join/list")
	public String list(@RequestParam(value = "status", defaultValue = "pre") String status,
			@RequestParam(value = "month", required = false) String month, Model model, HttpSession session,
			@RequestParam(defaultValue = "1") int pageNum) {

		// 현재 날짜를 기반으로 month가 없는 경우 이번 달로 설정
		LocalDate currentMonth = (month == null || month.equals("null"))
				? LocalDate.now().withDayOfMonth(1)
				: LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String currentMonthString = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

		log.debug("<<status>> : " + status);
		log.debug("<<month>>: : " + month);
		log.debug("<<currentMonth>>: : " + currentMonth);
		log.debug("<<currentMonthString>>: : " + currentMonthString);
		
		MemberVO member = (MemberVO) session.getAttribute("user");
		Map<String, Object> map = new HashMap<>();
		map.put("mem_num", member.getMem_num());
		map.put("status", status);
		map.put("currentMonthString", currentMonthString);

		// 페이징 정보
		int count = challengeService.selectChallengeJoinListRowCount(map);
		log.debug("count : " + count);

		PagingUtil page = new PagingUtil(pageNum, count, 3, 10, "list", "&status=" + status + "&month=" + month);

		map.put("start", page.getStartRow());
		map.put("end", page.getEndRow());

		// 이번 달의 챌린지만 필터링
		List<ChallengeJoinVO> list = challengeService.selectChallengeJoinList(map);

		// 각 챌린지에 대한 달성률과 참여금 계산
		List<Map<String, Object>> challengeDataList = list.stream().map(challengeJoin -> {
			Map<String, Object> challengeData = new HashMap<>();
			long chal_joi_num = challengeJoin.getChal_joi_num();
			Map<String, Object> verifyMap = new HashMap<>();
			verifyMap.put("chal_joi_num", chal_joi_num);

			PagingUtil samePage = new PagingUtil(pageNum, count, 3);
			verifyMap.put("start", samePage.getStartRow());
			verifyMap.put("end", samePage.getEndRow());

			List<ChallengeVerifyVO> verifyList = challengeService.selectChallengeVerifyList(verifyMap);
			

			// 인증 성공 횟수
			long successCount = verifyList.stream().filter(v -> v.getChal_ver_status() == 0).count();

			// 전체 주 수 계산
			LocalDate startDate = LocalDate.parse(challengeJoin.getChal_sdate(), DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDate endDate = LocalDate.parse(challengeJoin.getChal_edate(), DateTimeFormatter.ISO_LOCAL_DATE);
			long totalWeeks = ChronoUnit.WEEKS.between(startDate, endDate) + 1;

			// 전체 인증 횟수
			long totalCount = totalWeeks * challengeJoin.getChal_freq();

			// 달성률 계산
			int achieveRate = totalCount > 0 ? (int) ((double) successCount / totalCount * 100) : 0;

			// 참여금 관련 계산
			Long chal_fee = challengeJoin.getChal_fee();
			int returnPoint;
			int donaAmount;

			if (achieveRate == 100) {
				// 달성률이 100%인 경우
				returnPoint = (int) (chal_fee * 0.95);
				donaAmount = (int) (chal_fee * 0.10);
			} else if (achieveRate >= 90 && achieveRate <= 99) {
				// 달성률이 90~99%인 경우
				returnPoint = (int) (chal_fee * 0.90);
				donaAmount = (int) (chal_fee * 0.10);
			} else {
				returnPoint = (int) (achieveRate / 100.0 * chal_fee);
				donaAmount = (int) (chal_fee - returnPoint);
			}

			// 숫자를 포맷팅
			NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

			// 후기 작성 여부 확인
			Map<String, Object> reviewCheckMap = new HashMap<>();
			reviewCheckMap.put("chal_num", challengeJoin.getChal_num());
			reviewCheckMap.put("mem_num", member.getMem_num());
			ChallengeReviewVO review = challengeService.selectChallengeReviewByMemberAndChallenge(reviewCheckMap);
			boolean hasReview = review != null;

			// 챌린지 개설자 여부 확인 및 설정, 단체 챌린지 여부 확인
			ChallengeVO challenge = challengeService.selectChallenge(challengeJoin.getChal_num());
			challengeJoin.setHost(challenge.getMem_num() == member.getMem_num()); // 챌린지 개설자 여부 설정
			challengeJoin.setPub(challenge.getChal_public() == 0 ? true:false);

			// 챌린지 데이터 추가
			challengeData.put("challengeJoin", challengeJoin);		
			challengeData.put("achieveRate", achieveRate);
			challengeData.put("returnPoint", numberFormat.format(returnPoint));
			challengeData.put("donaAmount", numberFormat.format(donaAmount));
			challengeData.put("formattedFee", numberFormat.format(chal_fee));
			challengeData.put("hasReview", hasReview);

			return challengeData;
		}).collect(Collectors.toList());

		model.addAttribute("challengesByMonth", Map.of(currentMonthString, challengeDataList));
		model.addAttribute("status", status);
		model.addAttribute("currentMonth", currentMonthString);
		model.addAttribute("count",count);
		model.addAttribute("page", page.getPage());

		return "challengeJoinList";
	}

	/*
	 * ========================== 챌린지 단체 채팅 ==========================
	 */
	// 챌린지 채팅방 입장
	@GetMapping("/challenge/join/chal_chatDetail")
	public String joinChallengeChatRedirect(HttpSession session, Model model) {
		log.debug("챌린지 채팅방 입장 메서드 실행");
		long chal_num = (Long) session.getAttribute("chal_num");
		model.addAttribute("chal_num", chal_num);

		// 채팅방 이름
		ChallengeVO challenge = challengeService.selectChallenge(chal_num);
		model.addAttribute("chal_room_name", challenge.getChal_title());

		// 채팅 참여 회원 목록
		Map<String, Object> map = new HashMap<>();
		map.put("chal_num", chal_num);
		List<ChallengeJoinVO> list = challengeService.selectJoinMemberList(map);
		model.addAttribute("list", list);

		// 채팅 총 참여 인원 수
		model.addAttribute("count", list.size());
		
		// 마지막 방문 메시지 번호 가져오기
		MemberVO member = (MemberVO) session.getAttribute("user");
		Long lastChat_id = challengeService.selectLastChat_id(chal_num, member.getMem_num());
		model.addAttribute("lastChat_id", lastChat_id);
		
		// 채팅 정보 불러오기
		MemberVO user = (MemberVO) session.getAttribute("user");
		map.put("mem_num", user.getMem_num());
		List<ChallengeChatVO> chatList = challengeService.selectChallengeChat(map);
		model.addAttribute("chatList", chatList);
		log.debug("<<chatList = {}>>",chatList);
		return "chal_chatDetail";
	}

	/*
	 * ========================== 챌린지 인증 ==========================
	 */
	// 챌린지 인증 폼
	@GetMapping("/challenge/verify/write")
	public String verifyForm(@RequestParam("chal_joi_num") long chal_joi_num, Model model) {
		ChallengeVerifyVO challengeVerifyVO = new ChallengeVerifyVO();
		challengeVerifyVO.setChal_joi_num(chal_joi_num);
		model.addAttribute("challengeVerifyVO", challengeVerifyVO);

		ChallengeJoinVO challengeJoin = challengeService.selectChallengeJoin(chal_joi_num);
		if (challengeJoin != null) {
			ChallengeVO challenge = challengeService.selectChallenge(challengeJoin.getChal_num());
			if (challenge != null) {
				model.addAttribute("chal_title", challenge.getChal_title());
				model.addAttribute("chal_verify", challenge.getChal_verify());
				model.addAttribute("chal_num", challenge.getChal_num());
			}
		}

		return "challengeVerifyWrite";
	}

	// 챌린지 인증
	@PostMapping("/challenge/verify/write")
	public String submitVerify(@Valid @ModelAttribute("challengeVerifyVO") ChallengeVerifyVO challengeVerifyVO,
			long chal_num, BindingResult result, HttpServletRequest request, HttpSession session, Model model)
			throws IllegalStateException, IOException {
		log.debug("<<챌린지 인증 등록>> : " + challengeVerifyVO);

		// 인증 사진 유효성 검사
		MultipartFile uploadFile = challengeVerifyVO.getUpload();
		if (uploadFile == null || uploadFile.isEmpty()) {
			result.rejectValue("upload", "error.upload", "인증 사진 필수 입력");
		}

		// 유효성 체크
		if (result.hasErrors()) {
			ChallengeJoinVO challengeJoin = challengeService.selectChallengeJoin(challengeVerifyVO.getChal_joi_num());
			if (challengeJoin != null) {
				ChallengeVO challenge = challengeService.selectChallenge(challengeJoin.getChal_num());
				if (challenge != null) {
					model.addAttribute("chal_title", challenge.getChal_title());
					model.addAttribute("chal_verify", challenge.getChal_verify());
				}
			}
			return "challengeVerifyWrite";
		}

		// 회원 번호 설정
		MemberVO member = (MemberVO) session.getAttribute("user");
		challengeVerifyVO.setMem_num(member.getMem_num());

		// 인증 사진 업로드 및 파일 저장
		String filename = FileUtil.createFile(request, uploadFile);
		challengeVerifyVO.setChal_ver_photo(filename);

		// 등록 날짜 설정
		challengeVerifyVO.setChal_reg_date(new Date(System.currentTimeMillis()));

		// 챌린지 인증 등록
		challengeService.insertChallengeVerify(challengeVerifyVO);

		// view에 메시지 추가
		model.addAttribute("message", "챌린지 인증이 완료되었습니다!");
		model.addAttribute("url", request.getContextPath() + "/challenge/verify/list?chal_joi_num="
				+ challengeVerifyVO.getChal_joi_num() + "&chal_num=" + chal_num + "&status=on");

		return "common/resultAlert";
	}
	
	// 챌린지 인증 목록
	@GetMapping("/challenge/verify/list")
	public ModelAndView verifyList(long chal_joi_num, long chal_num,HttpSession session,
			@RequestParam(value = "status", defaultValue = "pre") String status,
			@RequestParam(defaultValue = "1") int pageNum) {
		//접근 권한 확인하기
		ChallengeJoinVO challengeJoin = challengeService.selectChallengeJoin(chal_joi_num);
		MemberVO user = (MemberVO) session.getAttribute("user");
		Long mem_joi_num = challengeService.selectLeaderJoiNum(chal_num);
		if(challengeJoin == null || challengeJoin.getMem_num() != user.getMem_num() || mem_joi_num == null) {
			ModelAndView error = new ModelAndView("common/resultAlert");
			error.addObject("message", "접근 권한이 없습니다.");
			error.addObject("url", "../join/list?status=on");
			return error;
		}
				
		Map<String, Object> map = new HashMap<>();
		map.put("chal_joi_num", chal_joi_num);

		// 본인의 총 인증 개수 불러오기
		int count = challengeService.selectChallengeVerifyListRowCount(map);

		PagingUtil page = new PagingUtil(pageNum, count, 6, 10, "list",
				"&chal_num=" + chal_num + "&chal_joi_num=" + chal_joi_num + "&status=on");

		List<ChallengeVerifyVO> verifyList = challengeService.selectChallengeVerifyList(map);

		ModelAndView mav = new ModelAndView("challengeVerifyList");
		mav.addObject("verifyList", verifyList);
		mav.addObject("chal_num", chal_num);
		mav.addObject("chal_joi_num", chal_joi_num);
		mav.addObject("status", status);
		mav.addObject("page", page.getPage());

		// 오늘 날짜 추가
		LocalDate today = LocalDate.now();
		mav.addObject("today", today.toString());

		// 챌린지 정보 가져오기
		ChallengeVO challenge = challengeService.selectChallenge(challengeJoin.getChal_num());
		int chalFreq = challengeJoin.getChal_freq();
		String chal_sdate = challengeJoin.getChal_sdate();
		String chal_edate = challengeJoin.getChal_edate();

		mav.addObject("challenge", challenge);
		mav.addObject("chalFreq", chalFreq);
		mav.addObject("chal_sdate", chal_sdate);
		mav.addObject("chal_edate", chal_edate);

		// 전체 주 수 계산
		LocalDate startDate = LocalDate.parse(chal_sdate, DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate endDate = LocalDate.parse(chal_edate, DateTimeFormatter.ISO_LOCAL_DATE);
		long totalWeeks = ChronoUnit.WEEKS.between(startDate, endDate) + 1;

		int totalFailedVerifications = 0;

		for (int weekNumber = 0; weekNumber < totalWeeks; weekNumber++) {
			int weeklyVerifications = challengeService.countWeeklyVerify(chal_joi_num, startDate, weekNumber);
			int failedInWeek = chalFreq - weeklyVerifications;

			// 주차 종료일이 현재보다 과거인 경우에만 실패 횟수를 누적
			LocalDate weekEndDate = startDate.plusDays((weekNumber + 1) * 7 - 1);
			if (weekEndDate.isBefore(today) || weekEndDate.equals(today)) {
				totalFailedVerifications += Math.max(failedInWeek, 0);
			}
			//이번 주 인증 완료 여부 확인
			LocalDate start = startDate.plusDays(weekNumber * 7);
			LocalDate end = startDate.plusDays((weekNumber+1) * 7);
			if(today.isAfter(start) && today.isBefore(end) ||today.isEqual(start) && today.isBefore(end)) {
				if(weeklyVerifications == chalFreq) {
					mav.addObject("hasCompletedWeeklyVerify", true);
				}
			}
		}

		// 인증 성공 횟수
		long successCount = verifyList.stream().filter(v -> v.getChal_ver_status() == 0).count();
		mav.addObject("successCount", successCount);

		// chal_ver_status가 1인 경우 실패 횟수로 간주
		long statusFailureCount = verifyList.stream().filter(v -> v.getChal_ver_status() == 1).count();
		mav.addObject("failureCount", statusFailureCount);

		// 총 실패 횟수 = 자동 실패 횟수 + 수동 인증 실패 횟수
		totalFailedVerifications += statusFailureCount;

		// 남은 인증 횟수
		long totalCount = totalWeeks * chalFreq;
		long remainingCount = totalCount - successCount - totalFailedVerifications;
		mav.addObject("remainingCount", Math.max(remainingCount, 0));

		// 달성률 계산 (정수로 변환)
		int achievementRate = (int) ((double) successCount / totalCount * 100);
		mav.addObject("achievementRate", achievementRate);
		
		//오늘의 인증 완료 여부 확인
		int todayVerify = challengeService.countTodayVerify(chal_joi_num);
		if(todayVerify > 0) {
			mav.addObject("hasTodayVerify", true);
		}		
		
		//페이징 처리를 위한 인증 개수 처리
		mav.addObject("count", count);

		// 회원이 챌린지 리더인지 확인			
		if (mem_joi_num == chal_joi_num) {
			mav.addObject("isLeader", true);
		} else {
			mav.addObject("isLeader", false);
		}

		return mav;
	}

	// 챌린지 인증 수정
	@PostMapping("/challenge/verify/update")
	@ResponseBody
	public ResponseEntity<String> updateVerify(@RequestParam("chal_ver_num") long chal_ver_num,
			@RequestParam("chal_content") String chal_content) {
		try {
			ChallengeVerifyVO challengeVerify = new ChallengeVerifyVO();
			challengeVerify.setChal_ver_num(chal_ver_num);
			challengeVerify.setChal_content(chal_content);
			challengeService.updateChallengeVerify(challengeVerify);
			return new ResponseEntity<>("인증 내용이 성공적으로 수정되었습니다.", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("인증 내용 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 챌린지 인증 삭제
	@PostMapping("/challenge/verify/delete")
	public ResponseEntity<String> deleteVerify(@RequestParam("chal_ver_num") long chal_ver_num) {
		try {
			challengeService.deleteChallengeVerify(chal_ver_num);
			return new ResponseEntity<>("인증이 삭제되었습니다.", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("인증 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * ========================== 챌린지 후기 ==========================
	 */
	// 챌린지 후기 작성 폼
	@GetMapping("/challenge/review/write")
	public String reviewForm(@RequestParam("chal_num") long chal_num, Model model) {
		ChallengeVO challenge = challengeService.selectChallenge(chal_num);
		model.addAttribute("chal_num", chal_num);
		model.addAttribute("challenge", challenge);
		return "challengeReviewWrite";
	}

	// 챌린지 후기 작성
	@PostMapping("/challenge/review/write")
	public String writeReview(@Valid @ModelAttribute("challengeReviewVO") ChallengeReviewVO challengeReviewVO,
			BindingResult result, HttpServletRequest request, HttpSession session, Model model) {
		if (result.hasErrors()) {
			return "challengeReviewWrite";
		}

		MemberVO member = (MemberVO) session.getAttribute("user");
		challengeReviewVO.setMem_num(member.getMem_num());
		challengeReviewVO.setChal_rev_ip(request.getRemoteAddr());

		challengeService.insertChallengeReview(challengeReviewVO);

		model.addAttribute("message", "후기가 등록되었습니다!");
		model.addAttribute("url",
				request.getContextPath() + "/challenge/detail?chal_num=" + challengeReviewVO.getChal_num());

		return "common/resultAlert";
	}

	// 챌린지 후기 목록
	@GetMapping("/challenge/review/list")
	public String reviewList(@RequestParam("chal_num") long chal_num,
			@RequestParam(value = "sortType", defaultValue = "latest") String sortType, Model model) {
		ChallengeVO challenge = challengeService.selectChallenge(chal_num);
		List<ChallengeReviewVO> reviewList = challengeService.selectChallengeReviewList(chal_num);

		double averageRating = reviewList.stream().mapToInt(ChallengeReviewVO::getChal_rev_grade).average().orElse(0.0);
		// 소수점 첫째 자리까지만 표시
		averageRating = Math.round(averageRating * 10) / 10.0;
		int reviewCount = reviewList.size();

		model.addAttribute("challenge", challenge);
		model.addAttribute("reviewList", reviewList);
		model.addAttribute("averageRating", averageRating);
		model.addAttribute("reviewCount", reviewCount);

		return "challengeReviewList";
	}

	/*
	 * ========================== 챌린지 스케줄러 ==========================
	 */
	// 환급 포인트 지급
	@GetMapping("/challenge/refundPoints")
	public ResponseEntity<String> refundPointsToUsers(@RequestParam("chal_num") Long chal_num) {
		try {
			challengeService.refundPointsToUsers(chal_num);
			return new ResponseEntity<>("포인트 환급이 완료되었습니다.", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("포인트 환급 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * ========================== 챌린지 관리자 ==========================
	 */
	// 챌린지 목록
	@GetMapping("/admin/adminChallenge")
	public String adminChallengeList(@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(defaultValue = "1") int order, String keyfield, String keyword, Model model) {

		log.debug("<<챌린지 목록 - order>> : " + order);

		Map<String, Object> map = new HashMap<String, Object>();
		if (keyword != null && keyword.equals("")) {
			keyword = null;
		}

		map.put("keyfield", keyfield);
		map.put("keyword", keyword);

		// 디버깅: 검색 필드와 키워드 출력
		log.debug("키필드: " + keyfield);
		log.debug("키워드: " + keyword);

		// 전체, 검색 레코드 수
		int count = challengeService.selectChallengeCount(map);

		// 페이지 처리
		PagingUtil page = new PagingUtil(keyfield, keyword, pageNum, count, 20, 10, "adminChallenge",
				"&order=" + order);

		List<ChallengeVO> list = null;
		if (count > 0) {
			map.put("order", order);
			map.put("start", page.getStartRow());
			map.put("end", page.getEndRow());

			list = challengeService.selectChallengeList(map);

			// 디버깅: 쿼리 후 결과 리스트 출력
			log.debug("검색된 챌린지 수: " + list.size());
			for (ChallengeVO challenge : list) {
				LocalDate sdate = LocalDate.parse(challenge.getChal_sdate());
				LocalDate edate = LocalDate.parse(challenge.getChal_edate());				
				LocalDate today = LocalDate.now();
				if(today.isAfter(edate)) {
					challenge.setChal_phase(2);
				}else if(today.isBefore(sdate)) {
					challenge.setChal_phase(0);
				}else {
					challenge.setChal_phase(1);
				}
				log.debug(challenge.toString());
			}
		}

		model.addAttribute("count", count);
		model.addAttribute("list", list);
		model.addAttribute("page", page.getPage());

		return "adminChallenge";
	}

	// 챌린지 상세
	@GetMapping("/admin/adminChallengeDetail")
	public ModelAndView adminChallengeDetail(@RequestParam("chal_num") long chal_num, HttpSession session) {
		ChallengeVO challenge = challengeService.selectChallenge(chal_num);

		Map<String, Object> map = new HashMap<>();
		map.put("chal_num", chal_num);

		// 참여금을 포맷팅
		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
		String formattedFee = numberFormat.format(challenge.getChal_fee());

		ModelAndView mav = new ModelAndView("adminChallengeDetail");
		mav.addObject("challenge", challenge);
		mav.addObject("formattedFee", formattedFee);

		return mav;
	}
}