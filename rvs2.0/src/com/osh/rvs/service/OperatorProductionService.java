package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.OperatorProductionEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.data.OperatorProductionForm;
import com.osh.rvs.mapper.data.OperatorProductionMapper;

import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class OperatorProductionService {
	private Logger logger = Logger.getLogger(getClass());

//	private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static final int work_start = 8;
	public static final int work_end = 17;
	public static final int work_start_min = 00;
	public static final int work_end_min = 15;

	/**
	 * 维修担当人一览
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<OperatorProductionForm> searchByCondition(ActionForm form, SqlSession conn) {
		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		List<OperatorProductionEntity> list = null;
		if (conditionBean.getAction_time_start() != null && conditionBean.getAction_time_start().equals(conditionBean.getAction_time_end())) {
			list = dao.getProductionFeatureByConditionOfDay(conditionBean);
		} else {
			list = dao.getProductionFeatureByCondition(conditionBean);
		}
		List<OperatorProductionForm> rtList = new ArrayList<OperatorProductionForm>();
		if (list != null) {
			BeanUtil.copyToFormList(list, rtList, null, OperatorProductionForm.class);
		}
		return rtList;
	}
	
	public OperatorProductionForm getDetail(ActionForm form, SqlSession conn) {
		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		OperatorProductionEntity entity = dao.getDetail(conditionBean);
		
		OperatorProductionForm rtForm = new OperatorProductionForm();
		if (entity != null) {
			BeanUtil.copyToForm(entity, rtForm, null);
		}
		
		return rtForm;
	}
	
	public List<OperatorProductionForm> getReportData(ActionForm form, SqlSession conn) {
		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		List<OperatorProductionEntity> list = dao.getProductionFeatureByKey(conditionBean);
		
		List<OperatorProductionForm> rtList = new ArrayList<OperatorProductionForm>();
		if (list != null) {
			BeanUtil.copyToFormList(list, rtList, null, OperatorProductionForm.class);
		}
		return rtList;
	}
	
	public List<OperatorProductionForm> getProductionFeatureByKey(ActionForm form, SqlSession conn,
			HttpSession session, OperatorProductionForm detail, String overtime_finish, Map<String, Object> listResponse) {

		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		// 取得作业/暂停/组件作业的一览(数据库原始数据)
		List<OperatorProductionEntity> list = dao.getProductionFeatureByKey(conditionBean);

		// 编辑后的一览
		List<OperatorProductionEntity> newList = new ArrayList<OperatorProductionEntity>();

		// 设置可编辑
		boolean editable = editable(session, detail, list, listResponse);//管理员，线长

		boolean isOwner = isOwner(session, detail);//本人操作

		boolean editOverwork = editable || isOwner;

		listResponse.put("editable", editOverwork);

		if (list == null || list.isEmpty()) { //不存在记录
			OperatorProductionEntity entity = new OperatorProductionEntity();
			entity.setPause_start_time(getNewDate(conditionBean.getAction_time(), work_start, work_start_min));
			entity.setPause_finish_time(getNewDate(conditionBean.getAction_time(), work_end, work_end_min));
			if (editOverwork) {
				entity.setLeak("true");
			}
			newList.add(entity);
		} else {
			// 空隙时间补上行
			newList = checkLeakTime(list, editable, isOwner, overtime_finish);
		}
		
		List<OperatorProductionForm> rtList = new ArrayList<OperatorProductionForm>();
		BeanUtil.copyToFormList(newList, rtList, null, OperatorProductionForm.class);
		return rtList;
	}
	
	public void savePause(ActionForm form, SqlSession conn) throws Exception {
//		OperatorProductionForm f = (OperatorProductionForm) form;
		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

//		conditionBean.setPause_finish_time(new Timestamp(format.parse(f.getPause_finish_time()).getTime()));
//		conditionBean.setPause_start_time(new Timestamp(format.parse(f.getPause_start_time()).getTime()));
		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		if (conditionBean.getPause_finish_time() == null) { // 当日最终的数据
			dao.deletePause(conditionBean);
			conditionBean.setPause_finish_time(getNewDate(new Date(), work_end, work_end_min));
			
			if (conditionBean.getReason() == null) {
				if (!CommonStringUtil.isEmpty(conditionBean.getComments())) {
					conditionBean.setReason(49); // 其他
				} else {
					return;
				}
			}
			
			dao.savePause(conditionBean);
		} else {
			String exist = dao.existPause(conditionBean);
			if (CommonStringUtil.isEmpty(exist)) {
				dao.savePause(conditionBean);
			} else {
				logger.info("did:" + conditionBean.getPause_start_time() + "->" + conditionBean.getPause_finish_time());
				dao.updatePause(conditionBean);
			}
		}
		
	}
	
	public void deletePauseOvertime(ActionForm form, SqlSession conn) throws Exception {
		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
//		OperatorProductionForm f = (OperatorProductionForm) form;
//		conditionBean.setPause_start_time(new Timestamp(format.parse(f.getPause_start_time()).getTime()));
		
		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		dao.deletePauseOvertime(conditionBean);
	}
	
	/**
	 * 取得加班信息
	 * @param form 条件
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public OperatorProductionForm getPauseOvertime(ActionForm form, SqlSession conn) throws Exception {
		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		OperatorProductionEntity entity = dao.getPauseOvertime(conditionBean);
		
		OperatorProductionForm rtForm = new OperatorProductionForm();
		if (entity != null) {
			BeanUtil.copyToForm(entity, rtForm, null);
		}

		return rtForm;
	}
	
	public void saveoverwork(ActionForm form, SqlSession conn) throws Exception {
//		OperatorProductionForm f = (OperatorProductionForm) form;
		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
//		conditionBean.setPause_start_time(new Timestamp(format.parse(f.getPause_start_time()).getTime()));
//		conditionBean.setPause_finish_time(new Timestamp(format.parse(f.getPause_finish_time()).getTime()));
		conditionBean.setReason(conditionBean.getOverwork_reason());
		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		dao.savePause(conditionBean);
	}
	
	public void updatePauseOvertime(ActionForm form, SqlSession conn) throws Exception {
		OperatorProductionEntity conditionBean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
//		OperatorProductionForm f = (OperatorProductionForm) form;
//		conditionBean.setPause_start_time(new Timestamp(format.parse(f.getPause_start_time()).getTime()));
//		conditionBean.setPause_finish_time(new Timestamp(format.parse(f.getPause_finish_time()).getTime()));
		
		conditionBean.setReason(conditionBean.getOverwork_reason());
		OperatorProductionMapper dao = conn.getMapper(OperatorProductionMapper.class);
		dao.updatePauseOvertime(conditionBean);
	}

	/**
	 * 可以编辑
	 * @param session
	 * @param form
	 * @param list
	 * @param listResponse 
	 * @return
	 */
	private boolean editable(HttpSession session, OperatorProductionForm form, List<OperatorProductionEntity> list, Map<String, Object> listResponse) {
		OperatorProductionEntity bean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, bean, null);

		// 今天的作业报告是不能被他人编辑的
		if (bean.getAction_time()!= null 
				&& DateUtil.compareDate(bean.getAction_time(), new Date()) == 0) {
			return false;
		}
		
		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = loginData.getPrivacies();
		if (privacies.contains(RvsConsts.PRIVACY_ADMIN)) { // 系统管理员
			listResponse.put("isAdmin", true);//管理员可输出工作日报
			return true;
		} else if(RvsConsts.ROLE_LINELEADER.equals(loginData.getRole_id()) && 
				bean.getSection_id().equals(loginData.getSection_id()) && 
				bean.getLine_id().equals(loginData.getLine_id())) {//线长,与当前操作人员同一课室,工程 TODO who did
			listResponse.put("isAdmin", true);//线长可输出工作日报
			Date finish = list.isEmpty() ? null : list.get(list.size() -1).getPause_finish_time();
			if (finish == null) { //最后一条记录的结束时间是空,没有生成下班记录
				return true;
			} else {
				Date end = getNewDate(finish, work_end, work_end_min); //下班时间
				if (finish.before(end)) {//下班之前的记录,没有生成下班记录
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * 补充整理日报项目
	 * @param list
	 * @param editable
	 * @param isOwner
	 * @param overtime_finish 当日加班结束时间
	 * @return
	 */
	private List<OperatorProductionEntity> checkLeakTime(List<OperatorProductionEntity> list, boolean editable,
			boolean isOwner, String overtime_finish) {
		List<OperatorProductionEntity> entities = new ArrayList<OperatorProductionEntity>();
		
		int size = list.size();
		logger.info("list.size.before=" + size);

		for(int i = 0; i < size; i++) {
			OperatorProductionEntity entity = list.get(i);
			if (!isProductionFeature(entity) && editable) {//b类管理员,线长可编辑
				entity.setLeak("true");
			} 
			Date action_time = null;
			if (i+1 < size) {
				action_time = list.get(i+1).getPause_start_time();
			}

			Date finish_time = list.get(i).getPause_finish_time();

			if (size == 1) { //只有一条记录
				checkStartTime(entities, entity, editable, isOwner);
				entities.add(entity);
				checkEndTime(entities, entity, editable, isOwner, overtime_finish);
			} else if (i == 0) {//第一条
				checkStartTime(entities, entity, editable, isOwner);
				entities.add(entity);
			} else if (i == size -1) {//最后一条
				// 有进行中的记录的话，到此结束为最后一条
				entities.add(entity);
				if (finish_time == null) {
					break;
				}
				checkEndTime(entities, entity, editable, isOwner, overtime_finish);
			} else {
				if (CommonStringUtil.isEmpty(entity.getSorc_no()) && action_time != null) {
					if (entity.getPause_finish_time() == null) {
						entity.setPause_finish_time(new Date(action_time.getTime()));
					} else if(entity.getPause_finish_time().after(action_time)) {
						entity.getPause_finish_time().setTime(action_time.getTime()); 
					}
				}
				entities.add(entity);
			}
			
			// 有进行中的记录的话，到此结束为最后一条
			if (finish_time == null) {
				continue;
			}

			Calendar cFinish = Calendar.getInstance();
			cFinish.setTime(finish_time);

			if (i+1 < size) {
				Calendar cAction = Calendar.getInstance();
				cAction.setTime(action_time);
				// 分钟数之差
				int diffMinutes = cAction.get(Calendar.HOUR_OF_DAY) * 60 + cAction.get(Calendar.MINUTE)
						- cFinish.get(Calendar.HOUR_OF_DAY) * 60 - cFinish.get(Calendar.MINUTE);
				if (diffMinutes >= 4) {
					// 补充间歇时段
					addNewEntity(entities, finish_time, action_time, editable, isOwner);
				}
			}
		}
		
		logger.info("list.size.after=" + entities.size());
		return entities;
	}
	
	private void checkStartTime(List<OperatorProductionEntity> entities, OperatorProductionEntity source, boolean editable, boolean isOwner) {
		Date start = source.getPause_start_time();
		Date work = getNewDate(start, work_start, work_start_min); //上班时间
		Calendar cAction = Calendar.getInstance();
		Calendar cFinish = Calendar.getInstance();
		cAction.setTime(start);
		cFinish.setTime(work);
		// 分钟数之差
		int diffMinutes = cAction.get(Calendar.HOUR_OF_DAY) * 60 + cAction.get(Calendar.MINUTE)
				- cFinish.get(Calendar.HOUR_OF_DAY) * 60 - cFinish.get(Calendar.MINUTE);
		if (diffMinutes >= 1) {
			// 补充间歇时段
			addNewEntity(entities, work, start, editable, isOwner);
		}
	}
	
	private void checkEndTime(List<OperatorProductionEntity> entities, OperatorProductionEntity source,
			boolean editable, boolean isOwner, String overtime_finish) {
		Date finish = source.getPause_finish_time();
		if (finish == null) {// 暂停中状态

		} else {
			Date off_duty = null;
			if (overtime_finish == null) {
				off_duty = getNewDate(source.getPause_start_time(), work_end, work_end_min); // 下班时间
			} else {
				off_duty = DateUtil.toDate(overtime_finish, DateUtil.DATE_TIME_PATTERN);
			}

			if (finish.before(off_duty)) {
				addNewEntity(entities, finish, off_duty, editable, isOwner);
			}
			// 如果结束时间已经不是当日（比如暂停跨日）
			if (DateUtil.compareDate(finish, off_duty) != 0) {
				Date _start = source.getPause_start_time();
				// 显示上设置为下班时间或开始时间晚的
				if (_start.after(off_duty)) {
					source.setPause_finish_time(_start);
				} else {
					source.setPause_finish_time(off_duty);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static Date getNewDate(Date source, int hour) {
		return getNewDate(source, hour , 0);
	}
	private static Date getNewDate(Date source, int hour, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(source);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}
	
	/**
	 * C类虚拟记录
	 * @param entities
	 * @param start
	 * @param end
	 */
	private void addNewEntity(List<OperatorProductionEntity> entities, Date start, Date end, boolean editable, boolean isOwner) {
		OperatorProductionEntity entity = new OperatorProductionEntity();
		entity.setPause_start_time(start);
		entity.setPause_finish_time(end);
		if (editable || isOwner) {
			entity.setLeak("true");
		}
		entities.add(entity);
	}
	
	private boolean isProductionFeature(OperatorProductionEntity entity){
		if (!CommonStringUtil.isEmpty(entity.getSorc_no()) || 
			!CommonStringUtil.isEmpty(entity.getModel_name()) || 
			!CommonStringUtil.isEmpty(entity.getProcess_code())) {
			return true;
		}
		
		return false;
	}

	/***
	 * 判断是否本人
	 * @param session
	 * @param form
	 * @return
	 */
	private boolean isOwner(HttpSession session, OperatorProductionForm form) {
		OperatorProductionEntity bean = new OperatorProductionEntity();
		BeanUtil.copyToBean(form, bean, null);
		
		if (DateUtil.compareDate(bean.getAction_time(), new Date()) != 0) {
			return false;
		} //不是今天
		
		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		// 不是本人在查看
		if (form.getOperator_id().equals(loginData.getOperator_id())) {
			return true;
		}

		return false;
	}
}
