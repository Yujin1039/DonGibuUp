	//초기 데이터(나의 인증 현황) 호출
	getVerify(1);
	
	//나의 인증 현황 클릭 이벤트
 	$('#verify_my_states').on('click',function(){
 		$('#verify_content').empty();
 		chal_joi_num = user_joi_num;
		getVerify(1);
	});
	
 	//인증 현황 페이지 버튼 클릭 이벤트(본인)
	$(document).on('click','.pageBtn.verifytrue',function(){
		$('#verify_content').empty();
		//페이지 번호를 읽어들임
		currentPage = $(this).attr('data-page');
		//목록 호출
		getVerify(currentPage);
	});
	
 	//인증 현황 페이지 버튼 클릭 이벤트(타인)
	$(document).on('click','.pageBtn.verifyfalse',function(){
		$('#verify_content').empty();
		//페이지 번호를 읽어들임
		currentPage = $(this).attr('data-page');
		//목록 호출
		getVerify(currentPage);
	});
 	
 	//참가자 목록 클릭 이벤트
	$('#join_member_list').on('click',function(){
		$('#verify_content').empty();
		chal_joi_num = user_joi_num;
		getItems(1);
	});
	
 	//참가자 목록 페이지 버튼 클릭 이벤트
	$(document).on('click','.pageBtn.join',function(){
		$('#verify_content').empty();
		//페이지 번호를 읽어들임
		currentPage = $(this).attr('data-page');
		//목록 호출
		getItems(currentPage);
	});
 	
	//타인의 인증 현황 클릭 이벤트
 	$(document).on('click','.each_verify_list',function(e){
 		e.preventDefault();
 		$('#verify_content').empty();
		chal_joi_num = $(this).attr('href').split('chal_joi_num=')[1];
 		getVerify(1);
 	});
	
	//타인의 인증 현황 돌아가기 이벤트
	$(document).on('click','.others_verify_list',function(e){
		e.preventDefault();
		$('#verify_content').empty();
		chal_joi_num = user_joi_num;
		getItems(1);
	});
	
	$(document).ready(function() {
	    // 이미지 클릭 이벤트
	    $(document).on('click', '.verify-photo', function() {
	        var src = $(this).attr('data-photo-src');
	        $('#modalImage').attr('src', src);
	        $('#photoModal').css('display', 'block');
	    });
	
	    // 모달 닫기 이벤트
	    $(document).on('click', '.custom-close', function() {
	        $('#photoModal').css('display', 'none');
	    });
	
	    // 모달 외부 클릭 시 닫기
	    $(window).on('click', function(event) {
	        if ($(event.target).is('#photoModal')) {
	            $('#photoModal').css('display', 'none');
	        }
	    });
	});
		
 	//참가자 목록을 불러오는 메서드
	function getItems(currentPage){
		$.ajax({
			url:'joinMemberList',
			type:'get',
			data:{chal_num:chal_num,chal_joi_num:chal_joi_num,pageNum:currentPage,rowCount:rowCount},
			dataType:'json',
			success:function(param){
				let output = '';
				output += '<div class="memberList">';
				$(param.list).each(function(index,item){
					output += '<div class="joinMem_container">';
					if (item.mem_photo) {
						output += '<img class="joinMem responsive-image" src="' + contextPath + '/upload/' + item.mem_photo + '" width="40" height="40">'; //회원 프로필
					} else {
						output += '<img class="joinMem responsive-image" src="' + contextPath + '/images/basicProfile.png" width="40" height="40">'; //회원 프로필 기본 이미지
					}
					output += '<span class="joinMem">';
					output += item.mem_nick;
					output += '</span>';
					output += '<span class="joinMem arrow">';
					output += '<a href="verifyMemberList?chal_joi_num='+item.chal_joi_num+'" class="each_verify_list"> > </a>';
					output += '</span>';
					output += '</div>';
				});	
				output += '</div>';
				$('#verify_content').append(output);				
				$('#verify_content').append(setPage(param.count,'join'));												
			},
			error:function(){
				alert('네트워크 오류');
			}
		});	
	}
	
 	//참가자 인증 현황을 불러오는 메서드
	function getVerify(currentPage){
		$.ajax({
			url:'verifyMemberList',
			type:'get',
			data:{chal_joi_num:chal_joi_num,pageNum:currentPage,rowCount:rowCount},
			dataType:'json',
			success:function(param){
				let output = '';
				let now = new Date();
				now.setHours(0, 0, 0, 0);
				if(!param.isUser){
					output += `<div class="memberInfo">`;
					if (param.member.mem_photo) {
						output += '<img class="joinMem responsive-image" src="' + contextPath + '/upload/' + param.member.mem_photo + '" width="40" height="40">'; //회원 프로필
					} else {
						output += '<img class="joinMem responsive-image" src="' + contextPath + '/images/basicProfile.png" width="40" height="40">'; //회원 프로필 기본 이미지
					}
					output += `<span class="joinMem">${param.member.mem_nick}</span>
										 <a href="joinMemberList" class="others_verify_list"> > </a>
										 </div>
					`;							
				}
				if(param.count == 0){
					output += '<div>표시할 정보가 없습니다.</div>';
				}else{					
					$(param.list).each(function(index,item){
						let reg_date = new Date(item.chal_reg_date);
						reg_date.setHours(0,0,0,0);						
						output += '<div class="challenge-verify-card">';
						output += '<img src="'+contextPath+'/upload/'+item.chal_ver_photo+'" width="100" height="50" class="responsive-image verify-photo" data-photo-src="'+contextPath+'/upload/'+item.chal_ver_photo+'">'; 
						output += '<div class="content">';
						output += '<div class="date-status">';
						output += '<span class="date">'+item.chal_reg_date+'</span>';	
						if(item.chal_ver_status == 0){
							output += `<span class="status success">성공</span>`;
						}else if(item.chal_ver_status == 1){
							output += `<span class="status failure">실패</span>`;
						}
						if(item.chal_content){
							output += '<div id="content-'+item.chal_ver_num+'" class="comment">'+item.chal_content+'</div>';
						}else{
							output += '<div id="content-'+item.chal_ver_num+'" class="comment"></div>';
						}	
						output += '</div>';
						output += '</div>';
						if(!param.isUser){
							output += '<button type="button">제보</button>';
						}else{
							//수정 폼
							output += '<div id="edit-form-'+item.chal_ver_num+'" class="edit-form" style="display: none;">';
							output += '<textarea id="textarea-'+item.chal_ver_num+'">'+item.chal_content+'</textarea>';
							output += '</div>';
							//수정/삭제 버튼 생성							
							if(reg_date.getTime() == now.getTime()){								
								output += `<button id="edit-button-${item.chal_ver_num}"
									onclick="toggleEditSave(${item.chal_ver_num})">수정</button>`;	
								output += `<button onclick="deleteVerify(${item.chal_ver_num})">삭제</button>`;
							}														
						}						
						output += '</div>';								
					});					
				}
				$('#verify_content').html(output);
				$('#verify_content').append(setPage(param.count,'verify'+param.isUser));
			},
			error:function(){
				alert('네트워크 오류');
			}
		});
	}
 	//페이징 처리를 하는 메서드
	function setPage(totalItem,method){
		$('.paging-btn').empty();
		
		if(totalItem == 0){
			return;
		}
		
		let totalPage = Math.ceil(totalItem/rowCount);
		
		if(currentPage == undefined || currentPage == ''){
			currentPage = 1;
		}
		
		//현재 페이지가 전체 페이지 수보다 크면 전체 페이지로 설정
		if(currentPage > totalPage){
			currentPage = totalPage;
		}
		
		//시작 페이지와 마지막 페이지 값 구하기
		var startPage = Math.floor((currentPage-1)/pageSize)*pageSize + 1;
		var endPage = startPage + pageSize - 1;
		
		//마지막 페이지가 전체 페이지 수보다 크면 전체 페이지 수로 설정
		if(endPage > totalPage){
			endPage = totalPage;
		}
		
		let pageInfo = '';
		
		if(startPage>pageSize){
			pageInfo += '<span class="pageBtn '+method+'" data-page='+(startPage-1)+'>[이전]</span>';
		}

		for(var i=startPage;i<=endPage;i++){
			pageInfo += '<span class="pageBtn '+method+'" data-page='+i+'>'+i+'</span>';
		}

		if(endPage < totalPage){
			pageInfo += '<span class="pageBtn '+method+'" data-page='+(startPage+pageSize)+'>[다음]</span>';
		}

		return pageInfo;
	}
	
	function toggleEditSave(chal_ver_num) {
		const editButton = $('#edit-button-' + chal_ver_num);
		const isEditing = editButton.text() === '저장';

		if (isEditing) {
			const newContent = $('#textarea-' + chal_ver_num).val();
			$.ajax({
				url: contextPath + '/challenge/verify/update',
				type: 'POST',
				data: { chal_ver_num: chal_ver_num, chal_content: newContent },
				success: function(response) {
					$('#content-' + chal_ver_num).text(newContent).show();
					$('#edit-form-' + chal_ver_num).hide();
					editButton.text('수정');
				},
				error: function(xhr, status, error) {
					alert('인증 내용 수정 중 오류가 발생했습니다.');
				}
			});
		} else {
			$.ajax({
				url: contextPath + '/challenge/verify/detail',
				type: 'GET',
				data: { chal_ver_num: chal_ver_num },
				success: function(response) {
					$('#content-' + chal_ver_num).hide();
					$('#edit-form-' + chal_ver_num).show();
					editButton.text('저장');
				},
				error: function(xhr, status, error) {
					alert('수정 폼을 로드하는 중 오류가 발생했습니다.');
				}
			});
		}
	}

	function deleteVerify(chal_ver_num) {
		if (confirm('인증을 삭제하시겠습니까?')) {
			$.ajax({
				url: contextPath + '/challenge/verify/delete',
				type: 'POST',
				data: { chal_ver_num: chal_ver_num },
				success: function(response) {
					alert('인증이 성공적으로 삭제되었습니다.');
					location.reload();  // 페이지를 새로고침하여 삭제된 내용을 반영
				},
				error: function(xhr, status, error) {
					alert('인증 삭제 중 오류가 발생했습니다.');
				}
			});
		}
	}
