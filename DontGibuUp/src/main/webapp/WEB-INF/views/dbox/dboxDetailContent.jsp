<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<img src="${pageContext.request.contextPath}/upload/dbox/${dbox.dbox_photo}"><br>

<button type="button" class="btn btn-dark active" onclick="location.href='${pageContext.request.contextPath}/dbox/${dbox.dbox_num}/content'">기부박스 소개</button>
<button type="button" class="btn btn-dark" onclick="location.href='${pageContext.request.contextPath}/dbox/${dbox.dbox_num}/donators'">기부현황</button>
<button type="button" class="btn btn-dark" onclick="location.href='${pageContext.request.contextPath}/dbox/${dbox.dbox_num}/news'">소식</button>
<br>
${dbox.dbox_content}<br>
<br>
<div style="border:1px solid;">
	<table>
		<c:forEach var="budget" items="${dboxBudget}">
		<tr>
			<td>
			${budget.dbox_bud_purpose}
			</td>
			<td>
			---------------${budget.dbox_bud_price}
			</td>
		</tr>
		</c:forEach>
	</table>
</div>
