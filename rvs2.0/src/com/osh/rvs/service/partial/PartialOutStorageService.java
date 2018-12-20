package com.osh.rvs.service.partial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.partial.FactProductionFeatureEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.mapper.partial.FactProductionFeatureMapper;
import com.osh.rvs.service.ProductionFeatureService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.validator.JustlengthValidator;
import framework.huiqing.common.util.validator.LongTypeValidator;

public class PartialOutStorageService {
	private final ProductionFeatureService pfService = new ProductionFeatureService();

	public void scan(FactProductionFeatureForm form, SqlSessionManager conn, HttpServletRequest req, List<MsgInfo> errors) throws Exception {
		FactProductionFeatureMapper factProductionFeatureMapper = conn.getMapper(FactProductionFeatureMapper.class);

		FactProductionFeatureEntity factProductionFeatureEntity = getMaterialPartial(form.getMaterial_id(), conn, errors);

		if (errors.size() > 0)
			return;

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		// 工位代码
		String processCode = factProductionFeatureEntity.getProcess_code();
		Integer productionType = null;
		if ("321".equals(processCode)) {
			productionType = 50;
		} else {
			productionType = 51;
		}

		factProductionFeatureEntity.setOperator_id(user.getOperator_id());
		factProductionFeatureEntity.setProduction_type(productionType);

		// 新建现品作业信息
		factProductionFeatureMapper.insert(factProductionFeatureEntity);

		ProductionFeatureEntity waitingPf = new ProductionFeatureEntity();
		waitingPf.setMaterial_id(factProductionFeatureEntity.getMaterial_id());
		waitingPf.setPosition_id(factProductionFeatureEntity.getPosition_id());
		waitingPf.setSection_id(factProductionFeatureEntity.getSection_id());
		waitingPf.setPace(factProductionFeatureEntity.getPace());
		waitingPf.setRework(factProductionFeatureEntity.getRework());
		waitingPf.setOperator_id(user.getOperator_id());

		// 开始作业
		pfService.startProductionFeature(waitingPf, conn);

		conn.commit();

		List<String> triggerList = new ArrayList<String>();
		triggerList.add("http://localhost:8080/rvspush/trigger/start/"
        		+ waitingPf.getMaterial_id() + "/" + user.getPosition_id());
		RvsUtils.sendTrigger(triggerList);

	}

	/**
	 * 待出库维修对象零件
	 *
	 * @param material_id
	 * @param conn
	 * @param errors
	 * @return
	 */
	public FactProductionFeatureEntity getMaterialPartial(String material_id, SqlSession conn, List<MsgInfo> errors) {
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

		if (errors.size() > 0) {
			return null;
		}

		FactProductionFeatureMapper dao = conn.getMapper(FactProductionFeatureMapper.class);

		FactProductionFeatureEntity entity = new FactProductionFeatureEntity();
		entity.setMaterial_id(material_id);

		// 待出库零件
		List<FactProductionFeatureEntity> list = dao.searchWaitOutStorage(entity);

		if (list == null || list.size() == 0) {
			MsgInfo error = new MsgInfo();
			error.setErrmsg("此维修对象零件不存在待出库！");
			errors.add(error);
			return null;
		}

		if (list.size() == 2) {
			// 如何存在两条零件签收数据，则优先处理【NS工程】321工位
			for (int i = 0; i < list.size(); i++) {
				entity = list.get(i);
				if ("321".equals(entity.getProcess_code())) {
					break;
				}
			}
		} else {
			entity = list.get(0);
		}

		if (entity.getBo_flg() == 1) {
			MsgInfo error = new MsgInfo();
			error.setErrmsg("此维修对象零件有BO！");
			errors.add(error);
			return null;
		}

		return entity;
	}

	public String getSpentTimes(FactProductionFeatureForm form, SqlSession conn) {
		FactProductionFeatureMapper factProductionFeatureMapper = conn.getMapper(FactProductionFeatureMapper.class);
		FactProductionFeatureEntity entity = new FactProductionFeatureEntity();

		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		// 相差毫秒数
		long millisecond = 0;

		List<FactProductionFeatureEntity> list = factProductionFeatureMapper.searchWorkRecord(entity);

		for (int i = 0; i < list.size(); i++) {
			entity = list.get(i);

			if (entity.getFinish_time() == null) {
				Calendar cal = Calendar.getInstance();
				millisecond += cal.getTimeInMillis() - entity.getAction_time().getTime();
			} else {
				millisecond += entity.getFinish_time().getTime() - entity.getAction_time().getTime();
			}
		}

		BigDecimal diff = new BigDecimal(millisecond);
		// 1分钟
		BigDecimal oneMinute = new BigDecimal(60000);

		BigDecimal spent = diff.divide(oneMinute, RoundingMode.UP);

		return spent.toString();
	}

}
