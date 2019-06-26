package com.osh.rvs.service.equipment;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.equipment.DeviceJigRepairRecordEntity;
import com.osh.rvs.bean.equipment.DeviceSpareAdjustEntity;
import com.osh.rvs.bean.equipment.DeviceSpareEntity;
import com.osh.rvs.bean.master.DevicesManageEntity;
import com.osh.rvs.bean.master.JigManageEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.equipment.DeviceJigRepairRecordForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.equipment.DeviceJigRepairRecordMapper;
import com.osh.rvs.mapper.equipment.DeviceSpareAdjustMapper;
import com.osh.rvs.mapper.equipment.DeviceSpareMapper;
import com.osh.rvs.mapper.master.DevicesManageMapper;
import com.osh.rvs.mapper.master.JigManageMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.FileUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class DeviceJigRepairService {

	/**
	 * 检索
	 * @param form
	 * @param conn
	 * @param listResponse 
	 * @param errors
	 * @return
	 */
	public void search(ActionForm form,
			SqlSession conn, Map<String, Object> listResponse, List<MsgInfo> errors) {
		DeviceJigRepairRecordMapper mapper = conn.getMapper(DeviceJigRepairRecordMapper.class);
		DeviceJigRepairRecordEntity condition = new DeviceJigRepairRecordEntity();

		BeanUtil.copyToBean(form, condition, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<DeviceJigRepairRecordEntity> list = mapper.search(condition);
		List<DeviceJigRepairRecordForm> formlist = new ArrayList<DeviceJigRepairRecordForm>();

		int countAll = 0; Set<String> keyAll = new HashSet<String>(); BigDecimal costAll = new BigDecimal(0); BigDecimal saveAll = new BigDecimal(0); 
		int countDev = 0; Set<String> keyDev = new HashSet<String>(); BigDecimal costDev = new BigDecimal(0); BigDecimal saveDev = new BigDecimal(0); 
		int countJig = 0; Set<String> keyJig = new HashSet<String>(); BigDecimal costJig = new BigDecimal(0); BigDecimal saveJig = new BigDecimal(0); 
		int countOth = 0; Set<String> keyOth = new HashSet<String>(); BigDecimal costOth = new BigDecimal(0); BigDecimal saveOth = new BigDecimal(0); 
		for (DeviceJigRepairRecordEntity entity : list) {
			DeviceJigRepairRecordForm resForm = new DeviceJigRepairRecordForm();
			BeanUtil.copyToForm(entity, resForm, CopyOptions.COPYOPTIONS_NOEMPTY);

			Integer objectType = entity.getObject_type();

			// 计算合计和节省
			if (entity.getPrice() != null) {
				BigDecimal bdTotalPrice = entity.getPrice().multiply(new BigDecimal(entity.getQuantity()));
				resForm.setTotal_price(bdTotalPrice.toPlainString());

				countAll += entity.getQuantity();
				costAll = costAll.add(bdTotalPrice);
				if (objectType == null || objectType == 9) {
					countOth += entity.getQuantity();
					costOth = costOth.add(bdTotalPrice);
				} else if (objectType == 1) {
					countDev += entity.getQuantity();
					costDev = costDev.add(bdTotalPrice);
				} else if (objectType == 2) {
					countJig += entity.getQuantity();
					costJig = costJig.add(bdTotalPrice);
				}

				if (entity.getOutsourcing_price() != null) {
					BigDecimal bdSavingPrice = entity.getOutsourcing_price().subtract(bdTotalPrice);
					resForm.setSaving_price(bdSavingPrice.toPlainString());

					saveAll = saveAll.add(bdSavingPrice);
					if (objectType == null || objectType == 9) {
						saveOth = saveOth.add(bdSavingPrice);
					} else if (objectType == 1) {
						saveDev = saveDev.add(bdSavingPrice);
					} else if (objectType == 2) {
						saveJig = saveJig.add(bdSavingPrice);
					}
				}
			}
			keyAll.add(entity.getDevice_jig_repair_record_key());
			if (objectType == null || objectType == 9) {
				keyOth.add(entity.getDevice_jig_repair_record_key());
			} else if (objectType == 1) {
				keyDev.add(entity.getDevice_jig_repair_record_key());
			} else if (objectType == 2) {
				keyJig.add(entity.getDevice_jig_repair_record_key());
			}

			Integer deviceHalt = entity.getDevice_halt();
			if (deviceHalt == null || deviceHalt > 1050) {
				resForm.setDevice_halt("-");
			} else {
				resForm.setDevice_halt((deviceHalt / 60) + ":" + CommonStringUtil.fillChar(("" + deviceHalt % 60), '0', 2, true));
			}

			// 照片
			String targetPath = PathConsts.BASE_PATH + PathConsts.PHOTOS + "\\dj_repair\\" + entity.getDevice_jig_repair_record_key();
			if (new File(targetPath).exists()) {
				resForm.setPhoto_flg("1");
			}

			formlist.add(resForm);
		}

		listResponse.put("recordList", formlist);

		DeviceJigRepairRecordForm allSummary = new DeviceJigRepairRecordForm();
		allSummary.setDevice_type_name("" + countAll);
		allSummary.setObject_name("" + keyAll.size());
		allSummary.setTotal_price(costAll.toPlainString());
		allSummary.setSaving_price(saveAll.toPlainString());
		listResponse.put("allSummary", allSummary);

		if (!keyDev.isEmpty()) {
			DeviceJigRepairRecordForm devSummary = new DeviceJigRepairRecordForm();
			devSummary.setDevice_type_name("" + countDev);
			devSummary.setObject_name("" + keyDev.size());
			devSummary.setTotal_price(costDev.toPlainString());
			devSummary.setSaving_price(saveDev.toPlainString());
			listResponse.put("devSummary", devSummary);
		}

		if (!keyJig.isEmpty()) {
			DeviceJigRepairRecordForm jigSummary = new DeviceJigRepairRecordForm();
			jigSummary.setDevice_type_name("" + countJig);
			jigSummary.setObject_name("" + keyJig.size());
			jigSummary.setTotal_price(costJig.toPlainString());
			jigSummary.setSaving_price(saveJig.toPlainString());
			listResponse.put("jigSummary", jigSummary);
		}

		if (!keyOth.isEmpty()) {
			DeviceJigRepairRecordForm othSummary = new DeviceJigRepairRecordForm();
			othSummary.setDevice_type_name("" + countOth);
			othSummary.setObject_name("" + keyOth.size());
			othSummary.setTotal_price(costOth.toPlainString());
			othSummary.setSaving_price(saveOth.toPlainString());
			listResponse.put("othSummary", othSummary);
		}
	}

	/**
	 * 管理编号Check + 提出权限Check
	 * 
	 * @param user
	 * @param form
	 * @param errors
	 * @param conn
	 */
	public void checkSubmitPrivacy(LoginData user, ActionForm form,
			List<MsgInfo> errors, SqlSession conn) {

		DeviceJigRepairRecordForm djrrForm = (DeviceJigRepairRecordForm) form;
		String objectType = djrrForm.getObject_type();
		String manageId = djrrForm.getManage_id();
		if (("1".equals(objectType) || "2".equals(objectType)) && CommonStringUtil.isEmpty(manageId)) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("manage_id");
			msgInfo.setErrcode("validator.required");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "管理编号"));
			errors.add(msgInfo);
			return;
		}

		List<Integer> privacies = user.getPrivacies();
		if (privacies.contains(RvsConsts.PRIVACY_TECHNOLOGY)
			|| privacies.contains(RvsConsts.PRIVACY_PROCESSING)) {
			return;
		}

		if (!djrrForm.getLine_id().equals(user.getLine_id())) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("line_id");
			msgInfo.setErrcode("privacy.objectOutOfDomain");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("privacy.objectOutOfDomain", "工程"));
			errors.add(msgInfo);
		}

		if ("1".equals(objectType)) {
			DevicesManageMapper mapper = conn.getMapper(DevicesManageMapper.class);

			DevicesManageEntity entity = mapper.getByKey(manageId);
			if (!user.getOperator_id().equals(entity.getResponsible_operator_id())
					&& !user.getOperator_id().equals(entity.getManager_operator_id())) {
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setComponentid("manage_id");
				msgInfo.setErrcode("privacy.objectOutOfDomain");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("privacy.objectOutOfDomain", "设备/一般工具"));
				errors.add(msgInfo);
			}
		} else if ("2".equals(objectType)) {
			JigManageMapper mapper = conn.getMapper(JigManageMapper.class);
			JigManageEntity entity = mapper.getByKey(manageId);
			if (!user.getOperator_id().equals(entity.getResponsible_operator_id())
					&& !user.getOperator_id().equals(entity.getManager_operator_id())) {
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setComponentid("manage_id");
				msgInfo.setErrcode("privacy.objectOutOfDomain");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("privacy.objectOutOfDomain", "专用工具"));
				errors.add(msgInfo);
			}
		}
	}

	/**
	 * 报修处理--直接报修
	 * @param form
	 * @param user 
	 * @param conn
	 */
	public void sumbit(ActionForm form, LoginData user, SqlSessionManager conn) {
		DeviceJigRepairRecordEntity insertEntity = new DeviceJigRepairRecordEntity();

		BeanUtil.copyToBean(form, insertEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		DeviceJigRepairRecordMapper mapper = conn.getMapper(DeviceJigRepairRecordMapper.class);

		insertEntity.setSubmit_time(new Date());

		mapper.insertRecord(insertEntity);

		CommonMapper cMapper = conn.getMapper(CommonMapper.class);

		String key = cMapper.getLastInsertID();

		insertEntity.setDevice_jig_repair_record_key(key);
		insertEntity.setSubmitter_id(user.getOperator_id());

		mapper.insertSubmit(insertEntity);
	}

	/**
	 * 验收处理
	 * @param form
	 * @param user 
	 * @param conn
	 */
	public void confirm(ActionForm form, LoginData user, SqlSessionManager conn) {
		DeviceJigRepairRecordEntity confirmEntity = new DeviceJigRepairRecordEntity();

		BeanUtil.copyToBean(form, confirmEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		confirmEntity.setConfirmer_id(user.getOperator_id());

		DeviceJigRepairRecordMapper mapper = conn.getMapper(DeviceJigRepairRecordMapper.class);

		mapper.updateConfirm(confirmEntity);
	}

	/**
	 * 取得编辑详细
	 * @param key
	 * @param conn
	 * @return
	 */
	public DeviceJigRepairRecordForm detail(String key, SqlSession conn) {
		DeviceJigRepairRecordMapper mapper = conn.getMapper(DeviceJigRepairRecordMapper.class);
		DeviceJigRepairRecordEntity entity = mapper.getDetailForRepair(key);

		if (entity == null) {
			return null;
		} else {
			DeviceJigRepairRecordForm retForm = new DeviceJigRepairRecordForm();

			BeanUtil.copyToForm(entity, retForm, CopyOptions.COPYOPTIONS_NOEMPTY);

			return retForm;
		}
	}

	public void checkConfirmPrivacy(LoginData user, ActionForm form,
			List<MsgInfo> errors, SqlSession conn) {
		List<Integer> privacies = user.getPrivacies();
		if (privacies.contains(RvsConsts.PRIVACY_TECHNOLOGY)
			|| privacies.contains(RvsConsts.PRIVACY_PROCESSING)) {
			return;
		}

		DeviceJigRepairRecordForm djrrForm = (DeviceJigRepairRecordForm) form;

		String key = djrrForm.getDevice_jig_repair_record_key();

		DeviceJigRepairRecordMapper mapper = conn.getMapper(DeviceJigRepairRecordMapper.class);
		DeviceJigRepairRecordEntity entity = mapper.getDetailForRepair(key);
		
		String manageId = entity.getManage_id();

		if (entity.getObject_type() == 1) {
			DevicesManageMapper devMapper = conn.getMapper(DevicesManageMapper.class);

			DevicesManageEntity dmEntity = devMapper.getByKey(manageId);
			if (!user.getOperator_id().equals(dmEntity.getResponsible_operator_id())
					&& !user.getOperator_id().equals(dmEntity.getManager_operator_id())) {
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setComponentid("manage_id");
				msgInfo.setErrcode("privacy.objectOutOfDomain");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("privacy.objectOutOfDomain", "设备/一般工具"));
				errors.add(msgInfo);
			}
		} else if (entity.getObject_type() == 2) {
			JigManageMapper jmMapper = conn.getMapper(JigManageMapper.class);
			JigManageEntity jmEntity = jmMapper.getByKey(manageId);
			if (!user.getOperator_id().equals(jmEntity.getResponsible_operator_id())
					&& !user.getOperator_id().equals(jmEntity.getManager_operator_id())) {
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setComponentid("manage_id");
				msgInfo.setErrcode("privacy.objectOutOfDomain");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("privacy.objectOutOfDomain", "专用工具"));
				errors.add(msgInfo);
			}
		}
	}

	public List<DeviceJigRepairRecordEntity> getCostsByKey(String key, SqlSession conn) {

		DeviceJigRepairRecordMapper mapper = conn.getMapper(DeviceJigRepairRecordMapper.class);

		return mapper.getConsumableByKey(key);
	}

	/**
	 * 编辑数据检查和整理
	 * @param form
	 * @param conn
	 * @param errors
	 */
	public void checkRepairFinish(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		DeviceJigRepairRecordForm djrrForm = (DeviceJigRepairRecordForm) form;
		boolean bFinish = (djrrForm.getRepair_complete_time() != null);
		if (bFinish) {
			djrrForm.setRepair_complete_time(DateUtil.toString(new Date(), DateUtil.DATE_TIME_PATTERN));
		}

		if (CommonStringUtil.isEmpty(djrrForm.getDevice_type_name())) {
			djrrForm.setDevice_type_id("00000000000");
			djrrForm.setModel_name("");
			djrrForm.setPrice(null);
			djrrForm.setQuantity(null);
		} else {

			// 使用备品
			if (bFinish && djrrForm.getDevice_type_id() != null && !"00000000000".equals(djrrForm.getDevice_type_id())) {
				int useQuantity = 0;
				if (!CommonStringUtil.isEmpty(djrrForm.getQuantity())) {
					useQuantity = Integer.parseInt(djrrForm.getQuantity(), 10);
				}

				if (useQuantity == 0) {
					MsgInfo error = new MsgInfo();
					error.setComponentid("quantity");
					error.setErrcode("validator.invalidParam.invalidMoreThanZero");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidMoreThanZero", 
							"备件使用数量"));
					errors.add(error);
				} else {
					DeviceSpareMapper dsMapper = conn.getMapper(DeviceSpareMapper.class);
					DeviceSpareEntity dsCond = new DeviceSpareEntity();
					dsCond.setDevice_type_id(djrrForm.getDevice_type_id());
					dsCond.setModel_name(djrrForm.getModel_name());
					dsCond.setDevice_spare_type(2);
					DeviceSpareEntity dsResult = dsMapper.getDeviceSpare(dsCond);

					if (dsResult == null || dsResult.getAvailable_inventory() < useQuantity) {
						MsgInfo error = new MsgInfo();
						error.setComponentid("quantity");
						error.setErrcode("info.equipment.lessThanSpareStorage");
						error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.equipment.lessThanSpareStorage", 
								dsResult.getDevice_type_name(), dsResult.getModel_name(), dsResult.getAvailable_inventory()));
						errors.add(error);
					} else {
						djrrForm.setLine_break("" + (dsResult.getAvailable_inventory() - useQuantity)); // 借用记录备件更新数量
					}
				}
			}
		}
	}

	public void repairEdit(ActionForm form, LoginData user,
			SqlSessionManager conn) {

		DeviceJigRepairRecordEntity editEntity = new DeviceJigRepairRecordEntity();
		BeanUtil.copyToBean(form, editEntity, null);

		DeviceJigRepairRecordMapper mapper = conn.getMapper(DeviceJigRepairRecordMapper.class);

		mapper.updateRecord(editEntity);

		// 插入维修者
		String existMaintainer = editEntity.getMaintainer_id();
		boolean insMaintainer = false;
		if (CommonStringUtil.isEmpty(existMaintainer)) {
			insMaintainer = true;
		} else {
			insMaintainer = true;
			String[] arrMaintainer = existMaintainer.split("/");
			for (String maintainer : arrMaintainer) {
				if (maintainer.equals(user.getOperator_id())) {
					insMaintainer = false;
					break;
				}
			}
		}

		if (insMaintainer) {
			editEntity.setMaintainer_id(user.getOperator_id());
			mapper.insertMaintainer(editEntity);
		}

		// 插入消耗品
		mapper.deleteConsumable(editEntity);
		mapper.insertConsumable(editEntity);

		// 备件出库
		boolean bFinish = (editEntity.getRepair_complete_time() != null);
		String deviceType = editEntity.getDevice_type_id();
		if (bFinish && deviceType != null && !"00000000000".equals(deviceType)) {
			DeviceSpareMapper dsMapper = conn.getMapper(DeviceSpareMapper.class);
			DeviceSpareAdjustMapper dsaMapper = conn.getMapper(DeviceSpareAdjustMapper.class);

			DeviceSpareEntity dsCond = new DeviceSpareEntity();
			dsCond.setDevice_type_id(editEntity.getDevice_type_id());
			dsCond.setModel_name(editEntity.getModel_name());
			dsCond.setDevice_spare_type(2);
			dsCond.setAvailable_inventory(editEntity.getLine_break()); // 借用记录备件更新数量

			dsMapper.updateAvailableInventory(dsCond);

			DeviceSpareAdjustEntity deviceSpareAdjustEntity = new DeviceSpareAdjustEntity();
			deviceSpareAdjustEntity.setDevice_type_id(editEntity.getDevice_type_id());
			deviceSpareAdjustEntity.setModel_name(editEntity.getModel_name());
			deviceSpareAdjustEntity.setDevice_spare_type(2);
			deviceSpareAdjustEntity.setAdjust_time(new Date());
			// 理由(入库)
			deviceSpareAdjustEntity.setReason_type(26);
			deviceSpareAdjustEntity.setAdjust_inventory(-editEntity.getQuantity());
			deviceSpareAdjustEntity.setOperator_id(user.getOperator_id());
			// 备注来源
			deviceSpareAdjustEntity.setComment("维修<repair_no key='" + editEntity.getDevice_jig_repair_record_key() + "'>" + editEntity.getObject_name() + "</repair_no>时使用。");
			
			// ②新建设备工具备品调整记录
			dsaMapper.insert(deviceSpareAdjustEntity);
		}
	}

	public void copyPhoto(String device_jig_repair_record_key, String photo_file_name) {
		// 把图片拷贝到目标文件夹下
		String today = DateUtil.toString(new Date(), "yyyyMM");
		String tempFilePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + today + "\\" + photo_file_name;
		String targetPath = PathConsts.BASE_PATH + PathConsts.PHOTOS + "\\dj_repair\\" + device_jig_repair_record_key;
		File confFile = new File(tempFilePath);
		if (confFile.exists()) {
			FileUtils.copyFile(tempFilePath, targetPath, true);
		}
	}

	public void delPhoto(String device_jig_repair_record_key) {
		String targetPath = PathConsts.BASE_PATH + PathConsts.PHOTOS + "\\dj_repair\\" + device_jig_repair_record_key;
		File confFile = new File(targetPath);
		if (confFile.exists()) {
			confFile.delete();
		}
	}
}
