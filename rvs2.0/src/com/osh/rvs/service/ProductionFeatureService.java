package com.osh.rvs.service;

import static framework.huiqing.common.util.CommonStringUtil.fillChar;
import static framework.huiqing.common.util.CommonStringUtil.joinBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.ProcessAssignEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.ProductionFeatureForm;
import com.osh.rvs.mapper.data.AlarmMesssageMapper;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.LeaderPcsInputMapper;
import com.osh.rvs.mapper.inline.MaterialProcessAssignMapper;
import com.osh.rvs.mapper.inline.ProductionAssignMapper;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.master.PositionMapper;
import com.osh.rvs.mapper.master.ProcessAssignMapper;
import com.osh.rvs.service.partial.MaterialPartialService;
import com.osh.rvs.service.proxy.ProcessAssignProxy;

import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class ProductionFeatureService {
	protected static final Logger logger = Logger.getLogger("Production");

	public void insert(ProductionFeatureEntity entity, SqlSession conn) throws Exception{
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		dao.insertProductionFeature(entity);
	}

	public void insertAcceptance(ProductionFeatureEntity entity, SqlSession conn){
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		dao.insertAcceptanceProductionFeature(entity);
	}

	public int checkOperateResult(String materialId, SqlSession conn) {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		return dao.checkOperateResult(materialId);
	}

	public List<ProductionFeatureForm> getProductionFeatureByMaterialId(ActionForm from, SqlSession conn) {
		ProductionFeatureEntity entity = new ProductionFeatureEntity();
		BeanUtil.copyToBean(from, entity, null);
		
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		List<ProductionFeatureEntity> list = dao.getProductionFeatureByMaterialId(entity);
		
		List<ProductionFeatureForm> listForm = new ArrayList<ProductionFeatureForm>();
		BeanUtil.copyToFormList(list, listForm, null, ProductionFeatureForm.class);
		
		return listForm;
	}
	
	public List<ProductionFeatureForm> getNoBeforeRework(String id, SqlSession conn) {
		List<ProductionFeatureForm> listForm = new ArrayList<ProductionFeatureForm>();
		
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		List<ProductionFeatureEntity> list = dao.getNoBeforeRework(id);
		
		BeanUtil.copyToFormList(list, listForm, null, ProductionFeatureForm.class);
		
		return listForm;
	}
	
	public List<ProductionFeatureForm> getFinishProductionFeature(String id, SqlSession conn) {
		List<ProductionFeatureForm> listForm = new ArrayList<ProductionFeatureForm>();
		
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		List<ProductionFeatureEntity> list = dao.getFinishProductionFeature(id);
		
		BeanUtil.copyToFormList(list, listForm, null, ProductionFeatureForm.class);
		
		return listForm;
	}
	

	/**
	 * 作业中发生暂停后，产生作业下一步暂停等待信息
	 * 作业中发生中断后恢复，产生作业下一步暂停等待信息
	 * @param productionFeature
	 * @param conn
	 */
	public void pauseToNext(ProductionFeatureEntity productionFeature, SqlSession conn){
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);

		// Pace + 1
		productionFeature.setPace(productionFeature.getPace() + 1);
		// 还没有处理者
		productionFeature.setOperator_id(null);
		// 状态总是暂停再开
		productionFeature.setOperate_result(RvsConsts.OPERATE_RESULT_PAUSE);
		productionFeature.setAction_time(null);
		productionFeature.setFinish_time(null);
		// 其他沿用

		// 作成新等待记录
		dao.insertProductionFeature(productionFeature);
	}


	/**
	 * 作业中发生暂停后，产生作业下一步暂停进行信息
	 * @param productionFeature
	 * @param conn
	 */
	public void pauseToSelf(ProductionFeatureEntity productionFeature, SqlSessionManager conn) {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);

		// Pace + 1
		Integer newPace = productionFeature.getPace() + 1;

		productionFeature.setPace(newPace);
		// 继承处理者
		// 状态总是暂停再开
		productionFeature.setOperate_result(RvsConsts.OPERATE_RESULT_PAUSE);

		productionFeature.setAction_time(getFirstPaceOnRework(productionFeature, conn).getAction_time());
		productionFeature.setFinish_time(null);
		// 其他沿用

		// 作成新等待记录
		dao.insertProductionFeature(productionFeature);
	}

	/**
	 * 取到本次返工中，？
	 * @param currentPace
	 * @param conn
	 * @return
	 */
	public ProductionFeatureEntity getFirstPaceOnRework(ProductionFeatureEntity currentPace, SqlSession conn) {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);

		// 设定为延续开始时间
		ProductionFeatureEntity firstPf = new ProductionFeatureEntity();
		firstPf.setMaterial_id(currentPace.getMaterial_id());
		firstPf.setPosition_id(currentPace.getPosition_id());
		firstPf.setSection_id(currentPace.getSection_id());
		firstPf.setRework(currentPace.getRework());
		firstPf.setPace(0);
		List<ProductionFeatureEntity> firstPfs = dao.searchProductionFeature(firstPf);
		if (firstPfs ==  null || firstPfs.size() == 0) {
			logger.error("No pace 0");
			//firstPf = new Date();
		} else {
			firstPf = firstPfs.get(0);
		}
		return firstPf;
	}

	/**
	 * 作业中发生中断后，产生中断等待信息
	 * @param productionFeature
	 * @param conn
	 */
	public void breakToNext(ProductionFeatureEntity productionFeature, SqlSession conn){
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);

		// Pace
		productionFeature.setPace(productionFeature.getPace() + 1);
		// 保持处理者
		// productionFeature.setOperator_id(null);
		// 状态总是暂停再开
		productionFeature.setOperate_result(RvsConsts.OPERATE_RESULT_BREAK);
		productionFeature.setAction_time(null);
		productionFeature.setFinish_time(null);
		// 其他沿用

		// 作成新等待记录
		dao.insertProductionFeature(productionFeature);
	}

	/**
	 * 发动工位
	 * @param material_id
	 * @param workingPf
	 * @param conn
	 * @throws Exception 
	 * @return 是否发动
	 */
	private void fingerPosition(MaterialEntity mEntity, boolean fixed,
			ProductionFeatureEntity workingPf, SqlSessionManager conn, ProductionFeatureMapper pfDao, ProcessAssignProxy paProxy, List<String> retList, List<String> triggerList) throws Exception {
		fingerPosition(mEntity, fixed, workingPf, conn, pfDao, paProxy, retList, triggerList, true);
	}
	private void fingerPosition(MaterialEntity mEntity, boolean fixed,
			ProductionFeatureEntity workingPf, SqlSessionManager conn, ProductionFeatureMapper pfDao, ProcessAssignProxy paProxy, List<String> retList, List<String> triggerList, boolean isFact) throws Exception {

		if (retList == null) retList = new ArrayList<String> ();

		String material_id = mEntity.getMaterial_id();
		String position_id = workingPf.getPosition_id();
		// 维修对象的课室
		String section_id = paProxy.getMaterial_section_id();
		if (section_id == null) { // (投线前) 
			section_id = workingPf.getSection_id();
		}

		if (fixed) { 
			// 固定工位生成等待区信息
			ProductionFeatureEntity entity = new ProductionFeatureEntity();
			entity.setMaterial_id(material_id);
			entity.setPosition_id(position_id);
			entity.setPace(0);
			entity.setSection_id(section_id);
			entity.setOperate_result(getPutinOperateResult(position_id));
			entity.setRework(workingPf.getRework());
			pfDao.insertProductionFeature(entity);

			if (isFact && triggerList!=null) {
				// 通知
				triggerList.add("http://localhost:8080/rvspush/trigger/in/" + position_id + "/"
		            		+ section_id + "/" + material_id + "/" + (paProxy.isLightFix ? "1" : "0"));
			}
			if (entity.getOperate_result() == 0)
				retList.add(position_id);

		} else {
			// 判断发动的工程是否有完成
			boolean passed = false;
			if (mEntity.getLevel() == 1) {
				for (int j = 0; j < PositionService.getS1PASSES().length; j++) {
					if (PositionService.getS1PASSES()[j].equals(fillChar(position_id, '0', 11, true))) {
						passed = true; break;
					}
				}
			}
			if (!passed) {
				passed = paProxy.checkWorked(position_id);
			}

			// 有完成，并且其先决也已完成，则由这个工位继续触发
			if (passed) {
				// 取得先决
				List<String> prevPositions = new ArrayList<String>();

				getPrev(paProxy, material_id, mEntity.getPat_id(), position_id, mEntity.getLevel(), prevPositions);

				if (prevPositions.size() == 0 || !isFact 
						|| paProxy.getFinishedCountByPositions(prevPositions) == prevPositions.size()) {
					workingPf.setPosition_id(position_id);
					List<String> x = fingerNextPosition(material_id, workingPf, conn, triggerList, isFact);
					if (x!=null) {
						retList.addAll(x);
					}
				}
			}
			// 判断得到的工程是否有未完成。
			else if (paProxy.checkWorking(position_id) > 0) {
				// 有的话则不影响原有工作
				logger.info(position_id+"工位进行中。");
			}
			// 没有完成则判断先决的工位是否都已经结束。
			else {
				// 取得先决
				List<String> prevPositions = new ArrayList<String>();

				getPrev(paProxy, material_id, mEntity.getPat_id(), position_id, mEntity.getLevel(), prevPositions);

				logger.info(position_id+"工位de先决："+prevPositions.size());

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("material_id", material_id);
				params.put("position_ids", prevPositions);

				if (prevPositions.size() == 0 || !isFact
						|| paProxy.getFinishedCountByPositions(prevPositions) == prevPositions.size()) {
					// 都已经结束生成等待区信息
					logger.info("维修对象"+material_id+"进行至"+position_id+"工位。( " + isFact + ")");
					ProductionFeatureEntity entity = new ProductionFeatureEntity();
					entity.setMaterial_id(material_id);
					entity.setPosition_id(position_id);
					entity.setPace(0);
					entity.setSection_id(section_id);
					entity.setOperate_result(getPutinOperateResult(position_id));

					Integer neoRework = workingPf.getRework();

					if (prevPositions.size() > 1) { // 取得先决的最大Rework
						int rework = pfDao.getReworkCountWithPositions(params);
						if (rework > neoRework) neoRework = rework;
					}
					if (position_id.equals("90") || position_id.equals("00000000090")) { // 部分完成
						pfDao.removeWaiting(material_id, position_id);
					}
					entity.setRework(neoRework);
					pfDao.insertProductionFeature(entity);

//					if (isFact && ("99".equals(position_id) || "00000000099".equals(position_id)
//							|| "27".equals(position_id) || "00000000027".equals(position_id))) {
//						MaterialPartialService mptlService = new MaterialPartialService();
//						mptlService.createMaterialPartialWithExistCheck(material_id, conn);
//					}

					if (isFact) {
						// 通知
						triggerList.add("http://localhost:8080/rvspush/trigger/in/" + position_id + "/" 
				            		+ section_id + "/" + material_id + "/" + (paProxy.isLightFix ? "1" : "0"));
					}

					logger.info("tranover");
					if (entity.getOperate_result() == 0)
						retList.add(position_id);
				} else if (position_id.equals("90") || position_id.equals("00000000090")) { // 部分完成
					// 都已经结束生成等待区信息
					logger.info("维修对象"+material_id+"部分进行至"+position_id+"工位。( " + isFact + ")");
					ProductionFeatureEntity entity = new ProductionFeatureEntity();
					entity.setMaterial_id(material_id);
					entity.setPosition_id(position_id);
					entity.setPace(0);
					entity.setSection_id(section_id);
					entity.setOperate_result(9); // 部分到达

					Integer neoRework = workingPf.getRework();

					if (prevPositions.size() > 1) { // 取得先决的最大Rework
						int rework = pfDao.getReworkCountWithPositions(params);
						if (rework > neoRework) neoRework = rework;
					}
					entity.setRework(neoRework);
					pfDao.insertProductionFeature(entity);
				}
			}
		}
	}

	private Integer getPutinOperateResult(String position_id) {
		// 211 本线后续工位从分解库位取得 //  正式化 2022/2 废弃
//		if (position_id.endsWith("17")
//				|| position_id.endsWith("18")
//				|| position_id.endsWith("19")) { // || position_id.endsWith("77")
//			return 7;
//		} else {
			return 0;
//		}
	}

	/** 判断线长点检 */
	public String checkLpi(String id, String process_code, SqlSession conn) {
		LeaderPcsInputMapper lpiMapper = conn.getMapper(LeaderPcsInputMapper.class);

		switch (process_code) {
		case "301" : 
			return lpiMapper.checkSnoutConfirm(id);
		case "302" : 
			return lpiMapper.checkCcdConfirm(id);
		}
		return null;
	}

	public int getReworkCountWithLine(String material_id, String line_id, SqlSession conn) {
		ProductionFeatureMapper pfDao = conn.getMapper(ProductionFeatureMapper.class);

		return pfDao.getReworkCountWithLine(material_id, line_id);
	}

	public boolean checkPositionDid(String material_id, String position_id, String operate_result, String rework, SqlSession conn) {
		ProductionFeatureMapper ppDao = conn.getMapper(ProductionFeatureMapper.class);
		return ppDao.checkPositionDid(material_id, position_id, operate_result, rework);
	}

	/**
	 * 发动下一个工位
	 * @param material_id
	 * @param workingPf
	 * @param conn
	 * @return 
	 * @throws Exception 
	 */
	public List<String> fingerNextPosition(String material_id, ProductionFeatureEntity workingPf, SqlSessionManager conn, List<String> triggerList) throws Exception {
		return fingerNextPosition(material_id, workingPf, conn, triggerList, true);
	}
	public List<String> fingerNextPosition(String material_id, ProductionFeatureEntity workingPf, SqlSessionManager conn, List<String> triggerList, boolean isFact) throws Exception {
		MaterialProcessService mpService = new MaterialProcessService();

		String position_id = workingPf.getPosition_id();
		List<String> partOrderPoses  = PositionService.getPositionsBySpecialPage("part_order", conn);
		List<String> dismantlePoses  = PositionService.getPositionsBySpecialPage("dismantle", conn);
		List<String> qualityAssurancePoses  = PositionService.getPositionsBySpecialPage("qualityAssurance", conn);

		// 发动工位
		List<String> nextPositions = new ArrayList<String>();

		MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
		MaterialEntity mEntity = mDao.getMaterialNamedEntityByKey(material_id);
		
		// 备品/RC品灭菌消毒完成
		if ("00000000010".equals(position_id) || "00000000011".equals(position_id)) {
			if(mEntity == null){
				mDao.updateOutLineTime(material_id);
				return null;
			}else{
				Integer break_back_flg = mEntity.getBreak_back_flg();
				
				if (break_back_flg == 3 || break_back_flg == 4) {
					mDao.updateOutLineTime(material_id);
					return null;
				}
			}
		}
		
		Integer level = mEntity.getLevel();
		boolean isLightFix = RvsUtils.isLightFix(level) && mEntity.getFix_type() == 1;
		
		String pat_id = mEntity.getPat_id(); // 维修流程主键

		ProcessAssignProxy paProxy = new ProcessAssignProxy(material_id, pat_id, mEntity.getSection_id(), isLightFix, conn);

		boolean fixed = true;

		// 返回的流程信息
		List<String> ret = new ArrayList<String>();

		// 固定流程工位 (有特殊界面的不列在内)
		if ("00000000010".equals(position_id) || "00000000011".equals(position_id)) { // 消毒灭菌
			String category_id = mEntity.getCategory_id();
			CategoryService cService = new CategoryService();
			String quote_pat_id = cService.getDetail(category_id, conn).getDefault_quote_pat_id();
			// paProxy = new ProcessAssignProxy(material_id, quote_pat_id, "00000000001", isLightFix, conn);
			ProcessAssignService pas = new ProcessAssignService();
			List<String> firstPositionIds = pas.getFirstPositionIds(quote_pat_id, conn);
			nextPositions.addAll(firstPositionIds);
		} else if (qualityAssurancePoses.contains(position_id)) { // 品保
			if (RvsConsts.POSITION_PRODUCT_QA.equals(position_id)) { 
				nextPositions.add(RvsConsts.POSITION_PRODUCT_SHIPPING);
			} else {
				nextPositions.add("00000000047");
			}
//		} else if (RvsConsts.POSITION_PRODUCT_QA.equals(position_id)) { // 品保
//			
		} else if ("00000000015".equals(position_id)) { // 图像检查
			if (mEntity.getBreak_back_flg() != null && mEntity.getBreak_back_flg() == 2) { // 未修理返还
				nextPositions.add("00000000047"); // 出货
			}
		} else if ( !isLightFix &&
				("00000000025".equals(position_id))) { //CCD or LG  || "00000000060".equals(position_id)
			// 已投线
			if (mEntity.getInline_time() != null) {
				ProcessAssignService pas = new ProcessAssignService();
				List<String> firstPositionIds = pas.getFirstPositionIds(mEntity.getPat_id(), conn);
				// 确认已经是否完成作业
				ProductionFeatureMapper ppDao = conn.getMapper(ProductionFeatureMapper.class);
				if (firstPositionIds.size() > 0) {
					for (String firstPositionId : firstPositionIds) {
						if (!ppDao.checkPositionDid(material_id, firstPositionId, "2", null)) {
							nextPositions.add(firstPositionId); // 分解
						}
					}
				}
			}
		} else { // 维修流程上
			if ("00000000016".equals(position_id)) { // 零件分解库位
//				DeposeStorageMapper dsMapper = conn.getMapper(DeposeStorageMapper.class);
//
//				// 返工等情况，判断已放入
//				Map<String, String> materiaIncase = dsMapper.getDeposeStorageByMaterial(material_id);
//				if (materiaIncase == null) {
//					String caseCode = null;
//					if (level == 1) {
//						caseCode = dsMapper.getNextEmptyStorage("S1");
//					} else {
//						String maxUsedCaseCode = dsMapper.getMaxStorage("S3");
//						caseCode = dsMapper.getNextEmptyStorage(maxUsedCaseCode);
//						if (caseCode == null) {
//							caseCode = dsMapper.getNextEmptyStorage("S3");
//						}
//					}
//					if (caseCode == null) {
//						if (isFact) {
//							throw new Exception(ApplicationMessage.WARNING_MESSAGES.getMessage("info.deposeStorage.full"));
//						} else {
//							ret.add("[" + ApplicationMessage.WARNING_MESSAGES.getMessage("info.deposeStorage.full") + "]");
//						}
//					} else {
//						dsMapper.putIntoStorage(material_id, caseCode);
//						Map<String, String> dsMap = dsMapper.getDeposeStorageByCode(caseCode);
//						ret.add("[内镜分解库位：" + dsMap.get("shelf_name") + "]");
//					}
//				} else {
//					ret.add("[内镜分解库位：" + materiaIncase.get("shelf_name") + "]");
//				}
			} else
			if (partOrderPoses.contains(position_id)) { // 零件订购
				// 2期进行后就取消 TODO
				if (isFact) {
					MaterialPartialService mptlService = new MaterialPartialService();
					mptlService.createMaterialPartialWithExistCheck(material_id, conn);
				}
			} else

//			if (!isLightFix && 
//					"00000000041".equals(position_id)
//				) { // 总组Over TODO
//				if (isFact) {
//
//					mpService.finishMaterialProcess(material_id, "00000000014", triggerList, conn);
//				}
//			} else
//			if (!isLightFix && "00000000050".equals(position_id)) { // WKEOver TODO
//				if (isFact) {
//					mpService.finishMaterialProcess(material_id, "00000000050", triggerList, conn);
//					// FSE 数据同步
//					try{
//						FseBridgeUtil.toUpdateMaterialProcess(material_id, "COM");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			} else
			if (!isLightFix && "00000000095".equals(position_id)) { // FEBDECOver TODO
				if (isFact) {
					// 检查本工程是否都完成
					ProcessAssignMapper paMapper = conn.getMapper(ProcessAssignMapper.class);
					if (paMapper.getWorkedLine(material_id, "00000000060")) {
						mpService.finishMaterialProcess(material_id, "00000000060", triggerList, conn);
					}
				}
			} else
//			if (!isLightFix && "00000000045".equals(position_id)) { // FEBCOMOver TODO
//				if (isFact) {
//					// 检查本工程是否都完成
//					ProcessAssignMapper paMapper = conn.getMapper(ProcessAssignMapper.class);
//					if (paMapper.getWorkedLine(material_id, "00000000061")) {
//						mpService.finishMaterialProcess(material_id, "00000000061", triggerList, conn);
//					}
//				}
//			} else
//			if (!isLightFix && "00000000063".equals(position_id)) { // PERIOver TODO
//				if (isFact) {
//					mpService.finishMaterialProcess(material_id, "00000000070", triggerList, conn);
//				}
//			} else
			if (!isLightFix && ("00000000031".equals(position_id) || "00000000085".equals(position_id))) { 
				if (isFact) {
					// 检查本工程是否都完成 // NSOver TODO
					ProcessAssignMapper paMapper = conn.getMapper(ProcessAssignMapper.class);
					if (paMapper.getWorkedLine(material_id, "00000000013")) {
						mpService.finishMaterialProcess(material_id, "00000000013", triggerList, conn);
//						// FSE 数据同步
//						try{
//							FseBridgeUtil.toUpdateMaterialProcess(material_id, "NS");
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
					}
				}
			} else
			if ("00000000088".equals(position_id) || "00000000089".equals(position_id) 
					|| "00000000099".equals(position_id) || "00000000100".equals(position_id)) {
				if (isFact) {
					// 检查本工程是否都完成 // 300 400 500 TODO
					ProcessAssignMapper paMapper = conn.getMapper(ProcessAssignMapper.class);
					if (paMapper.getWorkedLine(material_id, "00000000054")) {
						mpService.finishMaterialProcess(material_id, "00000000054", triggerList, conn);
					}
				}
			} else
			if (!isLightFix && ("00000000090".equals(position_id))) { 
				if (isFact) {
					// 检查本工程是否都完成 // fenjieOver TODO
					ProcessAssignMapper paMapper = conn.getMapper(ProcessAssignMapper.class);
					if (paMapper.getWorkedLine(material_id, "00000000012")) {
						mpService.finishMaterialProcess(material_id, "00000000012", triggerList, conn);

//						// FSE 数据同步
//						try{
//							FseBridgeUtil.toUpdateMaterialProcess(material_id, "DEC");
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
					}
				}
//			} else
//			if (!isLightFix && "00000000021".equals(position_id)) { // fenjieOver TODO
//
//				mEntity = mDao.getMaterialNamedEntityByKey(material_id);
//				if ("00000000016".equals(mEntity.getCategory_id())) { // 外科镜
//
//					if (isFact) {
//						mpService.finishMaterialProcess(material_id, "00000000012", triggerList, conn);
//
//						// FSE 数据同步
//						try{
//							FseBridgeUtil.toUpdateMaterialProcess(material_id, "ENDOEYE_DEC");
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
			}

			 // 维修流程参照
			boolean reachEnd = getNext(paProxy, material_id, pat_id, position_id, mEntity.getLevel(), nextPositions);
			reachEnd = reachEnd && (mEntity.getInline_time() != null);
			if (reachEnd) { // 最终检查流程
				CategoryService cService = new CategoryService();
				String qa_position_id = cService.getDetail(mEntity.getCategory_id(), conn).getQa_position_id();
				if (qa_position_id == null) {
					qa_position_id = RvsConsts.POSITION_QA;
					nextPositions.clear();
					nextPositions.add(qa_position_id);
					if (isFact) {
						if (qa_position_id == RvsConsts.POSITION_PRODUCT_QA) {
							// MF1Over
							// 检查本工程是否都完成
							ProcessAssignMapper paMapper = conn.getMapper(ProcessAssignMapper.class);
							if (paMapper.getWorkedLine(material_id, "00000000101")) {
								mpService.finishMaterialProcess(material_id, "00000000101", triggerList, conn);
							}
						} else {
							PositionService posService = new PositionService();
							String line_id = posService.getPositionEntityByKey(position_id, conn).getLine_id();
							if (line_id != null) {
								// 检查本工程是否都完成
								ProcessAssignMapper paMapper = conn.getMapper(ProcessAssignMapper.class);
								if (paMapper.getWorkedLine(material_id, line_id)) {
									mpService.finishMaterialProcess(material_id, line_id, triggerList, conn);
								}
							}
						}
					}
				} else {
					nextPositions.add(qa_position_id);
					PositionService posServ = new PositionService();
					PositionEntity posEntity = posServ.getPositionEntityByKey(position_id, conn);
					mpService.finishMaterialProcess(material_id, posEntity.getLine_id(), triggerList, conn);
				}
				fixed = true;
			} else {
				fixed = false;
			}

			if (isFact && dismantlePoses.contains(position_id)) {
				MaterialPartialService mptlService = new MaterialPartialService();
				mptlService.createMaterialPartialWithExistCheck(material_id, conn);
			}

//			 if (nextPositions.size() == 1 && RvsConsts.POSITION_QA.equals(nextPositions.get(0))) fixed = true;
//			 else if (nextPositions.size() == 1 && RvsConsts.POSITION_PERI_QA.equals(nextPositions.get(0))) fixed = true;
//			 else if (nextPositions.size() == 1 && RvsConsts.POSITION_PRODUCT_QA.equals(nextPositions.get(0))) {
//				fixed = true;
//				// MF1Over
//				if (isFact) {
//					// 检查本工程是否都完成
//					ProcessAssignMapper paMapper = conn.getMapper(ProcessAssignMapper.class);
//					if (paMapper.getWorkedLine(material_id, "00000000101")) {
//						mpService.finishMaterialProcess(material_id, "00000000101", triggerList, conn);
//					}
//				}
//			 }
//			 else fixed = false;

			if (isLightFix && isFact) {
				// 小修理
				MaterialProcessAssignMapper mpaMapper = conn.getMapper(MaterialProcessAssignMapper.class);
				// 取得覆盖工程
				List<LineEntity> lines = mpaMapper.getWorkedLines(material_id);

				for (LineEntity line : lines) {
					if (CommonStringUtil.isEmpty(line.getName())
							&& line.getInline_flg()) {

						mpService.finishMaterialProcess(material_id, line.getLine_id(), triggerList, conn);
					}
				}
			}
		}

		ProductionFeatureEntity nPf = new ProductionFeatureEntity();
		nPf.setSection_id(workingPf.getSection_id());
		nPf.setRework(workingPf.getRework());

		ProductionFeatureMapper pfDao = conn.getMapper(ProductionFeatureMapper.class);

		// 建立后续工位的初始作业信息
		for (String nextPosition_id : nextPositions) {
			nPf.setPosition_id(nextPosition_id);
			fingerPosition(mEntity, fixed, nPf, conn, pfDao, paProxy, ret, triggerList, isFact);
		}

		Set<String> dividePositions = PositionService.getDividePositions(conn);

		PositionMapper ps = conn.getMapper(PositionMapper.class);
		for (int i = 0; i < ret.size(); i++) {
			String ret_position_id = ret.get(i);

			if (ret_position_id.indexOf("[") < 0) {
				PositionEntity position = ps.getPositionByID(ret_position_id);

				if (position == null) continue;

				// 判断切线
				if (isFact && 
					dividePositions.contains(position.getPosition_id())) {
					mpService.checkDividePx(material_id, workingPf.getLine_id(), workingPf.getOperator_id(), conn);
				}

				// 变成文字描述
				ret.set(i, "[" + position.getProcess_code() + " " + position.getName() + "]");
			}
		}

		return ret;
	}

	/**
	 * 发动指定工位
	 * @param material_id
	 * @param fixed 已判断关联完成
	 * @param workingPf need Position/Section/Rework
	 * @param conn
	 * @throws Exception 
	 */
	public void fingerSpecifyPosition(String material_id, boolean fixed, ProductionFeatureEntity workingPf, List<String> triggerList, SqlSessionManager conn) throws Exception {
		MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
		MaterialEntity mEntity = mDao.getMaterialEntityByKey(material_id);

		ProductionFeatureMapper pfDao = conn.getMapper(ProductionFeatureMapper.class);
		Integer level = mEntity.getLevel();//等级

		boolean isLightFix = RvsUtils.isLightFix(level) && mEntity.getFix_type() == 1;

		ProcessAssignProxy paProxy = new ProcessAssignProxy(material_id, mEntity.getPat_id(), mEntity.getSection_id(), isLightFix, conn);

		fingerPosition(mEntity, fixed, workingPf, conn, pfDao, paProxy, null, triggerList);
	}

	public boolean getNext(ProcessAssignProxy paProxy, String material_id, String pat_id, String position_id, Integer level, List<String> nextPositions) {
		// 得到下一个工位
		List<PositionEntity> nextPositionsByPat = paProxy.getNextPositions(position_id);

		boolean reachEnd = false;

		String this_position_id = null;
		if (nextPositionsByPat.size() > 0) {
			this_position_id = nextPositionsByPat.get(0).getPosition_id();
		}

		if (this_position_id == null || ((RvsConsts.PROCESS_ASSIGN_LINE_END+"").equals(this_position_id))) {
			// 下一个工位是PROCESS_ASSIGN_LINE_END
			this_position_id = RvsConsts.PROCESS_ASSIGN_LINE_END + "";
			// 用它自身的Line_id区分
			ProcessAssignEntity pa = paProxy.getProcessAssign(position_id);
			if (pa == null) return true; // 未设流程时
			String line_id = pa.getLine_id();
			if ((RvsConsts.PROCESS_ASSIGN_LINE_BASE + "").equals(line_id)) {
				if (paProxy.getFinishedByLine(line_id)) {
					reachEnd = true;
//					if (RvsUtils.isPeripheral(level)) {
//						nextPositions.add(RvsConsts.POSITION_PERI_QA);
//					} else if (level == 0) {
//						nextPositions.add(RvsConsts.POSITION_PRODUCT_QA);
//					} else {
//						// 并且是主流程时，611工位
//						nextPositions.add(RvsConsts.POSITION_QA);
//					}
				}
			} else {
				// 并且不是主流程时，

				// 所在流程判断是否全部完成
				if (paProxy.getFinishedByLine(line_id)) {
					// 如果所在流程全部完成，触发流程的下一个工位
					reachEnd = getNext(paProxy, material_id, pat_id, line_id, level, nextPositions);
				}
			}
		} else {
			for (PositionEntity nextPosition : nextPositionsByPat) {
				// 判断每个工位是不是分线名
				if (Integer.parseInt(nextPosition.getPosition_id()) > RvsConsts.PROCESS_ASSIGN_LINE_BASE) {
					// 是分线得到分线的由0开始的工位 ,不考虑嵌套分线
					List<String> positions = paProxy.getPartStart(nextPosition.getPosition_id());
					for (String position : positions) {
						nextPositions.add(position);
					}
				} else {
					// 增加单个工位
					nextPositions.add(nextPosition.getPosition_id());
				}
			}
		}

		// S1等级略过工位
		if (level == 1 && !"00000000008".equals(pat_id) && !"00000000009".equals(pat_id)) { // TODO 补胶
			int lNextPositions = nextPositions.size();
			for (int i = lNextPositions - 1; i >= 0; i--) {
				String nextPosition = nextPositions.get(i);
				for (int j = 0; j < PositionService.getS1PASSES().length; j++) {
					if (PositionService.getS1PASSES()[j].equals(fillChar(nextPosition, '0', 11, true))) {
						nextPositions.remove(i);
						break;
					}
				}
			}
		}

		return reachEnd;
	}

	public void getPrev(ProcessAssignProxy paProxy, String material_id, String pat_id, String position_id, Integer level, List<String> prevPositions) {
		// 得到先决工位
		List<PositionEntity> prevPositionsByPat = paProxy.getPrevPositions(position_id);

		String this_position_id = null;
		if (prevPositionsByPat.size() > 0) {
			this_position_id = prevPositionsByPat.get(0).getPosition_id();
		}

		if (this_position_id == null || ((RvsConsts.PROCESS_ASSIGN_LINE_START+"").equals(this_position_id))) {
			// 上一个工位是0，用它自身的Line_id区分
			ProcessAssignEntity pa = paProxy.getProcessAssign(position_id);
			String line_id = pa.getLine_id();
			if ((RvsConsts.PROCESS_ASSIGN_LINE_BASE + "").equals(line_id)) {
				// 并且是主流程时，不需要先决
				return;
			} else {
				// 并且不是主流程时，

				// 取得所在流程的先决
				getPrev(paProxy, material_id, pat_id, line_id, level, prevPositions);
			}
		} else {
			for (PositionEntity prevPosition : prevPositionsByPat) {
				// 判断每个工位是不是分线名
				if (Integer.parseInt(prevPosition.getPosition_id()) > RvsConsts.PROCESS_ASSIGN_LINE_BASE) {
					// 是分线的话，分线中的全部工位都是先决工位 ,不考虑嵌套分线
					List<String> positions = paProxy.getPartAll(prevPosition.getPosition_id());
					for (String position : positions) {
						prevPositions.add(position);
					}
				} else {
					// 增加单个工位
					prevPositions.add(prevPosition.getPosition_id());
				}
			}
		}

		// S1等级略过工位
		if (level == 1 && !"00000000008".equals(pat_id) && !"00000000009".equals(pat_id)) {
			int lNextPositions = prevPositions.size();
			for (int i = lNextPositions - 1; i >= 0; i--) {
				String prevPosition = prevPositions.get(i);
				for (int j = 0; j < PositionService.getS1PASSES().length; j++) {
					if (PositionService.getS1PASSES()[j].equals(fillChar(prevPosition, '0', 11, true))) {
						prevPositions.remove(i);
						break;
					}
				}
			}
		}
	}

	/**
	 * 按条件检索作业情报，期望返回一条记录
	 * @param pf 检索条件
	 * @param conn
	 * @return
	 */
	public ProductionFeatureEntity searchProductionFeatureOne(ProductionFeatureEntity pf, SqlSessionManager conn) {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		List<ProductionFeatureEntity> results = dao.searchProductionFeature(pf);
		if (results != null && results.size() > 0) {
			return results.get(0);
		}
		return null;
	}

	public void changeSection(String material_id, String section_id, SqlSessionManager conn) throws Exception {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		dao.changeSection(material_id, section_id);
	}

	/**
	 * 按照当前的维修流程，重新自起点起寻找接下来需要开始作业的工位
	 * @param material_id
	 * @param rework
	 * @param ppDao
	 * @param conn
	 * @throws Exception
	 */
	public void reprocess(String material_id, int rework, ProductionFeatureMapper ppDao, List<String> triggerList, SqlSessionManager conn) throws Exception {
		// 重新开始执行流程
		// 取得维修流程
		MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
		MaterialEntity mEntity = mDao.getMaterialEntityByKey(material_id);

		// 找到整个流程的起点，开始触发。
		Integer level = mEntity.getLevel();
		boolean isLightFix = RvsUtils.isLightFix(level) && mEntity.getFix_type() == 1;

		String pat_id = mEntity.getPat_id(); // 维修流程主键
		ProcessAssignProxy paProxy = new ProcessAssignProxy(material_id, pat_id, mEntity.getSection_id(), isLightFix, conn);

		List<String> startPositions = paProxy.getPartStart("" + RvsConsts.PROCESS_ASSIGN_LINE_BASE);
		List<String> groupPositions = new ArrayList<String>(); // 作为大分支
		for (String startPosition : startPositions) {
			if (Integer.parseInt(startPosition) > RvsConsts.PROCESS_ASSIGN_LINE_BASE) {
				startPositions.addAll(paProxy.getPartStart(startPosition));
				groupPositions.add(startPosition);
			}
		}
		for (String startPosition : groupPositions) {
			startPositions.remove(startPosition);
		}

		ProductionFeatureEntity workingPf = new ProductionFeatureEntity();
		workingPf.setSection_id(mEntity.getSection_id());
		workingPf.setRework(rework);

		for (String startPosition : startPositions) {
			workingPf.setPosition_id(startPosition);
			fingerPosition(mEntity, false, workingPf, conn, ppDao, paProxy, null, triggerList);
		}
	}

	public void removeWorking(String material_id, String position_id, SqlSessionManager conn) throws Exception {
		ProductionFeatureMapper paDao = conn.getMapper(ProductionFeatureMapper.class);

		paDao.removeWaiting(material_id, position_id);
		if (position_id != null) 
			paDao.reworkOperateResult(material_id, position_id);
	}

	/**
	 * 检查是否有多处中断
	 * @param id
	 * @param conn
	 * @return
	 */
	public int checkOtherBreak(String id, SqlSessionManager conn) {
		AlarmMesssageMapper dao = conn.getMapper(AlarmMesssageMapper.class);
		return dao.countBreakUnPushedAlarmMessage(id);
	}

	public void startProductionFeature(ProductionFeatureEntity waitingPf, SqlSessionManager conn) throws Exception {
		// 作业信息状态改为，作业中
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		dao.startProductionFeature(waitingPf);
	}

	public void changeWaitProductionFeature(ProductionFeatureEntity workwaitingPf,
			SqlSessionManager conn) throws Exception {
		// 作业信息状态改为，作业中
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		dao.pauseWaitProductionFeature(workwaitingPf);
	}

	public void finishProductionFeature(ProductionFeatureEntity workingPf,
			SqlSessionManager conn) throws Exception {
		ProductionFeatureMapper pfdao = conn.getMapper(ProductionFeatureMapper.class);
		pfdao.finishProductionFeature(workingPf);
	}

	public void finishProductionFeatureSetFinish(ProductionFeatureEntity workingPf,
			SqlSessionManager conn) throws Exception {
		ProductionFeatureMapper pfdao = conn.getMapper(ProductionFeatureMapper.class);
		pfdao.finishProductionFeatureSetFinish(workingPf);
	}

	public String getFingerString(String material_id, List<String> fingerList, SqlSession conn, boolean isFact) {
		MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
		MaterialEntity mEntity = mDao.getMaterialEntityByKey(material_id);

		String idNo = mEntity.getSorc_no();
		if (idNo == null) idNo = "(机身号" + mEntity.getSerial_no() + ")";
		if (isFact)
			return ApplicationMessage.WARNING_MESSAGES
				.getMessage("info.transfer.justFinshed", idNo, joinBy(", ", fingerList.toArray(new String[fingerList.size()])));
		else
			return ApplicationMessage.WARNING_MESSAGES
				.getMessage("info.transfer.justNow", idNo, joinBy(", ", fingerList.toArray(new String[fingerList.size()])));
	}

	public Map<String, String> getPoistionsOfMaterial(String materialId, SqlSession conn) {
		ProductionFeatureMapper mapper = conn.getMapper(ProductionFeatureMapper.class);
		List<ProductionFeatureEntity> results = mapper.getWorkedPositionOfMaterial(materialId);
		Map<String, String> ret = new TreeMap<String, String>();
		for (ProductionFeatureEntity result : results) {
			ret.put(result.getPosition_id(), result.getProcess_code() + " " + result.getPosition_name());
		}
		return ret;
	}

	/** 维修等待区删除 */
	public void removeWaiting(String material_id, String position_id,
			SqlSessionManager conn) {
		ProductionAssignMapper mapper = conn.getMapper(ProductionAssignMapper.class);
		mapper.remove(material_id, position_id);
	}
	
	/**
	 * 判断是否完成消毒
	 * @param material_id
	 * @param conn
	 */
	public int checkFinishedDisinfection(String material_id, SqlSession conn) {
		// ProductionFeatureMapper mapper = conn.getMapper(ProductionFeatureMapper.class);
		return 1; // mapper.checkFinishedDisinfection(material_id);
	}

	public void updateFinishTime(ProductionFeatureEntity workingPf,SqlSessionManager conn){
		ProductionFeatureMapper mapper = conn.getMapper(ProductionFeatureMapper.class);
		mapper.updateFinishTime(workingPf);
	}

}
