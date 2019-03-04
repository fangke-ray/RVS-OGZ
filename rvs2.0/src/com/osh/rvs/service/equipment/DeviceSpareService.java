package com.osh.rvs.service.equipment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.equipment.DeviceSpareAdjustEntity;
import com.osh.rvs.bean.equipment.DeviceSpareEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.equipment.DeviceSpareForm;
import com.osh.rvs.mapper.equipment.DeviceSpareAdjustMapper;
import com.osh.rvs.mapper.equipment.DeviceSpareMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;

/**
 * 设备工具备品
 * 
 * @author liuxb
 * 
 */
public class DeviceSpareService {
	/**
	 * 检索
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<DeviceSpareForm> search(ActionForm form, SqlSession conn) {
		// 数据连接
		DeviceSpareMapper dao = conn.getMapper(DeviceSpareMapper.class);

		DeviceSpareEntity entity = new DeviceSpareEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 检索
		List<DeviceSpareEntity> list = dao.search(entity);

		List<DeviceSpareForm> respist = new ArrayList<DeviceSpareForm>();

		if (list != null && list.size() > 0) {
			// 复制数据到表单对象
			BeanUtil.copyToFormList(list, respist, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceSpareForm.class);
		}

		return respist;
	}

	/**
	 * 查询设备工具备品信息
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public DeviceSpareForm getDeviceSpare(ActionForm form, SqlSession conn) {
		// 数据连接
		DeviceSpareMapper dao = conn.getMapper(DeviceSpareMapper.class);

		DeviceSpareEntity entity = new DeviceSpareEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 查询设备工具备品信息
		entity = dao.getDeviceSpare(entity);

		DeviceSpareForm respForm = null;

		if (entity != null) {
			respForm = new DeviceSpareForm();
			BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respForm;
	}

	public String checkExistsStock(ActionForm form, Integer device_spare_type,
			SqlSession conn) {
		// 数据连接
		DeviceSpareMapper mapper = conn.getMapper(DeviceSpareMapper.class);

		DeviceSpareEntity entity = new DeviceSpareEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		entity.setDevice_spare_type(device_spare_type);

		// 查询设备工具备品信息
		DeviceSpareEntity ret = mapper.getDeviceSpare(entity);

		if (ret != null) {
			Integer available_inventory = ret.getAvailable_inventory();
			if (available_inventory > 0) {
				return "exists";
			}
		}

		return null;
	}

	/**
	 * 更新设备工具备品
	 * 
	 * @param form
	 * @param conn
	 */
	public void update(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceSpareMapper dao = conn.getMapper(DeviceSpareMapper.class);

		DeviceSpareEntity entity = new DeviceSpareEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.update(entity);
	}

	/**
	 * 新建设备工具备品
	 * 
	 * @param form
	 * @param conn
	 * @param req
	 * @param errors
	 */
	public void insert(ActionForm form, SqlSessionManager conn, HttpServletRequest req, List<MsgInfo> errors) {
		// 数据连接
		DeviceSpareMapper deviceSpareMapper = conn.getMapper(DeviceSpareMapper.class);
		DeviceSpareAdjustMapper deviceSpareAdjustMapper = conn.getMapper(DeviceSpareAdjustMapper.class);

		DeviceSpareEntity entity = new DeviceSpareEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 设备工具备品重复检查
		DeviceSpareEntity dbEntity = deviceSpareMapper.getDeviceSpare(entity);
		if (dbEntity != null) {
			String msg = "品名为[" + dbEntity.getDevice_type_name() + "],型号为[" + dbEntity.getModel_name() + "],备品种类为["
					+ CodeListUtils.getValue("device_spare_type", dbEntity.getDevice_spare_type().toString()) + "]";
			MsgInfo error = new MsgInfo();
			error.setErrcode("dbaccess.recordDuplicated");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", msg));
			errors.add(error);
			return;
		}

		// 备品种类
		Integer deviceSpareType = entity.getDevice_spare_type();
		if (deviceSpareType == 1) {// 消耗备品
			entity.setLocation(null);
		} else if (deviceSpareType == 2) {// 备件
			entity.setOrder_cycle(0);
		}

		// 新建设备工具备品
		deviceSpareMapper.insert(entity);

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		DeviceSpareAdjustEntity deviceSpareAdjustEntity = new DeviceSpareAdjustEntity();
		// 设备品名 ID
		deviceSpareAdjustEntity.setDevice_type_id(entity.getDevice_type_id());
		// 型号
		deviceSpareAdjustEntity.setModel_name(entity.getModel_name());
		// 备品种类
		deviceSpareAdjustEntity.setDevice_spare_type(entity.getDevice_spare_type());
		// 调整日时
		deviceSpareAdjustEntity.setAdjust_time(Calendar.getInstance().getTime());
		// 理由
		deviceSpareAdjustEntity.setReason_type(01);
		// 调整量
		deviceSpareAdjustEntity.setAdjust_inventory(entity.getAvailable_inventory());
		// 调整负责人
		deviceSpareAdjustEntity.setOperator_id(user.getOperator_id());

		// 新建设备工具备品调整记录
		deviceSpareAdjustMapper.insert(deviceSpareAdjustEntity);
	}

	/**
	 * 取消管理
	 * 
	 * @param form
	 * @param req
	 * @param conn
	 */
	public void canceManage(ActionForm form, HttpServletRequest req, SqlSessionManager conn) {
		// 数据连接
		DeviceSpareMapper deviceSpareMapper = conn.getMapper(DeviceSpareMapper.class);
		DeviceSpareAdjustMapper deviceSpareAdjustMapper = conn.getMapper(DeviceSpareAdjustMapper.class);

		DeviceSpareEntity entity = new DeviceSpareEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 查询设备工具备品信息
		entity = deviceSpareMapper.getDeviceSpare(entity);

		// 删除设备工具备品
		deviceSpareMapper.delete(entity);

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		DeviceSpareAdjustEntity deviceSpareAdjustEntity = new DeviceSpareAdjustEntity();
		// 设备品名 ID
		deviceSpareAdjustEntity.setDevice_type_id(entity.getDevice_type_id());
		// 型号
		deviceSpareAdjustEntity.setModel_name(entity.getModel_name());
		// 备品种类
		deviceSpareAdjustEntity.setDevice_spare_type(entity.getDevice_spare_type());
		// 调整日时
		deviceSpareAdjustEntity.setAdjust_time(Calendar.getInstance().getTime());
		// 理由
		deviceSpareAdjustEntity.setReason_type(02);
		// 调整量
		deviceSpareAdjustEntity.setAdjust_inventory(entity.getAvailable_inventory() * -1);
		// 调整负责人
		deviceSpareAdjustEntity.setOperator_id(user.getOperator_id());

		// 新建设备工具备品调整记录
		deviceSpareAdjustMapper.insert(deviceSpareAdjustEntity);

	}

	/**
	 * 盘点
	 * 
	 * @param form
	 * @param req
	 * @param conn
	 */
	public void stock(ActionForm form, HttpServletRequest req, SqlSessionManager conn) {
		// 数据连接
		DeviceSpareMapper deviceSpareMapper = conn.getMapper(DeviceSpareMapper.class);
		DeviceSpareAdjustMapper deviceSpareAdjustMapper = conn.getMapper(DeviceSpareAdjustMapper.class);

		DeviceSpareEntity entity = new DeviceSpareEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		entity.setAvailable_inventory(entity.getAdjust_inventory());

		// 修正有效库存
		Integer adjustInventory = entity.getAdjust_inventory();
		// 调整备注
		String comment = entity.getComment();

		DeviceSpareEntity dbEntity = deviceSpareMapper.getDeviceSpare(entity);

		// 更新有效库存
		deviceSpareMapper.updateAvailableInventory(entity);

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		DeviceSpareAdjustEntity deviceSpareAdjustEntity = new DeviceSpareAdjustEntity();
		// 设备品名 ID
		deviceSpareAdjustEntity.setDevice_type_id(dbEntity.getDevice_type_id());
		// 型号
		deviceSpareAdjustEntity.setModel_name(dbEntity.getModel_name());
		// 备品种类
		deviceSpareAdjustEntity.setDevice_spare_type(dbEntity.getDevice_spare_type());
		// 调整日时
		deviceSpareAdjustEntity.setAdjust_time(Calendar.getInstance().getTime());
		// 理由
		deviceSpareAdjustEntity.setReason_type(99);

		// adjust_inventory是输入的“修正有效库存” - 当前有效库存
		adjustInventory = adjustInventory - dbEntity.getAvailable_inventory();

		// 调整量
		deviceSpareAdjustEntity.setAdjust_inventory(adjustInventory);
		// 调整负责人
		deviceSpareAdjustEntity.setOperator_id(user.getOperator_id());
		// 调整备注
		deviceSpareAdjustEntity.setComment(comment);

		// 新建设备工具备品调整记录
		deviceSpareAdjustMapper.insert(deviceSpareAdjustEntity);
	}

	/**
	 * 管理
	 * 
	 * @param form
	 * @param req
	 * @param conn
	 * @param errors
	 */
	public void manage(ActionForm form, HttpServletRequest req, SqlSessionManager conn, List<MsgInfo> errors) {
		// 数据连接
		DeviceSpareMapper deviceSpareMapper = conn.getMapper(DeviceSpareMapper.class);
		DeviceSpareAdjustMapper deviceSpareAdjustMapper = conn.getMapper(DeviceSpareAdjustMapper.class);

		DeviceSpareEntity entity = new DeviceSpareEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 理由
		Integer reasonType = entity.getReason_type();
		// 数量
		Integer adjustInventory = entity.getAdjust_inventory();
		// 调整备注
		String comment = entity.getComment();

		if (reasonType == 23) {// 理由是“外借”时，取“数量”为负数
			adjustInventory = adjustInventory * -1;
		}

		DeviceSpareEntity dbEntity = deviceSpareMapper.getDeviceSpare(entity);
		// 当前有效库存
		Integer availableInventory = dbEntity.getAvailable_inventory();

		// 当前有效库存 + 数量
		availableInventory = availableInventory + adjustInventory;

		if (reasonType == 23 && availableInventory < 0) {// 如果值变成了负数则报错
			MsgInfo error = new MsgInfo();
			error.setErrmsg("外借数量大于当前有效库存。");
			errors.add(error);
			return;
		}

		entity.setAvailable_inventory(availableInventory);
		deviceSpareMapper.updateAvailableInventory(entity);

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		DeviceSpareAdjustEntity deviceSpareAdjustEntity = new DeviceSpareAdjustEntity();

		// 设备品名 ID
		deviceSpareAdjustEntity.setDevice_type_id(dbEntity.getDevice_type_id());
		// 型号
		deviceSpareAdjustEntity.setModel_name(dbEntity.getModel_name());
		// 备品种类
		deviceSpareAdjustEntity.setDevice_spare_type(dbEntity.getDevice_spare_type());
		// 调整日时
		deviceSpareAdjustEntity.setAdjust_time(Calendar.getInstance().getTime());
		// 理由
		deviceSpareAdjustEntity.setReason_type(reasonType);
		// 调整量
		deviceSpareAdjustEntity.setAdjust_inventory(adjustInventory);
		// 调整负责人
		deviceSpareAdjustEntity.setOperator_id(user.getOperator_id());
		// 调整备注
		deviceSpareAdjustEntity.setComment(comment);

		// 新建设备工具备品调整记录
		deviceSpareAdjustMapper.insert(deviceSpareAdjustEntity);
	}

	public Map<String, Integer> calculatePrice(SqlSession conn) {
		// 数据连接
		DeviceSpareMapper deviceSpareMapper = conn.getMapper(DeviceSpareMapper.class);

		Map<String, Integer> map = deviceSpareMapper.calculatePrice();

		return map;
	}

	/**
	 * 备品加入设备管理
	 * 
	 * @param form
	 * @param req
	 * @param conn
	 */
	public void setAsManageCode(ActionForm form, int reason_type, HttpServletRequest req, SqlSessionManager conn) {
		// 数据连接
		DeviceSpareMapper deviceSpareMapper = conn.getMapper(DeviceSpareMapper.class);
		DeviceSpareAdjustMapper deviceSpareAdjustMapper = conn.getMapper(DeviceSpareAdjustMapper.class);

		DeviceSpareEntity postEntity = new DeviceSpareEntity();
		CopyOptions cos = new CopyOptions();
		cos.excludeEmptyString(); cos.excludeNull();
		cos.fieldRename("manage_code", "location");
		BeanUtil.copyToBean(form, postEntity, cos);
		postEntity.setDevice_spare_type(1);

		DeviceSpareEntity dbEntity = deviceSpareMapper.getDeviceSpare(postEntity);

		// 当前有效库存
		Integer availableInventory = dbEntity.getAvailable_inventory();

		postEntity.setAvailable_inventory(availableInventory - 1);
		deviceSpareMapper.updateAvailableInventory(postEntity);

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		DeviceSpareAdjustEntity deviceSpareAdjustEntity = new DeviceSpareAdjustEntity();

		// 设备品名 ID
		deviceSpareAdjustEntity.setDevice_type_id(postEntity.getDevice_type_id());
		// 型号
		deviceSpareAdjustEntity.setModel_name(postEntity.getModel_name());
		// 备品种类
		deviceSpareAdjustEntity.setDevice_spare_type(1);
		// 调整日时
		deviceSpareAdjustEntity.setAdjust_time(Calendar.getInstance().getTime());
		// 理由
		deviceSpareAdjustEntity.setReason_type(reason_type);
		// 调整量
		deviceSpareAdjustEntity.setAdjust_inventory(-1);
		// 调整负责人
		deviceSpareAdjustEntity.setOperator_id(user.getOperator_id());
		// 调整备注
		String comment = "";
		if (reason_type == 25) {
			comment = "新建为管理编号：" + postEntity.getLocation();
		} else if (reason_type == 24) {
			comment = "替换为管理编号：" + postEntity.getLocation();
		}
		deviceSpareAdjustEntity.setComment(comment);

		// 新建设备工具备品调整记录
		deviceSpareAdjustMapper.insert(deviceSpareAdjustEntity);
		
	}

}
