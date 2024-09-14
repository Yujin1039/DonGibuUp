package kr.spring.goods.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import kr.spring.cart.vo.CartVO;
import kr.spring.goods.service.PurchaseService;
import kr.spring.goods.vo.PurchaseVO;
import kr.spring.member.vo.MemberVO;
import kr.spring.notify.service.NotifyService;
import kr.spring.notify.vo.NotifyVO;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/goods")
public class PurchaseController {

    private IamportClient impClient;

    @Value("${iamport.apiKey}")
    private String apiKey;

    @Value("${iamport.secretKey}")
    private String secretKey;
    
    @Autowired
	private NotifyService notifyService;
    @Autowired
    private PurchaseService purchaseService;
    
    @PostConstruct
    public void initImp() {
    	log.debug("API Key: " + apiKey);
        log.debug("Secret Key: " + secretKey);
        this.impClient = new IamportClient(apiKey, secretKey);
    }
    

    @PostMapping("/purchase")
    public String purchasePage(@RequestParam("imp_uid") String impUid, Model model) {
        model.addAttribute("imp_uid", impUid);
        return "goods/purchase";
    }

    @GetMapping("/purchase")
    public String getPurchasePage() {
        return "goods/purchase";
    }
    
    @GetMapping("/goodsList")
    public String goodsList() {
        return "goods/goodsList";
    }

    @GetMapping("/home")
    public String home() {
        return "main/main";
    }
    
    @PostMapping("/paymentVerify/{imp_uid}")
    @ResponseBody
    public IamportResponse<Payment> validateIamport(@PathVariable String imp_uid, HttpSession session) throws IamportResponseException, IOException {
        log.debug("결제 검증 요청: imp_uid = " + imp_uid);
        MemberVO member = (MemberVO) session.getAttribute("user");
        IamportResponse<Payment> payment;
        try {
            payment = impClient.paymentByImpUid(imp_uid);
            if (payment == null || payment.getResponse() == null) {
                log.error("결제 정보 조회 실패: imp_uid = " + imp_uid);
                throw new IamportResponseException("결제 정보 조회 실패", null);
            }
        } catch (IamportResponseException | IOException e) {
            log.error("결제 검증 중 예외 발생: imp_uid = " + imp_uid, e);
            throw e;
        }
        // 실 결제 금액 가져오기
        long paidAmount = payment.getResponse().getAmount().longValue();

        log.debug("결제 금액: " + paidAmount);
        log.debug("payment: " + payment);

        return payment;
    }

    @PostMapping("/purchaseComplete")
    @ResponseBody
    public Map<String, String> savePurchaseInfo(@RequestBody Map<String, Object> data, HttpSession session, HttpServletRequest request)
            throws IllegalStateException, IOException {
        Map<String, String> mapJson = new HashMap<>();
        // 세션 데이터 가져오기
        MemberVO member = (MemberVO) session.getAttribute("user");

        try {
            Long purchase_num = (Long) data.get("purchase_num");
            String impUid = (String) data.get("imp_uid");
            String merchantUid = (String) data.get("merchant_uid");
            int pamount = (Integer) data.getOrDefault("pamount", 0);
            int pay_price = (Integer) data.getOrDefault("pay_price", 0);
            String status = (String) data.get("status");
            Long item_num = Long.valueOf((Integer) data.get("item_num"));
            String itemName = (String) data.get("item_name");
            String buyerName = (String) data.get("buyer_name");
            Integer quantity = (Integer) data.get("quantity");
            String deliveryAddress = (String) data.get("delivery_address");
            int pointUsed = (Integer) data.getOrDefault("point_used", 0);

            log.debug("impUid : " + impUid);
            log.debug("merchantUid : " + merchantUid);
            log.debug("amount : " + pamount);
            log.debug("pay_price : " + pay_price);
            log.debug("status : " + status);
            log.debug("itemNum : " + item_num);
            log.debug("itemName : " + itemName);
            log.debug("buyerName : " + buyerName);
            log.debug("deliveryAddress : " + deliveryAddress);
            log.debug("quantity : " + quantity);

            if (member == null) {
                mapJson.put("result", "logout");
            } else {
                // 결제 정보 저장
                PurchaseVO purchaseVO = new PurchaseVO();
                purchaseVO.setImp_uid(impUid);
                purchaseVO.setMerchant_uid(merchantUid);
                purchaseVO.setPay_price(pay_price);
                purchaseVO.setPamount(pamount);
                purchaseVO.setPayStatus(0); // 결제 완료 상태로 설정
                purchaseVO.setItem_num(item_num);
                purchaseVO.setMem_num(member.getMem_num()); // mem_num 설정
                purchaseVO.setDelivery_address(deliveryAddress); // 추가된 부분
                purchaseVO.setPoint_used(pointUsed); // 추가된 부분
                purchaseVO.setItem_name(itemName);
                try {
                    purchaseService.insertPurchase(purchaseVO);
                    
                    // PurchaseVO의 purchase_num 업데이트
                    Long newPurchaseNum = purchaseService.getLastInsertedPurchaseNum();
                    purchaseVO.setPurchase_num(newPurchaseNum);

                    // 재고 업데이트
                    purchaseService.updateStock(item_num, null, quantity);

                    // NotifyVO 객체 정의
                    NotifyVO notifyVO = new NotifyVO();
                    notifyVO.setMem_num(purchaseVO.getMem_num()); // 알림 받을 회원 번호
                    notifyVO.setNotify_type(16); // 알림 타입
                    notifyVO.setNot_url("/goods/detail?item_num=" + purchaseVO.getItem_num()); // 알림을 누르면 반환할 url

                    // 동적 데이터 매핑
                    Map<String, String> dynamicValues = new HashMap<>();
                    // value로 전달하는 값은 String이어야 함
                    // 동적 데이터가 여러 개일 경우 여러 개 매핑
                    dynamicValues.put("purchase_num", String.valueOf(purchaseVO.getPurchase_num())); // 알림 템플릿 참조

                    // NotifyService 호출
                    notifyService.insertNotifyLog(notifyVO, dynamicValues); // 알림 로그 찍기
                    mapJson.put("result", "success");
                } catch (Exception e) {
                    log.error("결제 정보 저장 중 오류 발생", e);
                    mapJson.put("result", "error");
                    mapJson.put("message", "결제 정보 저장 중 오류가 발생했습니다. 관리자에게 문의하세요.");
                }
            }
        } catch (NumberFormatException e) {
            log.error("데이터 형식 오류: ", e);
            mapJson.put("result", "error");
            mapJson.put("message", "데이터 형식 오류가 발생했습니다. 관리자에게 문의하세요.");
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생: ", e);
            mapJson.put("result", "error");
            mapJson.put("message", "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.");
        }

        return mapJson;
    }



    /*===================================
     * 환불 처리
     *==================================*/
 // 기존 메서드 생략

    @PostMapping("/refundPage")
    public String refundPage(@RequestParam("imp_uid") String impUid, Model model) {
        model.addAttribute("imp_uid", impUid);
        return "goods/refund";
    }

    @PostMapping("/refund")
    @ResponseBody
    public Map<String, String> processRefund(@RequestBody Map<String, Object> data, HttpSession session, HttpServletRequest request)
            throws IllegalStateException, IOException {
        Map<String, String> mapJson = new HashMap<>();

        try {
            String impUid = (String) data.get("imp_uid");
            String reason = (String) data.get("reason");

            log.debug("impUid : " + impUid);
            log.debug("reason : " + reason);

            // 세션 데이터 가져오기
            MemberVO member = (MemberVO) session.getAttribute("user");

            if (member == null) {
                mapJson.put("result", "logout");
            } else {
                try {
                    // 환불 요청
                    CancelData cancelData = new CancelData(impUid, true); // imp_uid를 사용하여 환불 요청
                    cancelData.setReason(reason);

                    IamportResponse<Payment> cancelResponse = impClient.cancelPaymentByImpUid(cancelData);

                    if (cancelResponse.getResponse() != null) {
                        // 환불 성공 시, 데이터베이스에서 해당 항목 업데이트
                        purchaseService.updateRefundStatus(impUid, 2); // 2: 환불 완료 상태

                        mapJson.put("result", "success");
                        mapJson.put("message", "환불이 성공적으로 처리되었습니다.");
                        log.debug("환불 성공: " + cancelResponse.getResponse().getImpUid());
                    } else {
                        mapJson.put("result", "error");
                        mapJson.put("message", "환불 처리 중 오류가 발생했습니다.");
                        log.error("환불 처리 중 오류 발생: " + cancelResponse.getMessage());
                    }
                } catch (IamportResponseException | IOException e) {
                    log.error("환불 처리 중 예외 발생: ", e);
                    mapJson.put("result", "error");
                    mapJson.put("message", "환불 처리 중 오류가 발생했습니다. 관리자에게 문의하세요.");
                }
            }
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생: ", e);
            mapJson.put("result", "error");
            mapJson.put("message", "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.");
        }

        return mapJson;
    }
    
    @PostMapping("/purchaseFromCart")
    @ResponseBody
    public Map<String, String> purchaseFromCart(@RequestBody Map<String, Object> data, HttpSession session, HttpServletRequest request)
            throws IllegalStateException, IOException {
        Map<String, String> mapJson = new HashMap<>();
        MemberVO memberVO = (MemberVO) session.getAttribute("user");
        try {
            String impUid = (String) data.get("imp_uid");
            String merchantUid = (String) data.get("merchant_uid");
            int pamount = (Integer) data.get("pamount");
            int pay_price = (Integer) data.get("pay_price");
            String status = (String) data.get("status");
            String itemName = (String) data.get("item_name");
            String buyerName = (String) data.get("buyer_name");
            String deliveryAddress = (String) data.get("delivery_address");
            int pointUsed = (Integer) data.get("point_used");

            Long setSeq = 0L;
            log.debug("impUid : " + impUid);
            log.debug("merchantUid : " + merchantUid);
            log.debug("pamount : " + pamount);
            log.debug("pay_price : " + pay_price);
            log.debug("status : " + status);
            log.debug("itemName : " + itemName);
            log.debug("buyerName : " + buyerName);
            log.debug("deliveryAddress : " + deliveryAddress);
            log.debug("<<장바구니 결제>> - member : " + memberVO);
            if (memberVO == null) {
                mapJson.put("result", "logout");
            } else {
                setSeq = purchaseService.getSeq();
                log.debug("Generated Sequence: " + setSeq);

                PurchaseVO purchaseVO = new PurchaseVO();
                purchaseVO.setPurchase_num(setSeq);
                purchaseVO.setImp_uid(impUid);
                purchaseVO.setMerchant_uid(merchantUid);
                purchaseVO.setPamount(pamount);
                purchaseVO.setPay_price(pay_price);
                purchaseVO.setPayStatus(0); // 결제 완료 상태로 설정
                purchaseVO.setItem_name(itemName);
                purchaseVO.setBuyer_name(buyerName);
                purchaseVO.setMem_num(memberVO.getMem_num());
                purchaseVO.setDelivery_address(deliveryAddress);
                purchaseVO.setPoint_used(pointUsed);

                List<Map<String, Object>> cartItems = (List<Map<String, Object>>) data.get("cart_items");
                log.debug("cartItems: " + cartItems);

                if (cartItems == null || cartItems.isEmpty()) {
                    mapJson.put("result", "error");
                    mapJson.put("message", "cart_items가 null이거나 비어 있습니다. 데이터를 확인하세요.");
                    return mapJson;
                }

                try {
                    Long firstItemNum = null;
                    for (Map<String, Object> item : cartItems) {
                        Long item_num = Long.valueOf((Integer) item.get("item_num"));
                        if (firstItemNum == null) {
                            firstItemNum = item_num;
                        }
                        Long cartQuantity = Long.valueOf((Integer) item.get("cart_quantity"));
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("item_num", item_num);
                        paramMap.put("cart_quantity", cartQuantity);
                        paramMap.put("quantity", null);
                        purchaseService.updateStock(paramMap); // Update stock for cart items
                    }

                    purchaseService.insertPurchase(purchaseVO);

                    // NotifyVO 객체 정의
                    NotifyVO notifyVO = new NotifyVO();
                    notifyVO.setMem_num(purchaseVO.getMem_num()); // 알림 받을 회원 번호
                    notifyVO.setNotify_type(16); // 알림 타입
                    notifyVO.setNot_url("/goods/detail?item_num=" + firstItemNum); // 첫 번째 아이템 번호 사용

                    // 동적 데이터 매핑
                    Map<String, String> dynamicValues = new HashMap<>();
                    dynamicValues.put("purchase_num", String.valueOf(purchaseVO.getPurchase_num())); // 알림 템플릿 참조

                    // NotifyService 호출
                    notifyService.insertNotifyLog(notifyVO, dynamicValues); // 알림 로그 찍기

                    mapJson.put("result", "success");
                } catch (Exception e) {
                    log.error("결제 정보 저장 중 오류 발생", e);
                    mapJson.put("result", "error");
                    mapJson.put("message", "결제 정보 저장 중 오류가 발생했습니다. 관리자에게 문의하세요.");
                }
            }
        } catch (NumberFormatException e) {
            log.error("데이터 형식 오류: ", e);
            mapJson.put("result", "error");
            mapJson.put("message", "데이터 형식 오류가 발생했습니다. 관리자에게 문의하세요.");
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생: ", e);
            mapJson.put("result", "error");
            mapJson.put("message", "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.");
        }

        return mapJson;
    }
    @GetMapping("/purchaseHistory")
    public String getPurchaseHistory(HttpSession session, Model model) {
        MemberVO member = (MemberVO) session.getAttribute("user");

        if (member == null) {
            return "redirect:/member/login";
        }

        List<PurchaseVO> purchaseList = purchaseService.getPurchaseListByMember(member.getMem_num());
        model.addAttribute("purchaseList", purchaseList);

        return "purchaseHistory";
    }
}
