package com.osh.rvs.service.partial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.partial.FactPartialWarehouseEntity;
import com.osh.rvs.form.partial.FactPartialWarehouseForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

/**
 * 零件分装
 *
 * @author liuxb
 *
 */
public class PartialUnpackService {
	private final PartialWarehouseDetailService partialWarehouseDetailService = new PartialWarehouseDetailService();
	private final FactPartialWarehouseService factPartialWarehouseService = new FactPartialWarehouseService();

	/**
	 * 检查分装是否完成
	 *
	 * @param key
	 * @param conn
	 * @return true：分装完成，false：分装未完成
	 */
	public Boolean checkPackFinished(String key, String operatorID, SqlSession conn) {
		// 不同规格种别分装总数
		List<PartialWarehouseDetailForm> specKindQuantityList = partialWarehouseDetailService.countUnpackOfSpecKindByKey(key, conn);

		// 入库单中没有需要分装的零件
		if (specKindQuantityList.size() == 0)
			return true;

		FactPartialWarehouseForm searchForm = new FactPartialWarehouseForm();
		searchForm.setKey(key);
		searchForm.setOperator_id(operatorID);
		searchForm.setProduction_type("30");
		// 已经分装总数
		List<FactPartialWarehouseForm> packList = factPartialWarehouseService.countQuantityOfSpecKind(searchForm, conn);

		// 还未分装
		if (packList.size() == 0) {
			return false;
		}

		// 已经分装总数Map
		Map<String, String> packMap = new HashMap<String, String>();
		for (FactPartialWarehouseForm factPartialWarehouseForm : packList) {
			packMap.put(factPartialWarehouseForm.getSpec_kind(), factPartialWarehouseForm.getQuantity());
		}

		// 需要分装的数量与已经分装数量比较
		for (PartialWarehouseDetailForm partialWarehouseDetailForm : specKindQuantityList) {
			String specKind = partialWarehouseDetailForm.getSpec_kind();
			// 分装总数
			String total = partialWarehouseDetailForm.getTotal_split_quantity();

			if (packMap.containsKey(specKind)) {
				if (!packMap.get(specKind).equals(total)) {
					return false;
				}
			} else {
				return false;
			}
		}

		return true;
	}

	/**
	 * 收集本次分装数量
	 *
	 * @param req
	 * @return
	 */
	public List<FactPartialWarehouseForm> collectData(HttpServletRequest req) {
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		List<FactPartialWarehouseForm> list = new AutofillArrayList<FactPartialWarehouseForm>(FactPartialWarehouseForm.class);
		Map<String, String[]> parameters = req.getParameterMap();

		for (String parameterKey : parameters.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("fact_partial_warehouse".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameters.get(parameterKey);

					if ("spec_kind".equals(column)) {
						list.get(icounts).setSpec_kind(value[0]);
					} else if ("total_split_quantity".equals(column)) {
						list.get(icounts).setTotal_split_quantity(value[0]);
					} else if ("split_quantity".equals(column)) {
						list.get(icounts).setSplit_quantity(value[0]);
					} else if ("quantity".equals(column)) {
						list.get(icounts).setQuantity(value[0]);
					}
				}
			}
		}
		return list;
	}

	public void checkData(List<FactPartialWarehouseForm> list, List<MsgInfo> errors) {
		for (FactPartialWarehouseForm factPartialWarehouseForm : list) {
			Validators v = BeanUtil.createBeanValidators(factPartialWarehouseForm, BeanUtil.CHECK_TYPE_PASSEMPTY);
			v.add("quantity", v.required("本次分装数量"));

			List<MsgInfo> errs = v.validate();
			for (int i = 0; i < errs.size(); i++) {
				errs.get(i).setLineno(CodeListUtils.getValue("partial_spec_kind", factPartialWarehouseForm.getSpec_kind()));
			}
			errors.addAll(errs);
		}

		// 检查负数
		if (errors.size() == 0) {
			for (FactPartialWarehouseForm factPartialWarehouseForm : list) {
				FactPartialWarehouseEntity entity = new FactPartialWarehouseEntity();
				BeanUtil.copyToBean(factPartialWarehouseForm, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

				if (entity.getQuantity() < 0) {
					MsgInfo error = new MsgInfo();
					error.setLineno(CodeListUtils.getValue("partial_spec_kind", factPartialWarehouseForm.getSpec_kind()));
					error.setErrcode("validator.invalidParam.invalidMoreThanOrEqualToZero");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidMoreThanOrEqualToZero", "本次分装数量"));
					errors.add(error);
				}
			}
		}
	}
}
