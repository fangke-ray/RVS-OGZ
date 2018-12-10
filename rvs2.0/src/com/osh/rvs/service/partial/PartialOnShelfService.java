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
 * 零件上架
 *
 * @author liuxb
 *
 */
public class PartialOnShelfService {
	private final PartialCollationService partialCollationService = new PartialCollationService();
	private final FactPartialWarehouseService factPartialWarehouseService = new FactPartialWarehouseService();

	/**
	 * 检查上架是否完成
	 *
	 * @param key
	 * @param conn
	 * @return true：上架完成，false：上架未完成
	 */
	public Boolean checkOnShelfinished(String key, String operatorID, SqlSession conn) {
		// 核对完待上架的零件
		List<PartialWarehouseDetailForm> list = partialCollationService.filterCollation(key, "21", conn);

		if (list.size() == 0)
			return true;

		FactPartialWarehouseForm searchForm = new FactPartialWarehouseForm();
		searchForm.setKey(key);
		searchForm.setOperator_id(operatorID);
		searchForm.setProduction_type("40");

		// 已经上架总数
		List<FactPartialWarehouseForm> packList = factPartialWarehouseService.countQuantityOfSpecKind(searchForm, conn);

		// 还未上架
		if (packList.size() == 0) {
			return false;
		}

		// 已经上架总数Map
		Map<String, String> packMap = new HashMap<String, String>();
		for (FactPartialWarehouseForm factPartialWarehouseForm : packList) {
			packMap.put(factPartialWarehouseForm.getSpec_kind(), factPartialWarehouseForm.getQuantity());
		}

		// 需要上架的数量与已经上架数量比较
		for (PartialWarehouseDetailForm partialWarehouseDetailForm : list) {
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
	 * 收集本次上架数量
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

	/**
	 * 数据合法性检查
	 *
	 * @param list
	 * @param errors
	 */
	public void checkData(List<FactPartialWarehouseForm> list, List<MsgInfo> errors) {
		for (FactPartialWarehouseForm factPartialWarehouseForm : list) {
			Validators v = BeanUtil.createBeanValidators(factPartialWarehouseForm, BeanUtil.CHECK_TYPE_PASSEMPTY);
			v.add("quantity", v.required("本次上架数量"));

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
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidMoreThanOrEqualToZero", "本次上架数量"));
					errors.add(error);
				}
			}
		}
	}

}
