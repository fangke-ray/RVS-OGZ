package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.inline.MaterialProcessEntity;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.inline.MaterialProcessForm;
import com.osh.rvs.mapper.inline.MaterialProcessMapper;
import com.osh.rvs.mapper.master.HolidayMapper;
import com.osh.rvs.mapper.master.OperatorMapper;

import framework.huiqing.common.util.copy.BeanUtil;

public class MaterialProcessService {

	public MaterialProcessForm loadMaterialProcess(SqlSession conn, String id) {
		
		MaterialProcessForm form = null;
		
		MaterialProcessMapper dao = conn.getMapper(MaterialProcessMapper.class);
		MaterialProcessEntity entity = dao.loadMaterialProcess(id);
		
		if (entity != null) {
			form = new MaterialProcessForm();
			BeanUtil.copyToForm(entity, form, null);
		}

		return form;
	}
	
	public void updateMaterialProcess(ActionForm form, SqlSession conn) throws Exception {
		MaterialProcessEntity conditionBean = new MaterialProcessEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		MaterialProcessMapper mpMapper = conn.getMapper(MaterialProcessMapper.class);

		String materialId = conditionBean.getMaterial_id();
		
		if (conditionBean.getDec_finish_date() != null || conditionBean.getDec_plan_date() != null) {
			conditionBean.setFinish_date(conditionBean.getDec_finish_date());
			conditionBean.setScheduled_date(conditionBean.getDec_plan_date());
			conditionBean.setLine_id(mpMapper.getMaterialProcessLine(materialId, "1"));
			conditionBean.setPx(null);
			mpMapper.updateMaterialProcess(conditionBean);
		}
		
		if (conditionBean.getNs_finish_date() != null || conditionBean.getNs_plan_date() != null) {
			conditionBean.setFinish_date(conditionBean.getNs_finish_date());
			conditionBean.setScheduled_date(conditionBean.getNs_plan_date());
			conditionBean.setLine_id("00000000013");
			conditionBean.setPx(null);
			mpMapper.updateMaterialProcess(conditionBean);
		}
		
		if (conditionBean.getCom_finish_date() != null || conditionBean.getCom_plan_date() != null) {
			conditionBean.setFinish_date(conditionBean.getCom_finish_date());
			conditionBean.setScheduled_date(conditionBean.getCom_plan_date());
			conditionBean.setLine_id(mpMapper.getMaterialProcessLine(materialId, "0"));
			conditionBean.setPx(null);
			mpMapper.updateMaterialProcess(conditionBean);
		}
	}

	/**
	 * 已完成的工程完成时间取消
	 * @param material_id 维修对象ID
	 * @param conn
	 */
	public void undoLineComplete(String material_id, SqlSession conn) throws Exception {
		MaterialProcessMapper mapper = conn.getMapper(MaterialProcessMapper.class);
		MaterialProcessEntity entity = new MaterialProcessEntity();
		entity.setMaterial_id(material_id);

//		entity.setLine_id(line_id);
		mapper.undoLineComplete(entity);
	}

	/**
	 * 未修理返还后,还未结束的工程工作情况删除
	 * @param material_id 维修对象ID
	 * @param conn
	 */
	public void removeByBreak(String material_id, SqlSessionManager conn) throws Exception {
		MaterialProcessMapper mapper = conn.getMapper(MaterialProcessMapper.class);
		mapper.removeByBreak(material_id);
	}

	/**
	 * 结束工程
	 * @param material_id 维修对象ID
	 * @param line_id 工程ID
	 * @param triggerList 
	 * @param conn
	 */
	public void finishMaterialProcess(String material_id, String line_id, List<String> triggerList, SqlSessionManager conn) throws Exception {
		MaterialProcessMapper dao = conn.getMapper(MaterialProcessMapper.class);
		MaterialProcessEntity materialProcess = new MaterialProcessEntity();
		materialProcess.setMaterial_id(material_id);
		materialProcess.setLine_id(line_id);
		dao.finishMaterialProcess(materialProcess);
	}
	/**
	 * 获取维修对象进展工程
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public Map<String,String> getMaterialProcessLine(String material_id,SqlSession conn){
		MaterialProcessMapper dao = conn.getMapper(MaterialProcessMapper.class);
		List<String> lineIdList = dao.searchMaterialProcessLineByMaterialId(material_id);
		
		Map<String,String> map = new HashMap<String, String>();
		
		for(String line_id:lineIdList){
			map.put(line_id, line_id);
		}

		return map;
	}

	public void pxExchange(String material_id, String line_id, SqlSessionManager conn) {
		MaterialProcessMapper mapper = conn.getMapper(MaterialProcessMapper.class);
		mapper.updatePx(material_id, line_id);
	}

	public static int IDX_COM_FINISH = 0;
	public static int IDX_DEC_FINISH = 1;
	public static int IDX_DEC_FINISH_S1 = 0;
	public static int IDX_COM_FINISH_S1 = 1;
	public static int IDX_DEC_FINISH_S3 = 1;
	public static int IDX_COM_FINISH_S3 = 2;

	/**
	 * 取得当日投线的话，应当的完成时间（按OGZ投线标准）
	 * @param conn
	 * @return 
	 * 0: S1+2天投线，应当+2天分解完成
	 * 1: S1+2天投线，应当+3天出货；S2/S3+1天投线，应当+3天分解/NS 完成
	 * 2: S2/S3+1天投线，应当+4天出货
	 */
	public static Date[] getScheduleAssignTimes(SqlSession conn) {
		Date today = new Date();
		Date[] ret = new Date[3];

		HolidayMapper hMapper = conn.getMapper(HolidayMapper.class);

		Map<String, Object> cond = new HashMap<String, Object>();
		cond.put("date", today);
		cond.put("interval", 2);
		ret[IDX_DEC_FINISH_S1] = hMapper.addWorkdays(cond);

		cond.put("interval", 3);
		ret[IDX_COM_FINISH_S1] = hMapper.addWorkdays(cond);

		cond.put("interval", 4);
		ret[IDX_COM_FINISH_S3] = hMapper.addWorkdays(cond);

		return ret;
	}

	public void assignReworkTrigger(String material_id, String position_id,
			List<String> reworkPositions, SqlSessionManager conn) {
		MaterialProcessMapper mapper = conn.getMapper(MaterialProcessMapper.class);
		Map<String, Object> cond = new HashMap<String, Object>();

		cond.put("material_id", material_id);
		cond.put("break_position", position_id);
		cond.put("rework_positions", reworkPositions);

		mapper.assignReworkTrigger(cond);
	}
	
	public List<String> loadMaterialProcessLineIds(String id, SqlSession conn) {

		List<String> res = new ArrayList<String>();

		MaterialProcessMapper dao = conn.getMapper(MaterialProcessMapper.class);
		List<MaterialProcessEntity> entities = dao.loadMaterialProcessLines(id);

		for (MaterialProcessEntity entity : entities) {
			res.add(entity.getLine_id());
		}

		return res;
	}


	public void insertMaterialProcess(MaterialProcessEntity insertBean, SqlSessionManager conn) throws Exception {
		MaterialProcessMapper mapper = conn.getMapper(MaterialProcessMapper.class);
		mapper.insertMaterialProcess(insertBean);
	}

	public void setMaterialProcess(String material_id, Integer level, List<String> newHasLines, 
			Date[] scheduleTimes, Date[] scheduleAssignTimes, SqlSessionManager conn) throws Exception{
		// 工程存在与否判断
		Map<String, Integer> inAdvances = new HashMap<String, Integer>();
		inAdvances.put("00000000012", 1);
		inAdvances.put("00000000013", 1);
		inAdvances.put("00000000014", 0);
		inAdvances.put("00000000050", 0);
		inAdvances.put("00000000054", 0);
		inAdvances.put("00000000060", 1);
		inAdvances.put("00000000061", 0);
		inAdvances.put("00000000070", 0); // TODO inline

		for (String lineId : newHasLines) {
			MaterialProcessEntity insertBean = new MaterialProcessEntity();
			insertBean.setMaterial_id(material_id);
			insertBean.setLine_id(lineId);

			if (scheduleTimes == null) {
				Date today = new Date();
				insertBean.setScheduled_date(today);
				insertBean.setScheduled_assign_date(today);
			} else {
				if (inAdvances.get(lineId) == 0) {
					insertBean.setScheduled_date(scheduleTimes[IDX_COM_FINISH]);
					if (level == 1) {
						insertBean.setScheduled_assign_date(scheduleAssignTimes[IDX_COM_FINISH_S1]);
					} else {
						insertBean.setScheduled_assign_date(scheduleAssignTimes[IDX_COM_FINISH_S3]);
					}
				} else {
					insertBean.setScheduled_date(scheduleTimes[IDX_DEC_FINISH]);
					if (level == 1) {
						insertBean.setScheduled_assign_date(scheduleAssignTimes[IDX_DEC_FINISH_S1]);
					} else {
						insertBean.setScheduled_assign_date(scheduleAssignTimes[IDX_DEC_FINISH_S3]);
					}
				}
			}
			insertMaterialProcess(insertBean, conn);
		}
	}

	/**
	 * 重新设定维修进度计划
	 * @param material_id
	 * @param oldHasLines
	 * @param newHasLines
	 * @param conn
	 * @throws Exception
	 */
	public void resignMaterialProcess(String material_id, List<String> oldHasLines, List<String> newHasLines, SqlSessionManager conn) throws Exception{

		// 工程存在与否判断
		Map<String, Integer> toogles = new HashMap<String, Integer>();
		toogles.put("00000000012", 0);
		toogles.put("00000000013", 0);
		toogles.put("00000000014", 0);
		toogles.put("00000000050", 0);
		toogles.put("00000000054", 0);
		toogles.put("00000000060", 0);
		toogles.put("00000000061", 0);
		toogles.put("00000000070", 0); // TODO inline
		Map<String, Integer> inAdvances = new HashMap<String, Integer>();
		inAdvances.put("00000000012", 1);
		inAdvances.put("00000000013", 1);
		inAdvances.put("00000000014", 0);
		inAdvances.put("00000000050", 0);
		inAdvances.put("00000000054", 0);
		inAdvances.put("00000000060", 1);
		inAdvances.put("00000000061", 0);
		inAdvances.put("00000000070", 0); // TODO inline

		for (String lineId : toogles.keySet()) {
			if (newHasLines.contains(lineId) && !oldHasLines.contains(lineId)) {
				toogles.put(lineId, 1);
			} else if (!newHasLines.contains(lineId) && oldHasLines.contains(lineId)) {
				toogles.put(lineId, -1);
			}
		}

		Date[] dSchedulePlans = null;

		for (String lineId : toogles.keySet()) {
			if (toogles.get(lineId) == 1) { // 新增工程
				if (dSchedulePlans == null) {
					MaterialService mService = new MaterialService();
					MaterialEntity mBean = mService.loadMaterialDetailBean(conn, material_id);
					dSchedulePlans = RvsUtils.getTimeLimit(mBean.getAgreed_date(), 
							mBean.getLevel(), mBean.getScheduled_expedited(), conn, true);
				}

				MaterialProcessEntity insertBean = new MaterialProcessEntity();
				insertBean.setMaterial_id(material_id);
				if (inAdvances.get(lineId) == 0) {
					insertBean.setScheduled_date(dSchedulePlans[IDX_COM_FINISH]);
				} else {
					insertBean.setScheduled_date(dSchedulePlans[IDX_DEC_FINISH]);
				}

				insertBean.setLine_id(lineId);
				insertMaterialProcess(insertBean, conn);
			}
			if (toogles.get(lineId) == -1) { // 结束工程
				if (inAdvances.get(lineId) == 0) {
					MaterialProcessMapper mapper = conn.getMapper(MaterialProcessMapper.class);
					mapper.removeMaterialProcessLine(material_id, lineId);
				} else {
					finishMaterialProcess(material_id, lineId, null, conn);
				}
			}
		}
	}

	/**
	 * 进入分线工位,判断是需要切换工位
	 * 
	 * @param material_id
	 * @param line_id
	 * @param operator_id
	 * @param conn
	 * @throws Exception 
	 */
	public void checkDividePx(String material_id, String line_id,
			String operator_id, SqlSessionManager conn) throws Exception {
		if (operator_id == null) return;

		OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);
		OperatorEntity operator = oMapper.getOperatorByID(operator_id);
		if (operator.getPx() == null || operator.getPx() == 0) {
			return;
		}

		Integer materialPx = operator.getPx() - 1;

		MaterialProcessMapper mapper = conn.getMapper(MaterialProcessMapper.class);
		MaterialProcessEntity entity = mapper.loadMaterialProcessOfLine(material_id, line_id);
		if (!materialPx.equals(entity.getPx())) {
			entity.setPx(materialPx);
			entity.setLine_id(line_id);
			mapper.updateMaterialProcess(entity);
		}
	}
}
