<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!-- 게시판 목록 시작 -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<div class="page-main">
	<h2>나의 정기기부 목록</h2>
	<c:if test="${count == 0}">
	<div class="result-display">표시할 정기기부 현황이 없습니다.</div>
	</c:if>
	<c:if test="${count > 0}">
	<table class="striped-table">
		<tr>
			<th>정기기부 번호</th>
			<th>기부처</th>
			<th>월 기부액</th>
			<th>결제일</th>
			<th>진행여부</th>
			<th>결제수단</th>
		</tr>
		<c:forEach var="subscription" items="${list}">
			<tr>
				<td>${subscription.sub_num}</td>
				<td>
					${subscription.donationCategory.dcate_name} / ${subscription.donationCategory.dcate_charity}
				</td>
				<td>${subscription.sub_price}</td>
				<td>${subscription.sub_ndate}</td>
				<td>
					<c:if test="${subscription.sub_status==0}">
						진행중
					</c:if>
					<c:if test="${subscription.sub_status==1}">
						중단
					</c:if>
				</td>
				<td>
				    <c:choose>
				        <c:when test="${subscription.sub_method == 'card'}">
				            카드 / ${subscription.card_nickname}
				        </c:when>
				        <c:when test="${subscription.sub_method == 'easy_pay'}">
				            간편결제 / ${subscription.easypay_method}
				        </c:when>
				        <c:otherwise>
				            알 수 없음
				        </c:otherwise>
				    </c:choose>
				</td>
			</tr>
		</c:forEach>
	</table>
	</c:if>	
</div>