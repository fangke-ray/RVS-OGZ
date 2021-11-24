package com.osh.rvs.service.infect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.AlarmMesssageSendationEntity;
import com.osh.rvs.bean.infect.CheckUnqualifiedRecordEntity;
import com.osh.rvs.bean.master.DeviceTypeEntity;
import com.osh.rvs.bean.master.DevicesManageEntity;
import com.osh.rvs.bean.master.JigManageEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.infect.CheckUnqualifiedRecordForm;
import com.osh.rvs.form.master.DevicesManageForm;
import com.osh.rvs.form.master.ToolsManageForm;
import com.osh.rvs.mapper.infect.CheckUnqualifiedRecordMapper;
import com.osh.rvs.mapper.master.DevicesManageMapper;
import com.osh.rvs.service.AlarmMesssageService;
import com.osh.rvs.service.DevicesTypeService;

import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 
 * @Project rvs
 * @Package com.osh.rvs.service
 * @ClassName: CheckUnqualifiedRecordService
 * @Description: 点检不合格记录Service
 * @author lxb
 * @date 2014-8-13 上午11:47:58
 * 
 */
public class CheckUnqualifiedRecordService {
	/**
	 * 一览
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<CheckUnqualifiedRecordForm> search(ActionForm form, SqlSession conn) {
		CheckUnqualifiedRecordEntity entity = new CheckUnqualifiedRecordEntity();
		// 复制表单到数据对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		CheckUnqualifiedRecordMapper dao = conn.getMapper(CheckUnqualifiedRecordMapper.class);
		List<CheckUnqualifiedRecordEntity> list = dao.search(entity);

		List<CheckUnqualifiedRecordForm> formList = new ArrayList<CheckUnqualifiedRecordForm>();
		// 复制数据到表单对象
		BeanUtil.copyToFormList(list, formList, CopyOptions.COPYOPTIONS_NOEMPTY, CheckUnqualifiedRecordForm.class);

		return formList;
	}

	/**
	 * 借用设备(自由)下拉框/治具下拉框 借用设备(需确认)下拉框 备品件数
	 * 
	 * @param form
	 * @param listResponse 
	 * @param conn
	 * @return
	 */
	public void getAlterObjects(ActionForm form, Map<String, Object> listResponse, SqlSession conn) {
		CheckUnqualifiedRecordMapper dao = conn.getMapper(CheckUnqualifiedRecordMapper.class);
		CheckUnqualifiedRecordForm curForm = (CheckUnqualifiedRecordForm) form;

		String pFreeReferChooser = "";
		String pAcquireReferChooser = "";

		String objecType = curForm.getObject_type();// 对象类型
		if ("1".equals(objecType)) {// 设备工具
			DevicesManageEntity entity = new DevicesManageEntity();
			String device_type_id = curForm.getDevices_type_id();
			entity.setDevice_type_id(device_type_id);
			entity.setDevices_manage_id(curForm.getManage_id());

			DevicesTypeService dtService = new DevicesTypeService();
			DeviceTypeEntity dtEntity = dtService.getDeviceTypeById(device_type_id, conn);

			List<DevicesManageEntity> acquireList = null;
			List<DevicesManageEntity> freeList = null;

			if (dtEntity == null || dtEntity.getAlter_flg() == null || dtEntity.getAlter_flg() == 0) { // 自由
				// 自由替换品
				DevicesManageMapper dmMapper = conn.getMapper(DevicesManageMapper.class);
				entity = dmMapper.getByKey(curForm.getManage_id());
				freeList = dao.getDevicesNameReferChooser(entity);
			} else if (dtEntity.getAlter_flg() == 1) { // 管理
				freeList = new ArrayList<DevicesManageEntity>();
				acquireList = new ArrayList<DevicesManageEntity>();

				List<DevicesManageEntity> allList = dao.getDevicesNameReferChooser(entity);
				for (DevicesManageEntity alter : allList) {
					Integer freeDisplaceFlg = alter.getFree_displace_flg();
					if (freeDisplaceFlg == null) {
					} else if (freeDisplaceFlg == 0) { // △
						acquireList.add(alter);
					} else if (freeDisplaceFlg == 1) { // maru
						freeList.add(alter);
					}
				}

			} else if (dtEntity.getAlter_flg() == 2) { // 不便
				acquireList = new ArrayList<DevicesManageEntity>();

				List<DevicesManageEntity> allList = dao.getDevicesNameReferChooser(entity);
				for (DevicesManageEntity alter : allList) {
					if ("4".equals(alter.getStatus())) { // 衹有保管品
						acquireList.add(alter);
					}
				}
			}

			// 取得权限下拉框信息
			List<String[]> nList = new ArrayList<String[]>();
			List<DevicesManageForm> dmf = new ArrayList<DevicesManageForm>();

			if (freeList != null && freeList.size() > 0) {
				BeanUtil.copyToFormList(freeList, dmf, CopyOptions.COPYOPTIONS_NOEMPTY, DevicesManageForm.class);
				for (DevicesManageForm tempForm : dmf) {
					String[] dline = new String[4];
					dline[0] = tempForm.getDevices_manage_id();
					dline[1] = tempForm.getName();
					dline[2] = tempForm.getManage_code();
					dline[3] = tempForm.getModel_name();
					nList.add(dline);
				}
				pFreeReferChooser = CodeListUtils.getReferChooser(nList);
			}

			dmf = new ArrayList<DevicesManageForm>();
			nList = new ArrayList<String[]>();

			if (acquireList != null && acquireList.size() > 0) {
				BeanUtil.copyToFormList(acquireList, dmf, CopyOptions.COPYOPTIONS_NOEMPTY, DevicesManageForm.class);
				for (DevicesManageForm tempForm : dmf) {
					String[] dline = new String[4];
					dline[0] = tempForm.getDevices_manage_id();
					dline[1] = tempForm.getName();
					dline[2] = tempForm.getManage_code();
					dline[3] = tempForm.getModel_name();
					nList.add(dline);
				}
				pAcquireReferChooser = CodeListUtils.getReferChooser(nList);
			}

		} else if ("2".equals(objecType)) {// 治具
			JigManageEntity entity = new JigManageEntity();
			entity.setJig_manage_id(curForm.getManage_id());
			entity.setJig_no(curForm.getTools_no());
			List<JigManageEntity> list = dao.getToolsNameReferChooser(entity);
			List<ToolsManageForm> tmf = new ArrayList<ToolsManageForm>();

			// 取得权限下拉框信息
			List<String[]> nList = new ArrayList<String[]>();

			if (list != null && list.size() > 0) {
				BeanUtil.copyToFormList(list, tmf, null, ToolsManageForm.class);
				for (ToolsManageForm tempForm : tmf) {
					String[] dline = new String[5];
					dline[0] = tempForm.getTools_manage_id();
					dline[1] = tempForm.getTools_no();
					dline[2] = tempForm.getTools_name();
					dline[3] = tempForm.getManage_code();
					String sLocate = "";
					if (tempForm.getSection_name() != null) {
						sLocate += tempForm.getSection_name();
					}
					if (tempForm.getProcess_code() != null) {
						sLocate += " " + tempForm.getProcess_code() + "工位.";
					}
					dline[4] = sLocate;
					nList.add(dline);
				}
				pFreeReferChooser = CodeListUtils.getReferChooser(nList);
			}
		}

		listResponse.put("borrowFreeReferChooser", pFreeReferChooser);
		listResponse.put("borrowAcquireReferChooser", pAcquireReferChooser);

	}

	public CheckUnqualifiedRecordForm getById(ActionForm form, SqlSession conn) {
		CheckUnqualifiedRecordEntity tempEntity = new CheckUnqualifiedRecordEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, tempEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		CheckUnqualifiedRecordMapper dao = conn.getMapper(CheckUnqualifiedRecordMapper.class);
		CheckUnqualifiedRecordEntity entity = dao.getById(tempEntity);

		CheckUnqualifiedRecordForm returnForm = new CheckUnqualifiedRecordForm();
		// 复制数据到表单对象
		BeanUtil.copyToForm(entity, returnForm, CopyOptions.COPYOPTIONS_NOEMPTY);

		return returnForm;
	}

	/**
	 * 线长确认
	 * 
	 * @param form
	 * @param request
	 * @param triggerList 
	 * @param conn
	 * @throws Exception 
	 */
	public void updateByLineLeader(ActionForm form, HttpServletRequest request, List<String> triggerList, SqlSessionManager conn) throws Exception {
		CheckUnqualifiedRecordEntity entity = new CheckUnqualifiedRecordEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		LoginData loginData = (LoginData) request.getSession().getAttribute(RvsConsts.SESSION_USER);
		String operator_id = loginData.getOperator_id();
		entity.setLine_leader_id(operator_id);

		int iBorrowStatus = 0;
		if (entity.getBorrow_status() != null) iBorrowStatus = entity.getBorrow_status();

		CheckUnqualifiedRecordMapper dao = conn.getMapper(CheckUnqualifiedRecordMapper.class);

		dao.updateByLineLeader(entity);

		if (iBorrowStatus == 1) {
			// 中断信息解除
			CheckUnqualifiedRecordEntity nowEntity = dao.getById(entity);
			String alarm_message_id = nowEntity.getAlarm_message_id();
			
			// 处理人处理信息
			AlarmMesssageSendationEntity sendation = new AlarmMesssageSendationEntity();
			sendation.setAlarm_messsage_id( alarm_message_id );
			sendation.setComment("点检不合格解除");
			sendation.setRed_flg(1);
			sendation.setSendation_id(operator_id);
			sendation.setResolve_time(new Date());

			AlarmMesssageService amService = new AlarmMesssageService();
			amService.replaceAlarmMessageSendation(sendation, conn, triggerList);

		} else if (iBorrowStatus == 2) {
			// 通知设备管理员
		} else if (entity.getPosition_handle() == 4) {
			// 通知设备管理员
		}

	}

	/**
	 * 经理确认
	 * 
	 * @param form
	 * @param request
	 * @param conn
	 */
	public void updateByManage(ActionForm form, HttpServletRequest request, SqlSessionManager conn) {
		CheckUnqualifiedRecordEntity entity = new CheckUnqualifiedRecordEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		LoginData loginData = (LoginData) request.getSession().getAttribute(RvsConsts.SESSION_USER);
		String operator_id = loginData.getOperator_id();
		entity.setManager_id(operator_id);

		CheckUnqualifiedRecordMapper dao = conn.getMapper(CheckUnqualifiedRecordMapper.class);
		dao.updateByManage(entity);
	}

	/**
	 * 设备管理员确认
	 * 
	 * @param form
	 * @param request
	 * @param conn
	 */
	public void updateByTechnology(ActionForm form, HttpServletRequest request, SqlSessionManager conn) {
		CheckUnqualifiedRecordEntity entity = new CheckUnqualifiedRecordEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		LoginData loginData = (LoginData) request.getSession().getAttribute(RvsConsts.SESSION_USER);
		String operator_id = loginData.getOperator_id();
		entity.setTechnology_id(operator_id);

		CheckUnqualifiedRecordMapper dao = conn.getMapper(CheckUnqualifiedRecordMapper.class);
		dao.updateByTechnology(entity);
		
		if(entity.getObject_final_handle_result()!= null && entity.getObject_final_handle_result()==1){//废弃
			dao.updateStatus(entity);
		}
		
	}
	
	//获取借用物品的科室和工位
	public String getBorrowSectionAndLine(String borrow_object_id,Integer object_type,SqlSession conn){
		if(CommonStringUtil.isEmpty(borrow_object_id)){
			return null;
		}
		CheckUnqualifiedRecordEntity entity = new CheckUnqualifiedRecordEntity();
		entity.setBorrow_object_id(borrow_object_id);
		entity.setObject_type(object_type);
		
		CheckUnqualifiedRecordMapper dao = conn.getMapper(CheckUnqualifiedRecordMapper.class);
		CheckUnqualifiedRecordEntity temp=dao.getSectionAndLine(entity);
		if (temp.getProcess_code()==null) {
			return ""+temp.getSection_name();
		}
		return ""+temp.getSection_name()+" "+temp.getProcess_code()+"工位";
	}
}
