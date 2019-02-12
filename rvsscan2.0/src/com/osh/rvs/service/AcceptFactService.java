package com.osh.rvs.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.mapper.AcceptFactMapper;

import framework.huiqing.common.util.copy.DateUtil;


public class AcceptFactService {

	private static final int SEARCH_TARGET_NO_COMPLETE = 0;
	private static final int SEARCH_TARGET_QA_COMPLETE = 74;
	private static final int SEARCH_TARGET_WIP_COMPLETE = 73;

	/**
	 * 受理报价展示数据查询
	 * @param conn
	 * @return
	 */
	public Map<String, String> getSeries(SqlSession conn) {
		AcceptFactMapper dao = conn.getMapper(AcceptFactMapper.class);
		Map<String, String> ret = new HashMap<String, String>();

		//现品管理/受理报价-实绩 直送(受理、灭菌) S1(报价完成、投线)
		String series1_2 = "[{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#B6E0AA'],[1, '#449E2D']]}, y:#a#}," +
				" null," + 
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#FFFF33'],[1, '#F0F000']]}, y:#d#}," +
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#A0A000'],[1, '#B5B500']]}, y:#q#, dataLabels: {style: {color: '#646400'}, x:-10}},"+
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#A0A000'],[1, '#B5B500']]}, y:#t#, dataLabels: {style: {color: '#646400'}, x:-10}}]";
		
		//现品管理/受理报价-实绩 分室(受理、消毒、灭菌) S2S3(报价完成、投线)
		String series1_1 = "[{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#AABEE4'],[1, '#2125BE']]}, y:#a#, dataLabels: {style: {color: '#640064'}}} ," +
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#6699CC'],[1, '#4080BF']]}, y:#f#} ," +
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#6699CC'],[1, '#4080BF']]}, y:#s#} ," +
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#00A0A0'],[1, '#00B5B5']]}, y:#q#, dataLabels: {style: {color: '#006464'}, x:10}} ," +
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#00A0A0'],[1, '#00B5B5']]}, y:#t#, dataLabels: {style: {color: '#006464'}, x:10}}]";
		
		//现品管理/受理报价-实绩 D/M
		String series1_3 = "[null, null, null," +
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#00A0A0'],[1, '#00A843']]}, y:#q#, dataLabels: {style: {color: '#006464'}, x:5}},"+
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#00A0A0'],[1, '#00A843']]}, y:#t#, dataLabels: {style: {color: '#006464'}, x:5}}]";
		
		//现品管理/受理报价-实绩 E
		String series1_4 = "[null, null, null," +
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#00A0A0'],[1, '#00B5B5']]}, y:#q#, dataLabels: {style: {color: '#006464'}, x:20}},"+
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#3399FF'],[1, '#3333FF']]}, y:#t#, dataLabels: {style: {color: '#006464'}, x:20}}]";
		
		String series2_1 = "[#w# ,#d#, #s#, #q#, #u#, #i#," +
//				"{color:{linearGradient: { x1: 0, x2: 0, y1: 1, y2: 1 },stops: [[0, '#CF7474'],[1, '#9C3636']]}, y:#u#, dataLabels: {style: {color: '#642424'}}} ," +
				"{color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#00A0A0'],[1, '#00B5B5']]}, y:#t#, dataLabels: {style: {color: '#006464'}, x:12}}]";
		String series2_2 = "[null, null, null, null, null,null, " +
				" {color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#A0A000'],[1, '#B5B500']]}, y:#t#, dataLabels: {style: {color: '#646400'}, x:-12}}]";
		String series2_3 = "[null, null, null, null ,null, null," +
				" {color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#00A0A0'],[1, '#00A843 ']]}, y:#t#, dataLabels: {style: {color: '#006464'}, x:5}}]";
		String series2_4 = "[null, null, null, null ,null,null, " +
				" {color:{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#3399FF'],[1, '#3333FF ']]}, y:#t#, dataLabels: {style: {color: '#006464'}, x:20}}]";

		// 得到当日受理完成数
		List<Map<String,String>> acceptResult = dao.getAcceptResult();
		for (Map<String,String> acceptByDirect : acceptResult) {
			String directFlg = acceptByDirect.get("direct_flg");
			String countDirectFlg = acceptByDirect.get("count_direct_flg");
			if ("1".equals(directFlg)) {
				series1_2 = series1_2.replaceAll("#a#", countDirectFlg);
			} else if ("0".equals(directFlg)) {
				series1_1 = series1_1.replaceAll("#a#", countDirectFlg);
			}
		}

		// 得到当日消毒/灭菌/
		int countFrailiz = 0;
		int countFrailized = 0;
		List<Map<String, Object>> quatationResult = dao.getQuatationResult();
		for (Map<String, Object> quatationByPosition : quatationResult) {
			int position_id = ((Long) quatationByPosition.get("position_id")).intValue();
			Long countPosition_id = (Long) quatationByPosition.get("count_position_id");
			switch (position_id) {
			case 10 :
				series1_1 = series1_1.replaceAll("#f#", "" + countPosition_id);
				break;
			case 11 :
				series1_1 = series1_1.replaceAll("#s#", "" + countPosition_id);
				break;
			}
		}
		countFrailiz = dao.getWorkingSterilization();
		series1_2 = series1_2.replaceAll("#d#", "" + countFrailiz);
		
		int countS2S3 = 0;
		int countD = 0;
		int countE = 0;
		List<Map<String, Object>> quatation = dao.searchQuatationResult();
		for (Map<String, Object> quatationByPosition : quatation) {
			int level = ((Long) quatationByPosition.get("level")).intValue();
			Long countPosition_id = (Long) quatationByPosition.get("count_position_id");
			switch (level) {
				case 1 :
					series1_2 = series1_2.replaceAll("#q#", "" + countPosition_id);
					break;
				case 2 :
					countS2S3 += countPosition_id;
					break;
				case 3 :
					countS2S3 += countPosition_id;
					break;
				case 9 :
					countD += countPosition_id;
					break;
				case 91 :
				case 92 :
				case 93 :
				case 99 :
				case 96 :
				case 97 :
				case 98 :
					countD += countPosition_id;
					break;
				case 56 :
					countE += countPosition_id;
					break;
				case 57 :
					countE += countPosition_id;
					break;
				case 58 :
				case 59 :
					countE += countPosition_id;
					break;
			}
			
		}

		series1_1 = series1_1.replaceAll("#q#", "" + countS2S3);
		series1_3 = series1_3.replaceAll("#q#", "" + countD);
		series1_4 = series1_4.replaceAll("#q#", "" + countE);

		// 得到等待到货/报价/消毒/灭菌/投线数
		series2_1 = series2_1.replaceAll("#w#", "" + dao.getNotreachResult());
		series2_1 = series2_1.replaceAll("#q#", "" + dao.getQuatationWaitingResult(SEARCH_TARGET_NO_COMPLETE));
		series2_1 = series2_1.replaceAll("#d#", "" + dao.getDisinfectionWaitingResult());
		series2_1 = series2_1.replaceAll("#s#", "" + dao.getSterilizeWaitingResult());
		series2_1 = series2_1.replaceAll("#u#", "" + dao.getQuatationWaitingResult(SEARCH_TARGET_QA_COMPLETE));
		series2_1 = series2_1.replaceAll("#i#", "" + dao.getQuatationWaitingResult(SEARCH_TARGET_WIP_COMPLETE));
		List<Map<String, Object>> inlineWaitingResult = dao.getInlineWaitingResult();
		countS2S3 = 0;
		countD = 0;
		int peripheral = 0;//E
		for (Map<String, Object> inlineWaiting : inlineWaitingResult) {
			Integer level = ((Long) inlineWaiting.get("level")).intValue();
			Integer count_inline_waiting = ((Long) inlineWaiting.get("count_inline_waiting")).intValue();

			switch (level) {
			case 1 :
				series2_2 = series2_2.replaceAll("#t#", "" + count_inline_waiting);
				break;
			case 2 :
				countS2S3 += count_inline_waiting;
				break;
			case 3 :
				countS2S3 += count_inline_waiting;
				break;
			case 9 :
				countD += count_inline_waiting;
				break;
			case 91 :
			case 92 :
			case 93 :
			case 99 :
			case 96 :
			case 97 :
			case 98 :
				countD += count_inline_waiting;
				break;
			case 56 :
				peripheral += count_inline_waiting;
				break;
			case 57 :
				peripheral += count_inline_waiting;
				break;
			case 58 :
			case 59 :
				peripheral += count_inline_waiting;
				break;
			}
		}
		series2_1 = series2_1.replaceAll("#t#", "" + countS2S3);
		series2_3 = series2_3.replaceAll("#t#", "" + countD);
		series2_4 = series2_4.replaceAll("#t#", "" + peripheral);

		// 得到要求返品数
		series2_1 = series2_1.replaceAll("#u#", "" + dao.getUnrepairOrderResult());

		// 得到投线数
		List<Map<String, Object>> inlineResult = dao.getInlineResult();
		countS2S3 = 0;
		countD = 0;
		peripheral = 0;//E
		for (Map<String, Object> inline : inlineResult) {
			Integer level = ((Long) inline.get("level")).intValue();
			Integer count_inline = ((Long) inline.get("count_inline_fit")).intValue();

			switch (level) {
			case 1 :
				series1_2 = series1_2.replaceAll("#t#", "" + count_inline);
				break;
			case 2 :
				countS2S3 += count_inline;
				break;
			case 3 :
				countS2S3 += count_inline;
				break;
			case 9 :
				countD += count_inline;
				break;
			case 91 :
			case 92 :
			case 93 :
			case 99 :
			case 96 :
			case 97 :
			case 98 :
				countD += count_inline;
				break;
			case 56 :
				peripheral += count_inline;
				break;
			case 57 :
				peripheral += count_inline;
				break;
			case 58 :
			case 59 :
				peripheral += count_inline;
				break;
			}
		}
		series1_1 = series1_1.replaceAll("#t#", "" + countS2S3);
		series1_3 = series1_3.replaceAll("#t#", "" + countD);
		series1_4 = series1_4.replaceAll("#t#", "" + peripheral);

		// 清0
		series1_2 = series1_2.replaceAll("#\\w#", "0");
		series1_1 = series1_1.replaceAll("#\\w#", "0");
		series1_3 = series1_3.replaceAll("#\\w#", "0");
		series2_1 = series2_1.replaceAll("#\\w#", "0");
		series2_2 = series2_2.replaceAll("#\\w#", "0");
		series2_3 = series2_3.replaceAll("#\\w#", "0");

		// 本周开始时间
		Calendar friday = Calendar.getInstance();

		int theday = friday.get(Calendar.DAY_OF_WEEK);
		int diff = (theday + 1) % 7;
		friday.set(Calendar.HOUR_OF_DAY, 0);
		friday.set(Calendar.MINUTE, 0);
		friday.set(Calendar.SECOND, 0);
		friday.set(Calendar.MILLISECOND, 0);
		friday.add(Calendar.DATE, -diff);

		Date fridayDate = friday.getTime();
		// 周数据
		//本周到货登录实绩
		Map<String, BigDecimal> receptionInWeek = dao.getReceptionInWeek(fridayDate);
		if (receptionInWeek == null) {
			ret.put("series3_1", "[['大修理', 0], ['小修理', 0],['EndoEye',0], ['单元', 0],['周边设备',0]]"); // [['流水线', 138], ['EndoEye', 28], ['单元', 40]]
		} else {
			ret.put("series3_1", "[['大修理', "+receptionInWeek.get("heavy_fix")+"],['小修理',"+ receptionInWeek.get("light_fix") +"],['EndoEye', "+receptionInWeek.get("endoeye_sum")
						+"], ['单元', "+receptionInWeek.get("cell_sum")+"],['周边设备', "+receptionInWeek.get("peripheral")+"]]");
		}
		
		//本周到货登录实绩 RC分布
		Map<String, BigDecimal> receptionRCInWeek =  dao.getReceptionRCInWeek(fridayDate);
		String series3_2 = "[['OCM-GZ RC', #g#], ['OCM-BJ RC', #b#], ['OCM-SH RC',#h#], ['OCM-SY RC', #y#]]";
		if(receptionRCInWeek != null){
			series3_2 = series3_2.replaceAll("#g#", receptionRCInWeek.get("gz_sum").toString());
			series3_2 = series3_2.replaceAll("#b#", receptionRCInWeek.get("bj_sum").toString());
			series3_2 = series3_2.replaceAll("#h#", receptionRCInWeek.get("sh_sum").toString());
			series3_2 = series3_2.replaceAll("#y#", receptionRCInWeek.get("sy_sum").toString());
		}
		series3_2 = series3_2.replaceAll("#\\w#", "0");
		ret.put("series3_2",series3_2);
		
		//本周完成出货实绩
		Map<String, BigDecimal> shippingInWeek = dao.getShippingInWeek(fridayDate);
		if (shippingInWeek == null) {
			ret.put("series4_1", "[['大修理', 0],['小修理', 0], ['EndoEye', 0], ['单元', 0],['周边设备',0]]"); // [['流水线', 138], ['EndoEye', 28], ['单元', 40]]
		} else {
			ret.put("series4_1", "[['大修理', "+shippingInWeek.get("heavy_fix")+"],['小修理',"+shippingInWeek.get("light_fix")+"],['EndoEye', "+shippingInWeek.get("endoeye_sum")
						+"], ['单元', "+shippingInWeek.get("cell_sum")+"],['周边设备', "+shippingInWeek.get("peripheral")+"]]");
		}
		
		//本周完成出货实绩 RC分布
		Map<String, BigDecimal> shippingRCInWeek =  dao.getShippingRCInWeek(fridayDate);
		String series4_2 = "[['OCM-GZ RC', #g#], ['OCM-BJ RC', #b#], ['OCM-SH RC',#h#], ['OCM-SY RC', #y#]]";
		if(shippingRCInWeek != null){
			series4_2 = series4_2.replaceAll("#g#", shippingRCInWeek.get("gz_sum").toString());
			series4_2 = series4_2.replaceAll("#b#", shippingRCInWeek.get("bj_sum").toString());
			series4_2 = series4_2.replaceAll("#h#", shippingRCInWeek.get("sh_sum").toString());
			series4_2 = series4_2.replaceAll("#y#", shippingRCInWeek.get("sy_sum").toString());
		}
		series4_2 = series4_2.replaceAll("#\\w#", "0");
		ret.put("series4_2",series4_2);
		
		ret.put("series1_2", series1_2); // series1_2
		ret.put("series1_1", series1_1); // series1_1
		ret.put("series1_3", series1_3);
		ret.put("series1_4", series1_4);
		ret.put("series2_1", series2_1);
		ret.put("series2_2", series2_2);
		ret.put("series2_3", series2_3);
		ret.put("series2_4", series2_4);
		ret.put("friday", DateUtil.toString(fridayDate, "M月d日"));
		ret.put("countFrailized", "" + countFrailized);

		return ret;
	}

	public int getInlineWaitingForPeripheral(SqlSession conn) {
		AcceptFactMapper dao = conn.getMapper(AcceptFactMapper.class);
		List<Map<String, Object>> inlineWaitingResult = dao.getInlineWaitingResult();
		int peripheral = 0;//E
		for (Map<String, Object> inlineWaiting : inlineWaitingResult) {
			Integer level = ((Long) inlineWaiting.get("level")).intValue();
			Integer count_inline_waiting = ((Long) inlineWaiting.get("count_inline_waiting")).intValue();

			switch (level) {
			case 56 :
			case 57 :
			case 58 :
			case 59 :
				peripheral += count_inline_waiting;
				break;
			}
		}
		return peripheral;
	}
}
