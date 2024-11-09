$(function() {
	let chal_num = $('#chal_num').val();
	let last_chat_date = $('.date-position:last span').text();
	let latest_chat_id = $('.check-id:last').data('message-id');
	let message_socket;//웹소켓 식별자
	let stompClient;

	// 스크롤 제일 아래로 내리기
	$('#chatting_message').scrollTop($('#chatting_message')[0].scrollHeight);

	if ($('#chatDetail').length > 0) {
		connectWebSocket();
	}

	/*----------------------
	 * 웹 소켓 연결
	 *----------------------*/
	function connectWebSocket() {
		// 웹소켓 연결
		message_socket = new SockJS(`/ws-chat`);
		stompClient = Stomp.over(message_socket);

		stompClient.connect({}, () => {
			console.log('latest_chat_id : ' + latest_chat_id + ', lastChatId: ' + lastChatId);
			// 안 읽은 사람수 갱신 요청
			stompClient.send(`/pub/update-unread/${chal_num}/${userMemNum}/${lastChatId}/${latest_chat_id}`, {});
			// 새 채팅 불러오기
			stompClient.subscribe(`/sub/challenge/${chal_num}`, (message) => {
				console.log('채팅 메시지 수신됨');
				const chatMessage = JSON.parse(message.body);
				// 채팅 메시지 UI에 추가
				addChatMessage(userMemNum, chatMessage);
			});
			// 안 읽은 사람수 갱신 수신
			stompClient.subscribe(`/sub/update/${chal_num}`, (message) => {
				console.log('갱신 메시지 수신됨');
				const countInfo = JSON.parse(message.body);
				// UI 반영
				if (countInfo.mem_num != userMemNum) updateUnreadStatus(countInfo);
			});
		});

		// 연결이 성공한 후에만 disconnect 호출
		$(window).on('beforeunload', function() {
			if (stompClient) {
				stompClient.disconnect(() => {
					console.log('Chat disconnected');
					alert('chat close');
					window.close();
				});
			}
		});
	}

	/*----------------------
	 * 채팅하기
	 *----------------------*/
	//채팅 메시지 전송 1
	$('#chat_content').keydown(function(e) {
		if (e.keyCode == 13 && !e.shiftKey) {
			e.preventDefault();
			$('#chat_writeForm').trigger('submit');
		}
	});

	//채팅 메시지 전송 2
	$('#chal_submit').click(function(e) {
		e.preventDefault();
		$('#chat_writeForm').trigger('submit');
	});


	//채팅 메시지,파일 전송하기
	$('#chat_writeForm').submit(function(e) {
		if ($('#chat_content').val().trim() == '' && $('#fileUpload')[0].files.length == 0) {
			alert('전송할 내용이 없습니다.');
			$('#chat_content').val('').focus();
			return false;
		}
		if ($('#chat_content').val().length > 300) {
			alert('작성가능 글자수(300자)를 초과했습니다.');
			$('#chat_content').focus();
			return false;
		}
		//작성 글자수/300으로 남은 글자 수 표시하기

		let formData = new FormData(this);
		// 현재 날짜와 시간을 가져오기
		const currentDate = new Date();

		// 각 구성 요소를 가져오기
		const year = currentDate.getFullYear();
		const month = currentDate.getMonth() + 1;
		const day = currentDate.getDate();
		const hours = currentDate.getHours();
		const minutes = currentDate.getMinutes();
		const seconds = currentDate.getSeconds();

		// 날짜와 시간을 문자열로 포맷팅
		const formattedDate
			= `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
		formData.append('chat_date', formattedDate);

		let formObject = {};
		const uploadPromises = [];
		formData.forEach((value, key) => {
			if (key === 'upload') {
				if (value.size === 0) {
					return;
				} else {
					let uploadData = new FormData();
					uploadData.append(key, value);

					const uploadPromise = sendDataToServer(uploadData)
						.then(filename => {
							console.log('filename: ' + filename);
							if (filename != null) {
								formObject['chat_filename'] = filename;
							} else {
								alert('파일 전송이 실패했으므로 재전송하시기 바랍니다.');
								return;
							}
						});
					uploadPromises.push(uploadPromise);
				}
			} else {
				formObject[key] = value;
			}
		});

		Promise.all(uploadPromises).then(() => {
			console.log('업로드 완료:', formObject);
			//웹 소켓 통신
			stompClient.send(`/pub/sendChat`, {}, JSON.stringify(formObject));
			// DB에 채팅 반영
			$.ajax({
				url: 'chalWriteChat',
				type: 'post',
				data: formObject,
				success: function(param) {
					if (param.result == 'logout') {
						alert('로그인 후 채팅 가능합니다.');
						window.close();
					} else if (param.result = 'success') {
						//폼 초기화
						$('#chat_content').val('');
						$('#fileUpload').val('');
						$('.previewChatImage').hide();
					} else {
						alert('채팅 메시지 등록 오류 발생');
						stompClient.disconnect();
					}
				},
				error: function() {
					alert('네트워크 오류');
					stompClient.disconnect();
				}
			});//end of ajax
		});

		e.preventDefault();
	});

	// 서버에 이미지 업로드, 이미지 URL 반환
	async function sendDataToServer(formData) {
		console.log('sendDataToServer 메서드 실행');
		try {
			const response = await fetch('/challenge/uploadChatImage', {
				method: 'POST',
				body: formData
			});
			const filename = await response.text();
			console.log('filename: ' + filename);
			return filename; // 함수에서 filename을 반환
		} catch (error) {
			console.error('error: ' + error);
			return null; // 에러 발생 시 null 반환
		}
	}

	// 공통 메시지 처리 함수
	function addChatMessage(userMemNum, chatMessage) {
		let output = '';
		console.log(chatMessage);
		// 날짜 표시
		if (last_chat_date != chatMessage.chat_date.split(' ')[0]) {
			last_chat_date = chatMessage.chat_date.split(' ')[0];
			output += `<div class="date-position"><span>${last_chat_date}</span></div>`;
		}

		// 메시지 추가
		if (chatMessage.mem_num != userMemNum) {
			output += `<div class="to-position check-id" data-message-id="${(lastChatId + 1)}">
							<div class="space-photo">`;
			if (chatMessage.mem_photo) {
				output += `<img src="${contextPath}/upload/${chatMessage.mem_photo}" width="40" height="40" class="my-photo">`;
			} else {
				output += `<img src="${contextPath}/images/basicProfile.png" width="40" height="40" class="my-photo">`;
			}
			output += '</div><div class="space-clear"></div>';
			output += `${chatMessage.mem_nick}<div class="space-message">`;
		} else {
			output += `<div class="from-position check-id" data-message-id="${(lastChatId + 1)}">`;
		}

		// 실제 메시지
		output += '<div class="item">';
		if (chatMessage.chat_filename != null && chatMessage.chat_content != null) {
			output += `<img src="${contextPath}/upload/${chatMessage.chat_filename}" style="max-width: 200px; max-height: 200px;">`;
			output += `<p>${chatMessage.chat_content.replace(/\r\n/g, '<br>').replace(/\r/g, '<br>').replace(/\n/g, '<br>')}</p>`;
		} else if (chatMessage.chat_content != null) {
			output += `<p>${chatMessage.chat_content.replace(/\r\n/g, '<br>').replace(/\r/g, '<br>').replace(/\n/g, '<br>')}</p>`;
		} else {
			output += `<img src="${contextPath}/upload/${chatMessage.chat_filename}" style="max-width: 200px; max-height: 200px;">`;
		}
		output += '</div>';

		//안 읽은 사람수, 작성 시간 추출
		output += `<div class="item2">`;
		if (chatMessage.chat_readCount != totalCount) {
			output += `<div class="read-count">${totalCount - chatMessage.chat_readCount}</div>`;
		}
		output += `<div>${chatMessage.chat_date.split(' ')[1]}</div>
						   </div>`;
		output += '</div></div>';
		output += '</div>';

		// 문서 객체에 추가
		$('#chatting_message').append(output);
		// 스크롤을 하단에 위치시킴
		$('#chatting_message').animate({ scrollTop: $('#chatting_message')[0].scrollHeight }, 10);
		//$('#chatting_message').animate({ scrollTop: 100 * chatMessage.length }, 10);
	}

	function updateUnreadStatus(info) {
		// 읽지 않은 메시지 수 업데이트
		console.log('updateUnreadStatus: ' + info);
		$('.check-id').filter(function() {
			const messageId = parseInt($(this).data('message-id'), 10);
			console.log('messageId: ' + messageId + ', last_chat_id: ' + info.last_chat_id + ', latest_chat_id: ' + info.latest_chat_id);
			return messageId > info.last_chat_id && messageId <= info.latest_chat_id;
		}).each(function() {
			const $readCount = $(this).find('.read-count');

			if ($readCount.length) { // .read-count 요소가 존재하는 경우
				const currentCount = parseInt($readCount.text(), 10);
				if (currentCount > 1) {
					$readCount.text(currentCount - 1);
				} else {
					// .read-count 요소만 삭제
					$readCount.remove();
				}
			}
		});
	}

	/*----------------------
	 * 전송할 이미지 미리보기
	 *----------------------*/
	$('#fileUpload').on('change', function(event) {
		let file = event.target.files[0];

		if (file && file.type.startsWith('image/')) {
			let reader = new FileReader();

			reader.onload = function(e) {
				$('#previewChatImage').attr('src', e.target.result);
				$('.previewChatImage').show();
			};

			reader.readAsDataURL(file);
		} else {
			$('.previewChatImage').hide();
		}
	});

});
