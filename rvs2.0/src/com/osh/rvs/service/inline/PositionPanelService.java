package com.osh.rvs.service.inline;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.ForSolutionAreaEntity;
import com.osh.rvs.bean.inline.SoloProductionFeatureEntity;
import com.osh.rvs.bean.inline.WaitingEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.DeposeStorageMapper;
import com.osh.rvs.mapper.inline.PositionPanelMapper;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.inline.SoloProductionFeatureMapper;
import com.osh.rvs.mapper.master.ProcessAssignMapper;
import com.osh.rvs.mapper.qf.QuotationMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.JustlengthValidator;
import framework.huiqing.common.util.validator.LongTypeValidator;

public class PositionPanelService {
	protected static final Logger _log = Logger.getLogger("Production");

	/**
	 * 取得工位当前基本信息
	 * @param section_id
	 * @param position_id
	 * @param level
	 * @param conn
	 * @return
	 */
	public Map<String, Integer> getPositionMap(String section_id, String position_id, String level, SqlSession conn) {
		Map<String, Integer> retMap = new HashMap<String, Integer>();

		/// 从数据库中查询记录
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);

		// 取得今日作业时间
		retMap.put("run_cost", dao.checkPositionStartedWorkTime(section_id, position_id, level) / 60);

		// 取得今日操作时间
		retMap.put("operator_cost", dao.checkTodayWorkCost(section_id, position_id, level) / 60);

		// 取得处理件数
		retMap.put("finish_count", dao.getFinishCount(section_id, position_id, level));

		// 取得代工件数</td>
		retMap.put("support_count", dao.getLeaderSupportFinishCount(section_id, position_id, level));

		// 取得暂停次数
		// 取得中断次数
		int countPause = 0;
		int countBreak = 0;

		List<Map<String, Number>> breakcounts = dao.getTodayBreak(section_id, position_id, level);
		if (breakcounts != null) {
			for (Map<String, Number> breakcount : breakcounts) {
				int reasonCode = breakcount.get("reason").intValue();
				if (reasonCode >= 49 && reasonCode < 70) // 暂停
					countPause += breakcount.get("count_reason").intValue();
				else if (reasonCode < 20) // 中断
					countBreak += breakcount.get("count_reason").intValue();
				// 作业流程不算
			}
		}

		retMap.put("pause_count", countPause);
		retMap.put("break_count", countBreak);

		retMap.put("waiting_count", getWaiting(section_id, position_id, level, conn).size());
		return retMap;
	}

	/**
	 * 取得等待维修品
	 * @param section_id
	 * @param position_id
	 * @param level
	 * @param conn
	 * @return
	 */
	public List<ProductionFeatureEntity> getWaiting(String section_id, String position_id, String level, SqlSession conn) {
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		// 取得等待维修品
		List<ProductionFeatureEntity> waitings = dao.getWaiting(null, section_id, position_id, level);

		return waitings;
	}

	/**
	 * 判断是否所在工位的等待区中维修对象，如是则返回等待作业
	 * @param material_id
	 * @param session
	 * @param errors
	 */
	public List<WaitingEntity> checkWaitingMaterial(LoginData user, SqlSession conn) {

		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);

		List<WaitingEntity> ret = dao.getWaitingMaterial(user.getLine_id(), user.getSection_id(),
				user.getPosition_id(), user.getOperator_id(), user.getPx());

		return ret;
	}

	/**
	 * 判断是否所在工位的等待区中维修对象
	 * @param material_id
	 * @param session
	 * @param errors
	 */
	public List<ProductionFeatureEntity> isWaitingMaterial(String material_id, String section_id, String position_id,
			String level, SqlSession conn) {

		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		// 取得等待维修品
		List<ProductionFeatureEntity> waitings = dao.getWaiting(material_id, section_id, position_id, level);

		return waitings;
	}

	/**
	 * 检查扫描维修对象合法性
	 * @param material_id
	 * @param user
	 * @param errors
	 * @param conn
	 * @return
	 */
	public ProductionFeatureEntity checkMaterialId(String material_id, LoginData user, List<MsgInfo> errors, SqlSession conn) {
		ProductionFeatureEntity retWaiting = null;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("material_id", material_id);
		if (CommonStringUtil.isEmpty(material_id)) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("material_id");
			msgInfo.setErrcode("validator.required");
			msgInfo.setErrmsg("扫描失敗！");
			errors.add(msgInfo);
		}

		String message1 = new LongTypeValidator("扫描号码").validate(parameters, "material_id");
		if (message1 != null) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("material_id");
			msgInfo.setErrcode("validator.invalidParam.invalidIntegerValue");
			msgInfo.setErrmsg(message1);
			errors.add(msgInfo);
		}
		String message2 = new JustlengthValidator("扫描号码", 11).validate(parameters, "material_id");
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
			List<WaitingEntity> waitings = checkWaitingMaterial(user, conn);
			int count = waitings.size();
			if (count == 0) {
				// 等待区内没有维修对象
				msgInfo = new MsgInfo();
				msgInfo.setComponentid("material_id");
				msgInfo.setErrcode("info.linework.notInWaiting");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.notInWaiting"));
				errors.add(msgInfo);

				return retWaiting;
			} else {

				WaitingEntity scan = null;
				List<WaitingEntity> pExpedited = new ArrayList<WaitingEntity>();
//				List<WaitingEntity> lExpedited = new ArrayList<WaitingEntity>();
//				List<WaitingEntity> today = new ArrayList<WaitingEntity>();

				for (WaitingEntity waiting : waitings) {
					if (material_id.equals(waiting.getMaterial_id())) { // 是开始对象的话
						scan = waiting;
						// 在工位上等待的维修对象
						List<ProductionFeatureEntity> productionFeature = isWaitingMaterial(material_id,
								user.getSection_id(), user.getPosition_id(), user.getPx(), conn);
						count = productionFeature.size();
						if (count == 0) {
							// 维修对象不在用户所在等待区
							msgInfo = new MsgInfo();
							msgInfo.setComponentid("material_id");
							msgInfo.setErrcode("info.linework.notInWaiting");
							msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.notInWaiting"));
							errors.add(msgInfo);
						} else {
							// 如有则返回等待中的作业信息。
							retWaiting = productionFeature.get(0);

							if (errors.size() == 0) {
								ForSolutionAreaService fsaService = new ForSolutionAreaService();
								List<ForSolutionAreaEntity> blocks = fsaService.checkBlock(material_id, user.getPosition_id(), user.getLine_id(), conn);
								if (blocks != null && blocks.size() > 0) {
									ForSolutionAreaEntity block = blocks.get(0);
									String blockReason = CodeListUtils.getValue("offline_reason", ""+block.getReason());
									msgInfo = new MsgInfo();
									msgInfo.setComponentid("material_id");
									msgInfo.setErrcode("info.linework.notInWaiting");
									msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.blockedForSolve"
											, waiting.getSorc_no(), blockReason, block.getComment()));
									errors.add(msgInfo);
								}
							}
						}
					} else {
						if ("0".equals(waiting.getWaitingat()))
							// 限未处理的的需要优先项目
							if (waiting.getExpedited() != null && waiting.getExpedited() >= 10) { // 计划加急
								pExpedited.add(waiting);
//							} else if (waiting.getExpedited() != null && waiting.getExpedited() > 0) { // 线长加急
//								lExpedited.add(waiting);
//							} else if (waiting.getToday() != null && waiting.getToday() == 1) { // 今日 TODO not PA
//								today.add(waiting);
							}
					}
				}

				if (scan == null) {
					// 维修对象不在用户所在等待区
					msgInfo = new MsgInfo();
					msgInfo.setComponentid("material_id");
					msgInfo.setErrcode("info.linework.notInWaiting");
					msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.notInWaiting"));
					errors.add(msgInfo);
				} else {

//					String process_code = user.getProcess_code();
//					if ("251".equals(process_code) || "252".equals(process_code) || "321".equals(process_code) || "400".equals(process_code)) { // 代线长工位不按次序
//					} else if (scan.getToday() != null && scan.getToday() == 1) {
//					} else if (today.size() > 0) { // 本身不是加急，不是今日，等待区有今日
//						if ("211".equals(process_code) || "222".equals(process_code)) {
//							
//						} else {
//							msgInfo = new MsgInfo();
//							msgInfo.setComponentid("material_id");
//							msgInfo.setErrcode("info.linework.todayPlanFirst");
//							msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.todayPlanFirst", today.get(0).getSorc_no()));
//							errors.add(msgInfo);
//						}
//					}
				}
			}
		}

		if (retWaiting != null && retWaiting.getOperate_result() == 9){
			msgInfo = new MsgInfo();
			msgInfo.setComponentid("material_id");
			msgInfo.setErrcode("info.linework.partOfReady");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.partOfReady"));
			errors.add(msgInfo);
		}

		return retWaiting;
	}

	/**
	 * 检查再开维修对象合法性
	 * @param material_id
	 * @param user
	 * @param errors
	 * @param conn
	 * @return
	 */
	public ProductionFeatureEntity checkPausingMaterialId(String material_id, LoginData user, List<MsgInfo> errors, SqlSession conn) {
		ProductionFeatureEntity retPausing = null;

		if (CommonStringUtil.isEmpty(material_id)) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("material_id");
			msgInfo.setErrcode("info.linework.notInWaiting");
			msgInfo.setErrmsg("你要再开作业的维修对象已不存在于您的工作区。");
			errors.add(msgInfo);
		}

		if (errors.size() == 0) {
			// 取得用户信息

			PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
			// 在工位上暂停处理的维修对象
			retPausing = dao.getPausing(user.getOperator_id());

			if (retPausing == null) {
				// 维修对象不在用户所在等待区
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setComponentid("material_id");
				msgInfo.setErrcode("info.linework.notInWaiting");
				msgInfo.setErrmsg("你要再开作业的维修对象已不存在于您的工作区。");
				errors.add(msgInfo);
			}
		}
		return retPausing;
	}

	public SoloProductionFeatureEntity checkPausingSerialNo(String serial_no, LoginData user, List<MsgInfo> errors,
			SqlSession conn) {
		SoloProductionFeatureEntity retPausing = null;

		if (CommonStringUtil.isEmpty(serial_no)) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("serial_no");
			msgInfo.setErrcode("info.linework.notInWaiting");
			msgInfo.setErrmsg("你要再开作业的维修对象已不存在于您的工作区。");
			errors.add(msgInfo);
		}

		if (errors.size() == 0) {
			// 取得用户信息

			SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
			// 在工位上暂停处理的维修对象
			retPausing = dao.getPausing(user.getOperator_id());

			if (retPausing == null) {
				// 维修对象不在用户所在等待区
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setComponentid("serial_no");
				msgInfo.setErrcode("info.linework.notInWaiting");
				msgInfo.setErrmsg("你要再开作业的维修对象已不存在于您的工作区。");
				errors.add(msgInfo);
			}
		}
		return retPausing;
	}

	/**
	 * 取得维修对象基本信息表单
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public MaterialForm getMaterialInfo(String material_id, SqlSession conn) {
		MaterialForm materialForm = new MaterialForm();
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		MaterialEntity materialEntity = dao.getMaterialDetail(material_id);
		BeanUtil.copyToForm(materialEntity, materialForm, CopyOptions.COPYOPTIONS_NOEMPTY);

		return materialForm;
	}

	/**
	 * 取得维修对象工作信息
	 * @param material_id
	 * @param user
	 * @param conn
	 * @return
	 */
	public List<ProductionFeatureEntity> getPositionWorksByMaterial(String material_id, LoginData user,
			SqlSession conn) {
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		// 取得用户信息
		String position_id = user.getPosition_id();

		return dao.getPositionWorksByMaterial(material_id, position_id);
	}

	/**
	 * 取得维修对象在指定工位(单次返工中)总使用时间 
	 * @param waitingPf
	 * @param conn
	 * @return
	 */
	public Integer getTotalTimeByRework(ProductionFeatureEntity waitingPf, SqlSession conn) {
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		Integer totalTimeByRework = dao.getTotalTimeByRework(waitingPf);
		if (totalTimeByRework == null) {
			return 0;
		} else {
			return totalTimeByRework;
		}
	}

	/**
	 * 取得等待区信息一览
	 * @param waitingPf
	 * @param conn
	 * @return
	 */
	public List<WaitingEntity> getWaitingMaterial(String section_id, String position_id, String line_id,
			String operator_id, String level, String process_code, SqlSession conn) {
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);

		List<WaitingEntity> ret = dao.getWaitingMaterial(line_id, section_id, position_id, operator_id, level);

		for (WaitingEntity we : ret) {
			if ("0".equals(we.getWaitingat())) we.setWaitingat("未处理");
			else if ("4".equals(we.getWaitingat())) { // 暂停
				// 工位特殊暂停理由
				if (we.getPause_reason() != null && we.getPause_reason() >= 70) {
					String sReason = PathConsts.POSITION_SETTINGS.getProperty("step." + process_code + "." + we.getPause_reason());
					if (sReason == null) {
						we.setWaitingat("正常中断流程");
					} else {
						we.setWaitingat(sReason);
					}
				} else {
					we.setWaitingat("中断恢复");
				}
			}
			else if ("3".equals(we.getWaitingat())) we.setWaitingat("中断等待处置");
			else if ("7".equals(we.getWaitingat())) we.setWaitingat("库位放置中");
			else if ("9".equals(we.getWaitingat())) we.setWaitingat("前序部分完成");

//			if (we.getExpedited() == null || we.getExpedited() == 0) 
//				we.setExpedited(2);
//			if ((we.getToday() == null || we.getToday() != 1))
//				we.setExpedited(-we.getExpedited());
		}
		return ret;
	}

	/**
	 * 得到当前用户处理中维修品
	 * @param user
	 * @param conn
	 * @return
	 */
	public ProductionFeatureEntity getWorkingPf(LoginData user, SqlSession conn) {

		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		// 取得作业维修品
		ProductionFeatureEntity working = dao.getWorking(user.getOperator_id());

		return working;
	}

	/**
	 * 得到当前用户处理中或暂停中维修品
	 * @param user
	 * @param conn
	 * @return
	 */
	public ProductionFeatureEntity getProcessingPf(LoginData user, SqlSession conn) {
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		// 取得作业/暂停维修品
		ProductionFeatureEntity working = dao.getProcessing(user.getOperator_id());

		return working;
	}

	/**
	 * 得到当前用户处理中或辅助中维修品
	 * @param user
	 * @param conn
	 * @return
	 */
	public ProductionFeatureEntity getWorkingOrSupportingPf(LoginData user, SqlSession conn) {
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		// 取得辅助维修品
		ProductionFeatureEntity working = dao.getSupporting(user.getOperator_id());

		return working;
	}

	/**
	 * 得到当前用户暂停中维修品
	 * @param user
	 * @param conn
	 * @return
	 */
	public ProductionFeatureEntity getPausingPf(LoginData user, SqlSession conn) {

		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		// 取得等待维修品
		ProductionFeatureEntity pausing = dao.getPausing(user.getOperator_id());

		return pausing;
	}

	/**
	 * 得到当前用户处理中
	 * @param user
	 * @param errors
	 * @param conn
	 * @return
	 */
	public List<ProductionFeatureEntity> getWorkingPfs(LoginData user, SqlSession conn) {

		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);
		// 取得等待维修品
		List<ProductionFeatureEntity> workings = dao.getWorkingBatch(user.getPosition_id(), user.getOperator_id());

		return workings;
	}

	/**
	 * 取得工程检查票
	 * @param listResponse
	 * @param pf
	 * @param user
	 * @param conn
	 */
	public static void getPcses(Map<String, Object> listResponse, ProductionFeatureEntity pf, String sline_id,
			SqlSession conn) {
		getPcses(listResponse, pf, sline_id, false, conn);
	}
	public static void getPcses(Map<String, Object> listResponse, ProductionFeatureEntity pf, String sline_id,
			boolean isLeader, SqlSession conn) {
		String material_id = pf.getMaterial_id();
		MaterialForm mform = (MaterialForm) listResponse.get("mform");

		List<Map<String, String>> pcses = new ArrayList<Map<String, String>>();

		String[] showLines = {};
		if ("00000000016".equals(mform.getCategory_id())) {
			if ("00000000015".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "最终检验";
				showLines[1] = "外科硬镜修理工程";
			} else {
				showLines = new String[1];
				showLines[0] = "外科硬镜修理工程";
			}
		} else if (mform.getLevel().startsWith("5")) {
			showLines = new String[1];
			showLines[0] = "检查卡";
		} else {
			if ("00000000012".equals(sline_id)) {
				showLines = new String[1];
				showLines[0] = "分解工程";
			} else if ("00000000013".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "NS 工程";
				showLines[1] = "分解工程";
			} else if ("00000000014".equals(sline_id)) {
				showLines = new String[3];
				showLines[0] = "总组工程";
				showLines[1] = "分解工程";
				showLines[2] = "NS 工程";
			} else if ("00000000015".equals(sline_id)) {
				showLines = new String[4];
				showLines[0] = "最终检验";
				showLines[1] = "分解工程";
				showLines[2] = "NS 工程";
				showLines[3] = "总组工程";
			} else if ("00000000054".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "总组工程";
				showLines[1] = "NS 工程";
			} else if ("00000000060".equals(sline_id)) {
				showLines = new String[1];
				showLines[0] = "分解工程";
			} else if ("00000000061".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "总组工程";
				showLines[1] = "分解工程";
			}
		}

		for (String showLine : showLines) {
			Map<String, String> fileTempl = PcsUtils.getXmlContents(showLine, mform.getModel_name(), null, material_id, conn);

			if ("NS 工程".equals(showLine)) filterSolo(fileTempl, material_id, conn);

			Map<String, String> fileHtml = PcsUtils.toHtml(fileTempl, material_id, mform.getSorc_no(),
					mform.getModel_name(), mform.getSerial_no(), mform.getLevel(), pf.getProcess_code(), isLeader ? sline_id : null, conn);
			fileHtml = RvsUtils.reverseLinkedMap(fileHtml);
			pcses.add(fileHtml);
		}

		listResponse.put("pcses", pcses);
	}

	/**
	 * 取得工程检查票(工位全完成)
	 * @param listResponse
	 * @param pf
	 * @param user
	 * @param conn
	 */
	public static void getPcsesFinish(Map<String, Object> listResponse, ProductionFeatureEntity pf,
			SqlSession conn) {
		String material_id = pf.getMaterial_id();
		MaterialForm mform = (MaterialForm) listResponse.get("mform");

		List<Map<String, String>> pcses = new ArrayList<Map<String, String>>();

		String[] showLines = new String[4];
		showLines[0] = "最终检验";
		showLines[1] = "分解工程";
		showLines[2] = "NS 工程";
		showLines[3] = "总组工程";

		for (String showLine : showLines) {
			Map<String, String> fileTempl = PcsUtils.getXmlContents(showLine, mform.getModel_name(), null, material_id, conn);

			if ("NS 工程".equals(showLine)) filterSolo(fileTempl, material_id, conn);

			Map<String, String> fileHtml = PcsUtils.toHtml(fileTempl, material_id, mform.getSorc_no(),
					mform.getModel_name(), mform.getSerial_no(), mform.getLevel(), "619", null, conn);
			fileHtml = RvsUtils.reverseLinkedMap(fileHtml);
			pcses.add(fileHtml);
		}

		listResponse.put("pcses", pcses);
	}

	/**
	 * 根据实际工作履历，过滤可选的工位相关工程检查票
	 * @param fileTempl
	 * @param material_id
	 * @param conn
	 */
	private static void filterSolo(Map<String, String> fileTempl, String material_id, SqlSession conn) {

		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);

		List<String> snouts = new ArrayList<String>(); 
		List<String> ccds = new ArrayList<String>(); 
		List<String> eyeLens = new ArrayList<String>(); 
		List<String> ccdls = new ArrayList<String>(); 

		for (String key : fileTempl.keySet()) {
			if (key.contains("先端预制")) {
				snouts.add(key);
			}
			else if (key.contains("CCD盖玻璃")) {
				ccds.add(key);
			}
			else if (key.contains("LG")) {
				eyeLens.add(key);
			}
			else if (key.contains("CCD线")) { // TODO
				ccdls.add(key);
			}
		}
		// 如果有先端预制工程检查票
		if (snouts.size() > 0) {
			// 检查是否做过301工位
			if (!dao.checkPositionDid(material_id, "00000000024", null, null)) {
				for (String snout : snouts) {
					fileTempl.remove(snout);
				}
			}
		}
		
		// 如果有先端预制工程检查票
		if (ccds.size() > 0) {
			// 检查是否做过302工位
			if (!dao.checkPositionDid(material_id, "00000000025", null, null)) {
				for (String ccd : ccds) {
					fileTempl.remove(ccd);
				}
			}
		}

		// 如果有LG玻璃工程检查票
		if (eyeLens.size() > 0) {
			// 检查是否做过303工位
			if (!dao.checkPositionDid(material_id, "00000000060", null, null)) {
				for (String lg : eyeLens) {
					fileTempl.remove(lg);
				}
			}
		}

		// 如果有CCD线工程检查票
		if (ccdls.size() > 0) {
			// 检查是否做过304工位
			if (!dao.checkPositionDid(material_id, "00000000066", null, null)) {
				for (String ccdl : ccdls) {
					fileTempl.remove(ccdl);
				}
			}
		}
	}

	/**
	 * 检查当前是否存在未完成的辅助
	 * @param material_id
	 * @param position_id
	 * @param errors
	 * @param conn
	 */
	public void checkSupporting(String material_id, String position_id, List<MsgInfo> errors, SqlSession conn) {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		List<String> supportors = dao.checkSupporting(material_id, position_id);
		if (supportors.size() > 0) {
			MsgInfo info = new MsgInfo();
			info.setComponentid("material_id");
			info.setErrcode("info.linework.waitForSupporter");
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.waitForSupporter", 
					CommonStringUtil.joinBy("，", supportors.toArray(new String[supportors.size()]))));
			errors.add(info);
		}
	}

	/**
	 * 确定工程检查票是否全填写
	 * @param material_id
	 * @param position_id
	 * @param errors
	 * @param conn
	 */
	public void checkPcsEmpty(String pcs_input, List<MsgInfo> infoes) {
		if (CommonStringUtil.isEmpty(pcs_input)) return;
		if (pcs_input.contains("\"\"")) {
			MsgInfo info = new MsgInfo();
			info.setComponentid("material_id");
			info.setErrcode("info.linework.pcsCheck");
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.pcsCheck"));
			infoes.add(info);
		}
	}

	/**
	 * 取得维修对象工作信息
	 */
	public void getProccessingData(Map<String, Object> listResponse, String material_id, ProductionFeatureEntity pf,
			LoginData user, boolean reaction, SqlSession conn) throws Exception {
		// 取得维修对象信息。
		MaterialForm mform = this.getMaterialInfo(material_id, conn);
		listResponse.put("mform", mform);

		// 取得维修对象在本工位作业信息。 TODO v2
		// List<ProductionFeatureEntity> productionFeatureEntities = service.getPositionWorksByMaterial(material_id, user, conn);

		// 取到等待作业记录的本次返工总时间
		Integer spentMins = this.getTotalTimeByRework(pf, conn) / 60;
		listResponse.put("spent_mins", spentMins);

		// 取得维修对象的作业标准时间。
		String leagal_overline = RvsUtils.getLevelOverLine(mform.getModel_name(), mform.getCategory_name(), mform.getLevel(), user, null);
		String process_code = user.getProcess_code();
		Map<String, String> snoutModels = RvsUtils.getSnoutModels(conn);
		Set<String> snoutSaveTime341Models = RvsUtils.getSnoutSavetime341Models(conn);
		if (("331".equals(process_code) && snoutModels.containsKey(mform.getModel_id()))
				|| ("341".equals(process_code) && snoutSaveTime341Models.contains(mform.getModel_id()))) { // 判断先端预制使用改变时间
			ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
			// 判断先端预制
			if (dao.checkPositionDid(material_id, "00000000024", "2", null)) {
				// 用过先端预制的话减去15分钟
				leagal_overline = "" + (Integer.parseInt(leagal_overline) - 15);
			}
//		} else if ("151".equals(process_code) || "161".equals(process_code)) {
		}
		listResponse.put("leagal_overline", leagal_overline);

		// 取得维修对象在本工位中断/作业流程信息。

		// 发出闹钟信息
		if (reaction && leagal_overline != null && !"-1".equals(leagal_overline)) {
			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
			httpclient.start();
			try {
				// String material_id, String position_id, String line_id, Integer standard_minute, Integer cost_minute
		        HttpGet request = new HttpGet("http://localhost:8080/rvspush/trigger/start_alarm_clock_queue/" 
		        		+ material_id + "/" + user.getPosition_id() + "/" + user.getLine_id() + "/" + user.getOperator_id() + "/" 
		        		+ leagal_overline + "/" + spentMins);
		        httpclient.execute(request, null);
		    } catch (Exception e) {
			} finally {
				Thread.sleep(100);
				httpclient.shutdown();
			}
		}
	}

	/**
	 * 多件并行信息
	 */
	public void searchWorkingBatch(Map<String, Object> listResponse, LoginData user, SqlSession conn) {
		// 判断是否有在进行中的维修对象
		List<ProductionFeatureEntity> workingPfs = this.getWorkingPfs(user, conn);
		// 进行中的话
		if (workingPfs != null && workingPfs.size() > 0) {
			List<MaterialEntity> mForms = new ArrayList<MaterialEntity>();
			MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
			Date firstAction_time = new Date(4000000000000l); // 2096年
			for (ProductionFeatureEntity workingPf : workingPfs) {
				// 取得作业信息
				Date thisAction_time = workingPf.getAction_time();
				if (firstAction_time.after(thisAction_time)) {
					firstAction_time = thisAction_time;
				}
				// 取得维修对象表示信息
				MaterialEntity mForm = mDao.getMaterialNamedEntityByKey(workingPf.getMaterial_id());
				mForms.add(mForm);
			}

			listResponse.put("workingPfs", mForms);
			listResponse.put("action_time", DateUtil.toString(firstAction_time, "HH:mm"));
			// 页面设定为编辑模式
			listResponse.put("workstauts", "1");
		} else {
			// 准备中
			listResponse.put("workstauts", "0");
		}
	}

	/**
	 * 工程中最后一个工位时(目前仅261)，检查工程中所有工位是否已完成
	 * @param workingPf
	 * @param infoes
	 * @param conn
	 */
	public void checkLineOver(ProductionFeatureEntity workingPf, List<MsgInfo> infoes, SqlSession conn) {
		String processCode = workingPf.getProcess_code();
		if ("261".equals(processCode)) { // TODO all
			ProcessAssignMapper paDao = conn.getMapper(ProcessAssignMapper.class);
			boolean finishedByLine = paDao.getFinishedByLine(workingPf.getMaterial_id(), workingPf.getLine_id());
			if (!finishedByLine) {
				MsgInfo info = new MsgInfo();
				info.setComponentid("material_id");
				info.setErrmsg("本工程内还有作业未完成，请等待全部完成后再结束本工位。"); // TODO temp
				infoes.add(info);
			}
		}
	}

	/**
	 * 执行工位扫描后的特殊事件
	 * @param waitingPf
	 * @param conn
	 */
	public void executeActionByPosition(ProductionFeatureEntity waitingPf, SqlSessionManager conn) {
		String positionId = waitingPf.getPosition_id();

		if (waitingPf.getOperate_result() == 0 
				&& ("00000000033".equals(positionId) || "00000000042".equals(positionId) 
						|| "00000000048".equals(positionId) || "00000000050".equals(positionId))) {
		}
	}

	/**
	 * 取得使用设备
	 * @param position_id
	 * @return
	 */
	public String getManageNo(String position_id,SqlSession conn) {
		if ("00000000010".equals(position_id)) {
			// 消毒
			Map<String,String> map = new LinkedHashMap<String,String>();
			map.put("B006", "B006");	
			map.put("B007", "B007");
			String ret = "<span class=\"device_manage_select\"><select class=\"manager_no\" code=\"ER12102\">" + CodeListUtils.getSelectOptions(map, "", null, false) + "</select></span>"
					+ "<span class=\"device_manage_item ui-state-default\">消毒设备管理No. 选择: </span>";

			// 高温高压 B075	B076
			map = new LinkedHashMap<String,String>();
			map.put("B075", "B075");	
			map.put("B076", "B076");
			ret += "<span class=\"device_manage_select\"><select class=\"manager_no\" code=\"ER12101\">" + CodeListUtils.getSelectOptions(map, "", null, false) + "</select></span>"
					+ "<span class=\"device_manage_item ui-state-default\">高温高压设备管理No. 选择: </span>";

			return ret;
		} else if ("00000000011".equals(position_id)) {
			// 灭菌
			Map<String,String> map = new LinkedHashMap<String,String>();
			map.put("1号", "1号");	
			map.put("2号", "2号");

			String ret = "<span class=\"device_manage_select\"><select class=\"manager_no\" code=\"ER13101\">" + CodeListUtils.getSelectOptions(map, "", null, false) + "</select></span>"
					+ "<span class=\"device_manage_item ui-state-default\">灭菌设备管理No. 选择: </span>";
			return ret;
		}
		return null;
	}

	/**
	 * 取得点检相关信息
	 * @param section_id
	 * @param position_id
	 * @param conn
	 * @param line_id 
	 * @return
	 * @throws Exception 
	 */
	public String getInfectMessageByPosition(String section_id,
			String position_id, String line_id, SqlSession conn) throws Exception {
		String retComments = "";

		return retComments;
	}

	public void notifyPosition(String section_id, String position_id, String material_id, boolean isLight) {
		// 通知
		try {
			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
			httpclient.start();
			try {  
				String inUrl = "http://localhost:8080/rvspush/trigger/in/" + position_id + "/" + section_id;
				if (material_id != null) {
					inUrl += "/" + material_id + "/" + (isLight ? "1" : "0");
				}
	            HttpGet request = new HttpGet(inUrl);
	            _log.info("finger:"+request.getURI());
	            httpclient.execute(request, null);

	        } catch (Exception e) {
			} finally {
				Thread.sleep(100);
				httpclient.shutdown();
			}
		} catch (IOReactorException | InterruptedException e1) {
			_log.error(e1.getMessage(), e1);
		}
	}

	public int getWaitingMaterialOtherPx(String section_id,
			String position_id, String px, SqlSession conn) {
		if ("1".equals(px)) {
			return getWaiting(section_id, position_id, "2", conn).size();
		} else if ("2".equals(px)) {
			return getWaiting(section_id, position_id, "1", conn).size();
		}
		return 0;
	}

	/**
	 * 已在分解库位中的维修对象出库
	 * @param material_id
	 * @param conn
	 */
	public void getOutFromDeposeStorage(String material_id,
			SqlSessionManager conn) {
		DeposeStorageMapper dsMapper = conn.getMapper(DeposeStorageMapper.class);
		ProductionFeatureMapper pfMapper = conn.getMapper(ProductionFeatureMapper.class);

		// 取出库位
		dsMapper.removeFromStorage(material_id);

		// 其他工位成为取出状态
		pfMapper.storaged2Inline(material_id);
	}

	/**
	 * 报价优先判断
	 * @param material_id
	 * @param user
	 * @param errors
	 * @param conn
	 */
	public void checkAgreeDateForQuotate(String material_id, LoginData user,
			List<MsgInfo> errors, SqlSessionManager conn) {
		QuotationMapper mapper = conn.getMapper(QuotationMapper.class);
		List<MaterialEntity> waitings = mapper.getWaitings(user.getPosition_id());
		if (waitings.size() == 0) {
			return;
		}
		MaterialEntity scanMaterial = null;
		for (MaterialEntity waiting : waitings) {
			if (material_id.equals(waiting.getMaterial_id())) {
				scanMaterial = waiting;
				break;
			}
		}
		if (scanMaterial == null) {
			return;
		}

		// 返修可优先
		Integer iSpFlg = scanMaterial.getService_repair_flg();
		if (iSpFlg != null && iSpFlg > 0) {
			return;
		}

		// Endoeye单独
		boolean isSP = false;
		MaterialEntity firstMaterial = null;
		if ("06".equals(scanMaterial.getKind())) {
			isSP = true;
		}

		for (MaterialEntity waiting : waitings) {
			if (isSP && "06".equals(waiting.getKind())) {
				firstMaterial = waiting;
				break;
			}
			if (!isSP && !"06".equals(waiting.getKind())) {
				firstMaterial = waiting;
				break;
			}
		}
		if (firstMaterial == null) return;

//		if (firstMaterial.getScheduled_expedited() != 0 && scanMaterial.getScheduled_expedited() == 0) {
//			// 加急优先
//			MsgInfo msgInfo = new MsgInfo();
//			msgInfo.setComponentid("material_id");
//			msgInfo.setErrcode("info.linework.expeditedFirst");
//			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.expeditedFirst"
//					, firstMaterial.getSorc_no()));
//			errors.add(msgInfo);
//		} else 
		if (scanMaterial.getScheduled_date() == null){
			if (firstMaterial.getScheduled_date() != null) {
				// 同意优先
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setComponentid("material_id");
				msgInfo.setErrcode("info.linework.agreedDateFirst");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.agreedDateFirst"
						, firstMaterial.getSorc_no()));
				errors.add(msgInfo);
			}
		} else if (scanMaterial.getScheduled_date().compareTo(firstMaterial.getScheduled_date()) != 0) {
			// 同意优先
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("material_id");
			msgInfo.setErrcode("info.linework.agreedDateFirst");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.agreedDateFirst"
					, firstMaterial.getSorc_no()));
			errors.add(msgInfo);
		}
	}
}