package kr.spring.main.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.spring.category.service.CategoryService;
import kr.spring.category.vo.DonationCategoryVO;
import kr.spring.challenge.service.ChallengeService;
import kr.spring.challenge.vo.ChallengeVO;
import kr.spring.data.service.DataService;
import kr.spring.data.vo.TotalVO;
import kr.spring.dbox.service.DboxService;
import kr.spring.dbox.vo.DboxVO;
import kr.spring.goods.service.GoodsService;
import kr.spring.goods.vo.GoodsVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MainController {
	@Autowired
	DataService dataService;
	@Autowired
	private ChallengeService challengeService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private DboxService dboxService;
	@Autowired
	private CategoryService categoryService;
	@GetMapping("/")
	public String init() {
		return "redirect:/main/main";
	}
	
	//데이터, 인기챌린지, 운동챌린지
	@GetMapping("/main/main")
	public String main(Model model) {
		TotalVO totalVO = dataService.selectTotalMain();
		
		List<ChallengeVO> popularChallenges = challengeService.getPopularChallenges();
		List<ChallengeVO> exerciseChallenges = challengeService.getExerciseChallenges();
		List<DonationCategoryVO> categorylist = categoryService.selectList();
		
		model.addAttribute("categorylist",categorylist);
		model.addAttribute("totalVO", totalVO);
		model.addAttribute("popularChallenges", popularChallenges);
		model.addAttribute("exerciseChallenges", exerciseChallenges);
	    
		 // 최신 상품 하나만 가져오기
        GoodsVO todayGoods = goodsService.todayGoods();
        model.addAttribute("todayGoods", todayGoods);
        
        //기부박스 최근목록 5개
        List<DboxVO> dboxList = dboxService.mainDboxList();
        model.addAttribute("dboxList", dboxList);
        
        return "main"; // Tiles의 설정명
   } 
	
	@GetMapping("/main/admin")
	public String adminMain() {
		return "admin";//Tiles의 설정명
	}
	
	@GetMapping("/main/main2")
	public String main2() {
		
		return "main2";//Tiles의 설정명
	}
}








