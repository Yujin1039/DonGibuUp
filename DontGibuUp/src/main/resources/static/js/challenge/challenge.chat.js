$(function() {
	let message_socket;//웹소켓 식별자

	if ($('#chatDetail').length > 0) {
		connectWebSocket();
	}

	/*----------------------
	 * 웹 소켓 연결
	 *----------------------*/
	function connectWebSocket() {
		message_socket = new WebSocket('ws://localhost:8000/message-ws');

		message_socket.onopen = function(evt) {
			console.log('채팅메시지 접속 : ' + $('#chatDetail').length);
			//$('#chatkDetail').length = 0이면 접속, 1이면 미접속
			if ($('#chatDetail').length == 1) {
				let message = JSON.stringify({ type: 'chatMessage', content: 'msg' });
				message_socket.send(message);
			}
		};
		//서버로부터 메시지를 받으면 호출되는 함수 지정
		message_socket.onmessage = function(evt) {
			//메시지 읽기
			let data = JSON.parse(evt.data);

			if (data.type === 'chatMessage') {
				if ($('#chatDetail').length == 1) {
					readChat();
				}
			} else if (data.type === 'updateReadCount') {
				updateReadCount(data.messageId, data.readCount);
			}
		};

		function updateReadCount(messageId, readCount) {
			let messageElement = document.querySelector(`div[data-message-id='${messageId}'] .read-count`);
			if (messageElement) {
				messageElement.textContent = `${readCount}`;
			}
		}

		message_socket.onclose = function(evt) {
			//소켓이 종료된 후 부가적인 작성이 있을 경우 명시
			console.log('chat close');
			window.close();
		}
	}

	/*----------------------
	 * 채팅하기
	 *----------------------*/
	//채팅 메시지 입력하기
	$('#chat_content').keydown(function(e) {
		if (e.keyCode == 13 && !e.shiftKey) {
			e.preventDefault();
			$('#chat_writeForm').submit();
		}
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

		$.ajax({
			url: 'chalWriteChat',
			type: 'post',
			data: formData,
			processData: false,
			contentType: false,
			success: function(param) {
				if (param.result == 'logout') {
					alert('로그인 후 채팅 가능합니다.');
					window.close();
				} else if (param.result = 'success') {
					//폼 초기화
					$('#chat_content').val('');
					$('#fileUpload').val('');
					$('.previewChatImage').hide();
					//웹 소켓 통신 - 1:1 채팅과 구분하기 위한 코드 작성 
					let message = JSON.stringify({ type: 'chatMessage', content: 'msg' });
					message_socket.send(message);
				} else {
					alert('채팅 메시지 등록 오류 발생');
					message_socket.close();
				}
			},
			error: function() {
				alert('네트워크 오류');
				message_socket.close();
			}
		});//end of ajax
		e.preventDefault();
	});

	//채팅 메시지 읽기
	function readChat() {
		$.ajax({
			url: 'chalReadChat',
			type: 'post',
			data: { chal_num: $('#chal_num').val() },
			dataType: 'json',
			success: function(param) {
				if (param.result == 'logout') {
					alert('로그인 후 채팅 가능합니다.');
					message_socket.close();
					window.location.href = `${contextPath}/member/login`;
				} else if (param.result == 'success') {
					console.log('success');
					//채팅창 UI 초기화
					$('#chatting_message').empty();

					let chat_date = '';

					//채팅메시지 보여지게 하기	
					$(param.chatList).each(function(index, item) {
						let output = '';
						console.log(item.chat_content);
						if (item.chat_content && item.chat_content.indexOf('@{common}') >= 0) {
							//챌린지 시작 메시지, 신규 입장 메시지
							output += '<div class="greeting-message">';
							output += item.chat_content.substring(0, item.chat_content.indexOf('@{common}'));
							output += '</div>';
						} else {
							//날짜 표시
							if (chat_date != item.chat_date.split(' ')[0]) {
								chat_date = item.chat_date.split(' ')[0];
								output += '<div class="date-position"><span>' + chat_date + '</span></div>';
							}

							//메시지 표시
							if (item.chat_content != null && item.chat_filename != null) {//메시지,이미지 모두 있는 경우
								output += readImageAndMessage(param, item);
							} else if (item.chat_content != null) {//메시지만 있는 경우
								output += readContent(param, item);
							} else {//이미지만 있는 경우
								output += readImage(param, item);
							}
						}
						//문서 객체에 추가
						$('#chatting_message').append(output);
						//스크롤을 하단에 위치시킴
						//$('#chatting_message').scrollTop($("#chatting_message")[0].scrollHeight);
						//새 창으로 띄웠을 때 스크롤 보정
						$('#chatting_message').animate({ scrollTop: 100 * param.chatList.length }, 10);
					});
				} else {
					alert('채팅 오류 발생');
					message_socket.close();
				}
			},
			error: function() {
				alert('채팅 통신 오류 발생!');
				message_socket.close();
			}
		});
	}
	//전송한 채팅 메시지 불러오기
	function readContent(param, item) {
		let sub_output = '';

		//일반 메시지
		if (item.mem_num != param.mem_num) {
			sub_output += `<div class="to-position" data-message-id="${item.chat_id}">`;
			sub_output += '<div class="space-photo">';
			if (item.mem_photo != null) {
				sub_output += `<img src="${contextPath}/upload/${item.mem_photo}" width="40" height="40" class="my-photo">`;
			} else {
				sub_output += `<img src="${contextPath}/images/basicProfile.png" width="40" height="40" class="my-photo">`;
			}
			sub_output += '</div><div class="space-clear"></div>';
			sub_output += `${item.mem_nick}<div class="space-message">`;
		} else {
			sub_output += '<div class="from-position">';
		}
		sub_output += '<div class="item">';
		sub_output += '<span>' + item.chat_content.replace(/\r\n/g, '<br>').replace(/\r/g, '<br>').replace(/\n/g, '<br>') + '</span>';
		sub_output += '</div>';
		//안 읽은 사람수, 작성 시간 추출
		sub_output += `<div class="item2">`;
		if (item.chat_readCount != 0) {
			sub_output += `<div class="read-count">${item.chat_readCount}</div>`;
		}
		sub_output += `<div>${item.chat_date.split(' ')[1]}</div>
						   </div>`;
		sub_output += '</div></div>';
		sub_output += '</div>';

		return sub_output;
	}

	//채팅시 전송한 이미지 불러오기
	function readImage(param, item) {
		let sub_output = '';
		if (item.mem_num != param.mem_num) {
			sub_output += '<div class="to-position">';
			sub_output += '<div class="space-photo">';
			if (item.mem_photo != null) {
				sub_output += `<img src="${contextPath}/upload/${item.mem_photo}" width="40" height="40" class="my-photo">`;
			} else {
				sub_output += `<img src="${contextPath}/images/basicProfile.png" width="40" height="40" class="my-photo">`;
			}
			sub_output += '</div><div class="space-clear"></div>';
			sub_output += `${item.mem_nick}<div class="space-message">`;
		} else {
			sub_output += '<div class="from-position">';
		}
		sub_output += '<div class="item">';
		sub_output += `<img src="${contextPath}/upload/${item.chat_filename}" style="max-width: 200px; max-height: 200px;">`;
		sub_output += '</div>';
		//안 읽은 사람수, 작성 시간 추출
		sub_output += `<div class="item2">
						    <div>${item.chat_readCount}</div>
						    <div>${item.chat_date.split(' ')[1]}</div>
					   </div>`;
		sub_output += '</div></div>';
		sub_output += '</div>';

		return sub_output;
	}

	//채팅시 이미지,메시지가 모두 존재하는 경우
	function readImageAndMessage(param, item) {
		let sub_output = '';
		if (item.mem_num != param.mem_num) {
			sub_output += '<div class="to-position">';
			sub_output += '<div class="space-photo">';
			if (item.mem_photo != null) {
				sub_output += `<img src="${contextPath}/upload/${item.mem_photo}" width="40" height="40" class="my-photo">`;
			} else {
				sub_output += `<img src="${contextPath}/images/basicProfile.png" width="40" height="40" class="my-photo">`;
			}
			sub_output += '</div><div class="space-clear"></div>';
			sub_output += `${item.mem_nick}<div class="space-message">`;
		} else {
			sub_output += '<div class="from-position">';
		}
		sub_output += '<div class="item">';
		sub_output += `<div><img src="${contextPath}/upload/${item.chat_filename}" style="max-width: 200px; max-height: 200px;"></div>`;
		sub_output += '<div>' + item.chat_content.replace(/\r\n/g, '<br>').replace(/\r/g, '<br>').replace(/\n/g, '<br>') + '</div>';
		sub_output += '</div>';
		//안 읽은 사람수, 작성 시간 추출
		sub_output += `<div class="item2">
						    <div>${item.chat_readCount}</div>
						    <div>${item.chat_date.split(' ')[1]}</div>
					   </div>`;
		sub_output += '</div></div>';
		sub_output += '</div>';

		return sub_output;
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
