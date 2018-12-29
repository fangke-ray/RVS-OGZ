package com.osh.rvs.mapper.push;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface HolidayMapper {

	/** search*/
	public List<Date> searchHoliday(@Param("s_date") Date s_date, @Param("e_date") Date e_date);

	public List<String> searchHolidayOfNowMonth();

	public List<String> searchHolidayOfMonth(@Param("month") String month);

	public boolean existsHoliday(Date date) throws Exception;

	public boolean createHoliday(Date date) throws Exception;

	public Date getNextWorkDate(Date date);

	public Date addWorkdays(Map<String, Object> cond);

	public Date addMinutes(Map<String, Object> cond);
}
