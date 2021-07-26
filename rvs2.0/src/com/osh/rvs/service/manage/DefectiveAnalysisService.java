package com.osh.rvs.service.manage;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.AlarmMesssageEntity;
import com.osh.rvs.bean.inline.PauseFeatureEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisPartialEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisPhotoEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisQaEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisSearchEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.manage.DefectiveAnalysisForm;
import com.osh.rvs.mapper.data.AlarmMesssageMapper;
import com.osh.rvs.mapper.manage.DefectiveAnalysisMapper;
import com.osh.rvs.service.AlarmMesssageService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class DefectiveAnalysisService {

	private Logger logger = Logger.getLogger(getClass());

	private final static int STEP_NOTYET = -1;
	/** 不良提出 */
	private final static int STEP_POINTOUT = 0;
	/** 原因分析 */
	private final static int STEP_ANALYSIS = 1;
	/** 对策立案 */
	private final static int STEP_CASED = 2;
	/** 对策待实现 */
	private final static int STEP_REALIZING = 3;
	/** 对策待确认 */
	private final static int STEP_CONFIRM = 4;
	/** 委托关闭判断 */
	private final static int STEP_FINAL = 5;
	/** 关闭 */
	private final static int STEP_CLOSED = 9;

	/** 最终检查不良 */
	public final static int DEFECTIVE_TYPE_QA = 1;

	/** 新品零件不良 */
	public final static int DEFECTIVE_TYPE_XP = 2;

	/** 工程内发现 */
	public final static int DEFECTIVE_TYPE_FX = 3;

	/** 工程内不良 */
	public final static int DEFECTIVE_TYPE_BL = 4;

	/** 编辑权限 */
	public final static int POWERID_READONLY = 0;
	private final static int POWERID_LINELEADER = 1;
	private final static int POWERID_LINEMANAGER = 2;
	private final static int POWERID_QA_PROCESSOR = 3;
	private final static int POWERID_QA_MANAGER = 4;
	private final static int POWERID_TECHNOLOGY = 5;
	private final static int POWERID_TECH_MANAGER = 6;
	private final static int POWERID_FACTOR = 7;
	private final static int POWERID_SUPPORT_MANAGER = 8;

	/**
	 * 检索处理
	 * @param form
	 * @param department
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<DefectiveAnalysisForm> search(ActionForm form, SqlSession conn, List<MsgInfo> errors) {

		List<DefectiveAnalysisForm> ret = new ArrayList<DefectiveAnalysisForm>();

		DefectiveAnalysisSearchEntity bean = new DefectiveAnalysisSearchEntity();

		BeanUtil.copyToBean(form, bean, CopyOptions.COPYOPTIONS_NOEMPTY);

		DefectiveAnalysisMapper dao = conn.getMapper(DefectiveAnalysisMapper.class);

		try {
			List<DefectiveAnalysisSearchEntity> entitys = dao.search(bean);

			BeanUtil.copyToFormList(entitys, ret, CopyOptions.COPYOPTIONS_NOEMPTY, DefectiveAnalysisForm.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
			MsgInfo arg0 = new MsgInfo();
			arg0.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.search.timeout"));
			errors.add(arg0);
		}

		return ret;
	}

	public DefectiveAnalysisSearchEntity findEntityById(String alarm_message_id, SqlSession conn) {
		DefectiveAnalysisMapper dao = conn.getMapper(DefectiveAnalysisMapper.class);

		return dao.findOneById(alarm_message_id);
	}


	public DefectiveAnalysisForm findById(String alarmMessageId, SqlSession conn) throws Exception {

		DefectiveAnalysisForm form = new DefectiveAnalysisForm();

		DefectiveAnalysisMapper dao = conn.getMapper(DefectiveAnalysisMapper.class);

		DefectiveAnalysisSearchEntity entity = dao.findOneById(alarmMessageId);

		BeanUtil.copyToForm(entity, form, CopyOptions.COPYOPTIONS_NOEMPTY);

		if (entity.getStep() == STEP_NOTYET) {
			// 还没生成
			// 取得不良分类
			Integer defectiveTypeByBreakType = null;
			if (RvsConsts.WARNING_REASON_QAFORBID ==  entity.getReason()) {
				defectiveTypeByBreakType = DefectiveAnalysisService.DEFECTIVE_TYPE_QA;
			} else {
				defectiveTypeByBreakType = getDefectiveTypeByBreakType(""+alarmMessageId, conn);
			}

			if (defectiveTypeByBreakType != null) {
				form.setDefective_type("" + defectiveTypeByBreakType);
				// 取得管理编号
				form.setManage_code(getAutoManageCode(defectiveTypeByBreakType , conn));
			}
		}

		// 警报处理相关信息
		AlarmMesssageService amService = new AlarmMesssageService();
		AlarmMesssageMapper mapper = conn.getMapper(AlarmMesssageMapper.class);
		AlarmMesssageEntity breakAlarmMessage = mapper.getBreakAlarmMessageByKey(alarmMessageId);

		form.setAlarm_comments(amService.getComments(breakAlarmMessage, alarmMessageId, mapper));

		// 不良分类
		form.setDefective_type_disp(CodeListUtils.getValue("defective_type", form.getDefective_type()));
		// 对策进度
		form.setStep_disp(CodeListUtils.getValue("defective_step", form.getStep()));
		// 责任区分（生产线）
		form.setResponsibility_of_line_disp(CodeListUtils.getValue("defective_responsibility_of_line", form.getResponsibility_of_line()));
		// 责任区分（技术）
		form.setResponsibility_of_ptl_disp(CodeListUtils.getValue("defective_responsibility_of_ptl", form.getResponsibility_of_ptl()));
		// CAPA频度判断
		form.setCapa_frequency_disp(CodeListUtils.getValue("defective_capa_frequency", form.getCapa_frequency()));
		// CAPA重大度判断
		form.setCapa_major_disp(CodeListUtils.getValue("defective_capa_major", form.getCapa_major()));
		// 风险大小等级
		form.setCapa_risk_disp(CodeListUtils.getValue("defective_capa_risk", form.getCapa_risk()));
		// 入库零件不良处理
		form.setStored_parts_resolve_disp(CodeListUtils.getValue("defective_stored_parts_resolve", form.getStored_parts_resolve()));
		// 返工应对
		form.setRework_proceed_disp(setupReworkProceedValue(form.getRework_proceed()));
		// 维修等级
		form.setLevel_disp(CodeListUtils.getValue("material_level", form.getLevel()));
		// 波及性判断结果
		form.setInvolving_disp(setupInvolvingValue(form.getInvolving()));

		return form;
	}

	/**
	 * 创建处理
	 * @param form
	 * @param paramMap 
	 * @param orgEntity 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public Integer maintain(ActionForm form, Map<String, String[]> paramMap, DefectiveAnalysisSearchEntity orgEntity, 
			SqlSessionManager conn, Integer powerId, Integer userId, List<MsgInfo> errors) throws Exception {

		DefectiveAnalysisMapper dao = conn.getMapper(DefectiveAnalysisMapper.class);

		DefectiveAnalysisForm bean = (DefectiveAnalysisForm) form;

		String alarmMessageId = bean.getAlarm_message_id();

		DefectiveAnalysisSearchEntity dbEntity = dao.findOneById(alarmMessageId);

		switch (dbEntity.getStep()) {
		case STEP_POINTOUT:
			// 不良提出
			maintainStep0(bean, powerId, alarmMessageId, userId, orgEntity, dao, errors);
			break;
		case STEP_ANALYSIS:
			// 原因分析
			maintainStep1(bean, powerId, alarmMessageId, userId, orgEntity, dao, errors);
			break;
		case STEP_CASED:
			// 对策立案
			maintainStep2(bean, powerId, alarmMessageId, userId, orgEntity, dao, errors);
			break;
		case STEP_REALIZING:
			// 对策待实施
			maintainStep3(bean, powerId, alarmMessageId, userId, orgEntity, dao, errors);
			break;
		case STEP_CONFIRM:
			// 对策效果待确认
			maintainStep4(bean, powerId, alarmMessageId, userId, orgEntity, dao, errors);
			break;
		case STEP_FINAL:
			// 委托关闭判断
			maintainStep5(bean, powerId, alarmMessageId, userId, dao);
			break;
		default:
			// 关闭
			break;
		}

		if (errors.size() == 0) {
			photoLoadStep(paramMap, powerId, alarmMessageId, dbEntity.getStep(), bean.getDefective_type(), dao);
		}

		return dbEntity.getStep();
	}


	/**
	 * 处理通知
	 *  
	 * @param alarm_message_id
	 * @param step
	 * @param conn
	 */
	public void postMessage(String alarm_message_id, Integer step,
			SqlSessionManager conn) {
		switch (step) {
		case STEP_POINTOUT:
			// 不良提出

			// 通知上级经理 TODO
			// 通知技术人员 TODO
			// 如有零件相关通知现品线长 TODO

			// 通知技术人员 TODO

			break;
		case STEP_ANALYSIS:
			// 原因分析
			// 通知技术经理 TODO
			break;
		case STEP_CASED:
			// 对策立案
			// 通知技术经理 TODO
			break;
		case STEP_REALIZING:
			// 对策待实施
			// 通知上级经理 TODO
			break;
		case STEP_CONFIRM:
			// 对策效果待确认
			// 通知上级经理 TODO
			break;
		case STEP_FINAL:
		default:
		}
	}


	/**
	 * 上传或加载图片
	 * 
	 * @param paramMap
	 * @param powerId 
	 * @param alarmMessageId
	 * @param step
	 * @param defectiveType 
	 * @param dao
	 */
	private void photoLoadStep(Map<String, String[]> paramMap,
			Integer powerId, String alarmMessageId, Integer step, String defectiveType, DefectiveAnalysisMapper mapper) {
		// 判断编辑权限
		switch (step) {
		case STEP_POINTOUT:
			if (defectiveType.equals(""+DEFECTIVE_TYPE_QA)) {
				if (powerId != POWERID_QA_PROCESSOR) {
					return;
				}
			} else {
				if (powerId != POWERID_LINELEADER) {
					return;
				}
			}
			break;
		case STEP_ANALYSIS:
			if (powerId != POWERID_TECHNOLOGY) return;
			break;
		case STEP_CASED:
			// 对策立案
			if (powerId != POWERID_LINELEADER) return;
			break;
		case STEP_REALIZING:
			return;
		case STEP_CONFIRM:
			// 对策效果待确认
			if (powerId != POWERID_LINELEADER) return;
			break;
		case STEP_FINAL:
			return;
		default:
			// 关闭
			break;
		}

		// 取得现有本步骤图片
		List<DefectiveAnalysisPhotoEntity> orgList = mapper.findPhotoById(alarmMessageId);
		Map<Integer, String> orgMap = new HashMap<Integer, String>(); 
		for (DefectiveAnalysisPhotoEntity photoEntity: orgList) {
			if (photoEntity.getFor_step() == step) {
				orgMap.put(photoEntity.getSeq(), photoEntity.getFile_uuid());
			}
		}

		List<DefectiveAnalysisPhotoEntity> postPhotos = new AutofillArrayList<DefectiveAnalysisPhotoEntity>(DefectiveAnalysisPhotoEntity.class);
		Pattern p = Pattern.compile("(\\w+)\\[(\\d+)\\].(\\w+)");

		// 整理提交数据
		for (String parameterKey : paramMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("photo".equals(entity)) {
					String column = m.group(3);
					int icounts = Integer.parseInt(m.group(2));
					String[] value = paramMap.get(parameterKey);
					switch(column) {
					case "seq" : postPhotos.get(icounts).setSeq(Integer.parseInt(value[0])); break;
					case "for_step" : postPhotos.get(icounts).setFor_step(Integer.parseInt(value[0])); break;
					case "file_uuid" : postPhotos.get(icounts).setFile_uuid(value[0]); break;
					}
				}
			}
		}
		List<DefectiveAnalysisPhotoEntity> insPhotos = new ArrayList<DefectiveAnalysisPhotoEntity>();
		for (DefectiveAnalysisPhotoEntity postPhoto : postPhotos) {
			if (postPhoto.getFor_step() == step) {
				insPhotos.add(postPhoto);
			}
		}

		// 判断是否有更改
		boolean changed = orgMap.size() != insPhotos.size();
		if (!changed) {
			for (DefectiveAnalysisPhotoEntity insPhoto : insPhotos) {
				if (insPhoto.getFile_uuid().equals(orgMap.get(insPhoto.getSeq()))) {
					changed = true;
					break;
				}
			}
		}

		if (changed) {
			DefectiveAnalysisPhotoEntity delEntity = new DefectiveAnalysisPhotoEntity();
			delEntity.setAlarm_message_id(alarmMessageId);
			delEntity.setFor_step(step);
			mapper.deletePhoto(delEntity);

			for (DefectiveAnalysisPhotoEntity insPhoto : insPhotos) {
				insPhoto.setAlarm_message_id(alarmMessageId);
				mapper.insertPhoto(insPhoto);
			}
		}
	}

	/**
	 * 不良提出阶段
	 * @param bean
	 * @param powerId
	 * @param alarmMessageId
	 * @param userId
	 * @param orgEntity 
	 * @param dao
	 * @param errors 
	 * @return
	 * @throws Exception
	 */
	private void maintainStep0(DefectiveAnalysisForm bean, Integer powerId, String alarmMessageId, Integer userId, DefectiveAnalysisSearchEntity orgEntity, DefectiveAnalysisMapper dao, 
			List<MsgInfo> errors) throws Exception {

		boolean isExist = false;
		if (dao.count(alarmMessageId) == 1) {
			isExist = true;
		}
		if (isEmpty(bean.getDefective_type())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("defective_type");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "不良分类"));
			errors.add(error);
		}
		if (isEmpty(bean.getManage_code())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("manage_code");
			error.setErrcode("validator.required");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "管理编号"));
			errors.add(error);
		} else {
			String existId = dao.checkDuplicateManageCode(bean.getManage_code());
			if (existId != null && !existId.equals(CommonStringUtil.fillChar(alarmMessageId, '0', 11, true))) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("manage_code");
				error.setErrcode("dbaccess.columnNotUnique");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "管理编号", bean.getManage_code(), "不良报告"));
				errors.add(error);
			}
		}
		if (errors.size() > 0) {
			return;
		}
		// 不良提出阶段
		if (bean.getDefective_type().equals(""+DEFECTIVE_TYPE_QA)) {
			// 不良分类 = 最终不良品时
			if (powerId == POWERID_QA_PROCESSOR) {
				// 品质担当 执行 不良提出
				if (isExist) {
					// update
					DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

					entity.setAlarm_message_id(alarmMessageId);
					entity.setDefective_type(Integer.valueOf(bean.getDefective_type()));
					entity.setManage_code(bean.getManage_code());
					entity.setDefective_phenomenon(bean.getDefective_phenomenon());

					dao.updateSponsor(entity);

					DefectiveAnalysisQaEntity qaEntity = new DefectiveAnalysisQaEntity();
					qaEntity.setAlarm_message_id(alarmMessageId);
					qaEntity.setDefective_items(bean.getDefective_items());

					dao.updateQa(qaEntity);
				} else {
					// insert
					DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

					entity.setAlarm_message_id(alarmMessageId);
					entity.setDefective_type(Integer.valueOf(bean.getDefective_type()));
					entity.setManage_code(bean.getManage_code());
					entity.setStep(STEP_POINTOUT); // 不良提出
					entity.setSponsor_operator_id(userId);
					// 不良现象
					entity.setDefective_phenomenon(bean.getDefective_phenomenon());

					// QA 必定要返工
					entity.setRework_proceed(1);

					dao.insert(entity);

					// insert QA
					DefectiveAnalysisQaEntity qaEntity = new DefectiveAnalysisQaEntity();

					qaEntity.setAlarm_message_id(alarmMessageId);
					qaEntity.setDefective_items(bean.getDefective_items());

					dao.insertQa(qaEntity);
				}
			} else if (powerId == POWERID_LINEMANAGER || powerId == POWERID_QA_MANAGER) {
				// 品质上级 执行 不良提出确认
				DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();
				entity.setAlarm_message_id(alarmMessageId);
				entity.setPhenomenon_confirmer_id(userId);
				entity.setStep(STEP_ANALYSIS); // 原因分析

				dao.updatePhenomenonConfirmer(entity);
			}
		} else {
			// 工程担当 执行 不良提出
			if (isExist) {
				// 不良分类 <> 最终检查不良
				if (powerId == POWERID_LINELEADER) {
					// update
					DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

					entity.setAlarm_message_id(alarmMessageId);
					entity.setDefective_type(Integer.valueOf(bean.getDefective_type()));
					entity.setManage_code(bean.getManage_code());
					entity.setDefective_phenomenon(bean.getDefective_phenomenon());
					entity.setAppend_part_order(bean.getAppend_part_order());
					entity.setRework_proceed(Integer.valueOf(bean.getRework_proceed()));

					dao.updateSponsor(entity);

					// update QA
					DefectiveAnalysisPartialEntity partialEntity = new DefectiveAnalysisPartialEntity();

					partialEntity.setAlarm_message_id(alarmMessageId);
					partialEntity.setNongood_parts_situation(bean.getNongood_parts_situation());
					partialEntity.setReceive_date(DateUtil.toDate(bean.getReceive_date(), DateUtil.DATE_PATTERN));
					partialEntity.setStored_parts(bean.getStored_parts());
					partialEntity.setStored_parts_resolve(Integer.valueOf(bean.getStored_parts_resolve()));

					dao.updatePartial(partialEntity);
				} else if (powerId == POWERID_LINEMANAGER || powerId == POWERID_QA_MANAGER) {

					if (isEmpty(orgEntity.getDefective_phenomenon())) {
						MsgInfo e = new MsgInfo();
						e.setComponentid("defective_phenomenon");
						e.setErrcode("validator.required");
						e.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "不良现象"));
						// 没有提交故障现象
						errors.add(e);
					} else {
						// 工程上级 执行 不良提出确认
						DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();
						entity.setAlarm_message_id(alarmMessageId);
						entity.setStep(STEP_ANALYSIS); // 原因分析
						entity.setPhenomenon_confirmer_id(userId);

						dao.updatePhenomenonConfirmer(entity);
					}
				}
			} else {
				// insert 不良分析对策
				DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

				entity.setAlarm_message_id(alarmMessageId);
				entity.setDefective_type(Integer.valueOf(bean.getDefective_type()));
				entity.setManage_code(bean.getManage_code());
				entity.setStep(STEP_POINTOUT); // 不良提出
				entity.setSponsor_operator_id(userId);
				entity.setDefective_phenomenon(bean.getDefective_phenomenon());
				entity.setResponsibility_of_line(Integer.valueOf(bean.getResponsibility_of_line()));
				entity.setAppend_part_order(bean.getAppend_part_order());
				entity.setRework_proceed(Integer.valueOf(bean.getRework_proceed()));

				dao.insert(entity);

				// insert 不良分析对策新品零件
				DefectiveAnalysisPartialEntity partialEntity = new DefectiveAnalysisPartialEntity();

				partialEntity.setAlarm_message_id(alarmMessageId);
				partialEntity.setNongood_parts_situation(bean.getNongood_parts_situation());
				partialEntity.setReceive_date(DateUtil.toDate(bean.getReceive_date(), DateUtil.DATE_PATTERN));
				partialEntity.setStored_parts(bean.getStored_parts());
				partialEntity.setStored_parts_resolve(Integer.valueOf(bean.getStored_parts_resolve()));

				dao.insertPartial(partialEntity);
			}
		}

		return;
	}

	/**
	 * 原因分析
	 * @param bean
	 * @param powerId
	 * @param alarmMessageId
	 * @param userId
	 * @param orgEntity 
	 * @param dao
	 * @param errors 
	 * @return
	 * @throws Exception
	 */
	private Integer maintainStep1(DefectiveAnalysisForm bean, Integer powerId, String alarmMessageId, Integer userId,
			DefectiveAnalysisSearchEntity orgEntity, DefectiveAnalysisMapper dao, List<MsgInfo> errors) throws Exception {
		if (powerId == POWERID_LINELEADER) {
			// 工程担当

			// ****************
			// update 不良分析对策
			// ****************
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();
			// 警报ID
			entity.setAlarm_message_id(alarmMessageId);
			// 责任区分（生产线）
			entity.setResponsibility_of_line(Integer.valueOf(bean.getResponsibility_of_line()));
			// 更换零件对应
			entity.setAppend_part_order(bean.getAppend_part_order());
			// 返工对应
			entity.setRework_proceed(Integer.valueOf(bean.getRework_proceed()));

			dao.updateCauseAnalysis(entity);

			// **********************
			// update 不良分析对策新品零件
			// **********************
			DefectiveAnalysisPartialEntity partialEntity = new DefectiveAnalysisPartialEntity();

			partialEntity.setAlarm_message_id(alarmMessageId);
			// 不良零件情况
			partialEntity.setNongood_parts_situation(bean.getNongood_parts_situation());
			// 领取日期
			partialEntity.setReceive_date(DateUtil.toDate(bean.getReceive_date(), DateUtil.DATE_PATTERN));
			// 入库部品
			partialEntity.setStored_parts(bean.getStored_parts());
			// 入库零件不良处理
			partialEntity.setStored_parts_resolve(Integer.valueOf(bean.getStored_parts_resolve()));

			dao.updatePartial(partialEntity);
		} else if (powerId == POWERID_TECHNOLOGY) {
			// 技术担当

			// *****************
			// update 不良分析对策
			// *****************
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();
			// 警报ID
			entity.setAlarm_message_id(alarmMessageId);
			// 责任区分（技术）
			entity.setResponsibility_of_ptl(bean.getResponsibility_of_ptl());
			// 原因分析
			entity.setCause_analysis(bean.getCause_analysis());
			// 原因分析者
			entity.setCause_analyst_id(userId);
			// CAPA频度判断
			entity.setCapa_frequency(Integer.valueOf(bean.getCapa_frequency()));
			// CAPA重大度判断
			entity.setCapa_major(Integer.valueOf(bean.getCapa_major()));
			// 风险大小等级
			entity.setCapa_risk(Integer.valueOf(bean.getCapa_risk()));

			dao.updateCauseAnalysis(entity);

			// ******************
			// update 不良分析对策终检
			// ******************
			DefectiveAnalysisQaEntity qaEntity = new DefectiveAnalysisQaEntity();

			qaEntity.setAlarm_message_id(alarmMessageId);
			// 波及性判断结果
			qaEntity.setInvolving(Integer.valueOf(bean.getInvolving()));
			// 波及性判断理由
			qaEntity.setInvolving_reason(bean.getInvolving_reason());

			dao.updateQa(qaEntity);
		} else if (powerId == POWERID_FACTOR) {
			// 零件订购

			// *************
			// update 不良分析对策
			// *************
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();
			entity.setAlarm_message_id(alarmMessageId);
			// 追加订购者
			if (bean.getPartial_applyier_id() != null) {
				entity.setPartial_applyier_id(userId);
			} else {
				entity.setPartial_applyier_id(0);
			}

			dao.updateCauseAnalysis(entity);

			// *************
			// update 不良分析对策新品零件
			// *************
			DefectiveAnalysisPartialEntity partialEntity = new DefectiveAnalysisPartialEntity();

			partialEntity.setAlarm_message_id(alarmMessageId);
			// 零件定单次数
			partialEntity.setOccur_times(Integer.valueOf(bean.getOccur_times()));

			dao.updatePartial(partialEntity);

		} else if (powerId == POWERID_TECH_MANAGER) {
			// 技术上级
			// 执行 原因分析确认
			if (isEmpty(orgEntity.getCause_analysis())) {
				MsgInfo e = new MsgInfo();
				e.setComponentid("cause_analysis");
				e.setErrcode("validator.required");
				e.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "原因分析"));
				// 没有提交故障现象
				errors.add(e);
			} else {
				// *************
				// update 不良分析对策
				// *************
				DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

				entity.setAlarm_message_id(alarmMessageId);
				entity.setCause_confirmer_id(userId);
				entity.setStep(STEP_CASED); // 对策立案

				dao.updateCauseConfirmer(entity);
			}
		}
		return null;
	}

	/**
	 * 对策立案
	 * @param bean
	 * @param powerId
	 * @param alarmMessageId
	 * @param userId
	 * @param orgEntity 
	 * @param dao
	 * @param errors 
	 * @return
	 * @throws Exception
	 */
	private Integer maintainStep2(DefectiveAnalysisForm bean, Integer powerId, String alarmMessageId, Integer userId,
			DefectiveAnalysisSearchEntity orgEntity, DefectiveAnalysisMapper dao, List<MsgInfo> errors) throws Exception {

		if (powerId == POWERID_LINELEADER) {
			// 工程担当

			// ****************
			// update 不良分析对策
			// ****************
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();
			// 警报ID
			entity.setAlarm_message_id(alarmMessageId);
			// 责任区分（生产线）
			entity.setResponsibility_of_line(Integer.valueOf(bean.getResponsibility_of_line()));
			// 更换零件对应
			entity.setAppend_part_order(bean.getAppend_part_order());
			// 返工对应
			entity.setRework_proceed(Integer.valueOf(bean.getRework_proceed()));

			dao.updateCauseAnalysis(entity);

			// **********************
			// update 不良分析对策新品零件
			// **********************
			DefectiveAnalysisPartialEntity partialEntity = new DefectiveAnalysisPartialEntity();

			partialEntity.setAlarm_message_id(alarmMessageId);
			// 不良零件情况
			partialEntity.setNongood_parts_situation(bean.getNongood_parts_situation());
			// 领取日期
			partialEntity.setReceive_date(DateUtil.toDate(bean.getReceive_date(), DateUtil.DATE_PATTERN));
			// 入库部品
			partialEntity.setStored_parts(bean.getStored_parts());
			// 入库零件不良处理
			partialEntity.setStored_parts_resolve(Integer.valueOf(bean.getStored_parts_resolve()));

			dao.updatePartial(partialEntity);
		} else if (powerId == POWERID_TECHNOLOGY) {
			// 技术担当

			// ********************
			// update 不良分析对策
			// ********************
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

			entity.setAlarm_message_id(alarmMessageId);
			entity.setCountermeasures(bean.getCountermeasures());
			entity.setCm_filer_id(userId);

			dao.updateCmFiler(entity);

		} else if (powerId == POWERID_TECH_MANAGER) {
			// 技术上级
			// 执行 对策立案确认
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

			entity.setAlarm_message_id(alarmMessageId);
			entity.setCm_confirmer_id(userId);
			entity.setStep(STEP_REALIZING); // 对策待实施

			dao.updateCmConfirmer(entity);
		} else if (powerId == POWERID_FACTOR) {
			// 零件订购担当 确认追加订购

			// *************
			// update 不良分析对策
			// *************
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();
			entity.setAlarm_message_id(alarmMessageId);
			// 追加订购者
			entity.setPartial_applyier_id(userId);

			dao.updateCauseAnalysis(entity);

			// *************
			// update 不良分析对策新品零件
			// *************
			DefectiveAnalysisPartialEntity partialEntity = new DefectiveAnalysisPartialEntity();

			partialEntity.setAlarm_message_id(alarmMessageId);
			// 零件定单次数
			partialEntity.setOccur_times(Integer.valueOf(bean.getOccur_times()));

			dao.updatePartial(partialEntity);
		}
		return null;
	}

	/**
	 * 对策待实施
	 * @param bean
	 * @param powerId
	 * @param alarmMessageId
	 * @param userId
	 * @param orgEntity 
	 * @param dao
	 * @param errors 
	 * @return
	 * @throws Exception
	 */
	private Integer maintainStep3(DefectiveAnalysisForm bean, Integer powerId, String alarmMessageId, Integer userId,
			DefectiveAnalysisSearchEntity orgEntity, DefectiveAnalysisMapper dao, List<MsgInfo> errors) throws Exception {

		if (powerId == POWERID_LINELEADER) {
			// 工程担当
			// 执行 对策实施

			// update 不良分析对策
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

			entity.setAlarm_message_id(alarmMessageId);
			entity.setCm_processor_id(userId);

			dao.updateCmProcessor(entity);
		} else if (powerId == POWERID_LINEMANAGER || powerId == POWERID_QA_MANAGER) {
			// 工程上级
			// 执行 对策实施确认

			if (isEmpty(orgEntity.getCountermeasures())) {
				MsgInfo e = new MsgInfo();
				e.setComponentid("countermeasures");
				e.setErrcode("validator.required");
				e.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "对策"));
				// 没有提交故障现象
				errors.add(e);
			} else {
				// update 不良分析对策
				DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

				entity.setAlarm_message_id(alarmMessageId);
				entity.setCm_proc_confirmer_id(userId);
				entity.setStep(STEP_CONFIRM); // 对策效果待确认

				dao.updateCmProcConfirmer(entity);
			}
		}

		return null;
	}

	/**
	 * 对策效果待确认
	 * @param bean
	 * @param powerId
	 * @param alarmMessageId
	 * @param userId
	 * @param orgEntity 
	 * @param dao
	 * @param errors 
	 * @return
	 * @throws Exception
	 */
	private Integer maintainStep4(DefectiveAnalysisForm bean, Integer powerId, String alarmMessageId, Integer userId,
			DefectiveAnalysisSearchEntity orgEntity, DefectiveAnalysisMapper dao, List<MsgInfo> errors) throws Exception {

		if (powerId == POWERID_LINELEADER) {
			// 工程担当
			// 执行 对策效果
			// update 不良分析对策
			DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

			entity.setAlarm_message_id(alarmMessageId);
			entity.setCountermeasure_effects(bean.getCountermeasure_effects());
			entity.setCm_effect_verifier_id(userId);

			dao.updateCmEffectVerifier(entity);
		} else if (powerId == POWERID_LINEMANAGER || powerId == POWERID_QA_MANAGER) {
			// 工程上级
			// 执行 对策效果确认

			if (isEmpty(orgEntity.getCountermeasure_effects())) {
				MsgInfo e = new MsgInfo();
				e.setComponentid("countermeasure_effects");
				e.setErrcode("validator.required");
				e.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "对策效果确认"));
				// 没有提交故障现象
				errors.add(e);
			} else {
				// update 不良分析对策
				DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();

				entity.setAlarm_message_id(alarmMessageId);
				entity.setCm_effect_confirmer_id(userId);
				if (orgEntity.getDefective_type().equals(DEFECTIVE_TYPE_QA)) {
					// 不良分类 = 1：最终检查不良品
					entity.setStep(STEP_FINAL);
				} else {
					// 不良分类 = 非最终，其他3类
					entity.setStep(STEP_CLOSED); // 关闭
				}

				dao.updateCmEffectConfirmer(entity);
			}
		}

		return null;
	}

	/**
	 * 委托关闭判断
	 * @param bean
	 * @param powerId
	 * @param alarmMessageId
	 * @param userId
	 * @param dao
	 * @return
	 * @throws Exception
	 */
	private Integer maintainStep5(DefectiveAnalysisForm bean, Integer powerId, String alarmMessageId, Integer userId, DefectiveAnalysisMapper dao) throws Exception {


		if (bean.getDefective_type().equals(DEFECTIVE_TYPE_QA)) {
			// 最终不良品时
			// 委托关闭判断
			if (powerId == POWERID_LINEMANAGER || powerId == POWERID_QA_MANAGER) {
				// 品保上级 委托关闭

				// update 不良分析对策
				DefectiveAnalysisQaEntity qaEntity = new DefectiveAnalysisQaEntity();
				qaEntity.setAlarm_message_id(alarmMessageId);
				qaEntity.setClosing_judgment(bean.getClosing_judgment());
				qaEntity.setClosing_judger_id(userId);
				dao.updateClosingJudger(qaEntity);

			} else if (powerId == POWERID_SUPPORT_MANAGER) {
				// 支援G经理 确认委托关闭

				// update 不良分析对策
				DefectiveAnalysisEntity entity = new DefectiveAnalysisEntity();
				entity.setAlarm_message_id(alarmMessageId);
				entity.setStep(STEP_CLOSED); // 关闭
				dao.updateStep(entity);

				DefectiveAnalysisQaEntity qaEntity = new DefectiveAnalysisQaEntity();
				qaEntity.setAlarm_message_id(alarmMessageId);
				qaEntity.setClosing_confirmer_id(userId);
				dao.updateClosingConfirmer(qaEntity);
			}
		}

		return null;
	}

	/**
	 * 对策实施权限判断
	 * 
	 * @param user
	 * @param isAfterResolve 
	 * @param alarm_message_id
	 * @param entity
	 * @return PowerId
	 * @throws Exception
	 */
	public Integer setupPowerId(LoginData user, boolean isAfterResolve, String alarm_message_id, DefectiveAnalysisSearchEntity entity) throws Exception {

		Integer userId = Integer.valueOf(user.getOperator_id());
		String sectionId = user.getSection_id();
		String roleId = user.getRole_id();

		Integer lineId = 0;
		if (user.getLine_id() != null) {
			lineId = Integer.valueOf(user.getLine_id());
		}

		// 系统管理员处理
		if (roleId.equals(RvsConsts.ROLE_SYSTEM)) {
			if (isAfterResolve) {
				if (entity.getDefective_type() != null && entity.getDefective_type() == DEFECTIVE_TYPE_QA) {
					roleId = RvsConsts.ROLE_QAER;
				} else {
					roleId = RvsConsts.ROLE_LINELEADER;
				}
			} else {
				roleId = RvsConsts.ROLE_MANAGER;
			}
		}

		Integer alarm_message_line_id = entity.getLine_id();
		List<Integer> userPrivacies = user.getPrivacies();

		// 建立操作
		if (entity.getSponsor_operator_id() == null || entity.getSponsor_operator_id().equals(userId)) {
			if (entity.getDefective_type() != null && entity.getDefective_type() == DEFECTIVE_TYPE_QA) {
				if (entity.getStep() == STEP_NOTYET) {
					boolean inQaLine = false;
					for (LineEntity line : user.getLines()) {
						if (line.getLine_id().equals("00000000015")) {
							inQaLine = true;
							break;
						}
					}

					if (inQaLine && userPrivacies.contains(RvsConsts.PRIVACY_POSITION)) {
						entity.setStep(STEP_POINTOUT);
						return POWERID_QA_PROCESSOR;
					}
				}
			} else {
				if (userPrivacies.contains(RvsConsts.PRIVACY_LINE)
						&& entity.getStep() == STEP_NOTYET) {
					if (!(roleId.equals(RvsConsts.ROLE_LINELEADER) && lineId != alarm_message_line_id)) {
						entity.setStep(STEP_POINTOUT);
						return POWERID_LINELEADER;
					}
				}
			}
		}

		// 对策处理全部完成
		if (entity.getStep() == STEP_CLOSED) return POWERID_READONLY;

		boolean isManager = userPrivacies.contains(RvsConsts.PRIVACY_PROCESSING);
		boolean isResponsor = userPrivacies.contains(RvsConsts.PRIVACY_LINE);
		
		if (sectionId.equals("00000000001") && roleId.equals(RvsConsts.ROLE_LINELEADER) && 
				(lineId == alarm_message_line_id || alarm_message_line_id == 15)) {
			// 工程担当
			if (entity.getDefective_type() == DEFECTIVE_TYPE_QA
					&& (entity.getStep() == STEP_POINTOUT || entity.getStep() == STEP_NOTYET)) {
				// 最终不良检查
				return POWERID_READONLY;
			}

			if (alarm_message_line_id != 15 && 
					(entity.getStep() == STEP_POINTOUT || entity.getStep() == STEP_ANALYSIS || entity.getStep() == STEP_CASED)
					&& (entity.getSponsor_operator_id() == null || entity.getSponsor_operator_id().equals(userId))) {
				return POWERID_LINELEADER;
			} else if (entity.getStep() == STEP_REALIZING && (entity.getCm_processor_id() == null || entity.getCm_processor_id().equals(userId))) {
				return POWERID_LINELEADER;
			} else if (entity.getStep() == STEP_CONFIRM && (entity.getCm_effect_verifier_id() == null || entity.getCm_effect_verifier_id().equals(userId))) {
				return POWERID_LINELEADER;
			}
		} else if (sectionId.equals("00000000001") && isManager) {
			// 工程上级
			if ((entity.getStep() == STEP_POINTOUT) && (entity.getPhenomenon_confirmer_id() == null || entity.getPhenomenon_confirmer_id().equals(userId))) {
				return POWERID_LINEMANAGER;
			} else if (entity.getStep() == STEP_REALIZING && entity.getCm_processor_id() != null
					&& (entity.getCm_proc_confirmer_id() == null || entity.getCm_proc_confirmer_id().equals(userId))) {
				return POWERID_LINEMANAGER;
			} else if (entity.getStep() == STEP_CONFIRM && entity.getCm_effect_verifier_id() != null
					&& (entity.getCm_effect_confirmer_id() == null || entity.getCm_effect_confirmer_id().equals(userId))) {
				return POWERID_LINEMANAGER;
			} else if (entity.getStep() == STEP_FINAL
					&& (entity.getClosing_judger_id() == null || entity.getClosing_judger_id().equals(userId))) {
				return POWERID_LINEMANAGER;
			}

		} else if (sectionId.equals("00000000007") && roleId.equals(RvsConsts.ROLE_OPERATOR)) {
			// 品保担当
			if (entity.getDefective_type() == 1 && entity.getStep() == STEP_POINTOUT
					&& (entity.getSponsor_operator_id() == null || entity.getSponsor_operator_id().equals(userId))) {
				return POWERID_QA_PROCESSOR;
			}
		} else if (sectionId.equals("00000000007") && isResponsor) {
			// 品保上级
			if (entity.getStep() == STEP_POINTOUT && entity.getSponsor_operator_id() != null
					&& (entity.getPhenomenon_confirmer_id() == null || entity.getPhenomenon_confirmer_id().equals(userId))) {
				return POWERID_QA_MANAGER;
			} else if (entity.getStep() == STEP_REALIZING && entity.getCm_processor_id() != null
					&& (entity.getCm_proc_confirmer_id() == null || entity.getCm_proc_confirmer_id().equals(userId))) {
				return POWERID_QA_MANAGER;
			} else if (entity.getStep() == STEP_CONFIRM && entity.getCm_effect_verifier_id() != null
					&& (entity.getCm_effect_confirmer_id() == null || entity.getCm_effect_confirmer_id().equals(userId))) {
				return POWERID_QA_MANAGER;
			} else if (entity.getStep() == STEP_FINAL
					&& (entity.getClosing_judger_id() == null || entity.getClosing_judger_id().equals(userId))) {
				return POWERID_QA_MANAGER;
			}
		} else if (sectionId.equals("00000000011") && roleId.equals(RvsConsts.ROLE_OPERATOR)) {
			// 技术担当
			if (entity.getStep() == STEP_ANALYSIS
					&& (entity.getCause_analyst_id() == null || entity.getCause_analyst_id().equals(userId))) {
				return POWERID_TECHNOLOGY;
			} else if (entity.getStep() == STEP_CASED
					&& (entity.getCm_filer_id() == null || entity.getCm_filer_id().equals(userId))) {
				return POWERID_TECHNOLOGY;
			}
		} else if (sectionId.equals("00000000011") && isResponsor) {
			// 技术上级
			if (entity.getStep() == STEP_ANALYSIS && entity.getCause_analyst_id() != null
					&& (entity.getCause_confirmer_id() == null || entity.getCause_confirmer_id().equals(userId))) {
				return POWERID_TECH_MANAGER;
			} else if (entity.getStep() == STEP_CASED && entity.getCm_filer_id() != null
					&& (entity.getCm_confirmer_id() == null || entity.getCm_confirmer_id().equals(userId))) {
				return POWERID_TECH_MANAGER;
			}

		} else if (roleId.equals(RvsConsts.ROLE_FACTINLINE)) {
			// 零件订购担当
			if ((entity.getStep() == STEP_ANALYSIS || entity.getStep() == STEP_CASED)
					&& (entity.getPartial_applyier_id() == null || entity.getPartial_applyier_id().equals(userId))) {
				return POWERID_FACTOR;
			}
		} else if (sectionId.equals("00000000002") && roleId.equals(RvsConsts.ROLE_MANAGER)) {
			// 支援G经理
			if (entity.getStep() == STEP_FINAL && entity.getClosing_judger_id() != null
					&& (entity.getClosing_confirmer_id() == null || entity.getClosing_confirmer_id().equals(userId))) {
				return POWERID_SUPPORT_MANAGER;
			}
		}

		return POWERID_READONLY;
	}

	public Map<String, String> setupReworkProceedList() {
		Map<String, String> options = new TreeMap<String, String>();
		options.put("0", "不需要");
		options.put("1", "需要");

		return options;
	}

	public String setupReworkProceedValue(String key) {
		if (key == null) {
			return "";
		}
		return setupReworkProceedList().get(key);
	}

	public Map<String, String> setupInvolvingOptions() {
		Map<String, String> options = new TreeMap<String, String>();
		options.put("0", "无");
		options.put("1", "有");
		return options;
	}
	public String setupInvolvingValue(String key) {
		if (key == null) {
			return "";
		}
		return setupInvolvingOptions().get(key);
	}

	/**
	 * 从中断理由中选择不良分类
	 * @param alarmMessageId
	 * @param conn
	 * @return
	 */
	public Integer getDefectiveTypeByBreakType(String alarmMessageId,
			SqlSession conn) {
		AlarmMesssageMapper amMapper = conn.getMapper(AlarmMesssageMapper.class);
		PauseFeatureEntity pauseFeature = amMapper.getBreakOperatorMessageByID(alarmMessageId);
		switch(pauseFeature.getReason()) {
		case DEFECTIVE_TYPE_XP: // 新品零件不良
		case DEFECTIVE_TYPE_FX: // 工程内发现
		case DEFECTIVE_TYPE_BL: // 工程内不良
			return pauseFeature.getReason();
		default:
			return null;
		}
	}

	/**
	 * 取得自动生成的管理编号
	 * @param defectiveTypeByBreakType
	 * @param conn
	 * @return
	 */
	public String getAutoManageCode(int defectiveType,
			SqlSession conn) {
		return getAutoManageCode(defectiveType, null, conn);
	}
	public String getAutoManageCode(int defectiveType, String currentId,
			SqlSession conn) {
		// 取得年份
		String sManagePrefix = RvsUtils.getFYBussinessYearString(Calendar.getInstance()) + "-";
		switch(defectiveType) {
		case DEFECTIVE_TYPE_QA: sManagePrefix = "QD1001-5-" + sManagePrefix; break;
		case DEFECTIVE_TYPE_FX: sManagePrefix = "QD1001-6-" + sManagePrefix; break;
		case DEFECTIVE_TYPE_BL: sManagePrefix = "QD1001-1-" + sManagePrefix; break;
		case DEFECTIVE_TYPE_XP: sManagePrefix = "MS0101-6-" + sManagePrefix; break;
		}

		DefectiveAnalysisMapper mapper = conn.getMapper(DefectiveAnalysisMapper.class);
		DefectiveAnalysisEntity cond = new DefectiveAnalysisEntity();
		cond.setManage_code(sManagePrefix);
		cond.setAlarm_message_id(currentId);

		String sMaxManageCode = mapper.getMaxManageCode(cond);
		if (sMaxManageCode == null) {
			return sManagePrefix + "001";
		} else {
			String sSeqCode = sMaxManageCode.substring(sManagePrefix.length());
			return sManagePrefix +
					CommonStringUtil.fillChar("" + (Integer.parseInt(sSeqCode) + 1), '0', 3, true) ;
		}
	}


	public List<DefectiveAnalysisPhotoEntity> getPhotoList(String alarmMessageId, SqlSession conn){
		DefectiveAnalysisMapper mapper = conn.getMapper(DefectiveAnalysisMapper.class);

		// 取得图片
		return mapper.findPhotoById(alarmMessageId);
	}

}
