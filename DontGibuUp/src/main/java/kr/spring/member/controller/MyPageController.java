package kr.spring.member.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import kr.spring.config.validation.ValidationSequence;
import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MyPageController {
	@Autowired
	MemberService memberService;

	//자바빈 초기화
	@ModelAttribute
	public MemberVO initCommand() {
		return new MemberVO();
	}

	@GetMapping("/member/myPage")
	public String myPage() {
		return "redirect:/member/myPage/memberInfo";
	}

	//회원정보 수정 폼
	@GetMapping("/member/myPage/memberInfo")
	public String memberInfo(HttpSession session, Model model) {
		MemberVO user = (MemberVO) session.getAttribute("user");

		MemberVO memberVO = memberService.selectMemberDetail(user.getMem_num());

		if (memberVO.getMem_phone() != null) {
			model.addAttribute("phone2", memberVO.getMem_phone().substring(3, 7));
			model.addAttribute("phone3", memberVO.getMem_phone().substring(7, 11));
			log.debug("<<phone>> : " + memberVO.getMem_phone().substring(3, 7) + memberVO.getMem_phone().substring(7, 11));
		}

		if (memberVO.getMem_birth() != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate parsedDate = LocalDate.parse(memberVO.getMem_birth(), formatter);

			model.addAttribute("birth_year", parsedDate.getYear());
			model.addAttribute("birth_month", parsedDate.getMonthValue());
			model.addAttribute("birth_day", parsedDate.getDayOfMonth());
		}

		model.addAttribute("memberVO", memberVO);
		return "memberInfo";
	}

	//회원정보 수정
	@PostMapping("/member/myPage/updateMember")
	public String updateMemberInfo(@Validated(ValidationSequence.class) MemberVO memberVO, BindingResult result,
			HttpSession session, Model model) {

		if (result.hasFieldErrors("mem_nick")) {
			return "memberInfo";
		}

		if (memberVO.getMem_phone().equals("010")) {
			memberVO.setMem_phone(null);
		} else if (!memberVO.getMem_phone().matches("\\d{11}")) {
			result.rejectValue("mem_phone", "Pattern.mem_phone");
			return "memberInfo";
		}

		log.debug("<<회원정보 수정>> : " + memberVO);

		MemberVO user = (MemberVO) session.getAttribute("user");

		memberVO.setMem_num(user.getMem_num());
		memberVO.setMem_email(user.getMem_email());

		model.addAttribute("memberVO", memberVO);

		//회원정보 수정
		memberService.updateMember(memberVO);
		
		// 세션에 저장된 user 정보 업데이트
		user.setMem_nick(memberVO.getMem_nick());
		// 세션에 업데이트된 user 객체 저장
		session.setAttribute("user", user);
		
		//리디렉트할 모델 value 설정
		if (memberVO.getMem_phone() != null) {
			model.addAttribute("phone2", memberVO.getMem_phone().substring(3, 7));
			model.addAttribute("phone3", memberVO.getMem_phone().substring(7, 11));
			log.debug("<<phone>> : " + memberVO.getMem_phone().substring(3, 7) + memberVO.getMem_phone().substring(7, 11));
		}

		if (memberVO.getMem_birth() != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate parsedDate = LocalDate.parse(memberVO.getMem_birth(), formatter);

			model.addAttribute("birth_year", parsedDate.getYear());
			model.addAttribute("birth_month", parsedDate.getMonthValue());
			model.addAttribute("birth_day", parsedDate.getDayOfMonth());
		}
		
		return "memberInfo";
	}

	//비밀번호 수정 폼
	@GetMapping("/member/myPage/changePassword")
	public String changePasswordForm() {
		return "memberChangePassword";
	}
}
