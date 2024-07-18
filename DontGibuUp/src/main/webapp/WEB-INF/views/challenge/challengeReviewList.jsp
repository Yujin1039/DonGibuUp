<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>챌린지 후기 목록</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/challenge.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        function loadReviews(sortType) {
            const chalNum = ${challenge.chal_num};
            $.ajax({
                url: `${pageContext.request.contextPath}/challenge/review/sorted`,
                type: 'GET',
                data: { chal_num: chalNum, sortType: sortType },
                success: function (reviews) {
                    let reviewHtml = '';
                    reviews.forEach(review => {
                        reviewHtml += `
                            <div class="review-item">
                                <img src="${pageContext.request.contextPath}/upload/${review.mem_photo || 'basicProfile.png'}" alt="프로필 사진" class="profile-img">
                                <div class="review-content">
                                    <div class="review-header">
                                        <span class="nickname">${review.mem_nick}</span>
                                        <span class="date">${review.chal_rev_date}</span>
                                        <span class="rating">${'★'.repeat(review.chal_rev_grade)}${'☆'.repeat(5 - review.chal_rev_grade)}</span>
                                    </div>
                                    <div class="review-text">${review.chal_rev_content}</div>
                                </div>
                            </div>`;
                    });
                    $('.review-list').html(reviewHtml);
                }
            });
        }

        $(document).ready(function () {
            $('#sort-select').change(function () {
                loadReviews($(this).val());
            });
        });
    </script>
</head>
<body>
<div class="review-list-container">
    <h2>[ ${challenge.chal_title} ] 후기</h2>
    <hr>
    <div class="review-summary">
        <div class="average-rating">
            <span class="rating-value">${averageRating}</span>
            <span class="rating-stars">
                <c:forEach begin="1" end="5" varStatus="status">
                    <c:choose>
                        <c:when test="${status.index <= averageRating}">
                            ★
                        </c:when>
                        <c:otherwise>
                            ☆
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </span>
        </div>
        <div class="review-count">
            후기 ${reviewCount}개
        </div>
        <div class="sort-select-container">
            <select id="sort-select">
                <option value="latest">최신순</option>
                <option value="rating">별점순</option>
            </select>
        </div>
    </div>
    <hr>
    <div class="review-list">
        <c:forEach var="review" items="${reviewList}">
            <div class="review-item">
                <c:choose>
                    <c:when test="${empty review.mem_photo}">
                        <img src="${pageContext.request.contextPath}/images/basicProfile.png" alt="프로필 사진" class="profile-img">
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/upload/${review.mem_photo}" alt="프로필 사진" class="profile-img">
                    </c:otherwise>
                </c:choose>
                <div class="review-content">
                    <div class="review-header">
                        <span class="nickname">${review.mem_nick}</span>
                        <span class="date">${review.chal_rev_date}</span>
                        <span class="rating">
                            <c:forEach begin="1" end="5" varStatus="status">
                                <c:choose>
                                    <c:when test="${status.index <= review.chal_rev_grade}">
                                        ★
                                    </c:when>
                                    <c:otherwise>
                                        ☆
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </span>
                    </div>
                    <div class="review-text">${review.chal_rev_content}</div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>
</body>
</html>