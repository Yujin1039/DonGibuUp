package kr.spring.dbox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.dbox.dao.DboxMapper;
import kr.spring.dbox.vo.DboxBudgetVO;
import kr.spring.dbox.vo.DboxVO;

@Service
@Transactional
public class DboxServiceImpl implements DboxService {
	@Autowired
	DboxMapper dboxMapper;

	@Override
	public Long insertDbox(DboxVO dbox) {
		dbox.setDbox_num(dboxMapper.selectDboxNum());
		//Dbox 입력
		dboxMapper.insertDbox(dbox);
		//Dbox 모금액 사용 계획 입력
		for(DboxBudgetVO dboxBudget : dbox.getDboxBudgets()) {
			dboxMapper.insertDboxBudget(dboxBudget);
		}
		return dboxMapper.curDboxNum();
	}

}