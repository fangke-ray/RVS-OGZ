package com.osh.rvs.service.partial;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.partial.PartialWarehouseDetailEntity;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.mapper.partial.PartialWarehouseDetailMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 零件入库明细
 *
 * @author liuxb
 *
 */
public class PartialWarehouseDetailService {
	/**
	 * 新建零件入库明细
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void insert(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseDetailMapper dao = conn.getMapper(PartialWarehouseDetailMapper.class);

		PartialWarehouseDetailEntity entity = new PartialWarehouseDetailEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 新建零件入库明细
		dao.insert(entity);
	}

	/**
	 * 更新零件入库明细
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void update(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseDetailMapper dao = conn.getMapper(PartialWarehouseDetailMapper.class);

		PartialWarehouseDetailEntity entity = new PartialWarehouseDetailEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 更新零件入库明细
		dao.update(entity);
	}

	/**
	 * 根据零件入库单KEY查询零件入库明细
	 *
	 * @param key
	 * @param conn
	 * @return
	 */
	public List<PartialWarehouseDetailForm> searchByKey(String key, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseDetailMapper dao = conn.getMapper(PartialWarehouseDetailMapper.class);

		// 查询零件入库明细信息
		List<PartialWarehouseDetailEntity> list = dao.searchByKey(key);
		List<PartialWarehouseDetailForm> respList = new ArrayList<PartialWarehouseDetailForm>();

		if (list != null && list.size() > 0) {
			// 数据模型数据到表单
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseDetailForm.class);
		}

		return respList;
	}

	/**
	 * 统计各个规格种别总数量
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 * @return
	 */
//	public List<PartialWarehouseDetailForm> countQuantityOfSpecKind(String key, SqlSession conn) {
//		// 数据库连接对象
//		PartialWarehouseDetailMapper dao = conn.getMapper(PartialWarehouseDetailMapper.class);
//		// 统计各个规格种别总数量
//		List<PartialWarehouseDetailEntity> list = dao.countQuantityOfSpecKind(key);
//
//		List<PartialWarehouseDetailForm> respList = new ArrayList<PartialWarehouseDetailForm>();
//
//		if (list != null && list.size() > 0) {
//			// 复制模型数据到表单
//			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseDetailForm.class);
//		}
//
//		return respList;
//
//	}

	/**
	 * 删除零件入库明细
	 *
	 * @param key 零件入库单 KEY
	 * @param conn 数据库连接
	 */
	public void delete(String key, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseDetailMapper dao = conn.getMapper(PartialWarehouseDetailMapper.class);

		// 删除零件入库明细
		dao.delete(key);
	}

	/**
	 * 查询需要分装的零件入库明细
	 *
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<PartialWarehouseDetailForm> searchUnpackByKey(String key, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseDetailMapper dao = conn.getMapper(PartialWarehouseDetailMapper.class);

		List<PartialWarehouseDetailEntity> list = dao.searchUnpackByKey(key);
		List<PartialWarehouseDetailForm> respList = new ArrayList<PartialWarehouseDetailForm>();

		if (list != null && list.size() > 0) {
			// 数据模型数据到表单
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseDetailForm.class);
		}

		return respList;
	}

	/**
	 * 根据零件入库单KEY，统计不同规格种别分装总数
	 *
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<PartialWarehouseDetailForm> countUnpackOfSpecKindByKey(String key, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseDetailMapper dao = conn.getMapper(PartialWarehouseDetailMapper.class);

		List<PartialWarehouseDetailEntity> list = dao.countUnpackOfSpecKindByKey(key);
		List<PartialWarehouseDetailForm> respList = new ArrayList<PartialWarehouseDetailForm>();

		if (list != null && list.size() > 0) {
			// 数据模型数据到表单
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseDetailForm.class);
		}

		return respList;
	}

}
