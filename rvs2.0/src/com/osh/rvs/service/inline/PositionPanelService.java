package com.osh.rvs.service.inline;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.arnx.jsonic.JSON;

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
import com.osh.rvs.bean.infect.CheckResultEntity;
import com.osh.rvs.bean.infect.CheckUnqualifiedRecordEntity;
import com.osh.rvs.bean.infect.PeriodsEntity;
import com.osh.rvs.bean.infect.PeripheralInfectDeviceEntity;
import com.osh.rvs.bean.inline.ForSolutionAreaEntity;
import com.osh.rvs.bean.inline.PutinBalanceBound;
import com.osh.rvs.bean.inline.SoloProductionFeatureEntity;
import com.osh.rvs.bean.inline.WaitingEntity;
import com.osh.rvs.bean.manage.PcsInputLimitEntity;
import com.osh.rvs.bean.master.DevicesManageEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.ProcedureStepCountEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.master.ProcedureStepCountForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.infect.CheckResultMapper;
import com.osh.rvs.mapper.infect.CheckUnqualifiedRecordMapper;
import com.osh.rvs.mapper.infect.PeripheralInfectDeviceMapper;
import com.osh.rvs.mapper.inline.DeposeStorageMapper;
import com.osh.rvs.mapper.inline.PositionPanelMapper;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.inline.SoloProductionFeatureMapper;
import com.osh.rvs.mapper.master.DevicesManageMapper;
import com.osh.rvs.mapper.master.ProcessAssignMapper;
import com.osh.rvs.mapper.qf.QuotationMapper;
import com.osh.rvs.service.AlarmMesssageService;
import com.osh.rvs.service.CheckResultService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.master.ProcedureStepCountService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
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

	private static final BigDecimal SEC_IN_MIN = new BigDecimal(60);

	private static Map<String, PutinBalanceBound> putinBalanceBounds = new HashMap<String, PutinBalanceBound>(); 

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

		String process_code = user.getProcess_code();
		
		if(process_code.equals("121") || process_code.equals("131")){
			List<WaitingEntity> ls = dao.getSpareOrRcWaitingMaterial(user.getSection_id(), user.getPosition_id());
			ret.addAll(ls);
		}

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
									msgInfo.setErrcode("info.linework.blockedForSolve");
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
	public MaterialForm getMaterialInfo(String material_id, SqlSession conn) { // NO_UCD (use default)
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
	public List<WaitingEntity> getWaitingMaterial(String section_id, String position_id, String ar_line_id,
			String operator_id, String level, String process_code, SqlSession conn) {
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);

		List<WaitingEntity> ret = dao.getWaitingMaterial(ar_line_id, section_id, position_id, operator_id, level);

		if(process_code.equals("121") || process_code.equals("131")){
			List<WaitingEntity> ls = dao.getSpareOrRcWaitingMaterial(section_id, position_id);
			ret.addAll(ls);
		}
		
		PutinBalanceBound putinBalanceBound = null;
		BigDecimal furthestBalance = null, closestBalance = null; // 最遠采樣，最近采樣值 
		String furthestBalanceWaiting = null, closestBalanceWaiting = null; // 最遠采樣，最近采樣維修品 ID
		boolean todayRecommended = false;

		int balancePos = 0;
		if (process_code.equals("211") || process_code.equals("411")) { // 311 or 411
			String ar_position_id = position_id;
			if (process_code.equals("211")) {
				ar_line_id = "00000000013";
				ar_position_id = "00000000026";
			}
			if (!putinBalanceBounds.containsKey(ar_line_id)) {
				createPutinBalanceBound(section_id, ar_position_id, ar_line_id, conn);	
			}
			putinBalanceBound = putinBalanceBounds.get(ar_line_id);

			balancePos = putinBalanceBound.getBalancePos();
		}

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

			if (putinBalanceBound != null && !"3".equals(we.getWaitingat())) {
				// 不经过 NS 的不在分解提示
				if ("00000000013".equals(ar_line_id) && we.getLevel() == 1) {
					continue;
				}
				// PA或者BO的不計算
				if (we.getBlock_status() != null && we.getBlock_status() > 0) {
					continue;
				}
				// 已有当日产出要求，则非当日产出不排
				if (todayRecommended && we.getToday() == 0) {
					continue;
				}
				MaterialService ms = new MaterialService();
				MaterialEntity me = ms.loadSimpleMaterialDetailEntity(conn, we.getMaterial_id());
				int erestingStandard = getPatLineStandard(me.getModel_name(), me.getCategory_name()
						, me.getPat_id(), ar_line_id, conn);
				we.setLine_minutes(erestingStandard);
				BigDecimal balanceDiff = putinBalanceBound.evalBalanceDiff(erestingStandard);
				BigDecimal absBalanceDiff = balanceDiff.abs();

				if (!todayRecommended && we.getToday() == 1) {
					// 首个当日产出优先
					closestBalance = absBalanceDiff;
					closestBalanceWaiting = we.getMaterial_id();
					furthestBalance = balanceDiff;
					furthestBalanceWaiting = we.getMaterial_id();
					todayRecommended = true;
				} else {
					// 最接近
					if (closestBalance == null || closestBalance.compareTo(absBalanceDiff) > 0) {
						closestBalance = absBalanceDiff;
						closestBalanceWaiting = we.getMaterial_id();
					}
					// 最遠
					if (balancePos == 1 && balanceDiff.compareTo(BigDecimal.ZERO) < 0) {
						// 目前平衡較高時，選擇最低					
						if (furthestBalance == null || furthestBalance.compareTo(balanceDiff) > 0) {
							furthestBalance = balanceDiff;
							furthestBalanceWaiting = we.getMaterial_id();
						}
					} else if (balancePos == -1 && balanceDiff.compareTo(BigDecimal.ZERO) > 0) {
						// 目前平衡較低時，選擇最高			
						if (furthestBalance == null || furthestBalance.compareTo(balanceDiff) < 0) {
							furthestBalance = balanceDiff;
							furthestBalanceWaiting = we.getMaterial_id();
						}
					}
				}
			}
		}

		String hitRecommended = furthestBalanceWaiting;
		if (hitRecommended == null) hitRecommended = closestBalanceWaiting;

		if (hitRecommended != null) {
			for (WaitingEntity we : ret) {
				if (we.getMaterial_id().equals(hitRecommended)) {
					we.setLine_minutes(- we.getLine_minutes()); // 标记为最适合
					break;
				}
			}
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
		} else if (mform.getLevel()==null || RvsUtils.isPeripheral(mform.getLevel())) { // 没等级还要检查票的就只有周边
			showLines = new String[1];
			showLines[0] = "检查卡";
		} else if ("0".equals(mform.getLevel())) { // 制品
			if ("00000000076".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "出荷检查表";
				showLines[1] = "检查工程";
			} else {
				showLines = new String[1];
				showLines[0] = "检查工程";
			}
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
				if ("500".equals(pf.getProcess_code())) {
					showLines[0] = "总组工程";
					showLines[1] = "分解工程";
				} else {
					showLines[0] = "总组工程";
					showLines[1] = "NS 工程";
				}
			} else if ("00000000060".equals(sline_id)) {
				showLines = new String[1];
				showLines[0] = "分解工程";
			} else if ("00000000061".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "总组工程";
				showLines[1] = "分解工程";
			}
		}

		Map<String, Map<String, PcsInputLimitEntity>> limits = new HashMap<String, Map<String, PcsInputLimitEntity>>();
		listResponse.put("pcsLimits", limits);

		for (String showLine : showLines) {
			Map<String, String> fileTempl = PcsUtils.getXmlContents(showLine, mform.getModel_name(), null, material_id, limits, conn);

			if ("NS 工程".equals(showLine)) filterSolo(fileTempl, material_id, conn);
			if ("总组工程".equals(showLine)) MaterialService.filterLight(fileTempl, material_id, mform.getLevel(), conn);

			Map<String, String> fileHtml = PcsUtils.toHtml(fileTempl, material_id, mform.getSorc_no(),
					mform.getModel_name(), mform.getSerial_no(), mform.getLevel(), pf.getProcess_code(), isLeader ? sline_id : null, conn);
			fileHtml = RvsUtils.reverseLinkedMap(fileHtml);
			pcses.add(fileHtml);
		}

		// 中小修时不限制强制输入数值
		if ("00000000054".equals(sline_id) && limits.size() > 0) {
			for (String pcsPage : limits.keySet()) {
				Map<String, PcsInputLimitEntity> limitOfPage = limits.get(pcsPage);
				for (String tag : limitOfPage.keySet()) {
					limitOfPage.get(tag).setAllow_pass(true);
				}
			}
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

		String[] showLines = {};

		if (pf.getPosition_id().equals("00000000108")) { // TODO
			showLines = new String[2];
			showLines[0] = "出荷检查表";
			showLines[1] = "检查工程";
		} else {
			showLines = new String[6];
			showLines[0] = "检查卡";
			showLines[1] = "最终检验";
			showLines[2] = "外科硬镜修理工程";
			showLines[3] = "分解工程";
			showLines[4] = "NS 工程";
			showLines[5] = "总组工程";
		}

		for (String showLine : showLines) {
			Map<String, String> fileTempl = PcsUtils.getXmlContents(showLine, mform.getModel_name(), null, material_id, conn);

			if ("NS 工程".equals(showLine)) filterSolo(fileTempl, material_id, conn);
			if ("总组工程".equals(showLine)) MaterialService.filterLight(fileTempl, material_id, mform.getLevel(), conn);

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
		
		// 如果有CCD盖玻璃工程检查票
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
		Integer spentSecs = this.getTotalTimeByRework(pf, conn);
		Integer spentMins = spentSecs / 60;
		listResponse.put("spent_secs", spentSecs);
		listResponse.put("spent_mins", spentMins);

		// 取得维修对象的作业标准时间。
		String leagal_overline = RvsUtils.getLevelOverLine(mform.getModel_name(), mform.getCategory_name(), mform.getLevel(), user, null);
		String process_code = pf.getProcess_code();
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
				int dotpos = leagal_overline.indexOf(".");
				if (dotpos > 0) {
					leagal_overline = leagal_overline.substring(0, dotpos);
				}
				BigDecimal bdMin = new BigDecimal(spentSecs).divide(SEC_IN_MIN, 1, BigDecimal.ROUND_HALF_DOWN);
				// String material_id, String position_id, String line_id, Integer standard_minute, Integer cost_minute
		        HttpGet request = new HttpGet("http://localhost:8080/rvspush/trigger/start_alarm_clock_queue/" 
		        		+ material_id + "/" + user.getPosition_id() + "/" + user.getLine_id() + "/" + user.getOperator_id() + "/" 
		        		+ leagal_overline + "/" + bdMin.toPlainString());
		        httpclient.execute(request, null);
		    } catch (Exception e) {
			} finally {
				Thread.sleep(100);
				httpclient.shutdown();
			}
		}
	}

	/**
	 * 作业步骤计次启功
	 * 
	 * @param mform
	 * @param pf
	 * @param user
	 * @param conn
	 */
	public void getProcedureStepCount(MaterialForm mform, ProductionFeatureEntity pf,
			LoginData user, SqlSession conn){

		// 作业步骤计次
		ProcedureStepCountService pscService = new ProcedureStepCountService(); 

		ProcedureStepCountForm pscForm = new ProcedureStepCountForm();
		pscForm.setPosition_id(user.getPosition_id());
		pscForm.setPx(user.getPx());

		List<ProcedureStepCountForm> l = pscService.search(pscForm, conn);

		if (l.size() > 0) {
			String recieveFrom = pscService.startProcedureStepCount(mform, user.getPosition_id(), user.getPx(), pf, conn);
			if (recieveFrom != null && recieveFrom.endsWith("Exception")) {
				
			}
		}
	}

	/**
	 * 	/**
	 * 判断作业步骤次数计数
	 * 
	 * @param material_id 
	 * @param user
	 * @param listResponse
	 * @param infoes
	 * @param isFinish 工位完成
	 * @param pf 
	 * @param conn
	 * @throws Exception
	 */
	public void getProcedureStepCountMessage(String material_id, LoginData user, Map<String, Object> listResponse, List<MsgInfo> infoes,
			boolean isFinish, ProductionFeatureEntity pf, SqlSessionManager conn) throws Exception {
		// 判断是要计数的机型
		ProcedureStepCountService pscService = new ProcedureStepCountService(); 

		MaterialService mService = new MaterialService();
		MaterialEntity mEntity = mService.loadSimpleMaterialDetailEntity(conn, material_id);

		List<ProcedureStepCountEntity> lisr = pscService.getProcedureStepCountOfModel(mEntity.getModel_id(), pf, user.getPx(), conn);
		if (lisr == null || lisr.size() == 0) return;

		String recvMessage = pscService.finishProcedureStepCount(mEntity.getModel_id(), user.getPosition_id(), user.getPx(), conn);
		if (recvMessage != null && recvMessage.startsWith("getCount:")) {
			String rec = recvMessage.substring("getCount:".length());
			String[] se = rec.split(">>");
			if (se.length == 2) { // 最初版本
				int setTimes = 0, actualTimes = 0;
				try {
					setTimes = Integer.parseInt(se[0]); 
					actualTimes = Integer.parseInt(se[1]); 
				} catch (NumberFormatException e) {
				}
				if (actualTimes < setTimes) {
					MsgInfo info = new MsgInfo();
					info.setErrmsg("当前维修对象作业[KE-45胶水涂布次数2]应当进行 " + se[0] + " 次，实际记录 " + se[1] + " 次。请操作达到计数。");
					infoes.add(info);
				} else if (actualTimes > setTimes) {
					String message = "当前维修对象作业[KE-45胶水涂布次数2]应当进行 " + se[0] + " 次，实际记录 " + se[1] + " 次。";
					listResponse.put("procedure_step_count_message", message);
					AlarmMesssageService amService = new AlarmMesssageService();
					amService.createPscAlarmMessage(material_id, mEntity.getSorc_no(), message, user, conn);
				}
			} else {
				try {
					Map<String, String> decodedRec = JSON.decode(rec, Map.class);

					if (isFinish) {
						pscService.confirmFinish(decodedRec, material_id, user, lisr, listResponse, infoes, conn);
					} else {
						pscService.recordBreak(decodedRec, material_id, pf.getRework(), user, conn);
					}

				} catch (Exception e) {
					listResponse.put("procedure_step_count_message", e.getMessage());
				}
			}
		} else {
			MsgInfo meq = new MsgInfo();
			meq.setErrcode("info.inline.procedureStep.notCounting");
			meq.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.inline.procedureStep.notCounting"));
			infoes.add(meq);
//			listResponse.put("procedure_step_count_message", "没有开始作业计数。");
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
				
				if(mForm == null){
					mForm = new MaterialEntity();
					mForm = mDao.getMaterialEntityByKey(workingPf.getMaterial_id());
					mForm.setModel_name(mForm.getScheduled_manager_comment());
				}
				
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
	 * 检查必须使用部组
	 * @param workingPf
	 * @param infoes
	 * @param conn
	 */
	public void checkAccessary(ProductionFeatureEntity workingPf, List<MsgInfo> infoes, SqlSession conn) {
		String processCode = workingPf.getProcess_code();
		if ("002".equals(processCode)) {
			SoloProductionFeatureMapper spfMapper = conn.getMapper(SoloProductionFeatureMapper.class);
			List<ProductionFeatureEntity> used_snouts = spfMapper.findUsedSnoutsByMaterial(workingPf.getMaterial_id(), "00000000101");

			if (used_snouts.size() == 0) {
				MsgInfo info = new MsgInfo();
				info.setComponentid("material_id");
				info.setErrcode("info.product.withoutAccessory");
				info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.withoutAccessory"));
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
		// 查找点检不合格中断的项目
		CheckUnqualifiedRecordMapper curMapper 
			= conn.getMapper(CheckUnqualifiedRecordMapper.class);
		CheckUnqualifiedRecordEntity curEntity = new CheckUnqualifiedRecordEntity();
		curEntity.setSection_id(section_id);
		curEntity.setPosition_id(position_id);
		boolean hasBlocked = curMapper.checkBlockedToolsOnPosition(curEntity);
		if (hasBlocked) {
			return "本工位专用工具点检发生不合格且未解决，将限制工作。";
		}
		hasBlocked = curMapper.checkBlockedDevicesOnPosition(curEntity);
		if (hasBlocked) {
			return "本工位设备工具点检发生不合格且未解决，将限制工作。";
		}

		Date today = new Date();
		String todayString = DateUtil.toString(today, DateUtil.ISO_DATE_PATTERN);
		today = DateUtil.toDate(todayString, DateUtil.ISO_DATE_PATTERN);

		CheckResultMapper crMapper = conn.getMapper(CheckResultMapper.class);
		CheckResultEntity cond = new CheckResultEntity();
		cond.setSection_id(section_id);
		cond.setPosition_id(position_id);
		cond.setLine_id(line_id);

		PeriodsEntity period = CheckResultService.getPeriodsOfDate(todayString, conn);
		cond.setCheck_confirm_time_start(period.getStartOfMonth());
		cond.setCheck_confirm_time_end(period.getEndOfMonth());

		List<CheckResultEntity> list = crMapper.searchToolUncheckedOnPosition(cond);

		String retComments = "";
		if (list.size() > 0) {
			Set<String> responsibleOperatorName = new HashSet<String>();
			for (CheckResultEntity cr : list) {
				String name = cr.getOperator_name();
				if (name == null) name = "(无负责)";
				responsibleOperatorName.add(name);
			}
			String responsibleOperatorString = "(";
			for (String ron : responsibleOperatorName) {
				responsibleOperatorString += ron + " ";
			}
			responsibleOperatorString += ")";
			if (DateUtil.compareDate(today, period.getExpireOfMonthOfJig()) >= 0) {
				retComments += "本工位有"+list.size()+"件专用工具" + responsibleOperatorString + "在期限前未作点检，将限制工作。\n";
			} else {
				retComments += "本工位有"+list.size()+"件专用工具尚未点检，期限为"+
						DateUtil.toString(period.getExpireOfMonthOfJig(), DateUtil.ISO_DATE_PATTERN)+"，请在期限前完成点检。\n";
			}
		}

		Calendar now = Calendar.getInstance();

		// 设备
		String dailyDevices = crMapper.searchDailyDeviceUncheckedOnPosition(cond);
		if (!CommonStringUtil.isEmpty(dailyDevices)) {
			if (now.get(Calendar.HOUR_OF_DAY) >= 14) { // TODO SYSTEM PARAM 14
				// 下午2点锁定
				retComments += "本工位有以下日常点检设备："+dailyDevices+"在期限前未作点检，将限制工作。\n";
			} else {
				// 否则提醒
				retComments += "本工位有以下日常点检设备："+dailyDevices+"将到达点检期限，请尽快进行点检。\n";
			}
		}

		cond.setCycle_type(CheckResultService.TYPE_ITEM_WEEK);
		cond.setCheck_confirm_time_start(period.getStartOfWeek());
		cond.setCheck_confirm_time_end(period.getEndOfWeek());
		String regularDevices = crMapper.searchRegularyDeviceUncheckedOnPosition(cond);
		if (!CommonStringUtil.isEmpty(regularDevices)) {
			if (today.after(period.getStartOfWeek())) {
				// 期限内锁定
				retComments += "本工位有以下周点检设备："+regularDevices+"在期限前未作点检，将限制工作。\n";
			} else {
				// 否则提醒
				retComments += "本工位有以下周点检设备："+regularDevices+"将到达点检期限，请尽快进行点检。\n";
			}
		}

		cond.setCycle_type(CheckResultService.TYPE_ITEM_MONTH);
		cond.setCheck_confirm_time_start(period.getStartOfMonth());
		cond.setCheck_confirm_time_end(period.getEndOfMonth());
		regularDevices = crMapper.searchRegularyDeviceUncheckedOnPosition(cond);
		if (!CommonStringUtil.isEmpty(regularDevices)) {
			if (today.getTime() >= period.getExpireOfMonth().getTime()) {
				// 期限内锁定
				retComments += "本工位有以下月点检设备："+regularDevices+"在期限前未作点检，将限制工作。\n";
			} else {
				// 否则提醒
				retComments += "本工位有以下月点检设备："+regularDevices+"将到达点检期限("+
						DateUtil.toString(period.getExpireOfMonth(), DateUtil.ISO_DATE_PATTERN)+")，请尽快进行点检。\n";
			}
		}

		cond.setCycle_type(CheckResultService.TYPE_ITEM_PERIOD);
		cond.setCheck_confirm_time_start(period.getStartOfHbp());
		cond.setCheck_confirm_time_end(period.getEndOfHbp());
		regularDevices = crMapper.searchRegularyDeviceUncheckedOnPosition(cond);
		if (!CommonStringUtil.isEmpty(regularDevices)) {
			if (today.getTime() >= period.getExpireOfHbp().getTime()) {
				// 期限内锁定
				retComments += "本工位有以下半期点检设备："+regularDevices+"在期限前未作点检，将限制工作。\n";
			} else {
				// 否则提醒
				retComments += "本工位有以下半期点检设备："+regularDevices+"将到达点检期限("+
						DateUtil.toString(period.getExpireOfHbp(), DateUtil.ISO_DATE_PATTERN)+")，请尽快进行点检。\n";
			}
		}

		cond.setCycle_type(CheckResultService.TYPE_ITEM_YEAR);
		cond.setCheck_confirm_time_start(period.getStartOfPeriod());
		cond.setCheck_confirm_time_end(period.getEndOfPeriod());
		regularDevices = crMapper.searchRegularyDeviceUncheckedOnPosition(cond);
		if (!CommonStringUtil.isEmpty(regularDevices)) {
			if (today.getTime() >= period.getExpireOfPeriod().getTime()) {
				// 期限内锁定
				retComments += "本工位有以下全期点检设备："+regularDevices+"在期限前未作点检，将限制工作。\n";
			} else {
				// 否则提醒
				retComments += "本工位有以下全期点检设备："+regularDevices+"将到达点检期限("+
						DateUtil.toString(period.getExpireOfPeriod(), DateUtil.ISO_DATE_PATTERN)+")，请尽快进行点检。\n";
			}
		}

		// 重要工程日次点检
		if (now.get(Calendar.HOUR_OF_DAY) >= 15) {
			cond.setSpecialized(9);
			dailyDevices = crMapper.searchDailyDeviceUncheckedOnPosition(cond);
			if (!CommonStringUtil.isEmpty(dailyDevices)) {
				if (now.get(Calendar.HOUR_OF_DAY) > 16 ||
						(now.get(Calendar.HOUR_OF_DAY) == 16 && now.get(Calendar.MINUTE) > 30)) {
					// 下午2点锁定
					retComments += "本工位重要工程日次点检在期限前未作点检，将限制工作。\n";
				} else {
					// 否则提醒
					retComments += "本工位重要工程日次点检将到达点检期限，请尽快进行点检。\n";
				}
			}
		}

		// 日期，线长确认
		if (now.get(Calendar.HOUR_OF_DAY) >= 15) {
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);
			now.set(Calendar.MILLISECOND, 0);
			// 每天 不存在
			// 每周
			if (now.getTimeInMillis() == period.getLastOfWeek().getTime()) {
				cond.setCycle_type(CheckResultService.TYPE_ITEM_WEEK);
				cond.setCheck_confirm_time_start(period.getStartOfWeek());
				cond.setCheck_confirm_time_end(period.getEndOfWeek());

				regularDevices = crMapper.searchDeviceUnconfirmedOnPosition(cond);

				if (!CommonStringUtil.isEmpty(regularDevices)) {
					retComments += "本工位有以下每周确认设备："+regularDevices+"尚未经管理者确认，将限制工作。\n";
				}
			}
			// 每月
			if (now.getTimeInMillis() == period.getLastOfMonth().getTime()) {
				cond.setCycle_type(CheckResultService.TYPE_ITEM_MONTH);
				cond.setCheck_confirm_time_start(period.getStartOfMonth());
				cond.setCheck_confirm_time_end(period.getEndOfMonth());

				regularDevices = crMapper.searchDeviceUnconfirmedOnPosition(cond);

				if (!CommonStringUtil.isEmpty(regularDevices)) {
					retComments += "本工位有以下每月确认设备："+regularDevices+"尚未经管理者确认，将限制工作。\n";
				}

				String jigUnconfirmed = crMapper.searchJigUnconfirmedOnPosition(cond);
				if (!CommonStringUtil.isEmpty(jigUnconfirmed) && "0".equals(jigUnconfirmed)) {
					retComments += "本工位专用工具清点尚未经管理者确认，将限制工作。\n";
				}
			}
			// 半期
			if (now.getTimeInMillis() == period.getLastOfHbp().getTime()) {
				cond.setCycle_type(CheckResultService.TYPE_ITEM_PERIOD);
				cond.setCheck_confirm_time_start(period.getStartOfHbp());
				cond.setCheck_confirm_time_end(period.getEndOfHbp());

				regularDevices = crMapper.searchDeviceUnconfirmedOnPosition(cond);

				if (!CommonStringUtil.isEmpty(regularDevices)) {
					retComments += "本工位有以下每半期确认设备："+regularDevices+"尚未经管理者确认，将限制工作。\n";
				}
			}
		}

		return retComments;
	}

	public void notifyPosition(String section_id, String position_id, String material_id) {
		// 通知
		try {
			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
			httpclient.start();
			try {  
				String inUrl = "http://localhost:8080/rvspush/trigger/in/" + position_id + "/" + section_id;
				if (material_id != null) {
					inUrl += "/" + material_id + "/0";
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
		if (firstMaterial == null || firstMaterial.getScheduled_date() == null) return;

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
//			// 同意优先
//			MsgInfo msgInfo = new MsgInfo();
//			msgInfo.setComponentid("material_id");
//			msgInfo.setErrcode("info.linework.agreedDateFirst");
//			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.agreedDateFirst"
//					, firstMaterial.getSorc_no()));
//			errors.add(msgInfo);
		}
	}

	public void updatePutinBalance(String model_name, String category_name, String pat_id, String section_id, String line_id, String position_id, SqlSession conn) {
		if (!putinBalanceBounds.containsKey(line_id)) {
			createPutinBalanceBound(section_id, position_id, line_id, conn);	
		}
		PutinBalanceBound putinBalanceBound = putinBalanceBounds.get(line_id);
		int newStandard = getPatLineStandard(model_name, category_name, pat_id, line_id, conn);
		putinBalanceBound.putNowBalance(newStandard);
	}

	private void createPutinBalanceBound(String section_id, String position_id,
			String ar_line_id, SqlSession conn) {
		/// 从数据库中查询记录
		PositionPanelMapper dao = conn.getMapper(PositionPanelMapper.class);

		// 计算近期工程完成平均值
		// BigDecimal cntAvgCost = dao.getRecentLineProcessCost(ar_line_id);
		// 计算最近投入维修品流程
		List<MaterialEntity> recentInputPats = dao.getRecentInputModels(position_id);
		List<Integer> cntRecentInputs = new ArrayList<Integer>();
		for (MaterialEntity me : recentInputPats) {
			int patLineStandard = getPatLineStandard(me.getModel_name(), me.getCategory_name(), me.getPat_id(), ar_line_id, conn);
			if(patLineStandard > 0) cntRecentInputs.add(patLineStandard);
		}

		PutinBalanceBound putinBalanceBound = new PutinBalanceBound(null, cntRecentInputs);

		putinBalanceBounds.put(ar_line_id, putinBalanceBound);
	}

	private static Map<String, Integer> patLineStandards = new HashMap<String, Integer>();
	public static void clearPatLineStandards() {
		patLineStandards.clear();
	}
	/**
	 * 取得型号+流程的工程总标准工时
	 * @param model_name
	 * @param category_name
	 * @param pat_id
	 * @param line_id
	 * @param conn
	 * @return
	 */
	private int getPatLineStandard(String model_name, String category_name, String pat_id, String line_id,
			SqlSession conn) {
		String key = model_name + "_" + pat_id + "_" + line_id;
		if (!patLineStandards.containsKey(key)) {
			ProcessAssignMapper posMapper = conn.getMapper(ProcessAssignMapper.class);
			List<PositionEntity> positions = posMapper.getAllPositionsOfPatInLine(pat_id, line_id);

			int iSum = 0;
			for (PositionEntity position : positions) {
				String processCode = position.getProcess_code();
				try {
					String sLevel = RvsUtils.getLevelOverLine(model_name, category_name, "3", null, processCode);
					if (sLevel != null) {
						Integer iLevel = Integer.parseInt(sLevel);
						if (iLevel > 0) {
							iSum += iLevel;
						}
					}
				} catch (Exception e) {
					_log.error(e.getMessage(), e);
				}
			}
			patLineStandards.put(key, iSum);
		}

		if (!patLineStandards.containsKey(key)) {
			return 0;
		}
		return patLineStandards.get(key);
	}

	/**
	 * 取得周边设备检查使用设备工具 
	 * @param material_id
	 * @param callbackResponse 
	 * @param retEntity 
	 * @param waitingPf
	 * @param conn
	 * @return 当前工位点检是否完成
	 * @throws Exception 
	 */
	public boolean getPeripheralData(String material_id, ProductionFeatureEntity pfEntity,
			List<PeripheralInfectDeviceEntity> retEntity, boolean matchedDevice, SqlSession conn) {
		PeripheralInfectDeviceMapper dao = conn.getMapper(PeripheralInfectDeviceMapper.class);

		// 当前工位点检是否完成
		boolean infectFinishFlag = true;

		PeripheralInfectDeviceEntity condEntity = new PeripheralInfectDeviceEntity();
		condEntity.setMaterial_id(material_id);
		condEntity.setPosition_id(pfEntity.getPosition_id());
		condEntity.setRework(pfEntity.getRework());
		// 取得可点检项目
		List<PeripheralInfectDeviceEntity> resultEntities = dao.getPeripheralDataByMaterialId(condEntity);
		// 各组的内容
		// Map<Integer, PeripheralInfectDeviceEntity> resultEntityOfSeq = new HashMap<Integer, PeripheralInfectDeviceEntity>();

		DevicesManageMapper devicesManageDao = conn.getMapper(DevicesManageMapper.class);
		int seqTag = -1, seqCursor = -1, seqCount = 0;
		for (int i = 0; i < resultEntities.size(); i++) {
			PeripheralInfectDeviceEntity result = resultEntities.get(i);

			// 只有检查故障项对照时，才需要备品
			if (!matchedDevice && result.getSeq() == 0) {
				continue;
			}

			retEntity.add(result);

			// 根据每一项的品名及型号，取得manageCodeList
			DevicesManageEntity devicesManageEntity = new DevicesManageEntity();
			devicesManageEntity.setDevice_type_id(result.getDevice_type_id());
			devicesManageEntity.setModel_name(result.getModel_name());
			List<DevicesManageEntity> manageCodeList = devicesManageDao.getManageCode(devicesManageEntity);

			result.setGroup(0);
			if (seqTag != result.getSeq()) {
				// 新的编号
				// 整理上一个重复
				if (seqCount > 0) {
					resultEntities.get(seqCursor).setGroup(seqCount + 1);
					for (int j = 1; j <= seqCount; j++) {
						resultEntities.get(seqCursor + j).setGroup(-1);
					}
				}

				// 重新标记
				seqTag = result.getSeq();
				seqCursor = i;
				seqCount = 0;
			} else {
				// 重复的编号
				seqCount++;
			}

			Map<String, String> codeMap = new TreeMap<String, String>();
			String codeId = "";
			for (DevicesManageEntity bean : manageCodeList) {
				if (!CommonStringUtil.isEmpty(result.getDevice_manage_id())
						&& result.getDevice_manage_id().equals(bean.getDevices_manage_id())) {
					// 取得已选中的设备
					codeId = bean.getDevices_manage_id() + "," + bean.getCheck_result();
					// 同组第一个已点检
					resultEntities.get(seqCursor).setCheck_result("已点检");
				}
				codeMap.put(bean.getDevices_manage_id() + "," + bean.getCheck_result(), bean.getManage_code());
			}

			// 每一项的设备可选项
			result.setManageCodeOptions(CodeListUtils.getSelectOptions(codeMap, codeId, "", true));

			if (isEmpty(result.getDevice_manage_id())) {
				infectFinishFlag = false;
			}
		}

		// 整理上一个重复
		if (seqCount > 0) {
			resultEntities.get(seqCursor).setGroup(seqCount + 1);
			for (int j = 1; j <= seqCount; j++) {
				resultEntities.get(seqCursor + j).setGroup(-1);
			}
		}

		return infectFinishFlag;
	}

	/**
	 * 插入点检完成的数据
	 * @param req
	 * @param user
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public void finishcheck(HttpServletRequest req, LoginData user, SqlSession conn)
			throws Exception {
		List<PeripheralInfectDeviceEntity> list = new AutofillArrayList<PeripheralInfectDeviceEntity>(
				PeripheralInfectDeviceEntity.class);
		Map<String, String[]> map = (Map<String, String[]>) req.getParameterMap();
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		// 整理提交数据
		for (String parameterKey : map.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String table = m.group(1);
				if ("finishcheck".equals(table)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = map.get(parameterKey);
					if ("manage_id".equals(column)) {
						list.get(icounts).setDevice_manage_id(value[0]);
					} else if ("seq".equals(column)) {
						list.get(icounts).setSeq(Integer.parseInt(value[0]));
					}
				}
			}
		}

		// 取得当前作业中作业信息
		ProductionFeatureEntity workingPf = getWorkingPf(user, conn);

		PeripheralInfectDeviceMapper dao = conn.getMapper(PeripheralInfectDeviceMapper.class);
		for (PeripheralInfectDeviceEntity insertBean : list) {	
			insertBean.setMaterial_id(workingPf.getMaterial_id());
			insertBean.setPosition_id(workingPf.getPosition_id());
			insertBean.setRework(workingPf.getRework());
			insertBean.setUpdated_by(user.getOperator_id());
			// 新建记录插入到数据库中
			dao.insertFinishedData(insertBean);
		}
	}

}