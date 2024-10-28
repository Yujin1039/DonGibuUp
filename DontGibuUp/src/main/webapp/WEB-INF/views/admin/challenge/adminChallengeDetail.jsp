<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<script>
    var contextPath = '<%= request.getContextPath() %>';
</script>
<script src="${pageContext.request.contextPath}/js/challenge/challenge.fav.js"></script>
<div class="challenge-detail">
    <div class="challenge-header">
        <c:if test="${empty challenge.chal_photo}">
        		<img src="${pageContext.request.contextPath}/images/챌린지_기본이미지.jpg" alt="챌린지 사진">
        </c:if>
        <c:if test="${!empty challenge.chal_photo}">
        	<img src="${pageContext.request.contextPath}/upload/${challenge.chal_photo}" alt="챌린지 사진">
        </c:if>
        <div class="challenge-info2">
	        <div class="author-info">
	        		<h2 class="align-left">${challenge.chal_title}</h2>
	        </div>
        </div>
    </div>
    <div class="challenge-info2">
        <div class="author-info">
        	<c:if test="${empty challenge.mem_photo}">
        		<img src="${pageContext.request.contextPath}/images/basicProfile.png" alt="작성자 프사">
        	</c:if>
        	<c:if test="${!empty challenge.mem_photo}">
        		<img src="${pageContext.request.contextPath}/upload/${challenge.mem_photo}" alt="작성자 프사">
        	</c:if>
            <div class="details">
                <p><strong>${challenge.mem_nick}</strong></p>
            </div>
        </div>
    </div>
    <div class="challenge-stats">
        <div>
            <span>인증 빈도</span>
            <c:if test="${challenge.chal_freq == 0}">
        		<p>매일</p>
        	</c:if>
        	<c:if test="${challenge.chal_freq != 0}">
        		<p>주 ${challenge.chal_freq}일</p>
        	</c:if>
        </div>
        <div>
            <span>기간</span>
            <p>${challenge.chal_sdate} ~ ${challenge.chal_edate}</p>
        </div>
        <div>
            <span>참여금</span>
            <p><span style="color: blue;">${formattedFee}</span>원</p>
        </div>
        <div>
            <span>모집 인원</span>
            <p><span style="color: red;">${challenge.chal_join}명</span> / ${challenge.chal_max}명</p>
        </div>
        
 <%--        		<p><strong>총 참여금:</strong> <fmt:formatNumber value="${totalFee}" type="number" />원</p>
            <p><strong>총 기부금:</strong> <fmt:formatNumber value="${totalDonation}" type="number" />원</p>
            <p><strong>달성률 100% 참가자 수:</strong> ${fullAchievers}명</p>
            <p><strong>달성률 90% 이상 참가자 수:</strong> ${highAchievers}명</p>
            <p><strong>달성률 90% 미만 참가자 수:</strong> ${lowAchievers}명</p>
            <p><strong>평균 달성률:</strong> <fmt:formatNumber value="${averageAchievement}" type="number" minFractionDigits="1" maxFractionDigits="1" />%</p> --%>
    </div>
    <hr> 
</div>