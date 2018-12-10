package com.osh.rvs.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.ServiceRepairManageEntity;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.ServiceRepairManageMapper;

import framework.huiqing.common.util.copy.DateUtil;

public class ServiceRepairManageService {
	private static final int TARGET_RATE_ONE_DAY = 90;//答复一日达成目标
	private static final int TARGET_RATE_TWO_DAY = 95;//答复两日达成目标
	private static final int SERVICE_REPAIR=1;//保修期内返品
	private static final int ANSWER_IN_DEADLINE=2;//答复在24小时内

	public Map<String, Object>getData(Calendar calendar, SqlSession conn) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
	
		List<String> years = new ArrayList<String>();
		List<String> months = new ArrayList<String>();
		int nowAxis = RvsUtils.getMonthAxisInHalfBussinessYear(calendar.getTime(), true, years, months);
		
		// 当前月份
		String strMon=DateUtil.toString(calendar.getTime(), "M");
		
		ServiceRepairManageMapper qaMapper = conn.getMapper(ServiceRepairManageMapper.class);
		Map<String, Object> tLines=new HashMap<String,Object>();
		
		List<String> axisTextList=new ArrayList<String>();//X轴
		List<Integer> list_cuttent=new ArrayList<Integer>();//保修期内返品
		List<Integer> list_last=new ArrayList<Integer>();//上期保修期内返品
		List<Integer> one_day_rate=new ArrayList<Integer>();//答复一日达成率
		List<Integer> two_day_rate=new ArrayList<Integer>();//答复两日达成率
		List<Integer> last_one_day_rate=new ArrayList<Integer>();//上期答复一日达成率
		List<Integer> last_two_day_rate=new ArrayList<Integer>();//上期答复两日达成率
		List<Integer> target_oneday_rateList=new ArrayList<Integer>();//一日目标
		List<Integer> target_twoday_rateList=new ArrayList<Integer>();//两日目标
		
		ServiceRepairManageEntity entity = null;
		for (int i = 0; i < months.size();) {
			String axisText = months.get(i);
			
			if (!axisText.equals(strMon)) {
				if (i <= nowAxis) {
					//保修期内返品
				    entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(RvsUtils.getStartDate(years.get(i),months.get(i)));
					entity.setQa_referee_time_end(RvsUtils.getEndDate(years.get(i),months.get(i)));
					entity.setService_repair_flg(SERVICE_REPAIR);
					int count_current = qaMapper.searchServiceRepair(entity);
					list_cuttent.add(count_current);

					//上期保修期内返品
					entity= new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(RvsUtils.getStartDate(String.valueOf(Integer.valueOf(years.get(i))-1),months.get(i)));
					entity.setQa_referee_time_end(RvsUtils.getEndDate(String.valueOf(Integer.valueOf(years.get(i))-1),months.get(i)));
					entity.setService_repair_flg(SERVICE_REPAIR);
					int count_last = qaMapper.searchServiceRepair(entity);
					list_last.add(count_last);
					
					//当月达成总数
					entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(RvsUtils.getStartDate(years.get(i),months.get(i)));
					entity.setQa_referee_time_end(RvsUtils.getEndDate(years.get(i),months.get(i)));
					int count_current_total = qaMapper.searchServiceRepair(entity);
				
					//当月一日达成总数
					entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(RvsUtils.getStartDate(years.get(i),months.get(i)));
					entity.setQa_referee_time_end(RvsUtils.getEndDate(years.get(i),months.get(i)));
					entity.setAnswer_in_deadline(ANSWER_IN_DEADLINE);
					int count_current_complete = qaMapper.searchServiceRepair(entity);
					
					//答复一日达成率
					Integer rate=null;
					if(count_current_total !=0){
						rate = (int) (BigDecimal.valueOf(count_current_complete).divide(BigDecimal.valueOf(count_current_total),2,BigDecimal.ROUND_HALF_UP).doubleValue()*100);
					}
					one_day_rate.add(rate);
					
					//当月两日达成总数
					entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(RvsUtils.getStartDate(years.get(i),months.get(i)));
					entity.setQa_referee_time_end(RvsUtils.getEndDate(years.get(i),months.get(i)));
					entity.setAnswer_in_deadline(-1);
					count_current_complete = qaMapper.searchServiceRepair(entity);
					
					rate=null;
					if(count_current_total !=0){
						rate = (int) (BigDecimal.valueOf(count_current_complete).divide(BigDecimal.valueOf(count_current_total),2,BigDecimal.ROUND_HALF_UP).doubleValue()*100);
					}
					two_day_rate.add(rate);
					
					//上期当月达成总数
					entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(RvsUtils.getStartDate(String.valueOf(Integer.valueOf(years.get(i))-1),months.get(i)));
					entity.setQa_referee_time_end(RvsUtils.getEndDate(String.valueOf(Integer.valueOf(years.get(i))-1),months.get(i)));
					entity.setQa_referee_time(new Date());
					int count_last_total = qaMapper.searchServiceRepair(entity);
					
					///上期当月两日达成总数
					entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(RvsUtils.getStartDate(String.valueOf(Integer.valueOf(years.get(i))-1),months.get(i)));
					entity.setQa_referee_time_end(RvsUtils.getEndDate(String.valueOf(Integer.valueOf(years.get(i))-1),months.get(i)));
					entity.setQa_referee_time(new Date());
					entity.setAnswer_in_deadline(ANSWER_IN_DEADLINE);
					int count_last_complete = qaMapper.searchServiceRepair(entity);
					//上期答复一日达成率
					rate = null;
					if(count_last_total !=0){
						rate = (int) (BigDecimal.valueOf(count_last_complete).divide(BigDecimal.valueOf(count_last_total),2,BigDecimal.ROUND_HALF_UP).doubleValue()*100);
					}
					last_one_day_rate.add(rate);
					
					entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(RvsUtils.getStartDate(String.valueOf(Integer.valueOf(years.get(i))-1),months.get(i)));
					entity.setQa_referee_time_end(RvsUtils.getEndDate(String.valueOf(Integer.valueOf(years.get(i))-1),months.get(i)));
					entity.setQa_referee_time(new Date());
					entity.setAnswer_in_deadline(-1);
					count_last_complete = qaMapper.searchServiceRepair(entity);
					//上期答复两日达成率
					rate = null;
					if(count_last_total !=0){
						rate = (int) (BigDecimal.valueOf(count_last_complete).divide(BigDecimal.valueOf(count_last_total),2,BigDecimal.ROUND_HALF_UP).doubleValue()*100);
					}
					last_two_day_rate.add(rate);
				}

				axisTextList.add(axisText + "月");
				target_oneday_rateList.add(TARGET_RATE_ONE_DAY);
				target_twoday_rateList.add(TARGET_RATE_TWO_DAY);
				i++;
			} else {
				List<Date> startDates = new ArrayList<Date>();
				List<Date> endDates = new ArrayList<Date>();
				RvsUtils.getWeekAxisInMonth(Calendar.getInstance().getTime(), true, startDates, endDates);
				
				for (int index = 0;index < startDates.size();index++) {
					Date start = startDates.get(index);
					Date end = endDates.get(index);
					
					entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(start);
					entity.setQa_referee_time_end(end);
					entity.setService_repair_flg(SERVICE_REPAIR);
					Integer count_current_momth = qaMapper.searchServiceRepair(entity);
					if(count_current_momth==0){
						count_current_momth=null;
					}
					list_cuttent.add(count_current_momth);//保修期内返品
					list_last.add(null);
					
					//每周达成总数
					entity = new ServiceRepairManageEntity();
					entity.setQa_referee_time_start(start);
					entity.setQa_referee_time_end(end);
					entity.setQa_referee_time(new Date());
					int count_cuttent_total = qaMapper.searchServiceRepair(entity);

					//一日达成总数
					entity.setAnswer_in_deadline(ANSWER_IN_DEADLINE);
					int count_current_complete = qaMapper.searchServiceRepair(entity);
					
					Integer rate=null;
					if(count_cuttent_total !=0){
						rate = (int) (BigDecimal.valueOf(count_current_complete).divide(BigDecimal.valueOf(count_cuttent_total),2,BigDecimal.ROUND_HALF_UP).doubleValue()*100);
					}
					one_day_rate.add(rate);
					last_one_day_rate.add(null);
					
					//两日达成总数
					entity.setAnswer_in_deadline(-1);
					count_current_complete = qaMapper.searchServiceRepair(entity);
					rate=null;
					if(count_cuttent_total !=0){
						rate = (int) (BigDecimal.valueOf(count_current_complete).divide(BigDecimal.valueOf(count_cuttent_total),2,BigDecimal.ROUND_HALF_UP).doubleValue()*100);
					}
					two_day_rate.add(rate);
					last_two_day_rate.add(null);
					
					target_oneday_rateList.add(TARGET_RATE_ONE_DAY);
					target_twoday_rateList.add(TARGET_RATE_TWO_DAY);

					axisTextList.add(DateUtil.toString(start, "M/d") +"<br>"+"～" + DateUtil.toString(end, "M/d"));
				}
				
				i++;
			}
			
			tLines.put("axisTextList",axisTextList);
			tLines.put("list_cuttent",list_cuttent);
			tLines.put("list_last",list_last);
			tLines.put("one_day_rate",one_day_rate);
			tLines.put("two_day_rate",two_day_rate);
			tLines.put("last_one_day_rate",last_one_day_rate);
			tLines.put("last_two_day_rate",last_two_day_rate);
			tLines.put("target_oneday_rateList",target_oneday_rateList);
			tLines.put("target_twoday_rateList",target_twoday_rateList);
		}
		
		ret.put("tLines",tLines);
		return ret;
	}
	
	public Map<String,Object> getCurrentData(Calendar calendar,SqlSession conn)throws Exception{
		ServiceRepairManageMapper dao = conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		Map<String,Object> map=new HashMap<String,Object>();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		SimpleDateFormat sdf=new SimpleDateFormat(DateUtil.DATE_TIME_PATTERN);
		String start_date_of_day=sdf.format(calendar.getTime());
		
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		String end_date_of_day=sdf.format(calendar.getTime());
	
		entity.setQa_referee_time_start(sdf.parse(start_date_of_day));
		entity.setQa_referee_time_end(sdf.parse(end_date_of_day));
		entity.setService_repair_flg(1);
		//保内返品分析完成件数
		int cuttent_count=dao.searchServiceRepair(entity);
		
		//分析等待中数
		int wait_count=dao.searchAnalyseWaitting();
		
		//分析进行中数
		int analyse_count=dao.searchCurrentData();
		
		
		
		map.put("cuttent_count", cuttent_count);
		map.put("analyse_count", analyse_count);
		map.put("wait_count", wait_count);
		
		
		return map;
	}

	
}