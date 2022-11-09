package com.osh.rvs.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.ServiceRepairManageEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.AllPositionsMapper;
import com.osh.rvs.mapper.ServiceRepairManageMapper;

import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.message.ApplicationMessage;

public class AllPositionsService {

	public List<Map<String, Object>> getPositions(SqlSession conn) {
		AllPositionsMapper dao = conn.getMapper(AllPositionsMapper.class);
		return dao.getHeapAndWork();
	}

	public Map<String, Map<String, String>> makeOnjs(Map<String, Object> listResponse, List<Map<String, Object>> objs, SqlSession conn) {
		Map<String, Map<String, String>> ret = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> retDivided = new HashMap<String, Map<String, String>>();
		String quotationCount = "0";
		for (Map<String, Object> obj : objs) {
			String position = (String) obj.get("PROCESS_CODE");

			String[] keys = null;
			if (position == null) continue;
			String overline = "-";
			if (position.startsWith("1") || position.startsWith("6")) {
				overline = getOverLine(position, null);
				if ("0".equals(overline))
					if ("111".equals(position) || "131".equals(position)) {
						overline = "30";
					} else if ("121".equals(position) || "141".equals(position) || "151".equals(position)) {
						overline = "20";
					} else if ("161".equals(position)) {
						overline = "8";
					} else if ("171".equals(position)) {
						overline = "10";
					}
				keys = new String[]{position};
			} else {
				Long section = (Long) obj.get("section_id");
				overline = getOverLine(position, section);
				if (section == null) {
					keys = new String[]{position+ "_1", position+ "_2"};
				} else if (1L == section) {
					keys = new String[]{position+ "_1"};
				} else if (3L == section) {
					keys = new String[]{position+ "_2"};
				} else if (9L == section || 10L == section) {
					keys = new String[]{position+ "_9"};
				} else {
					keys = new String[]{position+ "_3"};
				}
			}

			for (String key : keys) {
				Map<String, String> record = new HashMap<String, String>();
				String today_work = toString(obj.get("today_work"), "0");
				String heaps = toString(obj.get("w_count"), "0");
				record.put("heaps", heaps);
//				if (position.startsWith("2") || position.startsWith("3") || position.startsWith("4") || position.startsWith("5")) {
//					record.put("l_heaps", toString(obj.get("l_count"), "0"));
//					record.put("h_heaps", toString(obj.get("h_count"), "0"));
//				}
				if ("111".equals(key)) {
					// 取得在途 TODO
					// 当日受理
					listResponse.put("acceptCount", today_work);
				} else if ("151".equals(key) || "161".equals(key)) {
					quotationCount = stringAddCalc(quotationCount, today_work);
				}
				checkStatus(position, record, overline, heaps, obj.get("alarm_messsage_id"), conn);
				record.put("overline", overline);
				record.put("today_work", today_work);
				record.put("avg_cost", toString(obj.get("avg_cost"), "-"));
				record.put("countm", toString(obj.get("countm"), ""));
				if (ret.containsKey(key)) {
					// TODO merge
				} else {
					ret.put(key, record);
					boolean divisionFlg = Boolean.TRUE.equals(obj.get("light_division_flg"));
					if (divisionFlg) { // 分线
						Map<String, String> recordA = new HashMap<String, String>();
						Map<String, String> recordB = new HashMap<String, String>();
						recordA.put("heaps", toString(obj.get("h_count"), "0"));
						recordB.put("heaps", toString(obj.get("l_count"), "0"));
						recordA.put("overline", overline);
						recordB.put("overline", overline);
						recordA.put("today_work", "0");
						recordA.put("avg_cost", "-");
						recordB.put("today_work", "0");
						recordB.put("avg_cost", "-");

						checkStatus(position, recordA, overline, recordA.get("heaps"), record.get("alarm_messsage_id"), conn);
						checkStatus(position, recordB, overline, recordB.get("heaps"), record.get("alarm_messsage_id"), conn);

						retDivided.put(key+"A", recordA);
						retDivided.put(key+"B", recordB);
					}
				}
			}
		}

		// 查询独立工位 301 TODO 601
		ServiceRepairManageMapper srmMapper = conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		entity.setQa_referee_time_start(null); // 今日
		entity.setService_repair_flg(1);

		Map<String, String> recordSolo = new HashMap<String, String>();
		recordSolo.put("heaps", "" + (srmMapper.searchAnalyseWaitting() + srmMapper.searchCurrentData()));
		recordSolo.put("today_work", "" + srmMapper.searchServiceRepair(entity));
		recordSolo.put("avg_cost", "-");
		recordSolo.put("status", "free");
		String overline601 = getOverLine("601", null);
		recordSolo.put("overline", overline601);
		BigDecimal bdHeaps = new BigDecimal(recordSolo.get("heaps"));
		if (overline601 != null 
				&& bdHeaps.compareTo(new BigDecimal(overline601)) > 0 ) {
			recordSolo.put("status", "over");
		} else if (bdHeaps.compareTo(BigDecimal.ZERO) > 0) {
			recordSolo.put("status", "noml");
		}
		ret.put("601", recordSolo);

		// 补充没有查询到信息的工位
		for (Map<String, Object> obj : objs) {
			String position = (String) obj.get("PROCESS_CODE");
			if (position.startsWith("2") || position.startsWith("3") || position.startsWith("4") ) {
				if (!ret.containsKey(position + "_1")) {
					Map<String, String> record = new HashMap<String, String>();
					record.put("heaps", "0");
					record.put("today_work", "0");
					record.put("avg_cost", "-");
					record.put("status", "free");
					record.put("overline", getOverLine(position, 1L));
					ret.put(position + "_1", record);
				}

				if (!ret.containsKey(position + "_2")) {
					Map<String, String> record = new HashMap<String, String>();
					record.put("heaps", "0");
					record.put("today_work", "0");
					record.put("avg_cost", "-");
					record.put("status", "free");
					record.put("overline", getOverLine(position, 3L));
					ret.put(position + "_2", record);
				}

				if (!ret.containsKey(position + "_3")) {
					Map<String, String> record = new HashMap<String, String>();
					record.put("heaps", "0");
					record.put("today_work", "0");
					record.put("avg_cost", "-");
					record.put("status", "free");
					record.put("overline", getOverLine(position, 12L));
					ret.put(position + "_3", record);
				}
			}
		}

		// 分线工位
		if (!retDivided.isEmpty()) {
			AllPositionsMapper dao = conn.getMapper(AllPositionsMapper.class);
			List<Map<String, Object>> outcomeDivided = dao.getOutcomeDivided();
			for (Map<String, Object> obj : outcomeDivided) {
				String position = (String) obj.get("PROCESS_CODE");
				Long section = (Long) obj.get("section_id");
				Long px = (Long) obj.get("px");
				String key = position + "_" + section + (px == 1 ? "B" : "A");
				if (retDivided.containsKey(key)) {
					Map<String, String> record = retDivided.get(key);
					String today_work = toString(obj.get("today_work"), "0");
					record.put("today_work", today_work);
					record.put("avg_cost", toString(obj.get("avg_cost"), "-"));
				}
			}
			ret.putAll(retDivided);
		}

		listResponse.put("quotationCount", quotationCount);
		return ret;
	}

	private String stringAddCalc(String add1, String add2) {
		if (add1 == null || add2 == null) return "-";
		try {
			int iadd1 = Integer.parseInt(add1);
			int iadd2 = Integer.parseInt(add2);
			return "" + (iadd1 + iadd2);
		} catch(Exception e) {
			return "-";
		}
	}

	private void checkStatus(String position, Map<String, String> record, String overline, String heaps, Object alarm_messsage_id, SqlSession conn) {
		if (alarm_messsage_id != null) {
			record.put("status", "erro");
			// 取得错误信息
			AllPositionsMapper dao = conn.getMapper(AllPositionsMapper.class);
			Map<String, Object> messageInfo = dao.getAlarmMessage(alarm_messsage_id.toString());
			String messageText = "";
			Long reason = (Long) messageInfo.get("reason");
			if (reason != null && reason == 6) {
				messageText = messageInfo.get("material_name") + "品保不通过";
			} else if (reason != null && reason == 1) {
				messageText = messageInfo.get("material_name") + "在" + position + "工位";
				Long pReason = (Long) messageInfo.get("p_reason");
				if (pReason != null && pReason < 5) {
					messageText += CodeListUtils.getValue("break_reason", "0" + pReason);
				} else {
					String breakMessage = ApplicationMessage.WARNING_MESSAGES.getMessage("break."+ position + "." + pReason);
					if (breakMessage != null) {
						messageText += breakMessage;
					}
				}
				if (messageInfo.get("comments") != null) {
					messageText += messageInfo.get("comments");
				}
			}
			record.put("alarm", messageText);
				
			return;
		}
		try {
			int iheap = Integer.parseInt(heaps);
			if (iheap == 0) {
				record.put("status", "free");
				return;
			}

			int ioverline = Integer.parseInt(overline);
			if (ioverline !=0 && iheap > ioverline) {
				record.put("status", "over");
			} else {
				record.put("status", "noml");
			}
		} catch(Exception e) {
		}
	}

	private String getOverLine(String process_code, Long section) {
		String ret;
		if (section == null) {
			ret = RvsUtils.getWaitingflow("0", null, process_code);
		} else {
			ret = RvsUtils.getWaitingflow(section.toString(), null, process_code);
		}
		if ("400".equals(process_code)) ret += "0";
		return ret;
	}

	private String toString(Object object, String alterNull) {
		if (object == null) return alterNull;
		return object.toString();
	}

	public void getBannerCounts(Map<String, Object> listResponse, SqlSession conn) {
		AllPositionsMapper dao = conn.getMapper(AllPositionsMapper.class);
		listResponse.put("wipsize", dao.getWipCount() + "台");
		int waitInline = dao.getWaitInline();
		int inlineToday = dao.getInlineToday();
		listResponse.put("inlinesize", waitInline + "⇒" + inlineToday + "台");
		int agreeCount = dao.getAgreeCount();
		listResponse.put("agreeCount", agreeCount);

		if (waitInline == 0) {
			listResponse.put("notInlineRate", 0);
		} else {
			listResponse.put("notInlineRate", 100 - inlineToday * 100 / (waitInline + inlineToday));
		}

		int waitShipping = dao.getWaitShipping();
		int shippingToday = dao.getShippingToday();

		listResponse.put("shippingsize", waitShipping + "⇒" + shippingToday + "台");
		int c1Count = dao.getLineOutcome("14", "1");
		int c2Count = 0; // dao.getLineOutcome("14", "3");
		int c3Count = dao.getLineOutcome("14", "12");
//		waitShipping = c1Count + c2Count;
		if (waitShipping == 0) {
			listResponse.put("notShipRate", 0);
		} else {
			listResponse.put("notShipRate", 100 - shippingToday * 100 / (shippingToday + waitShipping));
		}

		listResponse.put("inlineCount", inlineToday);
		
		listResponse.put("D1Count", dao.getLineOutcome("12", "1"));
//		listResponse.put("D2Count", dao.getLineOutcome("12", "3"));
		listResponse.put("N1Count", dao.getLineOutcome("13", "1"));
//		listResponse.put("N2Count", dao.getLineOutcome("13", "3"));

		listResponse.put("C1Count", c1Count);
		listResponse.put("C2Count", c2Count);
		listResponse.put("C3Count", c3Count);
		listResponse.put("C1Plan", dao.getPlanToday("1"));
//		listResponse.put("C2Plan", dao.getPlanToday("3"));

		int QOK = dao.QaResult(RvsConsts.OPERATE_RESULT_FINISH);
		int QNG = dao.QaResult(RvsConsts.OPERATE_RESULT_SENDBACK);
		listResponse.put("QOKCount", QOK);
		listResponse.put("QNGCount", QNG);
	}

	public void getBannerBoCounts(Map<String, Object> listResponse, SqlSession conn) {
		AllPositionsMapper mapper = conn.getMapper(AllPositionsMapper.class);
		List<Map<String, Object>> boMaterialsOfSectionLine = mapper.getBoMaterialsOfSectionLine();

		int D1 = 0,D2 = 0,N1 = 0,N2 = 0,C1 = 0,C2 = 0,C3 = 0;

		for (Map<String, Object> boMaterials : boMaterialsOfSectionLine) {
			String line_id = boMaterials.get("line_id").toString();
			String section_id = boMaterials.get("section_id").toString();
			int cnt = Integer.parseInt(boMaterials.get("cnt").toString());
			if ("12".equals(line_id) && "1".equals(section_id)) {
				D1 = cnt;
			} else if ("13".equals(line_id) && "1".equals(section_id)) {
				N1 = cnt;
			} else if ("14".equals(line_id) && "1".equals(section_id)) {
				C1 = cnt;
			} else if ("12".equals(line_id) && "3".equals(section_id)) {
				D2 = cnt;
			} else if ("13".equals(line_id) && "3".equals(section_id)) {
				N2 = cnt;
			} else if ("14".equals(line_id) && "3".equals(section_id)) {
				C2 = cnt;
			} else if ("14".equals(line_id) && "12".equals(section_id)) {
				C3 = cnt;
			}
		}
//		C1 += (D1+N1);
//		C2 += (D2+N2);
		listResponse.put("boMaterialsD1", D1);
		listResponse.put("boMaterialsD2", D2);
		listResponse.put("boMaterialsN1", N1);
		listResponse.put("boMaterialsN2", N2);
		listResponse.put("boMaterialsC1", C1);
		listResponse.put("boMaterialsC2", C2);
		listResponse.put("boMaterialsC3", C3);

		int boMaterialsAll = mapper.getBoMaterialsAll();
		listResponse.put("boMaterialsAll", boMaterialsAll);

//		int decomStorageCount = mapper.getDecomStorageCount();
//		listResponse.put("decomStorageCount", decomStorageCount);
	}

	public List<Map<String, Object>> getErrorAlarms(HttpServletRequest req, SqlSession conn) {
		AllPositionsMapper dao = conn.getMapper(AllPositionsMapper.class);
		String line = req.getParameter("line_id");
		if ("2".equals(line)) {
			return dao.getErrorAlarms(req.getParameter("process_code"), "00000000003");
		} else {
			return dao.getErrorAlarms(req.getParameter("process_code"), line);
		}
	}
}
