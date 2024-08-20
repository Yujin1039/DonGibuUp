<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<br><br><br><br>
<div class="challenge-create-container-custom nanum">
    <form:form action="write" id="challenger_register" enctype="multipart/form-data" modelAttribute="challengeVO">
        	<div class="header">
	        <h5>챌린지 개설하기</h5>
	    </div>
	    <hr>
        <table class="form-table-custom">
            <tr>
                <td><form:label path="chal_public">챌린지 유형<span class="mandatory">*</span></form:label></td>
                <td>
                    <form:radiobutton path="chal_public" value="0" id="chal_public_0" class="custom-radio"/>
		            <label for="chal_public_0" class="custom-radio-label">단체</label>
		            <form:radiobutton path="chal_public" value="1" id="chal_public_1" class="custom-radio"/>
		            <label for="chal_public_1" class="custom-radio-label">개인</label>
                    <br>
                    <form:errors path="chal_public" cssClass="error-color"/>
                    <script type="text/javascript">
                    	let p0 = document.getElementById('chal_public_0');
                    	let p1 = document.getElementById('chal_public_1');
                    	
                    	p1.onclick = function(){
                    		let max = document.getElementById('chal_max');
                    		max.value = 1;
                    		max.setAttribute('disabled',true);                   	                   		
                    	};
                    	p0.onclick = function(){
                    		let max = document.getElementById('chal_max');
                    		max.value = '';
                    		max.removeAttribute('disabled');                   	                   		
                    	};
                    </script>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_type">카테고리 선택<span class="mandatory">*</span></form:label></td>
                <td>
                    <form:radiobutton path="chal_type" value="0" id="chal_type_0" class="custom-radio"/>
		            <label for="chal_type_0" class="custom-radio-label">운동</label>
		            <form:radiobutton path="chal_type" value="1" id="chal_type_1" class="custom-radio"/>
		            <label for="chal_type_1" class="custom-radio-label">식습관</label>
		            <form:radiobutton path="chal_type" value="2" id="chal_type_2" class="custom-radio"/>
		            <label for="chal_type_2" class="custom-radio-label">생활</label>
		            <form:radiobutton path="chal_type" value="3" id="chal_type_3" class="custom-radio"/>
		            <label for="chal_type_3" class="custom-radio-label">정서</label>
		            <form:radiobutton path="chal_type" value="4" id="chal_type_4" class="custom-radio"/>
		            <label for="chal_type_4" class="custom-radio-label">취미</label>
		            <form:radiobutton path="chal_type" value="5" id="chal_type_5" class="custom-radio"/>
		            <label for="chal_type_5" class="custom-radio-label">환경</label>
		            <form:radiobutton path="chal_type" value="6" id="chal_type_6" class="custom-radio"/>
		            <label for="chal_type_6" class="custom-radio-label">공부</label>
		            <form:radiobutton path="chal_type" value="7" id="chal_type_7" class="custom-radio"/>
		            <label for="chal_type_7" class="custom-radio-label">기타</label>
                    <br>
                    <form:errors path="chal_type" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_title">챌린지 제목<span class="mandatory">*</span></form:label></td>
                <td>
                    <form:input path="chal_title" placeholder="예) 1만보 걷기" class="input-text-custom"/>
                    <br>
                    <form:errors path="chal_title" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_content">챌린지 소개</form:label></td>
                <td>
                    <form:textarea path="chal_content" class="input-textarea-custom"/>
                    <br>
                    <form:errors path="chal_content" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_sdate">챌린지 시작일<span class="mandatory">*</span></form:label></td>
                <td>
                    <form:input type="date" path="chal_sdate" id="chal_sdate" class="input-date-custom"/>
                    <script type="text/javascript">
						let today = new Date();
						today.setDate(today.getDate() + 1);
						
						let year = today.getFullYear();
						let month = ('0'+(today.getMonth()+1)).slice(-2);
						let day = ('0'+today.getDate()).slice(-2);
						
						let minDate = year+'-'+month+'-'+day;
						
						document.getElementById('chal_sdate').setAttribute('min',minDate);
					</script>
                    <br>
                    <form:errors path="chal_sdate" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_period">챌린지 진행기간<span class="mandatory">*</span></form:label></td>
                <td>
                    <form:radiobutton path="chal_period" value="1" id="chal_period_1" class="custom-radio"/>
		            <label for="chal_period_1" class="custom-radio-label">1주 동안</label>
		            <form:radiobutton path="chal_period" value="2" id="chal_period_2" class="custom-radio"/>
		            <label for="chal_period_2" class="custom-radio-label">2주 동안</label>
		            <form:radiobutton path="chal_period" value="3" id="chal_period_3" class="custom-radio"/>
		            <label for="chal_period_3" class="custom-radio-label">3주 동안</label>
		            <form:radiobutton path="chal_period" value="4" id="chal_period_4" class="custom-radio"/>
		            <label for="chal_period_4" class="custom-radio-label">4주 동안</label>
                    <br>
                    <form:errors path="chal_period" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_freq">인증 빈도<span class="mandatory">*</span></form:label></td>
                <td>
                    <form:select path="chal_freq" class="input-select-custom">
                        <option disabled="disabled" selected>선택하세요</option>                                               
                        <form:option value="1">주1일</form:option>
                        <form:option value="2">주2일</form:option>
                        <form:option value="3">주3일</form:option>
                        <form:option value="4">주4일</form:option>
                        <form:option value="5">주5일</form:option>
                        <form:option value="6">주6일</form:option>
                        <form:option value="7">매일</form:option>
                    </form:select>
                    <br>
                    <form:errors path="chal_freq" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_verify">인증 방법<span class="mandatory">*</span></form:label></td>
                <td>
                    <form:textarea path="chal_verify" class="input-textarea-custom"/>
                    <br>
                    <form:errors path="chal_verify" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_fee">참가비<span class="mandatory">*</span></form:label></td>
                <td>
                    <form:input path="chal_fee" placeholder="1000원 이상 입력" class="input-text-custom"/>원
                    <br>
                    <form:errors path="chal_fee" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="chal_max">최대 모집 인원</form:label></td>
                <td>
                    <form:input path="chal_max" class="input-text-custom"/>명
                    <br>
                    <form:errors path="chal_max" cssClass="error-color"/>
                </td>
            </tr>
            <tr>
                <td><form:label path="upload">대표사진</form:label></td>
                <td>
                    <input type="file" name="upload" id="upload" onchange="previewImage(event)"/>
                    <div class="image-preview">
                        <img id="imagePreview" src="#" alt="Image Preview" style="display: none;"/>
                    </div>
                </td>
            </tr>
        </table>
        <br>
        	<div class="submit-button">
			<input type="button" value="취소" class="cancel-button" onclick="history.back();"/>
			<form:button class="submit-button-custom submit-button2">등록</form:button>
		</div>
    </form:form>
</div>
<br><br><br><br>
<script>
	const radios = document.querySelectorAll('.custom-radio');
	radios.forEach(radio => radio.checked = false);
	
    function previewImage(event) {
        var reader = new FileReader();
        reader.onload = function(){
            var output = document.getElementById('imagePreview');
            output.src = reader.result;
            output.style.display = 'block';
        };
        reader.readAsDataURL(event.target.files[0]);
    }
</script>
