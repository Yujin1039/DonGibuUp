<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<script src="https://cdn.iamport.kr/v1/iamport.js"></script>
<div class="line">
    <h2>챌린지 참가하기</h2>
    <div>
        <img src="<c:url value='/images/${challenge.chal_photo}' />" alt="${challenge.chal_title}" />
        <h3>${challenge.chal_title}</h3>
        <p>${challenge.chal_freq}</p>
        <p>${challenge.chal_sdate} ~ ${challenge.chal_edate}</p>
        
        <p>참여금 <span id="chal_fee">${challenge.chal_fee}</span>원</p>
        <p>100% 성공 : <span class="chal_fee_90"></span>원 + 추가 (??)원 환급, <span class="chal_fee_10"></span>원 기부</p>
        <p>90% 이상 성공 : <span class="chal_fee_90"></span>원 환급, <span class="chal_fee_10"></span>원 기부</p>
        <p>90% 미만 성공 : 성공률만큼 환급, 나머지 기부</p>
    </div>
    <form:form action="leaderJoin" id="joinAndPay" enctype="multipart/form-data" modelAttribute="challengeJoinVO">
        <ul>
            <li>
                <form:label path="dcate_num">기부 카테고리</form:label>
                <c:forEach var="category" items="${categories}">
                    <form:radiobutton path="dcate_num" value="${category.dcate_num}" label="${category.dcate_name}" data-charity="${category.dcate_charity}"/>
                </c:forEach>
                <span class="error-color" style="display:none;">기부카테고리를 선택하세요</span>
            </li>
            <li>
                <label>기부처:</label>
                <span id="charityInfo"></span>
            </li>
        </ul>
        <div class="align-center">
            결제 조건 및 서비스 약관에 동의합니다
            <button type="button" id="pay">결제하기</button>
        </div>
    </form:form>
</div>

<script>
	setChallengePointRules();
	$('input[type="radio"]').prop('checked', false);
	console.log("${member}");
	
	$('input[type="radio"]').click(function(){
		$('.error-color').hide();
		let charityName = $(this).attr('data-charity');
		$('#charityInfo').text(charityName);
	});
	
	$('#pay').click(function(){
		if($('input[name="dcate_num"]:checked').length < 1){
			$('.error-color').show();
			return;
		}
		
		payAndEnroll();
	});
	
  function formatNumber(num) {
  	return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  }

  function setChallengePointRules() {
  	let chalFeeElement = document.getElementById('chal_fee');
    let chalFee90Element = document.querySelectorAll('.chal_fee_90');
    let chalFee10Element = document.querySelectorAll('.chal_fee_10');

    if (chalFeeElement) {
      var chalFee = parseInt(chalFeeElement.innerText.replace(/,/g, ''), 10);
      var chalFee90 = (chalFee * 0.9).toFixed(0);
      var chalFee10 = chalFee - chalFee90;

      chalFeeElement.innerText = formatNumber(chalFee);
            
      chalFee90Element.forEach(function(e){
            	e.innerText = formatNumber(chalFee90);
      });
      chalFee10Element.forEach(function(e){
            	e.innerText = formatNumber(chalFee10);
      });
    }
  }
  
  function payAndEnroll(){
		
		IMP.init("imp71075330");
		
		IMP.request_pay(
				  {
				    pg: "tosspayments", // 반드시 "tosspayments"임을 명시해주세요.
				    merchant_uid: "merchant_" + new Date().getTime(),
				    name: "${challenge.chal_title}",
				    pay_method: "card",
				    escrow: false,
				    amount: 109000,
				    buyer_name: "${member.mem_nick}",
				    buyer_email: "${member.email}",
				    currency: "KRW",
				    locale: "ko",
				    custom_data: { userId: 30930 },
				    appCard: false,
				    useCardPoint: false,
				    bypass: {
				      tosspayments: {
				        useInternationalCardOnly: false, // 영어 결제창 활성화
				      },
				    },
				  },
				  (rsp) => {
					  console.log("결제 응답 객체: ", rsp); // 응답 객체 전체를 콘솔에 출력하여 디버깅

			            if (rsp.success) {
			                console.log('success');
			            } else {		            	
			            	console.log('rsp');
			            }
				  },
				);
  }			
</script>