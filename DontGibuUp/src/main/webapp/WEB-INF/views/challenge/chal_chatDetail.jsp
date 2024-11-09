<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<script src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js"></script>
<script>
 	let contextPath = '${pageContext.request.contextPath}';
 	let userMemNum = ${user.mem_num};
 	let totalCount = ${count};
 	let lastChatId = ${lastChat_id};
</script>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
<script src="${pageContext.request.contextPath}/js/challenge/challenge.chat.js"></script>
<div id="chatDetail">
	<div class="chatInfo">
		<span>${chal_room_name}</span> 
		<div class="dropdown">
			<span class="dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" id="chalAttend">(👁️ ${count}명)</span>
			<ul class="dropdown-menu" aria-labelledby="chalAttend">
				<c:forEach var="member" items="${list}">
				<li class="dropdown-item">
					<c:if test="${empty member.mem_photo}">
						<img src="${pageContext.request.contextPath}/images/basicProfile.png" width="20" height="20">
					</c:if>
					<c:if test="${!empty member.mem_photo}">
						<img src="${pageContext.request.contextPath}/upload/${member.mem_photo}" width="20" height="20">
					</c:if>
					${member.mem_nick}
					<c:if test="${member.mem_num != user.mem_num}">
						<span onclick="location.href='/cs/report?report_source=3&reported_mem_num=${member.mem_num}'" style='cursor:pointer' class="chatReport">🚨</span>					
					</c:if>
				</li>
			</c:forEach>
			</ul>
		</div>			
	</div>
	<div id="chatting_message">
		<c:set var="chat_date" value="${empty chat_date ? '' : chat_date}" />
		<c:forEach var="chat" items="${chatList}">
			<c:if test="${chat_date != chat.chat_date.split(' ')[0] && (chat.chat_filename || chat.chat_content.indexOf('@{common}') < 0)}">
				<div class="date-position">
					<c:set var="chat_date" value="${chat.chat_date.split(' ')[0]}" />           			
            		<span>${chat_date}</span>
				</div>				
			</c:if>
			<c:choose>
				<c:when test="${!empty chat.chat_content && chat.chat_content.indexOf('@{common}') >= 0}">
					<div class="greeting-message check-id" data-message-id="${chat.chat_id}">
						<c:set var="greets" value="${fn:split(chat.chat_content, '@{common}')}" />
						<c:set var="chat_date" value="${chat.chat_date}" />
						${greets[0]}
					</div>
				</c:when>
				<c:when test="${chat.mem_num != user.mem_num}">
					<div class="to-position check-id" data-message-id="${chat.chat_id}">
						<div class="space-photo">
						<c:if test="${!empty chat.mem_photo}">
							<img src="${contextPath}/upload/${chat.mem_photo}" width="40" height="40" class="my-photo">
						</c:if>
						<c:if test="${empty chat.mem_photo}">
							<img src="${contextPath}/images/basicProfile.png" width="40" height="40" class="my-photo">
						</c:if>
						</div>
						<div class="space-clear"></div>
						${chat.mem_nick}
						<div class="space-message">
							<div class="item">								
								<c:choose>									
									<c:when test="${empty chat.chat_filename}">
										<p>${fn:replace(fn:replace(fn:replace(chat.chat_content, '\\r\\n', '<br>'), '\\r', '<br>'), '\\n', '<br>')}</p>								
									</c:when>									
									<c:when test="${empty chat.chat_content}">
										<img src="${contextPath}/upload/${chat.chat_filename}" style="max-width: 200px; max-height: 200px;">
									</c:when>
									<c:otherwise>
										<img src="${contextPath}/upload/${chat.chat_filename}" style="max-width: 200px; max-height: 200px;">
										<p>${fn:replace(fn:replace(fn:replace(chat.chat_content, '\\r\\n', '<br>'), '\\r', '<br>'), '\\n', '<br>')}</p>
									</c:otherwise>
								</c:choose>
							</div>
							<div class="item2">
								<c:if test="${chat.chat_readCount != 0}">
									<div class="read-count">${chat.chat_readCount}</div>
								</c:if>
								<div>
									<c:set var="dateParts" value="${fn:split(chat.chat_date, ' ')}" /> ${dateParts[1]}
								</div>
							</div>
						</div>
					</div>
				</c:when>
				<c:otherwise>
					<div class="from-position check-id" data-message-id="${chat.chat_id}">
						<div class="item">
							<c:choose>									
								<c:when test="${empty chat.chat_filename}">
									<p>${fn:replace(fn:replace(fn:replace(chat.chat_content, '\\r\\n', '<br>'), '\\r', '<br>'), '\\n', '<br>')}</p>								
								</c:when>									
								<c:when test="${empty chat.chat_content}">
									<img src="${contextPath}/upload/${chat.chat_filename}" style="max-width: 200px; max-height: 200px;">
								</c:when>
								<c:otherwise>
									<img src="${contextPath}/upload/${chat.chat_filename}" style="max-width: 200px; max-height: 200px;">
									<p>${fn:replace(fn:replace(fn:replace(chat.chat_content, '\\r\\n', '<br>'), '\\r', '<br>'), '\\n', '<br>')}</p>
								</c:otherwise>
							</c:choose>
						</div>
						<div class="item2">
						<c:if test="${chat.chat_readCount != 0}">
							<div class="read-count">${chat.chat_readCount}</div>
						</c:if>
							<div>
								<c:set var="dateParts" value="${fn:split(chat.chat_date, ' ')}" /> ${dateParts[1]}
							</div>
						</div>
					</div>
				</c:otherwise>
			</c:choose>
		</c:forEach>
	</div>
	<div class="previewChatImage" style="display: none;">
		<img id="previewChatImage" src="" style="max-width: 200px; max-height:200px;">
	</div>
	<form id="chat_writeForm" enctype="multipart/form-data">
		<label for="fileUpload">
			<span class="upload_button">📁</span>
		</label>
        <input type="file" name="upload" class="file-input" id="fileUpload" accept="image/*">	
        <input type="hidden" name="chal_num" id="chal_num" value="${chal_num}">			
		<input type="hidden" name="mem_num" id="mem_num" value="${user.mem_num}">	
		<input type="hidden" name="mem_nick" id="mem_nick" value="${user.mem_nick}">
		<input type="hidden" name="mem_photo" id="mem_photo" value="${user.mem_photo}">
	  	<textarea rows="5" cols="30" name="chat_content" id="chat_content"></textarea>  
		<input type="submit" value="⩥" id="chal_submit">
	</form>
</div>