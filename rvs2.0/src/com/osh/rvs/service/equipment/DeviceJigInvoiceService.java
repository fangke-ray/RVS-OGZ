package com.osh.rvs.service.equipment;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.equipment.DeviceJigInvoiceEntity;
import com.osh.rvs.form.equipment.DeviceJigInvoiceForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.equipment.DeviceJigInvoiceMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 设备工具治具订购询价
 * 
 * @author liuxb
 * 
 */
public class DeviceJigInvoiceService {
	/**
	 * 新建设备工具治具订购询价
	 * 
	 * @param form
	 * @param conn
	 */
	public String insert(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigInvoiceMapper dao = conn.getMapper(DeviceJigInvoiceMapper.class);
		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);

		DeviceJigInvoiceEntity entity = new DeviceJigInvoiceEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.insert(entity);

		String lastInsertID = commonMapper.getLastInsertID();

		return lastInsertID;
	}

	public void updatePrice(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigInvoiceMapper dao = conn.getMapper(DeviceJigInvoiceMapper.class);

		DeviceJigInvoiceEntity entity = new DeviceJigInvoiceEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.updatePrice(entity);
	}

	/**
	 * 订购询价明细
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public DeviceJigInvoiceForm getDeviceJigInvoice(ActionForm form, SqlSession conn) {
		// 数据连接
		DeviceJigInvoiceMapper dao = conn.getMapper(DeviceJigInvoiceMapper.class);

		DeviceJigInvoiceEntity entity = new DeviceJigInvoiceEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		entity = dao.getDeviceJigInvoiceById(entity.getInvoice_id());

		DeviceJigInvoiceForm respFom = null;

		if (entity != null) {
			respFom = new DeviceJigInvoiceForm();
			// 复制数据库
			BeanUtil.copyToForm(entity, respFom, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respFom;
	}

	/**
	 * 查询最后一次询价
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public DeviceJigInvoiceForm getLastTimeInvoice(ActionForm form, SqlSession conn) {
		// 数据连接
		DeviceJigInvoiceMapper dao = conn.getMapper(DeviceJigInvoiceMapper.class);

		DeviceJigInvoiceEntity entity = new DeviceJigInvoiceEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		entity = dao.getLastTimeInvoice(entity);

		DeviceJigInvoiceForm respFom = null;

		if (entity != null) {
			respFom = new DeviceJigInvoiceForm();
			// 复制数据库
			BeanUtil.copyToForm(entity, respFom, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respFom;
	}

}
