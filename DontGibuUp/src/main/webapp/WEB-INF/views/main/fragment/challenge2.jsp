<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="java.text.SimpleDateFormat, java.util.Date" %>
<!DOCTYPE html>
<html>
<head>
<style>
    .exercise-challenges {
        display: flex;
        justify-content: space-around;
        flex-wrap: wrap;
        gap: 20px;
        padding: 20px;
    }

    .challenge-item {
        background: #fff;
        border: 1px solid #ddd;
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        width: 220px;
        overflow: hidden;
        transition: box-shadow 0.3s ease-in-out;
        position: relative;
        text-decoration: none; /* 링크 텍스트 밑줄 제거 */
        color: inherit; /* 링크 텍스트 색상 상속 */
    }

    .challenge-item:hover {
        box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    }

    .challenge-thumbnail {
        width: 100%;
        height: 180px;
        object-fit: cover;
        transition: transform 0.3s ease; /* 확대 전환 효과 */
    }

    .challenge-item:hover .challenge-thumbnail {
        transform: scale(1.1); /* 확대 효과 */
    }

    .challenge-info {
        padding: 10px;
        padding-left: 15px;
    }

    .challenge-item h3 {
        font-size: 1.3em;
        margin: 2px 0;
        color: #5a6f80;
    }

    .challenge-item p {
        color: #666;
        margin: 5px 0;
    }
    
    .profile-pic {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        object-fit: cover;
        margin-right: 2px;
    }
    
    .participation-rate {
        font-size: 13px;
    }
</style>
</head>
<body>
<div>
    <div class="title-style">여름철 체력증진! 운동 챌린지</div>
    <div class="exercise-challenges nanum">
        <c:forEach var="challenge" items="${exerciseChallenges}">
            <a href="${pageContext.request.contextPath}/challenge/detail?chal_num=${challenge.chal_num}" class="challenge-item">
                <c:choose>
                    <c:when test="${empty challenge.chal_photo}">
                        <img src="${pageContext.request.contextPath}/images/챌린지_기본이미지.jpg" alt="챌린지 사진" class="challenge-thumbnail"/>
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/upload/${challenge.chal_photo}" alt="챌린지 사진" class="challenge-thumbnail"/>
                    </c:otherwise>
                </c:choose>
                <div class="challenge-info">
                  	<p>
                    <c:if test="${empty challenge.mem_photo}">
		        		<img src="${pageContext.request.contextPath}/images/basicProfile.png" alt="작성자 프사" class="profile-pic">
			        	</c:if>
			        	<c:if test="${!empty challenge.mem_photo}">
			        		<img src="${pageContext.request.contextPath}/upload/${challenge.mem_photo}" alt="작성자 프사" class="profile-pic">
			        	</c:if>
                        ${challenge.mem_nick}
                    </p>
                    <h3>${challenge.chal_title}</h3>
			        <p class="participation-rate" style="color:#ff5757; margin-top:5px;">
			            <c:set var="participationRate" value="${(challenge.chal_join * 100.0) / challenge.chal_max}" />
			            <b><c:out value="${fn:substringBefore(participationRate, '.')}" />% 달성</b>
			        </p>
                </div>
            </a>
        </c:forEach>
    </div>
</div>
</body>
</html>