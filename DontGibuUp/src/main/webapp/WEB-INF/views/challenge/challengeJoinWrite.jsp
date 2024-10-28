<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.iamport.kr/v1/iamport.js"></script>
<script type="text/javascript">
        let chalTitle = "${challengeVO.chal_title}";
        var chalFee = ${challengeVO.chal_fee};
        let memberNick = "${member.mem_nick}";
        let memberEmail = "${member.email}";
        let memberNum = "${member.mem_num}";
        let pageContextPath = "${pageContext.request.contextPath}";
        let s_date = "${challengeVO.chal_sdate}"; //챌린지 시작 날짜 가져오기
        let mem_point = ${mem_point};
        let chal_num = "${challengeVO.chal_num}";
</script>
<script src="${pageContext.request.contextPath}/js/challenge/challenge.join.pay.js"></script>
<br><br><br><br>
<div class="join-container nanum">
	<div class="challenge-info3">
		<c:if test="${empty challengeVO.chal_photo}">
			<img src="${pageContext.request.contextPath}/images/챌린지_기본이미지.jpg"
				alt="챌린지 사진"  class="challenge-thumbnail">
		</c:if>
		<c:if test="${!empty challengeVO.chal_photo}">
			<img
				src="${pageContext.request.contextPath}/upload/${challengeVO.chal_photo}"
				alt="챌린지 사진"  class="challenge-thumbnail">
		</c:if>
		<div class="challenge-details">
			<h2>${challengeVO.chal_title}</h2>
			<c:if test="${challengeVO.chal_freq == 0}">
				<p>매일</p>
			</c:if>
			<c:if test="${challengeVO.chal_freq != 0}">
				<p>주 ${challengeVO.chal_freq}일</p>
			</c:if>
			<p>${challengeVO.chal_sdate}~ ${challengeVO.chal_edate}</p>
		</div>
	</div>
	<form:form id="challenge_join" enctype="multipart/form-data" modelAttribute="challengeJoinVO">
		<label for="dcate_num"><h6 style="color: #212529;">기부 카테고리</h6></label>&nbsp;
		<span id="charityInfo"></span> 
		<span id="dcate_num_error" class="error-color" style="display: none;">기부 카테고리를 선택하세요.</span>
		<br><br>
				
		<c:forEach var="category" items="${categories}">
			<input type="radio" class="custom-radio" name="dcate_num" id="dcate_${category.dcate_num}" value="${category.dcate_num}" data-charity="${category.dcate_charity}">
			<label class="custom-radio-label" for="dcate_${category.dcate_num}">${category.dcate_name}</label>
		</c:forEach>
				
		<br><br>
		<li class="result-details">
			<p style="margin-top: 16px; font-size: 14px;">
				100% 성공 <span class="right"><span class="chal_fee_90"></span>p
				+ 추가 <span class="chal_fee_5"></span>p 환급, 
				<span class="chal_fee_10"></span>원 기부</span>
			</p>
			<p style="font-size: 14px;">
				90% 이상 성공 <span class="right"><span class="chal_fee_90"></span>p 환급, <span class="chal_fee_10"></span>원 기부</span>
			</p>
			<p style="font-size: 14px;">
				90% 미만 성공 <span class="right">성공률만큼 환급, 나머지 기부</span>
			</p>
		</li>
		<br><br>
		<div class="payment-details">
		    <h6>
		        참여금 
		        <span class="amount-container">
		            <span class="amount chal_fee"></span>&nbsp;<span class="currency">원</span>
		        </span>
		    </h6>
		    <br>
		    <h6>
		        보유 포인트 
		        <span class="amount-container2">
		            <span class="amount mem-point">${mem_point}</span>&nbsp;<span class="currency">p</span>
		        </span>
		    </h6>
		    <h6>
		        사용할 포인트 
		        <span class="input-container">
		            <input type="text" class="used-point" value="0">&nbsp;<span class="currency">p</span>
		        </span>
		    </h6>
		    <hr>
		    <h6 style="font-size: 24px;">
		        결제금액 
		        <span class="amount-container">
		            <span class="amount final_fee"></span>&nbsp;<span class="currency">원</span>
		        </span>
		    </h6>
		</div>
		<br>
		<div class="align-center">
			<p style="color: #ddd;"><b>결제 조건 및 서비스 약관에 동의합니다</b></p>
			<button type="button" id="pay2">결제하기</button>
		</div>
	</form:form>
</div>
<br><br><br><br>