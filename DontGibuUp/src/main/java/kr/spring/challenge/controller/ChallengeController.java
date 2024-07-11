package kr.spring.challenge.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import kr.spring.category.vo.DonationCategoryVO;
import kr.spring.challenge.service.ChallengeService;
import kr.spring.challenge.vo.ChallengeJoinVO;
import kr.spring.challenge.vo.ChallengeVO;
import kr.spring.member.vo.MemberVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    @ModelAttribute("challengeVO")
    public ChallengeVO initChallengeVO() {
        return new ChallengeVO();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    /*==========================
     *  챌린지 개설하기
     *==========================*/
    @GetMapping("/challenge/write")
    public String form() {
        return "challengeWrite";
    }

    @PostMapping("/challenge/write")
    public String checkValidation(@Valid ChallengeVO challengeVO, BindingResult result, 
                                  HttpServletRequest request, HttpSession session, Model model) throws IllegalStateException, IOException {
        log.debug("<<챌린지 개설 정보 확인>> : " + challengeVO);

        // 유효성 체크
        if (result.hasErrors()) {
            return form();
        }

        challengeVO.calculateChalEdate();
        session.setAttribute("challengeVO", challengeVO);

        return "main";
    }

    /*==========================
     *  챌린지 목록
     *==========================*/
    @GetMapping("/challenge/list")
    public String list() {
        return "challengeList";
    }

    /*==========================
     *  챌린지 상세
     *==========================*/
    @GetMapping("/challenge/detail")
    public ModelAndView chalDetail(@RequestParam("chal_num") long chal_num) {
        ChallengeVO challenge = challengeService.selectChallenge(chal_num);
        return new ModelAndView("challengeView", "challenge", challenge);
    }

    /*==========================
     *  챌린지 참가 폼
     *==========================*/
    @GetMapping("/challenge/join")
    public String joinForm(@RequestParam("chal_num") long chal_num, Model model) {
        ChallengeVO challengeVO = challengeService.selectChallenge(chal_num);
        List<DonationCategoryVO> categories = challengeService.selectDonaCategories();

        model.addAttribute("categories", categories);
        model.addAttribute("challengeVO", challengeVO);

        ChallengeJoinVO challengeJoinVO = new ChallengeJoinVO();
        challengeJoinVO.setChal_num(chal_num);  // 챌린지 번호 설정
        model.addAttribute("challengeJoinVO", challengeJoinVO);

        return "challengeJoinForm";
    }

    /*==========================
     *  챌린지 참가 및 결제
     *==========================*/
    @PostMapping("/challenge/join")
    public String join(@Valid @ModelAttribute("challengeJoinVO") ChallengeJoinVO challengeJoinVO, BindingResult result,
                       HttpServletRequest request, HttpSession session, Model model) throws IllegalStateException, IOException {
        //log.debug("<<챌린지 신청 확인>> : " + challengeJoinVO);

        // 유효성 체크
        if (result.hasErrors()) {
            log.debug("<<유효성 검사 실패>> : " + result.getAllErrors());
            return "challengeJoinForm";
        }

        // 회원번호
        MemberVO member = (MemberVO) session.getAttribute("user");
        if (member == null) {
            log.debug("<<로그인되지 않은 사용자>>");
            model.addAttribute("message", "로그인 후 신청 가능합니다.");
            model.addAttribute("url", request.getContextPath() + "/member/login");
            return "common/resultAlert";
        }
        challengeJoinVO.setMem_num(member.getMem_num());
        // ip
        challengeJoinVO.setChal_joi_ip(request.getRemoteAddr());
        
        log.debug("<<챌린지 신청 확인>> : " + challengeJoinVO);
        
        // 챌린지 신청
        challengeService.insertChallengeJoin(challengeJoinVO);

        // view에 메시지 추가
        model.addAttribute("message", "챌린지 신청이 완료되었습니다!");
        model.addAttribute("url", request.getContextPath() + "/challenge/list");

        return "common/resultAlert";
    }

    /*==========================
     *  챌린지 참가 목록
     *==========================*/
    @GetMapping("/challenge/joinList")
    public ModelAndView joinList(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("user");
        Map<String, Object> map = new HashMap<>();
        map.put("mem_num", member.getMem_num());

        List<ChallengeJoinVO> list = challengeService.selectChallengeJoinList(map);

        return new ModelAndView("challengeJoinList", "list", list);
    }

    /*==========================
     *  챌린지 참가 상세
     *==========================*/
    @GetMapping("/challenge/joinDetail")
    public ModelAndView joinDetail(@RequestParam("chal_joi_num") Long chal_joi_num) {
        ChallengeJoinVO challengeJoin = challengeService.selectChallengeJoin(chal_joi_num);
        return new ModelAndView("challengeJoinView", "challengeJoin", challengeJoin);
    }
}