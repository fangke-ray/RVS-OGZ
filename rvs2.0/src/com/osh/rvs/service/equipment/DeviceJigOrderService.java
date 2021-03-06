package com.osh.rvs.service.equipment;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.equipment.DeviceJigOrderDetailEntity;
import com.osh.rvs.bean.equipment.DeviceJigOrderEntity;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.equipment.DeviceJigOrderForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.equipment.DeviceJigOrderDetailMapper;
import com.osh.rvs.mapper.equipment.DeviceJigOrderMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

/**
 * 
 * @author liuxb
 * 
 */
public class DeviceJigOrderService {
	/**
	 * 新建设备工具治具订单
	 * @param form
	 * @param conn
	 * @param errors
	 * @return
	 */
	public String insert(ActionForm form, SqlSessionManager conn, List<MsgInfo> errors) {
		// 数据连接
		DeviceJigOrderMapper deviceJigOrderMapper = conn.getMapper(DeviceJigOrderMapper.class);
		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);

		DeviceJigOrderEntity entity = new DeviceJigOrderEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		DeviceJigOrderForm deviceJigOrderForm = this.getDeviceJigOrderByOrderNo(entity.getOrder_no(), conn);
		
		if (deviceJigOrderForm != null) {
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
	
	/**
	 * 根据订单号查询
	 * @param orderNo
	 * @param conn
	 * @return
	 */
	public DeviceJigOrderForm getDeviceJigOrderByOrderNo(String orderNo,SqlSession conn){
		// 数据连接
		DeviceJigOrderMapper deviceJigOrderMapper = conn.getMapper(DeviceJigOrderMapper.class);
		
		DeviceJigOrderEntity entity = deviceJigOrderMapper.getDeviceJigOrderByOrderNo(orderNo);
		
		DeviceJigOrderForm respForm = null;
		
		if(entity!=null){
			respForm = new DeviceJigOrderForm();
			BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}
		
		return respForm;
	}
	
	/**
	 * 更新设备工具治具订单
	 * @param form
	 * @param conn
	 */
	public void update(ActionForm form,SqlSessionManager conn){
		// 数据连接
		DeviceJigOrderMapper deviceJigOrderMapper = conn.getMapper(DeviceJigOrderMapper.class);
		
		DeviceJigOrderEntity entity = new DeviceJigOrderEntity();
		//复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		deviceJigOrderMapper.update(entity);
	}
	

	public List<DeviceJigOrderForm> searchUnProvide(SqlSession conn) {
		// 数据连接
		DeviceJigOrderMapper dao = conn.getMapper(DeviceJigOrderMapper.class);
		List<DeviceJigOrderEntity> list = dao.searchUnQuotation();

		List<DeviceJigOrderForm> respList = new ArrayList<DeviceJigOrderForm>();

		BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceJigOrderForm.class);

		return respList;

	}
	
	public String getMaxTempOrderNo(SqlSession conn){
		// 数据连接
		DeviceJigOrderMapper dao = conn.getMapper(DeviceJigOrderMapper.class);
		
		String orderNO = dao.getMaxTempOrderNo();
		
		return orderNO;
	}
	

	/** 到货验收 **/
	private static final String METHOD_DEVICE_JIG_ORDER_INLINE_RECEPT = "device_jig_order_inline_recept";

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
			int confirm_quantity, String login_operator_id, String manage_code,
			SqlSessionManager conn) {
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		entity.setOrder_key(order_key);
		entity.setObject_type(object_type);
		entity.setDevice_type_id(device_type_id);
		entity.setModel_name(model_name);
		entity.setApplicator_id(applicator_id);
		entity.setConfirm_quantity(confirm_quantity);

		dao.updateConfirmQuantity(entity);

		if (object_type == 2) {
			manage_code = "" + confirm_quantity;
		}
		List<String> triggerList = new ArrayList<String>();
		triggerList.add("http://localhost:8080/rvspush/trigger/" + METHOD_DEVICE_JIG_ORDER_INLINE_RECEPT + "/" +
				login_operator_id + "/" +
				order_key + "/" +
				object_type + "/" +
				device_type_id + "/" +
				model_name + "/" +
				applicator_id + "/" +
				manage_code);

		// 控制其他工程
		RvsUtils.sendTrigger(triggerList);

	}
	
	
	/**
	 * 取得全部订单号(参照列表)
	 * @param conn
	 * @return
	 */
	public String getOptions(String flg,SqlSession conn) {
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);
		List<DeviceJigOrderDetailEntity> list = new ArrayList<DeviceJigOrderDetailEntity>();
		
		//询价单，订购列表
		if("1".equals(flg)){
			list = dao.searchInvoiceReferChooser();
		} else if("2".equals(flg)){//订单,订购列表
			list = dao.searchOrderReferChooser();
		}
		
		List<String[]> mList = new ArrayList<String[]>();
		for (DeviceJigOrderDetailEntity model: list) {
			String[] mline = new String[3];
			mline[0] = model.getOrder_key();
			mline[1] = model.getOrder_no();
			mline[2] = DateUtil.toString(model.getApplicate_date(), DateUtil.DATE_PATTERN);
			mList.add(mline);
		}

		String mReferChooser = CodeListUtils.getReferChooser(mList);
		
		return mReferChooser;
	}
}
