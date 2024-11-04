<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
	let contextPath = "${pageContext.request.contextPath}";
	let status = "${status}"; 
</script>
<div id="challengeContainer">
	<div class="month-section">
		<h2 class="align-left">
			<span id="prevMonth" style="cursor: pointer;">&lt;</span>&nbsp;&nbsp;&nbsp;<span
				id="currentMonth" data-month="${currentMonth}">${currentMonth.replace("-", "년&nbsp;&nbsp;")}월</span>
			&nbsp;&nbsp;<span id="nextMonth" style="cursor: pointer;">&gt;</span>
		</h2>
		<br>
		<div class="challenge-list">
			<c:if test="${count == 0}">
				<div class="result-display">신청하신 챌린지 중 시작 날짜가 이번달인 챌린지가 없습니다.</div>
			</c:if>
			<c:if test="${count != 0}">			
			<c:forEach var="entry" items="${challengesByMonth}">
				<c:forEach var="challengeData" items="${entry.value}">
					<c:set var="challengeJoin" value="${challengeData.challengeJoin}" />
					<div class="challenge-card ${challengeJoin.host ? 'host-card' : 'participant-card'}">					
						<div class="card-header">
						<h3 style="color: #5a6f80;">
							<a href="${pageContext.request.contextPath}/challenge/detail?chal_num=${challengeJoin.chal_num}" >
								${challengeJoin.chal_title}&nbsp;
							</a>
						    <c:if test="${challengeData.achieveRate >= 90}">
						        <i class="bi bi-trophy-fill" style="font-size: 0.8em; color: #FFD700;"></i>
						    </c:if>
						</h3>
							<div class="view-detail">
								<c:choose>
									<c:when test="${status == 'pre'}">
										<a class="red-custom" href="#" onclick="deleteChallenge(${challengeJoin.chal_joi_num},${challengeData.isHost})">챌린지취소</a>
									</c:when>
									<c:when test="${status == 'on'}">
										<a class="orange-custom" href="${pageContext.request.contextPath}/challenge/verify/list?chal_num=${challengeJoin.chal_num}&chal_joi_num=${challengeJoin.chal_joi_num}&status=${status}">인증내역</a>
										<c:if test="${challengeJoin.pub}">
											<a class="gray-custom chal_talk" href="#" data-chal-num="${challengeJoin.chal_num}"
											data-chal-joi-num="${challengeJoin.chal_joi_num}" data-status="${status}">단체톡방</a>
										</c:if>										
									</c:when>
									<c:when test="${status == 'post'}">
										<a class="orange-custom" href="${pageContext.request.contextPath}/challenge/verify/list?chal_num=${challengeJoin.chal_num}&chal_joi_num=${challengeJoin.chal_joi_num}&status=${status}">인증내역</a>
										<c:if test="${challengeData.hasReview}">
							                <a class="gray-custom" href="${pageContext.request.contextPath}/challenge/review/list?chal_num=${challengeJoin.chal_num}">후기보기</a>
							            </c:if>
							            <c:if test="${!challengeData.hasReview}">
							                <a class="dgray-custom" href="${pageContext.request.contextPath}/challenge/review/write?chal_num=${challengeJoin.chal_num}">후기작성</a>
							            </c:if>
									</c:when>
								</c:choose>
							</div>
						</div>
						<p>${challengeJoin.chal_sdate}- ${challengeJoin.chal_edate}</p>
						<div>
							<div style="margin-bottom: 10px;">
								<b> 
								<span style="color: #198754;">달성률</span> 
								<span style="color: #198754;">${challengeData.achieveRate}%</span>
							</div>
							<div class="progress progress-md">
								<div class="progress-bar bg-success" role="progressbar"style="width: ${challengeData.achieveRate}%" 
								aria-valuenow="100-${challengeData.achieveRate}" aria-valuemin="0" aria-valuemax="100"></div>
							</div>
							</b>
						</div>
						<br>
						<div class="details">
							<div>
								<span><b>참여금</b></span> <span>${challengeData.formattedFee}원</span>
							</div>
							<div>
								<span><b>환급포인트</b></span> <span style="color: #17a2b8;">${challengeData.returnPoint}p</span>
							</div>
							<div>
								<span><b>기부금</b></span> <span>${challengeData.donaAmount}원</span>
							</div>
							<div>
								<span><b>기부처</b></span> <span>${challengeJoin.dcate_charity}</span>
							</div>
						</div>
					</div>
				</c:forEach>
			</c:forEach>
			</c:if>
		</div>
		<div class="align-center">${page}</div>
	</div>
</div>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/challenge/challenge.join.list.js"></script>