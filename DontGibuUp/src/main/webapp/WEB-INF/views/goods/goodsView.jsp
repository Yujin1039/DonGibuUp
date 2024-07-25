<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<script src="https://cdn.iamport.kr/v1/iamport.js"></script>
<script>
    let itemName = "${goods.item_name}";
    let itemPrice = "${goods.item_price}";
    let buyerName = "${sessionScope.user.mem_nick}";
    let itemNum = "${goods.item_num}";
    let pageContextPath = "${pageContext.request.contextPath}";  
    
    function addToCart() {
        var form = document.getElementById('cartForm');
        form.submit();
    }
</script>

<div class="page-main">
    <h2>${goods.item_name}</h2>
    <ul class="detail-info">
        <li><img src="${pageContext.request.contextPath}${goods.item_photo}" width="300" height="300" class="my-photo2"></li>
        <li>재고: ${goods.item_stock}<br> 카테고리: ${goods.dcate_num}<br> 가격: ${goods.item_price}<br> 수량: <input type="number" id="quantity" name="quantity" value="1" min="1" max="${goods.item_stock}"></li>
    </ul>
    <div class="detail-content">${goods.item_detail}</div>
    <div>
        <input type="button" value="목록" onclick="location.href='list'">

        <form id="purchaseForm" method="post">
            <input type="hidden" name="merchantUid" value="${goods.item_num}_${System.currentTimeMillis()}">
            <input type="hidden" id="goods_do_price" name="amount" value="${goods.item_price}">
            <input type="button" value="구매하기" id="buy_now_button">
        </form>

        <form id="cartForm" action="${pageContext.request.contextPath}/cart/insert" method="post">
            <input type="hidden" name="item_num" value="${goods.item_num}">
            <c:if test="${not empty sessionScope.user}">
                <input type="hidden" name="mem_num" value="${sessionScope.user.mem_num}">
            </c:if>
            <label for="cart_quantity">수량:</label>
            <input type="number" id="cart_quantity" name="cart_quantity" value="1" min="1" max="${goods.item_stock}">
            <input type="button" value="장바구니 담기" onclick="addToCart()">
        </form>
    </div>
    <hr size="1" width="100%">
</div>

<!-- 모달 창 -->
<div class="modal fade" id="staticBackdrop" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="staticBackdropLabel">구매 정보</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <!-- 포인트 입력 필드 -->
                <div class="mb-3">
                    <label for="goods_do_point" class="form-label">사용할 포인트</label>
                    <input type="number" class="form-control calculate" id="goods_do_point" placeholder="사용할 포인트 입력">
                </div>
                <!-- 결제 금액 출력 필드 -->
                <div class="mb-3">
                    <label class="form-label">결제 금액</label>
                    <span id="pay_sum"></span>
                </div>
                <!-- 보유 포인트 -->
                <input type="hidden" id="mem_point" value="${sessionScope.user.mem_point}">
                <!-- 결제 금액 메시지 -->
                <div id="no"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                <button type="button" class="btn btn-primary" id="confirm_purchase_button">결제</button>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/goods/purchase.js"></script>
