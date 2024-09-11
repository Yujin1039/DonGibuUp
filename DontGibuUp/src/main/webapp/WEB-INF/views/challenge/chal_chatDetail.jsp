<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<script>
 	let contextPath = '${pageContext.request.contextPath}';
</script>
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
	<div id="chatting_message"></div>
	<div class="previewChatImage" style="display: none;">
		<img id="previewChatImage" src="" style="max-width: 200px; max-height:200px;">
	</div>
	<form id="chat_writeForm" enctype="multipart/form-data">
		<label for="fileUpload">
			<span class="upload_button">📁</span>
		</label>
        <input type="file" name="upload" class="file-input" id="fileUpload" accept="image/*">			
		<input type="hidden" name="chal_num" id="chal_num" value="${chal_num}">	
	  	<textarea rows="5" cols="30" name="chat_content" id="chat_content"></textarea>  
		<input type="submit" value="⩥" id="chal_submit">
	</form>
</div>
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.min.js"></script>