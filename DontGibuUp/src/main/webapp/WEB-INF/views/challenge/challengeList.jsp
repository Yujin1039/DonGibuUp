<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<script src="${pageContext.request.contextPath}/js/challenge/challenge.list.js"></script>
<script>
	let pageContext = "${pageContext.request.contextPath}";
</script>

<section class="section-padding">
<div class="container">
	<div class="nanum">
			<div class="align-center">
				<div class="past-or-present">
					<a class="p-chal clicked present" href="list">참가 가능 챌린지</a>
					<a class="p-chal past" href="pastList">지난 챌린지</a>
				</div>
				<br>
				<div class="tags-block">
					<a href="#" class="category-link tags-block-link" data-category="">전체</a>
					<c:forEach var="cate" items="${categories}" varStatus="status">
						<a href="#" class="category-link tags-block-link" data-category="${cate.ccate_num}">${cate.ccate_name}</a>
					</c:forEach>
				</div>				
				<%-- <form class="custom-form donate-form" action="#" method="get"
					role="form">
					<div class="row" style="padding-left: 250px; padding-right: 250px;">
						<div class="col-lg-6 col-6 form-check-group form-check-group-donation-frequency">
						    <div class="form-check form-check-radio">
						        <input class="form-check-input" type="radio" name="DonationFrequency" id="DonationFrequencyOne" checked>
						        <label class="form-check-label" for="DonationFrequencyOne" style="cursor: pointer;">
						            참가 가능한 챌린지
						        </label>
						    </div>
						</div>
						<div class="col-lg-6 col-6 form-check-group form-check-group-donation-frequency">
							<div class="form-check form-check-radio">
								<input class="form-check-input" type="radio"
									name="DonationFrequency" id="DonationFrequencyMonthly">
								<label class="form-check-label" for="DonationFrequencyMonthly">
									<a href="pastList">지난 챌린지</a> </label>
							</div>
						</div>
					</div>
					<br>
				</form> --%>
			</div>			
		</div>
		<br>
		<form id="searchTitle">
			<div class="nanum" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
			    <div style="flex: 1; display: flex; gap: 10px; align-items: center;">
			        <select class="order" name="order" style="padding: 8px 8px; border-radius: 5px; border: 1px solid #ccc;">
			            <option value="0" <c:if test="${param.order == 0}">selected</c:if>>최신순</option>
			            <option value="1" <c:if test="${param.order == 1}">selected</c:if>>인기순</option>
			            <option value="2" <c:if test="${param.order == 2}">selected</c:if>>시작일순</option>
			            <option value="3" <c:if test="${param.order == 3}">selected</c:if>>참여인원순</option>
			        </select>
			        <select class="freqOrder" name="freqOrder" style="padding: 8px 8px; border-radius: 5px; border: 1px solid #ccc;">
			            <option value="" <c:if test="${empty param.freqOrder}">selected</c:if>>인증빈도</option>
			            <option value="0" <c:if test="${param.freqOrder == 7}">selected</c:if>>매일</option>
			            <option value="1" <c:if test="${param.freqOrder == 1}">selected</c:if>>주1일</option>
			            <option value="2" <c:if test="${param.freqOrder == 2}">selected</c:if>>주2일</option>
			            <option value="3" <c:if test="${param.freqOrder == 3}">selected</c:if>>주3일</option>
			            <option value="4" <c:if test="${param.freqOrder == 4}">selected</c:if>>주4일</option>
			            <option value="5" <c:if test="${param.freqOrder == 5}">selected</c:if>>주5일</option>
			            <option value="6" <c:if test="${param.freqOrder == 6}">selected</c:if>>주6일</option>
			        </select>
			    </div>
			    <div style="flex: 1; display: flex; gap: 10px; justify-content: flex-end; align-items: center;">
			        <c:if test="${!empty user}">
			            <input type="button" class="btn-custom" value="챌린지 개설하기" onclick="location.href='write'">
			        </c:if>
			        <input type="search" name="keyword" id="keyword" value="${param.keyword}" placeholder="제목을 입력하세요" style="padding: 5px 10px; border-radius: 5px; border: 1px solid #ccc;">
			        <input type="submit" value="찾기" style="padding: 6px 12px; border-radius: 5px; background-color: #597081; color: white; border: none; cursor: pointer;">
			    </div>
			</div>
		</form>
		<br>
		<div id="output" class="row"></div>
</div>

</section>