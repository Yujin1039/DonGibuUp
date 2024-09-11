<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- 현재 날짜를 캡처 --%>
<%
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
    String currentDate = sdf.format(new java.util.Date());
    request.setAttribute("currentDate", currentDate);
%>

    <script src="${pageContext.request.contextPath}/js/jquery.min.js"></script>
    <script>
    	/* 이것 때문에 좋아요 안될 수 있음 */
        let contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/challenge/challenge.fav.js"></script>
<br><br><br><br>
<div class="challenge-detail nanum">
    <div class="challenge-header">
	    <c:if test="${empty challenge.chal_photo}">
	        <img src="${pageContext.request.contextPath}/images/챌린지_기본이미지.jpg" alt="챌린지 사진" class="challenge-thumbnail">
	    </c:if>
	    <c:if test="${!empty challenge.chal_photo}">
	        <img src="${pageContext.request.contextPath}/upload/${challenge.chal_photo}" alt="챌린지 사진" class="challenge-thumbnail">
	    </c:if>
	    <div class="challenge-info-overlay">
	        	<h2 class="challenge-title nanum">
				<a href="${pageContext.request.contextPath}/challenge/detail?chal_num=${challenge.chal_num}" style="color: white; text-decoration: none;">
					${challenge.chal_title}
				</a>
			</h2>	
	        <div class="like-button-container">
	            <button id="likeBtn" class="like-btn" data-num="${challenge.chal_num}">
	                <i class="bi bi-heart" id="likeIcon"></i>&nbsp;
	                <span id="output_fcount" class="nanum"></span>
	            </button>
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
                ${challenge.mem_nick}
            </div>
        </div>
        	<!-- DB에서 현재 날짜를 가져오는게 나았을 듯 -->
        <c:choose>
            <c:when test="${currentDate > challenge.chal_edate}">
                <button class="btn-custom" disabled>챌린지 마감</button>
            </c:when>
            <c:when test="${isJoined}">
                <button class="btn-custom" disabled>참가중</button>
            </c:when>
            <c:otherwise>
                <button class="btn-custom" onclick="location.href='join/write?chal_num=${challenge.chal_num}'">참가하기</button>
            </c:otherwise>
        </c:choose>
    </div>
	<div class="challenge-stats">
	    <div class="row">
	        <div class="col">
	            <span>인증 빈도</span>&nbsp;&nbsp;
	            <c:if test="${challenge.chal_freq == 7}">
	                <p class="inline-text">매일</p>
	            </c:if>
	            <c:if test="${challenge.chal_freq != 7}">
	                <p class="inline-text">주 ${challenge.chal_freq}일</p>
	            </c:if>
	        </div>
	        <div class="col">
	            <span>기간</span>&nbsp;&nbsp;
	            <p class="inline-text">${challenge.chal_sdate} ~ ${challenge.chal_edate}</p>
	        </div>
	    </div>
	    <div class="row">
	        <div class="col">
	            <span>참여금</span>&nbsp;&nbsp;
	            <p class="inline-text"><span style="color: blue;">${formattedFee}</span>원</p>
	        </div>
	        <div class="col">
	            <span>모집 인원</span>&nbsp;&nbsp;
	            <p class="inline-text"><span style="color: red;">${currentParticipants}명</span> / ${challenge.chal_max}명</p>
	        </div>
	    </div>
	</div>
    
    <!-- 챌린지 후기 -->  
    <c:if test="${reviewCount > 0}">
    <div style="padding: 10px;">
	    <div class="review-summary" style="padding: 10px;">
	        <h5 style="color: #212529; margin: 0;">참가자 후기</h5>
	        <span class="rating-stars"><i class="bi bi-star-fill"></i></span>
	        <span class="rating-value">${averageRating}</span>
	        <span class="review-count">(${reviewCount}개)</span>
	        <div class="btn-all-reviews">
	            <button onclick="location.href='${pageContext.request.contextPath}/challenge/review/list?chal_num=${challenge.chal_num}'">모두보기</button>
	        </div>
	    </div>
	    <div class="review-container">
	        <c:forEach var="review" items="${reviewList}" begin="0" end="2">
	            <div class="review-item">
	                <div class="review-content">
	                    <div class="review-header">
	                    <span class="rating">
	                            <c:forEach begin="1" end="5" varStatus="status">
	                                <c:choose>
	                                    <c:when test="${status.index <= review.chal_rev_grade}">
	                                        <i class="bi bi-star-fill"></i>
	                                    </c:when>
	                                    <c:otherwise>
	                                        <i class="bi bi-star"></i>
	                                    </c:otherwise>
	                                </c:choose>
	                            </c:forEach>
	                        </span>
	                    </div>
	                    <div>
	                    	<span class="nickname">${review.mem_nick}</span>
	                    	<span class="date">${review.chal_rev_date}</span>      
	                    </div>           
	                     <div class="review-text">
	                        <c:choose>
	                            <c:when test="${fn:length(review.chal_rev_content) > 12}">
	                                ${fn:substring(review.chal_rev_content, 0, 13)}..
	                            </c:when>
	                            <c:otherwise>
	                                ${review.chal_rev_content}
	                            </c:otherwise>
	                        </c:choose>
	                    </div>
	                </div>
	            </div>
	        </c:forEach>
	    </div>
	    <hr style="border: none; border-top: 1px solid #666666; margin: 30px 0 0;">
    </div>
    </c:if>
    
    <!-- 챌린지 상세 내용 -->     
	<div class="custom-form subscribe-form" style="margin: 20px;">
	<c:if test="${not empty challenge.chal_content}">
        <h5 class="mb-4">이런 분들께 추천합니다</h5>
        <p class="align-center">${challenge.chal_content}</p>
        <br>
    </c:if>
        <h5 class="mb-4">이렇게 인증해주세요</h5>
		<p class="align-center">${challenge.chal_verify}</p>
    </div>
                                                        
    <div class="align-right" onclick="location.href='/cs/report?report_source=1&chal_num=${challenge.chal_num}&reported_mem_num=${challenge.mem_num}'" style="cursor:pointer">
    		🚨신고하기
    </div>
</div>
<br><br><br><br>