document.addEventListener("DOMContentLoaded", function() {
    // 모든 sub-price 요소를 가져옵니다.
    document.querySelectorAll('.sub-price').forEach(function(element) {
        var price = parseInt(element.innerText, 10);
        if (!isNaN(price)) {
            // 가격을 천 단위로 구분하여 포맷합니다.
            element.innerText = price.toLocaleString() + '원';
        }
    });

    // 탭 전환 함수 정의
    function openTab(event, tabId) {
        // 모든 탭 콘텐츠 숨기기
        var tabContents = document.querySelectorAll('.tab-content');
        tabContents.forEach(function(content) {
            content.classList.remove('active');
        });

        // 모든 탭 버튼 비활성화
        var tabButtons = document.querySelectorAll('.tab-button');
        tabButtons.forEach(function(button) {
            button.classList.remove('active');
        });

        // 클릭된 탭 콘텐츠 표시
        document.getElementById(tabId).classList.add('active');

        // 클릭된 탭 버튼 활성화
        event.currentTarget.classList.add('active');
    }

    // openTab 함수를 글로벌 스코프에 노출
    window.openTab = openTab;
});
