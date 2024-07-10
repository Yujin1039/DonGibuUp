package kr.spring.goods.service;

import java.util.List;
import java.util.Map;

import kr.spring.goods.vo.GoodsVO;

public interface GoodsService {
	public List<GoodsVO> selectList(Map<String,Object>map);
	public void insertGoods(GoodsVO goodsVO);
	public Integer selectRowCount(Map<String,Object> map);
	public void updateGoods(GoodsVO goods);

	
	//상품이미지 수정
}
