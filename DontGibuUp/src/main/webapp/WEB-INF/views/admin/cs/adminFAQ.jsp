<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
</script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/admin/admin.faq.js"></script>
<div class="page-main">
	<h2>자주 하는 질문 (FAQ)</h2>
	<div>
		<a href="faq?">전체</a> |
		<a href="faq?category=0">정기기부</a> |
		<a href="faq?category=1">기부박스</a> |
		<a href="faq?category=2">챌린지</a> |
		<a href="faq?category=3">굿즈샵</a> |
		<a href="faq?category=4">기타</a>
	</div>
		<ul id="faq_list">
		<c:if test="${!empty list}">
			<c:if test="${empty list}">
			<li>등록된 질문이 없습니다.</li>
		</c:if>
		<c:forEach var="faq" items="${list}">
			<li class="faq-item">
				<div class="faq-category-text">
				<c:if test="${faq.faq_category == 0}">
				정기기부
				</c:if>
				<c:if test="${faq.faq_category == 1}">
				기부박스
				</c:if>
				<c:if test="${faq.faq_category == 2}">
				챌린지
				</c:if>
				<c:if test="${faq.faq_category == 3}">
				굿즈샵
				</c:if>
				<c:if test="${faq.faq_category == 4}">
				기타
				</c:if>
				</div>
				<h4 class="faq-question-text">Q. ${faq.faq_question}</h4>
				<div class="faq-answer-text">A. ${faq.faq_answer}</div>
				<div class="align-right">
					<button class="modifyBtn" data-num="${faq.faq_num}">수정</button>
					<button class="deleteBtn" data-num="${faq.faq_num}">삭제</button>				
				</div>
			</li>
		</c:forEach>
		</c:if>
		</ul>
	<form id="insert_faq" method="post">
	<ul>
	    <li class="radio-option">
	        <input type="radio" id="regularDonation" name="faq_category" value="0">
	        <label for="regularDonation">정기기부</label>
	    </li>
	    <li class="radio-option">
	        <input type="radio" id="donationBox" name="faq_category" value="1">
	        <label for="donationBox">기부박스</label>
	    </li>
	    <li class="radio-option">
	        <input type="radio" id="challenge" name="faq_category" value="2">
	        <label for="challenge">챌린지</label>
	    </li>
	    <li class="radio-option">
	        <input type="radio" id="goodsShop" name="faq_category" value="3">
	        <label for="goodsShop">굿즈샵</label>
	    </li>
	    <li class="radio-option">
	        <input type="radio" id="other" name="faq_category" value="4">
	        <label for="other">기타</label>
	    </li>
	    <li>
	    	<span id="category_check_msg"></span>
	    </li>
		<li>
			<input id="faq_question" name="faq_question" type="text" placeholder="질문">
			<span id="question_check_msg"></span>	
		</li>
		<li>
			<textarea id="faq_answer" name="faq_answer" rows="5" cols="60" placeholder="내용"></textarea>
			<span id="answer_check_msg"></span>
		</li>
		<li class="align-right">
			<input type="submit" value="등록">	
		</li>
	
	
	</ul>
	</form>
</div>
