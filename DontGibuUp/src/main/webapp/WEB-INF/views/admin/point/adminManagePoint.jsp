<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!-- 회원 포인트 관리 시작 -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/admin/admin.point.js"></script>
<div class="page-main">
	<h2>포인트 관리</h2>
	<form action="managePoint" id="search_form" method="get">
		<ul class="search">
			<li>
				<select name="keyfield" id="keyfield">
					<option value="1" <c:if test="${param.keyfield == 1}">selected</c:if>>회원번호</option>
					<option value="2" <c:if test="${param.keyfield == 2}">selected</c:if>>이메일</option>
					<option value="3" <c:if test="${param.keyfield == 3}">selected</c:if>>닉네임</option>
				</select>
			</li>
			<li>
				<input type="search" name="keyword" id="keyword" value="${param.keyword}">
			</li>
			<li>
				<input type="submit" value="찾기">
				<button id="allResultBtn">전체</button>
			</li>
		</ul>
		<div class="align-right">
			<select id="order" name="order">
				<option value="1" <c:if test="${param.order == 1}">selected</c:if>>최근 가입</option>
				<option value="2" <c:if test="${param.order == 2}">selected</c:if>>이메일</option>
				<option value="3" <c:if test="${param.order == 3}">selected</c:if>>닉네임</option>
			</select>
			<script type="text/javascript">
				$('#order').change(function() {
							location.href = 'managePoint?&keyfield='
											+ $('#keyfield').val()
											+ '&keyword='
											+ $('#keyword').val()
											+ '&order=' + $('#order').val();
								});
			</script>
		</div>
	</form>
	<c:if test="${count == 0}">
	<div class="result-display">표시할 회원이 없습니다.</div>
	</c:if>
	<c:if test="${count > 0}">
	<div>총 ${count} 건의 레코드</div>
	<table class="striped-table">
		<tr>
			<th>회원번호</th>
			<th>이메일</th>
			<th>닉네임</th>
			<th>가입일</th>
			<th>포인트</th>
		</tr>
		<c:forEach var="member" items="${list}">
		<tr class="mem-item">
			<td class="align-center">${member.mem_num}</td>
			<td class="align-left">${member.mem_email}</td>
			<td class="align-center">${member.mem_nick}</td>
			<td class="align-center">${member.mem_date}</td>
			<td class="align-center">
				<input type="number" class="member-point" value="${member.mem_point}" step="100" min="0" > P
				<button class="updatePointBtn" data-num="${member.mem_num}">수정</button>
			</td>
		</tr>
		</c:forEach>
	</table>
	<div class="align-center">${page}</div>
	</c:if>
</div>
<!-- 회원 목록 끝 -->