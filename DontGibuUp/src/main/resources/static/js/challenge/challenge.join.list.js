let pageNum;

if (pageNum == null) {
	pageNum = 1;
}

$(document).on('click', '.chal_talk', function(e) {
	e.preventDefault();
	let chal_num = $(this).data('chal-num');
	$.ajax({
		url: 'joinChal_chat',
		type: 'post',
		data: { chal_num: chal_num },
		success: function(rsp) {
			if (rsp.result == 'logout') {
				alert('로그인 후 채팅 가능합니다.');				
			} else if (rsp.result == 'success') {
				window.open('chal_chatDetail', 'Popup', 'width=700,height=800');
			}
		},
		error: function() {
			alert('네트워크 오류');
		}
	});
});

function loadChallenges(month) {
	$.ajax({
		url: contextPath + `/challenge/join/list?month=${month}&status=${status}&pageNum=${pageNum}`,
		/*data: { status: status, month: month },*/
		success: function(response) {
			$('#challengeContainer').html($(response).find('#challengeContainer').html());
			updateEventListeners(); // Update event listeners after AJAX load
		},
		error: function(xhr, status, error) {
			console.error('AJAX Error:', status, error);
		}
	});
}

function deleteChallenge(chalJoiNum, isLeader) {
	if (isLeader) {
		if (!confirm('챌린지를 삭제하시겠습니까?')) {
			return;
		}
	} else {
		if (!confirm('챌린지를 취소하시겠습니까?')) {
			return;
		}
	}

	$.ajax({
		url: 'delete',
		type: 'POST',
		data: JSON.stringify({
			chal_joi_num: chalJoiNum,
			isLeader: isLeader
		}),
		contentType: 'application/json; charset=utf-8',
		success: function(response) {
			if (isLeader) {
				alert('챌린지가 삭제되었습니다.');
			} else {
				alert('챌린지가 취소되었습니다.');
			}			
			location.reload(); // 페이지 새로고침
		},
		error: function(xhr, status, error) {
			console.error('AJAX Error:', status, error);
			alert('챌린지 취소 중 오류가 발생했습니다.');
		}
	});
}

function updateEventListeners() {
	$('#prevMonth').off('click').on('click', function() {
		const currentMonth = $('#currentMonth').data('month');
		const date = new Date(currentMonth + "-01");
		date.setMonth(date.getMonth() - 1);
		const newMonth = date.toISOString().slice(0, 7);
		$('#currentMonth').data('month', newMonth);
		loadChallenges(newMonth);
	});

	$('#nextMonth').off('click').on('click', function() {
		const currentMonth = $('#currentMonth').data('month');
		const date = new Date(currentMonth + "-01");
		date.setMonth(date.getMonth() + 1);
		const newMonth = date.toISOString().slice(0, 7);
		$('#currentMonth').data('month', newMonth);
		loadChallenges(newMonth);
	});
}

updateEventListeners(); // Initial call to set event listeners

// Make deleteChallenge function globally accessible
window.deleteChallenge = deleteChallenge;