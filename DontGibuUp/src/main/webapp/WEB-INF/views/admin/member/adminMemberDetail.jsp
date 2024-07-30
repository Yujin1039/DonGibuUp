<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
    <!-- 관리자 회원 상세 시작 -->
<div class="page-main">
 	<h2>관리자 회원 상세</h2><br>
 		<button type="button" class="btn btn-outline-success mt-2 mb-2" onclick="location.href='/admin/manageMember'">목록</button>
 	<!-- 회원 주요 정보 -->
 	<h5>회원 주요 정보</h5>
 	<section class="container mt-5 mb-4">
		<div id="member_total_item" class="mb-1">
		    <div class="mb-1">
		        <div class="">
		            <c:if test="${!empty member.mem_photo}">
		            <img src="${pageContext.request.contextPath}/upload/${member.mem_photo}" class="rounded-circle my-image">&nbsp;${member.mem_nick}님
		            </c:if>
		            <c:if test="${empty member.mem_photo}">
		            <img src="${pageContext.request.contextPath}/images/basicProfile.png" class="rounded-circle my-image">&nbsp;${member.mem_nick}님
		            </c:if>
		        </div>
		        <div>${mem_nick}</div>
		    </div>
		    <div class="row text-center d-flex">
		        <div class="total-menu col-12 col-sm-3 mb-3 mb-sm-0 border-right">
		            <div class="total-label">포인트</div>
		            <div class="total-content" id="mem_point"><fmt:formatNumber value="${memberTotal.mem_point}" type="number" minFractionDigits="0" maxFractionDigits="0"/>P</div>
		        </div>
		        <div class="total-menu col-12 col-sm-3 mb-3 mb-sm-0 border-right">
		            <div class="total-label">총 기부횟수</div>
		            <div class="total-content" id="total_count">${memberTotal.total_count}</div>
		        </div>
		        <div class="total-menu col-12 col-sm-3 mb-3 mb-sm-0 border-right">
		            <div class="total-label">누적 기부액</div>
		            <div class="total-content" id="total_amount"><fmt:formatNumber value="${memberTotal.total_amount}" type="number" minFractionDigits="0" maxFractionDigits="0"/></div>
		        </div>
		        <div class="total-menu col-12 col-sm-3 border-right-last-none">
		            <div class="total-label">회원 등급</div>
		            <div class="total-content" id="mem_auth"> 	
						<c:if test="${member.auth_num == 1}">
						<img src="/images/auth/auth1.png" class="rounded-circle my-auth">
						</c:if>
						<c:if test="${member.auth_num == 2}">
						<img src="/images/auth/auth2.png" class="rounded-circle my-auth">
						</c:if>
						<c:if test="${member.auth_num == 3}">
						<img src="/images/auth/auth3.png" class="rounded-circle my-auth">
						</c:if>
						<c:if test="${member.auth_num == 4}">
						<img src="/images/auth/auth4.png" class="rounded-circle my-auth">
						</c:if>
						<c:if test="${member.auth_num == 5}">
						<img src="/images/auth/auth5.png" class="rounded-circle my-auth">
						</c:if>
						<c:if test="${member.auth_num == 6}">
						<img src="/images/auth/auth6.png" class="rounded-circle my-auth">
						</c:if>
					</div>
		        </div>
		    </div>
		</div>
		<p class="ms-2"><small>*총 기부 횟수와 누적 기부액은 정기 기부, 기부 박스, 챌린지, 굿즈샵을 통해 이루어진 모든 기부 활동을 종합하여 표시됩니다.</small></p>
	</section>
	<!-- 회원 등급 변경 -->
 	<h5>회원 등급 변경</h5>
 	<div class="shadow-sm p-3 mb-5 bg-body-tertiary rounded">
		<form action="Change" id="auth_form" method="get">
			<input type="hidden" name="mem_num" value="${member.mem_num}">
			<!-- 기부흙 -->
			<div class="form-check form-check-inline">
			  <input class="form-check-input status" type="radio" name="member_auth" value="1"
			  	<c:if test="${member.auth_num == 1}">checked</c:if>
			  >
			  <label for="auth1">
			  	<img src="/images/auth/auth1.png" class="rounded-circle my-image">&nbsp;기부흙
			  </label>
			</div>
			<!-- 기부씨앗 -->
			<div class="form-check form-check-inline">
			  <input class="form-check-input status" type="radio" name="member_auth" value="2"
			  	<c:if test="${member.auth_num == 2}">checked</c:if>
			  >
			  <label for="auth2">
			  	<img src="/images/auth/auth2.png" class="rounded-circle my-image">&nbsp;기부씨앗
			  </label>
			</div>
			<!-- 기부새싹 -->
			<div class="form-check form-check-inline">
			  <input class="form-check-input status" type="radio" name="member_auth" value="3"
			  	<c:if test="${member.auth_num == 3}">checked</c:if>
			  >
			  <label for="auth3">
			  	<img src="/images/auth/auth3.png" class="rounded-circle my-image">&nbsp;기부새싹
			  </label>
			</div>
			<!-- 기부꽃 -->
			<div class="form-check form-check-inline">
			  <input class="form-check-input status" type="radio" name="member_auth" value="4"
			  	<c:if test="${member.auth_num == 4}">checked</c:if>
			  >
			  <label for="auth4">
			  	<img src="/images/auth/auth4.png" class="rounded-circle my-image">&nbsp;기부꽃
			  </label>
			</div>
			<!-- 기부나무 -->
			<div class="form-check form-check-inline">
			  <input class="form-check-input status" type="radio" name="member_auth" value="5"
			  	<c:if test="${member.auth_num == 5}">checked</c:if>
			  >
			  <label for="auth5">
			  	<img src="/images/auth/auth5.png" class="rounded-circle my-image">&nbsp;기부나무
			  </label>
			</div>
			<!-- 기부숲 -->
			<div class="form-check form-check-inline">
			  <input class="form-check-input status" type="radio" name="member_auth" value="6"
			  	<c:if test="${member.auth_num == 6}">checked</c:if>
			  >
			  <label for="auth6">
			  	<img src="/images/auth/auth6.png" class="rounded-circle my-image">&nbsp;기부숲
			  </label>
			</div>
			<br>
			<button type="submit" class="btn btn-outline-success mt-2">변경</button>
	 	</form>
	 	<script>
	 	$('#auth_form').submit(function(){
	 		if(!confirm('변경하시겠습니까?')){
	 			return false;
	 		}
	 	});
	 	</script>
	 	
	 	등급변경 기준<br>
	 	<small>
	 	<img src="/images/auth/auth1.png" class="rounded-circle my-image"> - 기본등급<br>
		<img src="/images/auth/auth2.png" class="rounded-circle my-image"> - 기부액 10,000원, 기부횟수 5회<br>
		<img src="/images/auth/auth3.png" class="rounded-circle my-image"> - 기부액 100,000원, 기부횟수 10회<br>
		<img src="/images/auth/auth4.png" class="rounded-circle my-image"> - 기부액 1,000,000원, 기부횟수 30회<br>
		<img src="/images/auth/auth5.png" class="rounded-circle my-image"> - 기부액 10,000,000원, 기부횟수 30회<br>
		<img src="/images/auth/auth6.png" class="rounded-circle my-image"> - 기부액 100,000,000원, 기부횟수 30회<br>
	 	</small>
	</div>
 	
 	<h5>신청자 정보</h5>
 	<div class="shadow-sm p-3 mb-5 bg-body-tertiary rounded">
 		회원 번호 : ${member.mem_num}<br>
	 	닉네임 : 	<c:if test="${!empty member.mem_photo}">
                <img src="${pageContext.request.contextPath}/upload/${member.mem_photo}" class="rounded-circle my-image">&nbsp;${member.mem_nick}&nbsp;님<br>
                </c:if>
                <c:if test="${empty member.mem_photo}">
                <img src="${pageContext.request.contextPath}/images/basicProfile.png" class="rounded-circle my-image">&nbsp;${member.mem_nick}&nbsp;님<br>
                </c:if>
	 	상태 : 
			<c:if test="${member.mem_status == 0}">
			탈퇴회원
			</c:if>
			<c:if test="${member.mem_status == 1}">
			정지회원
			</c:if>
			<c:if test="${member.mem_status == 2}">
			일반회원
			</c:if>
			<c:if test="${member.mem_status == 9}">
			관리자
			</c:if>
	 	<br>
	 	이메일 : ${member.mem_email}<br>	
 	</div>
</div>
<!-- 관리자 회원 상세 끝 -->