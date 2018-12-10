package com.osh.rvs.service.partial;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.partial.FactPartialWarehouseEntity;
import com.osh.rvs.form.partial.FactPartialWarehouseForm;
import com.osh.rvs.mapper.partial.FactPartialWarehouseMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 现品入库作业数
 *
 * @author liuxb
 *
 */
public class FactPartialWarehouseService {

	/**
	 * 新建现品入库作业数
	 *
	 * @param form
	 * @param conn
	 * @throws Exception
	 */
	public void insert(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		FactPartialWarehouseMapper dao = conn.getMapper(FactPartialWarehouseMapper.class);
		FactPartialWarehouseEntity entity = new FactPartialWarehouseEntity();

		// 复制表单数据到模型对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.insert(entity);
	}

	/**
	 * 统计每个规格种别入库作业总数
	 *
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<FactPartialWarehouseForm> countQuantityOfSpecKind(ActionForm form, SqlSession conn) {
		// 数据库连接对象
		FactPartialWarehouseMapper dao = conn.getMapper(FactPartialWarehouseMapper.class);
		FactPartialWarehouseEntity entity = new FactPartialWarehouseEntity();

		// 复制表单数据到模型对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<FactPartialWarehouseEntity> list = dao.countQuantityOfSpecKind(entity);
		List<FactPartialWarehouseForm> respList = new ArrayList<FactPartialWarehouseForm>();

		if (list != null && list.size() > 0) {
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, FactPartialWarehouseForm.class);
		}

		return respList;
	}
}
