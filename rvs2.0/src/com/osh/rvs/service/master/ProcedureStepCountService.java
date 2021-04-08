package com.osh.rvs.service.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.master.ProcedureStepCountEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.SocketCommunitcator;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.master.ProcedureStepCountForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.master.ProcedureStepCountMapper;
import com.osh.rvs.service.AlarmMesssageService;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

public class ProcedureStepCountService {

	private static final Integer RELATION_TYPE_MODEL = 1;

	private Logger _log = Logger.getLogger(getClass());

	private static Set<String> countPositionSet = null; 

	public static 	Set<String> getCountPositionSet(SqlSession conn) {
		if (countPositionSet == null) {
			countPositionSet = new HashSet<String>();
			// 从数据库中查询记录
			ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
			List<ProcedureStepCountEntity> list = mapper.searchProcedureStepCount(new ProcedureStepCountEntity());
			for (ProcedureStepCountEntity entity : list) {
				countPositionSet.add(entity.getPosition_id());
			}
		}
		return countPositionSet;
	}

	/**
	 * 客户端登录
	 * 
	 * @param client_ip 客户端IP
	 * @param process_code 客户端设置的工位号
	 * @param line_part 客户端设置的工位分线
	 * @param callbackResponse
	 * @param conn
	 */
	public void clientLogin(String client_ip, String process_code,
			String line_part, Map<String, Object> callbackResponse,
			SqlSessionManager conn) {
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity condi = new ProcedureStepCountEntity();
		condi.setProcess_code(process_code);
		if ("A".equals(line_part)) condi.setPx(1);
		if ("B".equals(line_part)) condi.setPx(2);
		List<ProcedureStepCountEntity> list = mapper.searchProcedureStepCount(condi);

		if (list.size() == 0) {
			callbackResponse.put("error", "当前设定的岗位没有计数规则设置。");
		} else {
			List<ProcedureStepCountEntity> avaCounterList = new ArrayList<ProcedureStepCountEntity>();
			for (ProcedureStepCountEntity entity : list) {
				if (!client_ip.equals(entity.getClient_address())) {
					// 更新客户端地址
					entity.setClient_address(client_ip);
					mapper.updateClientAddress(entity);
				}

				ProcedureStepCountEntity avaCounter = new ProcedureStepCountEntity();
				avaCounter.setProcedure_step_count_id(entity.getProcedure_step_count_id());
				avaCounter.setName(entity.getName());
				avaCounterList.add(avaCounter);
			}
			callbackResponse.put("avaCounterList", avaCounterList);
		}
	}

	public List<ProcedureStepCountEntity> getProcedureStepCountOfModel(String model_id, ProductionFeatureEntity pf, String px, 
			SqlSession conn) {
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity entity = new ProcedureStepCountEntity();
		entity.setRelation_type(RELATION_TYPE_MODEL);
		entity.setRelation_id(model_id);
		entity.setPosition_id(pf.getPosition_id());
		if ("1".equals(px)) {
			entity.setPx(1);
		} else if ("2".equals(px)) {
			entity.setPx(2);
		}
		return mapper.searchProcedureStepOfModel(entity);
		
		
	}

	/**
	 * 开始计数
	 * 
	 * @param mform
	 * @param position_id
	 * @param operate_result 
	 * @param conn
	 * @return
	 */
	public String startProcedureStepCount(MaterialForm mform,
			String position_id, String px, ProductionFeatureEntity pf, SqlSession conn) {

		SocketCommunitcator scUtil = new SocketCommunitcator();
		Map<String, Object> map = new HashMap<String, Object>();
		// 放置维修品信息
		map.put("omr_notifi_no", mform.getSorc_no());
		map.put("model_name", mform.getModel_name());
		map.put("serial_no", mform.getSerial_no());

		// 取得要实现的计数信息
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);

		List<ProcedureStepCountEntity> list = getProcedureStepCountOfModel(mform.getModel_id(), pf, px, conn);

		if (list.size() == 0) {
			return "NotSetException";
		}

		// 返回要实现的计数信息
		Map<String, String> setTimesMap = new HashMap<String, String>();
		String client_address = null;
		for (ProcedureStepCountEntity pscEntity : list) {
			client_address = pscEntity.getClient_address();
			// 各计数的计数标准
			setTimesMap.put(pscEntity.getProcedure_step_count_id(), "" + pscEntity.getStep_times());
			// if (pscEntity.getProcedure_step_count_id().equals("00000000001")) {
			//	map.put("set_times", "" + list.get(0).getStep_times()); // old version
			// }
		}

		map.put("set_times_map", setTimesMap);

		if (pf.getOperate_result() > 0) {
			// 取得前次
			ProcedureStepCountEntity pfEntity = new ProcedureStepCountEntity();
			pfEntity.setMaterial_id(mform.getMaterial_id());
			pfEntity.setPosition_id(position_id);
			pfEntity.setRework(pf.getRework());

			Map<String, String> countedTimesMap = new HashMap<String, String>();

			List<ProcedureStepCountEntity> listCounted = mapper.getMaterialCountedInPosition(pfEntity);
			for (ProcedureStepCountEntity pscEntity : listCounted) {
				countedTimesMap.put(pscEntity.getProcedure_step_count_id(), "" + pscEntity.getStep_times());
			}
			map.put("counted_times_map", countedTimesMap);
		}

		return scUtil.clientSendMessage(client_address, 50023, "In:" + JSON.encode(map));
	}

	/**
	 * 从最后取得数开始计数
	 * 
	 * @param mform
	 * @param position_id
	 * @param operate_result 
	 * @param conn
	 * @return
	 */
	public String startProcedureStepCountContinue(MaterialEntity mentity,
			Map<String, ProcedureStepCountEntity> setTimesEntityMap, Map<String, String> decodedRec, SqlSession conn) {

		SocketCommunitcator scUtil = new SocketCommunitcator();
		Map<String, Object> map = new HashMap<String, Object>();
		// 放置维修品信息
		map.put("omr_notifi_no", mentity.getSorc_no());
		map.put("model_name", mentity.getModel_name());
		map.put("serial_no", mentity.getSerial_no());

		// 取得要实现的计数信息
		Map<String, String> setTimesMap = new HashMap<String, String>();

		String client_address = null;

		for (String id : setTimesEntityMap.keySet()) {
			if(client_address == null) client_address = setTimesEntityMap.get(id).getClient_address();
			// 各计数的计数标准
			setTimesMap.put(id, "" + setTimesEntityMap.get(id).getStep_times());
		}

		// 返回要实现的计数信息
		map.put("set_times_map", setTimesMap);

		// 就是前次
		map.put("counted_times_map", decodedRec);

		return scUtil.clientSendMessage(client_address, 50023, "In:" + JSON.encode(map));
	}

	public String finishProcedureStepCount(String model_id, String position_id, String px, SqlSessionManager conn) {
		SocketCommunitcator scUtil = new SocketCommunitcator();
		
		// 取得要实现的计数信息
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity entity = new ProcedureStepCountEntity();
		entity.setRelation_type(RELATION_TYPE_MODEL);
		entity.setRelation_id(model_id);
		entity.setPosition_id(position_id);
		if ("1".equals(px)) {
			entity.setPx(1);
		} else if ("2".equals(px)) {
			entity.setPx(2);
		}
		List<ProcedureStepCountEntity> list = mapper.searchProcedureStepOfModel(entity);

		if (list.size() == 0) {
			return "NotSetException";
		}

		return scUtil.clientSendMessage(list.get(0).getClient_address(), 50023, "Out:");
	}

	/**
	 * 反映计数况情况
	 * 
	 * @param decodedRec
	 * @param material_id
	 * @param user
	 * @param listResponse
	 * @param infoes
	 * @param conn
	 * @throws Exception
	 */
	public void confirmFinish(Map<String, String> decodedRec,
			String material_id, LoginData user, List<ProcedureStepCountEntity> standardlist,
			Map<String, Object> listResponse, List<MsgInfo> infoes,
			SqlSessionManager conn) throws Exception {
		Map<String, ProcedureStepCountEntity> setTimesMap = new HashMap<String, ProcedureStepCountEntity>();

		MaterialService mService = new MaterialService();
		MaterialEntity mEntity = mService.loadSimpleMaterialDetailEntity(conn, material_id);

		for (ProcedureStepCountEntity pscEntity : standardlist) {
			setTimesMap.put(pscEntity.getProcedure_step_count_id(), pscEntity);
		}

		boolean leak = false;
		String warningContents = "";

		for (String key : decodedRec.keySet()) {
			if (setTimesMap.containsKey(key)) {
				int setTimes = 0, actualTimes = 0;
				try {
					setTimes = setTimesMap.get(key).getStep_times(); 
					actualTimes = Integer.parseInt(decodedRec.get(key));

					if (actualTimes < setTimes) {
						MsgInfo info = new MsgInfo();
						info.setErrcode("info.inline.procedureStep.leakCounting");
						info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.inline.procedureStep.leakCounting", setTimesMap.get(key).getName(), setTimes, actualTimes));
						
						// info.setErrmsg("当前维修对象作业[" + setTimesMap.get(key).getName() + "]应当进行 " + setTimes + " 次，实际记录 " + actualTimes + " 次。请操作达到计数。");
						infoes.add(info);
						_log.info(info.getErrmsg());
//						alarmMessageContents += info.getErrmsg();
						leak = true;
					} else if (actualTimes > setTimes) {
						warningContents += ApplicationMessage.WARNING_MESSAGES.getMessage("info.inline.procedureStep.overCounting", setTimesMap.get(key).getName(), setTimes, actualTimes);
						// alarmMessageContents += "当前维修对象作业[" + setTimesMap.get(key).getName() + "]应当进行 " + setTimes + " 次，实际已经记录了 " + actualTimes + " 次。";
					}

				} catch (NumberFormatException e) {
				}
			}
		}

		if (warningContents.length() > 0) {
			listResponse.put("procedure_step_count_message", warningContents);
			AlarmMesssageService amService = new AlarmMesssageService();
			amService.createPscAlarmMessage(material_id, mEntity.getSorc_no(), warningContents, user, conn);
			_log.info(warningContents);
		}

		// 如果计数错误则继续计数
		if (leak) {
			String pscString = startProcedureStepCountContinue(mEntity, setTimesMap, decodedRec, conn);
			_log.info(pscString);
		}
	}

	/**
	 * 检索记录列表
	 * @param form 提交表单
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @return List<ModelForm> 查询结果表单
	 */
	public List<ProcedureStepCountForm> search(ActionForm form, SqlSession conn) {
		ProcedureStepCountEntity conditionBean = new ProcedureStepCountEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

		// 从数据库中查询记录
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		List<ProcedureStepCountEntity> list = mapper.searchProcedureStepCount(conditionBean);

		List<ProcedureStepCountForm> retList = new ArrayList<ProcedureStepCountForm>();
		BeanUtil.copyToFormList(list, retList, CopyOptions.COPYOPTIONS_NOEMPTY, ProcedureStepCountForm.class);

		return retList;
	}

	/**
	 * 作业步骤计数详细信息
	 * 
	 * @param form
	 * @param dtlResponse 
	 * @param conn
	 * @param errors
	 * @return
	 */
	public void getDetail(ActionForm form, Map<String, Object> dtlResponse, SqlSession conn,
			List<MsgInfo> errors) {
		// 表单复制到数据对象
		ProcedureStepCountEntity coditionBean = new ProcedureStepCountEntity();
		BeanUtil.copyToBean(form, coditionBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		String id = coditionBean.getProcedure_step_count_id();

		// 从数据库中作业步骤计数种类
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		List<ProcedureStepCountEntity> detailEntities = mapper.searchProcedureStepCount(coditionBean);

		ProcedureStepCountForm detailForm = new ProcedureStepCountForm();

		if (detailEntities.size() > 0) {
			BeanUtil.copyToForm(detailEntities.get(0), detailForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		// 查询结果放入Ajax响应对象
		dtlResponse.put("detailForm", detailForm);

		// 从数据库中作业步骤型号计数
		List<ProcedureStepCountEntity> modelList = mapper.searchProcedureStepOfModel(coditionBean);

		dtlResponse.put("modelList", modelList);
	}

	/**
	 * 检查机型关联计数并返回
	 * 
	 * @param form
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<ProcedureStepCountEntity> customValidate(Map<String, String[]> parameterMap, List<MsgInfo> errors) {

		List<ProcedureStepCountForm> procedureStepCountForms = new AutofillArrayList<ProcedureStepCountForm>(ProcedureStepCountForm.class);
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		
		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("models".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("step_times".equals(column)) {
						procedureStepCountForms.get(icounts).setStep_times(value[0]);
					} else if ("relation_id".equals(column)) {
						procedureStepCountForms.get(icounts).setRelation_id(value[0]);
					}
				}
			}
		}

		List<ProcedureStepCountEntity> procedureStepCountEntities = new ArrayList<ProcedureStepCountEntity>();
		for (ProcedureStepCountForm procedureStepCountForm : procedureStepCountForms) {
			Validators v = BeanUtil.createBeanValidators(procedureStepCountForm, BeanUtil.CHECK_TYPE_PASSEMPTY);
			errors.addAll(v.validate());
			ProcedureStepCountEntity procedureStepCountEntity = new ProcedureStepCountEntity();
			BeanUtil.copyToBean(procedureStepCountForm, procedureStepCountEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
			procedureStepCountEntities.add(procedureStepCountEntity);
		}
		return procedureStepCountEntities;
	}

	/**
	 * 插入数据库
	 * 
	 * @param form
	 * @param procedureStepOfModels
	 * @param session
	 * @param conn
	 */
	public void insert(ActionForm form,
			List<ProcedureStepCountEntity> procedureStepOfModels,
			HttpSession session, SqlSessionManager conn) {
		// 插入作业步骤名称
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity insertBean = new ProcedureStepCountEntity();
		BeanUtil.copyToBean(form, insertBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		insertBean.setUpdated_by(user.getOperator_id());

		mapper.insertProcedureStepCount(insertBean);

		// 集合中加入此工位
		if (countPositionSet != null)
			countPositionSet.add(insertBean.getPosition_id());

		CommonMapper cmapper = conn.getMapper(CommonMapper.class);
		String lastInsertID = cmapper.getLastInsertID();

		// 插入作业步骤机型次数
		for (ProcedureStepCountEntity procedureStepOfModel : procedureStepOfModels) {
			procedureStepOfModel.setProcedure_step_count_id(lastInsertID);
			procedureStepOfModel.setRelation_type(1);
			mapper.insertProcedureStepOfModel(procedureStepOfModel);
		}

	}

	public void update(ActionForm form,
			List<ProcedureStepCountEntity> procedureStepOfModels,
			HttpSession session, SqlSessionManager conn) {
		// 变更作业步骤名称
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity updateBean = new ProcedureStepCountEntity();
		BeanUtil.copyToBean(form, updateBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		updateBean.setUpdated_by(user.getOperator_id());

		mapper.updateProcedureStepCount(updateBean);

		// 删除作业步骤机型次数
		mapper.deleteProcedureStepOfModel(updateBean);

		// 插入作业步骤机型次数
		for (ProcedureStepCountEntity procedureStepOfModel : procedureStepOfModels) {
			procedureStepOfModel.setProcedure_step_count_id(updateBean.getProcedure_step_count_id());
			procedureStepOfModel.setRelation_type(1);
			mapper.insertProcedureStepOfModel(procedureStepOfModel);
		}

		// 等待重新记录集合
		countPositionSet = null;
	}

	public void dodelete(ActionForm form,
			HttpSession session, SqlSessionManager conn) {
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);

		ProcedureStepCountEntity updateBean = new ProcedureStepCountEntity();
		BeanUtil.copyToBean(form, updateBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		mapper.deleteProcedureStepCount(updateBean);
		mapper.deleteProcedureStepOfModel(updateBean);
	}

	/**
	 * 维修品中断时临时记录计数
	 * 
	 * @param decodedRec
	 * @param material_id
	 * @param user
	 * @param conn
	 */
	public void recordBreak(Map<String, String> decodedRec, String material_id, Integer rework,
			LoginData user, SqlSessionManager conn) {
		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);

		for (String key : decodedRec.keySet()) {
			int actualTimes = 0;
			try {
				actualTimes = Integer.parseInt(decodedRec.get(key));
			} catch (NumberFormatException e) {
			}

			if (actualTimes == 0) continue;

			int px = 0;
			if ("1".equals(user.getPx())) {
				px = 1;
			} else if ("2".equals(px)) {
				px = 2;
			}

			ProcedureStepCountEntity entity = new ProcedureStepCountEntity();
			entity.setProcedure_step_count_id(key);
			entity.setStep_times(actualTimes);
			entity.setMaterial_id(material_id);
			entity.setPx(px);
			entity.setRework(rework);

			mapper.insertProcedureStepRecord(entity);
		}
	}
}
