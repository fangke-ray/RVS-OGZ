package com.osh.rvs.service.partial;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.partial.PartialWarehouseEntity;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.mapper.partial.PartialWarehouseMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 零件入库单
 *
 * @author liuxb
 *
 */
public class PartialWarehouseService {
	public List<PartialWarehouseForm> search(ActionForm form, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = new PartialWarehouseEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<PartialWarehouseEntity> list = dao.search(entity);

		List<PartialWarehouseForm> respList = new ArrayList<PartialWarehouseForm>();
		if (list != null && list.size() > 0) {
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseForm.class);
		}

		return respList;
	}

	/**
	 * 新建零件入库单
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void insert(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = new PartialWarehouseEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 新建零件入库单
		dao.insert(entity);
	}

	/**
	 * 删除零件入库单
	 *
	 * @param key 零件入库单 KEY
	 * @param conn 数据库连接
	 */
	public void delete(String key, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		// 删除零件入库单
		dao.delete(key);
	}

	/**
	 * 更新入库进展
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void updateStep(ActionForm form, SqlSession conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = new PartialWarehouseEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 更新入库进展
		dao.updateStep(entity);
	}

	/**
	 * 根据key查询零件入库单信息
	 *
	 * @param key 零件入库单 KEY
	 * @param conn 数据库连接
	 * @return respForm 零件入库单,如果存零件入库单则返回零件入库单，如果不存零件入库单则返回NULL
	 */
	public PartialWarehouseForm getByKey(String key, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		// 查询零件入库单信息
		PartialWarehouseEntity entity = dao.getByKey(key);

		PartialWarehouseForm respForm = null;

		if (entity != null) {
			respForm = new PartialWarehouseForm();
			// 复制模型数据到表单
			BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respForm;
	}

	/**
	 * 根据DN 编号查询零件入库单信息
	 *
	 * @param DnNo DN 编号
	 * @param conn 数据库连接
	 * @return respForm 零件入库单,如果存零件入库单则返回零件入库单，如果不存零件入库单则返回NULL
	 */
	public PartialWarehouseForm getPartialWarehouseByDnNo(String DnNo, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = dao.getByDnNo(DnNo);
		PartialWarehouseForm respForm = null;

		if (entity != null) {
			respForm = new PartialWarehouseForm();
			// 复制模型数据到表单
			BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respForm;

	}

	/**
	 * 查询当前入库进展信息
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 * @return respList
	 */
	public List<PartialWarehouseForm> searchStepPartialWarehouse(ActionForm form, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = new PartialWarehouseEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<PartialWarehouseForm> respList = new ArrayList<PartialWarehouseForm>();
		// 查询当前入库进展信息
		List<PartialWarehouseEntity> list = dao.searchStepPartialWarehouse(entity);
		if (list != null && list.size() > 0) {
			// 复制模型数据到表单
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseForm.class);
		}

		return respList;
	}

	/**
	 * 查询当前入库进展信息
	 * （表格内容：序号/入库单日期/DN 编号/零件编号/零件名称/入库单数量/核对数量/核对日期/核对人员）
	 * 
	 * @param conn 数据库连接
	 */
	public void createUnmatchReport() {
		
	}

}
