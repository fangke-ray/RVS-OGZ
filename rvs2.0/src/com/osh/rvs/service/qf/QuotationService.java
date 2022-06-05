package com.osh.rvs.service.qf;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.inline.MaterialCommentMapper;
import com.osh.rvs.mapper.qf.QuotationMapper;
import com.osh.rvs.service.CustomerService;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

public class QuotationService {
	Logger _log = Logger.getLogger(getClass());

	/**
	 * 更新维修对象信息
	 * @param bean
	 * @param conn
	 * @throws Exception
	 */
	public void updateMaterial(MaterialEntity entity, SqlSessionManager conn) throws Exception {
		QuotationMapper dao = conn.getMapper(QuotationMapper.class);
		if (!isEmpty(entity.getCustomer_name())) {
			CustomerService cservice = new CustomerService();
			entity.setCustomer_id(cservice.getCustomerStudiedId(entity.getCustomer_name(), entity.getOcm(), conn));
		}

		Date[] workDates = RvsUtils.getTimeLimit(entity.getAgreed_date(), entity.getLevel(), null, conn, false);
		Date workDate = workDates[0];
		entity.setScheduled_date(workDate);

		// 只有周边设备放WIP库位
		if (entity.getWip_location() != null) {
			if (!RvsUtils.isPeripheral(entity.getLevel())) {
				entity.setWip_location(null);
			}
		}
		dao.updateMaterial(entity);
	}

	public void getProccessingData(Map<String, Object> responseBean, String material_id, 
			LoginData user, SqlSession conn) throws Exception {
		QuotationService service = new QuotationService();
		// 取得维修对象信息。
		MaterialForm mform = service.getMaterialInfo(material_id, user,conn);
		responseBean.put("mform", mform);
		// 取得维修对象的作业标准时间。
		responseBean.put("leagal_overline", RvsUtils.getZeroOverLine(mform.getModel_name(), mform.getCategory_name(), user, null));
	}

	public MaterialForm getMaterialInfo(String material_id, LoginData user, SqlSession conn) {
		MaterialForm materialForm = new MaterialForm();
		QuotationMapper dao = conn.getMapper(QuotationMapper.class);
		MaterialEntity materialEntity = dao.getMaterialDetail(material_id);
		BeanUtil.copyToForm(materialEntity, materialForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		// 取得维修对象备注
		MaterialCommentMapper mapper = conn.getMapper(MaterialCommentMapper.class);
		String comment = mapper.getMyMaterialComment(material_id, user.getOperator_id());
		materialForm.setComment(comment);

		String otherComment = mapper.getMaterialComments(material_id, user.getOperator_id());
		materialForm.setScheduled_manager_comment(otherComment);

		return materialForm;
	}

	public void listRefresh(LoginData user, Map<String, Object> listResponse, SqlSession conn) {
		String position_id = user.getPosition_id();
		QuotationMapper qDao = conn.getMapper(QuotationMapper.class);
		// 取得待报价处理对象一览 151 or 161
		List<MaterialEntity> waitings = qDao.getWaitings(position_id);

		// 取得暂停一览 151 or 161
		List<MaterialEntity> paused = qDao.getPaused(position_id);

		// 取得今日已完成处理对象一览
		List<MaterialEntity> finished = qDao.getFinished(position_id);

		List<MaterialForm> waitingsForm = new ArrayList<MaterialForm>();
		List<MaterialForm> pausedForm = new ArrayList<MaterialForm>();
		List<MaterialForm> finishedForm = new ArrayList<MaterialForm>();

		BeanUtil.copyToFormList(waitings, waitingsForm, CopyOptions.COPYOPTIONS_NOEMPTY, MaterialForm.class);

		String process_code = "";

		for (MaterialEntity pe : paused) {
			MaterialForm pausedMaterialForm = new MaterialForm();
			BeanUtil.copyToForm(pe, pausedMaterialForm, CopyOptions.COPYOPTIONS_NOEMPTY);

			// 工位特殊暂停理由
			// 优先 特殊暂停 》 暂停 》  其他暂停 》 未处理 》 中断
			if (pe.getNow_pause_reason() != null)
				if (pe.getNow_pause_reason() >= 70) {
					process_code = "";
					if ("00000000013".equals(pe.getProcessing_position())) process_code = "151";//TODO zhenggui
					else if ("00000000014".equals(pe.getProcessing_position())) process_code = "161";

					String sReason = PathConsts.POSITION_SETTINGS.getProperty("step." + process_code + "." + pe.getNow_pause_reason());
					pausedMaterialForm.setStatus("" + pe.getNow_pause_reason());
					if (sReason == null) {
						pausedMaterialForm.setOperate_result("");
					} else {
						pausedMaterialForm.setOperate_result(sReason);
					}
				} else if (pe.getNow_pause_reason() >= 40){
					pausedMaterialForm.setOperate_result("暂停");
					pausedMaterialForm.setStatus("40");
				} else if (pe.getNow_pause_reason() <= 30){
					pausedMaterialForm.setOperate_result("中断");
					pausedMaterialForm.setStatus("10");
				} else {
					pausedMaterialForm.setOperate_result("未处理");
					pausedMaterialForm.setStatus("20");
				}
			else {
				_log.warn(pausedMaterialForm.getMaterial_id() + "出现未分类的暂停:" + pe.getNow_pause_reason());
				pausedMaterialForm.setOperate_result("其他暂停");
				pausedMaterialForm.setStatus("31");
			}
			pausedForm.add(pausedMaterialForm);
		}

		BeanUtil.copyToFormList(finished, finishedForm, CopyOptions.COPYOPTIONS_NOEMPTY, MaterialForm.class);

		listResponse.put("waitings", waitingsForm);
		listResponse.put("paused", pausedForm);
		listResponse.put("finished", finishedForm);
	}
	
	public void updateComment(MaterialForm materialForm,LoginData user,SqlSessionManager conn){
		MaterialCommentMapper mapper = conn.getMapper(MaterialCommentMapper.class);
		
		//画面上提交的备注内容为空时，进一步查询原来是否已经存在备注了
		if(CommonStringUtil.isEmpty(materialForm.getComment())){
			//查询维修对象备注是否存在
			String dbComment = mapper.getMyMaterialComment(materialForm.getMaterial_id(), user.getOperator_id());
			
			//如果原来存在备注，则删除修对象备注
			if(!CommonStringUtil.isEmpty(dbComment)){
				mapper.deleteMaterialComment(materialForm.getMaterial_id(), user.getOperator_id());
			}
		}else{//画面上提交的备注内容不为空
			// 更新维修对象备注
			MaterialService materialService = new MaterialService();
			materialService.updateMaterialComment(materialForm.getMaterial_id(), user.getOperator_id(), materialForm.getComment(),conn);
			
		}
	}

}
