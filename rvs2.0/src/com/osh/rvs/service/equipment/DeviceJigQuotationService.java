package com.osh.rvs.service.equipment;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.equipment.DeviceJigQuotationEntity;
import com.osh.rvs.form.equipment.DeviceJigQuotationForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.equipment.DeviceJigQuotationMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 设备工具治具订购报价
 * 
 * @author liuxb
 * 
 */
public class DeviceJigQuotationService {
	/**
	 * 查询全部报价
	 * 
	 * @param conn
	 * @return
	 */
	public List<DeviceJigQuotationForm> searchAll(SqlSession conn) {
		// 数据连接
		DeviceJigQuotationMapper dao = conn.getMapper(DeviceJigQuotationMapper.class);
		List<DeviceJigQuotationEntity> list = dao.searchAll();

		List<DeviceJigQuotationForm> respList = new ArrayList<DeviceJigQuotationForm>();
		BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceJigQuotationForm.class);

		return respList;

	}

	/**
	 * 新建设备工具治具订购报价
	 * 
	 * @param form
	 * @param conn
	 */
	public String insertQuotation(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigQuotationMapper dao = conn.getMapper(DeviceJigQuotationMapper.class);
		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);

		DeviceJigQuotationEntity entity = new DeviceJigQuotationEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.insert(entity);

		String lastInsertID = commonMapper.getLastInsertID();
		return lastInsertID;
	}

	/**
	 * 更新报价
	 * 
	 * @param form
	 * @param conn
	 */
	public void updateQuotation(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigQuotationMapper dao = conn.getMapper(DeviceJigQuotationMapper.class);

		DeviceJigQuotationEntity entity = new DeviceJigQuotationEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.update(entity);
	}

	/**
	 * 根据报价Key查询报价信息
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public DeviceJigQuotationForm getQuotationById(String quotationID, SqlSession conn) {
		// 数据连接
		DeviceJigQuotationMapper dao = conn.getMapper(DeviceJigQuotationMapper.class);
		DeviceJigQuotationEntity entity = dao.getDeviceJigQuotationById(quotationID);

		DeviceJigQuotationForm respFom = null;

		if (entity != null) {
			respFom = new DeviceJigQuotationForm();
			// 复制数据
			BeanUtil.copyToForm(entity, respFom, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respFom;
	}

	/**
	 * 根据报价单号查询报价信息
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public DeviceJigQuotationForm getQuotationByNO(String quotation_no, SqlSession conn) {
		// 数据连接
		DeviceJigQuotationMapper dao = conn.getMapper(DeviceJigQuotationMapper.class);
		DeviceJigQuotationEntity entity = dao.getDeviceJigQuotationByQuotationNo(quotation_no);

		DeviceJigQuotationForm respFom = null;

		if (entity != null) {
			respFom = new DeviceJigQuotationForm();
			// 复制数据
			BeanUtil.copyToForm(entity, respFom, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respFom;
	}
}
