package com.osh.rvs.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AcceptFactMapper {

	// 受理
	public List<Map<String, String>> getAcceptResult();

	// 消毒灭菌
	public List<Map<String, Object>> getQuatationResult();

	// 在途
	public Integer getNotreachResult();

	// 等待报价
	public Integer getQuatationWaitingResult(int search_target);

	// 等待投线
	public List<Map<String, Object>> getInlineWaitingResult();

	public Integer getUnrepairOrderResult();

	// 投线
	public List<Map<String, Object>> getInlineResult();

	// 本周到货登录实绩
	public Map<String, BigDecimal> getReceptionInWeek(Date date);

	// 本周完成出货实绩
	public Map<String, BigDecimal> getShippingInWeek(Date date);

	public Integer getWorkingSterilization();

	// 报价完成
	public List<Map<String, Object>> searchQuatationResult();

	// 等待消毒
	public Integer getDisinfectionWaitingResult();

	// 等待灭菌
	public Integer getSterilizeWaitingResult();

	// 本周到货登录实绩RC分布
	public Map<String, BigDecimal> getReceptionRCInWeek(Date date);

	// 本周完成出货实绩RC分布
	public Map<String, BigDecimal> getShippingRCInWeek(Date date);
}
