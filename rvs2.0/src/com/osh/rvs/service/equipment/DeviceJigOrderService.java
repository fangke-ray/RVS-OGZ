package com.osh.rvs.service.equipment;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.equipment.DeviceJigOrderDetailEntity;
import com.osh.rvs.bean.equipment.DeviceJigOrderEntity;
import com.osh.rvs.form.equipment.DeviceJigOrderForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.equipment.DeviceJigOrderDetailMapper;
import com.osh.rvs.mapper.equipment.DeviceJigOrderMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;

/**
 * 
 * @author liuxb
 * 
 */
public class DeviceJigOrderService {
	public String insert(ActionForm form, SqlSessionManager conn, List<MsgInfo> errors) {
		// 数据连接
		DeviceJigOrderMapper deviceJigOrderMapper = conn.getMapper(DeviceJigOrderMapper.class);
		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);

		DeviceJigOrderEntity entity = new DeviceJigOrderEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		DeviceJigOrderEntity dbEntity = deviceJigOrderMapper.getDeviceJigOrderByOrderNo(entity.getOrder_no());
		if (dbEntity != null) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("dbaccess.recordDuplicated");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", "订单号" + entity.getOrder_no()));
			errors.add(error);
			return null;
		}

		deviceJigOrderMapper.insert(entity);

		String lastInsertID = commonMapper.getLastInsertID();

		return lastInsertID;
	}

	public List<DeviceJigOrderForm> searchUnProvide(SqlSession conn) {
		// 数据连接
		DeviceJigOrderMapper dao = conn.getMapper(DeviceJigOrderMapper.class);
		List<DeviceJigOrderEntity> list = dao.searchUnQuotation();

		List<DeviceJigOrderForm> respList = new ArrayList<DeviceJigOrderForm>();

		BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceJigOrderForm.class);

		return respList;

	}

	/**
	 * 订购品导入管理编号
	 * @param order_key
	 * @param object_type
	 * @param device_type_id
	 * @param model_name
	 * @param applicator_id
	 * @param confirm_quantity
	 * @param conn
	 */
	public void setAsManageCode(String order_key, int object_type,
			String device_type_id, String model_name, String applicator_id,
			int confirm_quantity, SqlSessionManager conn) {
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		entity.setOrder_key(order_key);
		entity.setObject_type(object_type);
		entity.setDevice_type_id(device_type_id);
		entity.setModel_name(model_name);
		entity.setApplicator_id(applicator_id);
		entity.setConfirm_quantity(confirm_quantity);

		dao.updateConfirmQuantity(entity);
	}

}
