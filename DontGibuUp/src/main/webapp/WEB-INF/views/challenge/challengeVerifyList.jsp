<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>챌린지 인증내역</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/challenge.css">
</head>
<body>
<h2>챌린지 인증내역</h2>
<div class="challenge-verify-list">
    <c:forEach var="verify" items="${verifyList}">
        <div class="challenge-verify-card">
            <img src="<c:url value='/images/${verify.chal_ver_photo}'/>" alt="인증 사진">
            <div class="content">
                <div class="date-status">
                    <span class="date">${verify.chal_reg_date}</span>
                    <c:choose>
                        <c:when test="${verify.chal_ver_status == 0}">
                            <span class="status success">성공</span>
                        </c:when>
                        <c:when test="${verify.chal_ver_status == 1}">
                            <span class="status failure">실패</span>
                        </c:when>
                    </c:choose>
                </div>
                <div class="comment">${verify.chal_content}</div>
            </div>
        </div>
    </c:forEach>
</div>
<div class="align-center">
    <c:choose>
        <c:when test="${status == 'post'}">
            <!-- 완료된 챌린지의 경우 버튼 숨김 -->
        </c:when>
        <c:when test="${hasTodayVerify}">
            <button class="disabled-button" disabled>오늘 인증 완료</button>
        </c:when>
        <c:when test="${hasCompletedWeeklyVerifications}">
            <button class="disabled-button" disabled>이번주 인증 완료</button>
        </c:when>
        <c:otherwise>
            <button class="active-button" onclick="location.href='${pageContext.request.contextPath}/challenge/verify/write?chal_joi_num=${chal_joi_num}&status=${status}'">인증하기</button>
        </c:otherwise>
    </c:choose>
    <div>
        <!-- 디버깅: hasCompletedWeeklyVerifications 값 출력 -->
        hasCompletedWeeklyVerifications: ${hasCompletedWeeklyVerifications}
    </div>
</div>
</body>
</html>