package com.osh.rvs.service.product;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.ForSolutionAreaEntity;
import com.osh.rvs.bean.inline.MaterialFactEntity;
import com.osh.rvs.bean.inline.SoloProductionFeatureEntity;
import com.osh.rvs.bean.inline.WaitingEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.PositionPanelMapper;
import com.osh.rvs.mapper.inline.SoloProductionFeatureMapper;
import com.osh.rvs.mapper.manufact.ProductMapper;
import com.osh.rvs.mapper.qf.MaterialFactMapper;
import com.osh.rvs.service.MaterialProcessService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ProcessAssignService;
import com.osh.rvs.service.ProductionFeatureService;
import com.osh.rvs.service.inline.ForSolutionAreaService;
import com.osh.rvs.service.inline.PositionPanelService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.JustlengthValidator;

public class ProductService {

	/**
	 * 取得最新序列号和相应产品
	 * @param sizeBefore 最新之前编号数
	 * @param sizeAfter 最新之后编号数
	 * @param conn
	 * @return
	 */
	public List<MaterialEntity> getSerialNos(int sizeBefore, int sizeAfter, SqlSession conn) {
		String tMonth = this.getSerialMonthlyPrefix();
		ProductMapper pMapper = conn.getMapper(ProductMapper.class);
		String lastProductSerialNo = pMapper.getLastProductSerialNo(tMonth);

		List<MaterialEntity> rets = new ArrayList<MaterialEntity>();

		// 取得显示用序号
		List<String> serialNoList = new ArrayList<String>();
		if (lastProductSerialNo == null) {
			// 本月没有
			for (int i = 0; i < sizeBefore + sizeAfter; i++) {
				serialNoList.add(tMonth + CommonStringUtil.fillChar((i+1) + "", '0', 4, true));
			}
		} else {
			String numericPart = lastProductSerialNo.substring(3); // .replaceAll("^^\\d(\\d*)$", "$1");
			int numericPartSerial = Integer.parseInt(numericPart);
			int numericPartLength = 4; // numericPart.length();
			String nonNumericPart = lastProductSerialNo
					.substring(0, lastProductSerialNo.length() - numericPartLength);
			int beforeSerial = numericPartSerial;
			int afterSerial = numericPartSerial;
			for (int i = 0; i < sizeBefore; i++) {
				beforeSerial--;
				if (beforeSerial == 0) break;
				serialNoList.add(0, nonNumericPart +  CommonStringUtil.fillChar(beforeSerial + "", '0', 4, true));
			}
			for (int i = 0; i < sizeAfter; i++) {
				serialNoList.add(nonNumericPart +  CommonStringUtil.fillChar(afterSerial + "", '0', 4, true));
				afterSerial++;
			}
		}

		// 按序列号取得产品信息
		List<MaterialEntity> productions = pMapper.getProductsBySerials(serialNoList);

		String lastModelName = "";
		for (String serialNo : serialNoList) {
			boolean hit = false;
			for (MaterialEntity production : productions) {
				lastModelName = production.getModel_name();
				if (production.getSerial_no().equals(serialNo)) {
					rets.add(production);
					hit = true;
					break;
				}
			}
			if (!hit) {
				MaterialEntity production = new MaterialEntity();
				production.setSerial_no(serialNo);
				production.setModel_name(lastModelName);
				rets.add(production);
			}
		}

		return rets;
	}

	/**
	 * 取得月序列号前缀
	 * @return
	 */
	public String getSerialMonthlyPrefix() {
		return DateUtil.toString(new Date(), "yMM").substring(3, 6);
	}

	/**
	 * 后续产品型号改动
	 * 
	 * @param conn
	 */
	public void setNewProductModel(String model_id, SqlSessionManager conn) {
		
		MaterialMapper mapper = conn.getMapper(MaterialMapper.class);

		mapper.setNewProductModel(model_id);
	}

	public ProductionFeatureEntity checkSerialNo(String serial_no,
			LoginData user, List<MsgInfo> errors, SqlSessionManager conn) throws Exception {
		ProductionFeatureEntity retWaiting = null;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("serial_no", serial_no);
		if (CommonStringUtil.isEmpty(serial_no)) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("material_id");
			msgInfo.setErrcode("validator.required");
			msgInfo.setErrmsg("扫描失敗！");
			errors.add(msgInfo);
		}

		String message2 = new JustlengthValidator("扫描序列号码", 7).validate(parameters, "serial_no");
		if (message2 != null) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("material_id");
			msgInfo.setErrcode("validator.invalidParam.invalidJustLengthValue");
			msgInfo.setErrmsg(message2);
			errors.add(msgInfo);
		}

		MsgInfo msgInfo;
		if (errors.size() == 0) {
			// 存在于等待区check

			// 在工位上等待的维修对象
			List<WaitingEntity> waitings = checkWaitingProduct(user, conn);
			int count = waitings.size();
			if (count == 0) {
				// 等待区内没有维修对象
				msgInfo = new MsgInfo();
				msgInfo.setComponentid("material_id");
				msgInfo.setErrcode("info.product.notInWaiting");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.notInWaiting"));
				errors.add(msgInfo);

				return retWaiting;
			} else {

				WaitingEntity scan = null;

				for (WaitingEntity waiting : waitings) {
	
//
//				WaitingEntity waiting = waitings.get(0);
//
				if (serial_no.equals(waiting.getSerial_no())) { // 是开始对象的话

					PositionPanelService ppService = new PositionPanelService();
					// 在工位上等待的维修对象
					List<ProductionFeatureEntity> productionFeature = ppService.isWaitingMaterial(waiting.getMaterial_id(),
							user.getSection_id(), user.getPosition_id(), user.getPx(), conn);
					count = productionFeature.size();
					if (count == 0 && user.getProcess_code().equals("002")) {
						
						ProductionFeatureEntity newEntity = new ProductionFeatureEntity();
						newEntity.setMaterial_id(waiting.getMaterial_id());
						newEntity.setSerial_no(serial_no);
						newEntity.setProcess_code(user.getProcess_code());
						newEntity.setOperate_result(0);
						newEntity.setPosition_id(user.getPosition_id());
						newEntity.setSection_id(user.getSection_id());
						newEntity.setPace(0);
						newEntity.setRework(0);

						setInline(waiting.getMaterial_id(), user.getSection_id(), "00000000228", conn); // TODO 228

						productionFeature.add(newEntity);
						count = 1;
					}
					if (count == 0) {
						// 维修对象不在用户所在等待区
						msgInfo = new MsgInfo();
						msgInfo.setComponentid("material_id");
						msgInfo.setErrcode("info.product.notInWaiting");
						msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.notInWaiting"));
						errors.add(msgInfo);
					} else {
						// 如有则返回等待中的作业信息。
						retWaiting = productionFeature.get(0);
						scan = waiting;

						if (errors.size() == 0) {
							ForSolutionAreaService fsaService = new ForSolutionAreaService();
							List<ForSolutionAreaEntity> blocks = fsaService.checkBlock(waiting.getMaterial_id(), user.getPosition_id(), user.getLine_id(), conn);
							if (blocks != null && blocks.size() > 0) {
								ForSolutionAreaEntity block = blocks.get(0);
								String blockReason = CodeListUtils.getValue("offline_reason", ""+block.getReason());
								msgInfo = new MsgInfo();
								msgInfo.setComponentid("material_id");
								msgInfo.setErrcode("info.product.blockedForSolve");
								msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.blockedForSolve"
										, waiting.getSerial_no(), blockReason, block.getComment()));
								errors.add(msgInfo);
							}
						}
					}
//				} else {
//					for (WaitingEntity waitingI : waitings) {
//						if (serial_no.equals(waitingI.getSerial_no())) { 
//							scan = waitingI;
//							break;
//						}
//					}
//				}
//

					break;
				}

			}
				if (scan == null) {
					// 维修对象不在用户所在等待区
					msgInfo = new MsgInfo();
					msgInfo.setComponentid("material_id");
					msgInfo.setErrcode("info.product.notInWaiting");
					msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.notInWaiting"));
					errors.add(msgInfo);
//				} else if (retWaiting == null) {
//					msgInfo = new MsgInfo();
//					msgInfo.setComponentid("material_id");
//					msgInfo.setErrcode("info.product.serialFirst");
//					msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.serialFirst", waiting.getSerial_no()));
//					errors.add(msgInfo);
				}
		}

		}

		return retWaiting;	
	}

	private void setInline(String material_id, String section_id, String pat_id, SqlSessionManager conn) throws Exception {
		MaterialFactMapper factMapper = conn.getMapper(MaterialFactMapper.class);

		MaterialFactEntity updateEntity = new MaterialFactEntity();
		updateEntity.setMaterial_id(material_id);
		updateEntity.setSection_id(section_id);
		updateEntity.setPat_id(pat_id); // TODO pat_id
		updateEntity.setQuotation_first(0);
		factMapper.updateInline(updateEntity);

		MaterialProcessService mps = new MaterialProcessService();
		ProcessAssignService pas = new ProcessAssignService();
		List<String> newHasLines = pas.checkPatHasLine(pat_id, null, conn);
		mps.setMaterialProcess(material_id, null, newHasLines, null, null, conn);

		// 插入作业
		PositionPanelService pps = new PositionPanelService();

		List<String> firstPositionIds = pas.getFirstPositionIds(pat_id, conn);
		for (String firstPositionId : firstPositionIds) {
			addFeatureEntity(material_id, firstPositionId, section_id, conn);
			pps.notifyPosition(section_id, firstPositionId, material_id);
		}
	}

	/**
	 * 设定起始工位信息
	 * @param featureEntities
	 * @param materialId
	 * @param positionId
	 * @param sectionId
	 * @throws Exception 
	 */
	private void addFeatureEntity(String materialId,
			String positionId, String sectionId, SqlSessionManager conn) throws Exception {
		ProductionFeatureEntity featureEntity = new ProductionFeatureEntity ();

		featureEntity.setOperate_result(0);
		featureEntity.setPace(0);
		featureEntity.setRework(0);
		featureEntity.setMaterial_id(materialId);
		featureEntity.setPosition_id(positionId);
		featureEntity.setSection_id(sectionId);

		ProductionFeatureService pfService = new ProductionFeatureService();
		pfService.fingerSpecifyPosition(materialId, true, featureEntity, new ArrayList<String>(), conn);
	}

	/**
	 * 判断是否所在工位的等待区中产品，如是则返回等待作业
	 * @param material_id
	 * @param session
	 * @param errors
	 */
	public List<WaitingEntity> checkWaitingProduct(LoginData user, SqlSession conn) {

		PositionPanelMapper ppMapper = conn.getMapper(PositionPanelMapper.class);

		List<WaitingEntity> ret = ppMapper.getWaitingMaterial(user.getLine_id(), user.getSection_id(),
				user.getPosition_id(), user.getOperator_id(), user.getPx());

		String process_code = user.getProcess_code();
		
		if(process_code.equals("002")){
			// 开始工位
			ProductMapper productMapper = conn.getMapper(ProductMapper.class);
			List<WaitingEntity> ls = productMapper.getWaitingStartOfSection(user.getSection_id());
			ret.addAll(ls);
		}
		
		return ret;
	}

	/**
	 * 取得当前型号可使用的先端头一览
	 * @param model_id
	 * @param conn
	 * @return
	 */
	public String getRefers(String model_id, SqlSession conn) {
		String refer =  "";
		// 寻找型号可使用的先端头一览
		SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
		List<SoloProductionFeatureEntity> snouts = dao.getSnoutsByModel(model_id);

		for (int i = 0 ; i < snouts.size(); i++) {
			SoloProductionFeatureEntity line = snouts.get(i);
			if (i == 0 && model_id.equals(line.getModel_id())) {
				refer += "<tr class='firstMatchSnout'>"; // background-color:lightgreen;
			} else {
				refer += "<tr>";
			}
			refer += "<td class='referId' style='display:none'>" + line.getSerial_no() + "</td>";
			refer += "<td><nobr>" + CommonStringUtil.decodeHtmlText(line.getSerial_no()) + "</nobr></td>";
			refer += "</tr>";
		}

		return refer;
	}

	public void createArm(String model_id, String serial_no,
			LoginData user, SqlSessionManager conn) throws Exception {
		// 建立ARM制品信息
		MaterialService mService = new MaterialService();
		MaterialEntity mBean = new MaterialEntity();
		mBean.setModel_id(model_id);
		mBean.setSerial_no(serial_no);
		mBean.setTicket_flg(1);
		mBean.setPat_id("00000000229"); // TODO 229
		mBean.setScheduled_expedited(1);
		mBean.setFix_type(RvsConsts.PROCESS_TYPE_ARM_LINE);
		String materialId = mService.insertProduct(mBean, user.getSection_id(), conn);

		ProductionFeatureEntity featureEntity = new ProductionFeatureEntity ();

		featureEntity.setOperate_result(0);
		featureEntity.setPace(0);
		featureEntity.setRework(0);
		featureEntity.setMaterial_id(materialId);
		featureEntity.setPosition_id(user.getPosition_id());
		featureEntity.setSection_id(user.getSection_id());

		ProductionFeatureService pfService = new ProductionFeatureService();
		pfService.fingerSpecifyPosition(materialId, true, featureEntity, new ArrayList<String>(), conn);
	}

	/***
	 * 取得提交维修对象ID(复数)
	 * @param parameterMap
	 * @return
	 */
	public List<MaterialEntity> getModelSerials(Map<String, String[]> parameterMap) {
		List<MaterialEntity> rets = new AutofillArrayList<MaterialEntity>(MaterialEntity.class);
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("materials".equals(entity)) {
					String column = m.group(2);
					Integer no = Integer.parseInt(m.group(3), 10);

					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("material_id".equals(column)) {
						rets.get(no).setMaterial_id(value[0]);
					} else if ("serial_no".equals(column)) {
						rets.get(no).setSerial_no(value[0]);
					} else if ("model_name".equals(column)) {
						rets.get(no).setModel_name(value[0]);
					}
				}
			}
		}
		return rets;
	}

	/**
	 * 检查序列号是否已登录
	 * 型号是否一致
	 * 不一致强制更新
	 * 
	 * @param model_id 提交的型号 ID
	 * @param serial_no 提交的序列号
	 * @param changeModel 不一致是否强制更新
	 * @param conn
	 * @param errors 
	 * @return
	 */
	public String getIdBySerialWithModelCheck(String model_id,
			String serial_no, boolean changeModel, SqlSessionManager conn, List<MsgInfo> errors) {
		MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);

		List<MaterialEntity> list = mMapper.getProductBySerialNo(serial_no);

		if (list.size() == 0) {
			return null;
		} else {
			MaterialEntity entity = list.get(0);

			boolean diffModel = !model_id.equals(entity.getModel_id());
			if (diffModel) {
				if (!changeModel) {
					MsgInfo info = new MsgInfo();
					info.setErrcode("info.product.serialNotMatchModel");
					info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.serialNotMatchModel", serial_no));
					errors.add(info);
				} else {
					entity.setModel_id(model_id);
					mMapper.updateMaterialSerialModel(entity);
				}
			}

			return entity.getMaterial_id();
		}
	}

}
