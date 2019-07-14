package com.osh.rvs.service.partial;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.partial.PartialWarehouseDnEntity;
import com.osh.rvs.form.partial.PartialWarehouseDnForm;
import com.osh.rvs.mapper.partial.PartialWarehouseDnMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 零件入库DN编号
 *
 * @author liuxb
 *
 */
public class PartialWarehouseDnService {

	/**
	 * 根据入库单KEY查询零件入库DN编号
	 *
	 * @param key
	 * @param conn
	 * @return
	 */
	public List<PartialWarehouseDnForm> searchByKey(String key, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseDnMapper dao = conn.getMapper(PartialWarehouseDnMapper.class);

		List<PartialWarehouseDnEntity> list = dao.getByKey(key);

		List<PartialWarehouseDnForm> respList = new ArrayList<PartialWarehouseDnForm>();

		if (list != null && list.size() > 0) {
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseDnForm.class);
		}

		return respList;
	}

	/**
	 * 新建零件入库DN编号
	 *
	 * @param form
	 * @param conn
	 */
	public void insert(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseDnMapper dao = conn.getMapper(PartialWarehouseDnMapper.class);

		PartialWarehouseDnEntity entity = new PartialWarehouseDnEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 新建零件入库DN编号
		dao.insert(entity);
	}

	/**
	 * 删除零件入库DN编号
	 *
	 * @param key
	 * @param conn
	 * @throws Exception
	 */
	public void delete(String key, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseDnMapper dao = conn.getMapper(PartialWarehouseDnMapper.class);

		// 删除入库DN编号
		dao.delete(key);
	}

	/**
	 * 根据DN 编号查询零件入库单信息
	 *
	 * @param DnNo DN 编号
	 * @param conn 数据库连接
	 * @return respForm 零件入库单,如果存零件入库单则返回零件入库单，如果不存零件入库单则返回NULL
	 */
	public PartialWarehouseDnForm getPartialWarehouseDnByDnNo(String dnNo, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseDnMapper dao = conn.getMapper(PartialWarehouseDnMapper.class);

		PartialWarehouseDnEntity entity = dao.getByDnNo(dnNo);
		PartialWarehouseDnForm respForm = null;

		if (entity != null) {
			respForm = new PartialWarehouseDnForm();
			// 复制模型数据到表单
			BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respForm;

	}
}
