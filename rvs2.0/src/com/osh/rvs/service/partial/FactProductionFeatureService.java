package com.osh.rvs.service.partial;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.partial.FactProductionFeatureEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.mapper.partial.FactProductionFeatureMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 现品作业信息
 *
 * @author liuxb
 *
 */
public class FactProductionFeatureService {

	/**
	 * 新建现品作业信息
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void insert(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		FactProductionFeatureMapper dao = conn.getMapper(FactProductionFeatureMapper.class);

		FactProductionFeatureEntity entity = new FactProductionFeatureEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 新建现品作业信息
		dao.insert(entity);
	}

	/**
	 * 更新处理结束时间
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void updateFinishTime(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		FactProductionFeatureMapper dao = conn.getMapper(FactProductionFeatureMapper.class);

		FactProductionFeatureEntity entity = new FactProductionFeatureEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 查询未结束作业记录
		dao.updateFinishTime(entity);
	}

	/**
	 * 查询未结束作业的记录
	 *
	 * @param req
	 * @param conn
	 * @return
	 */
	public FactProductionFeatureForm searchUnFinishProduction(HttpServletRequest req, SqlSession conn) {
		// 数据库连接对象
		FactProductionFeatureMapper dao = conn.getMapper(FactProductionFeatureMapper.class);

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		FactProductionFeatureEntity entity = new FactProductionFeatureEntity();
		// 操作者 ID
		entity.setOperator_id(user.getOperator_id());

		// 查询未结束作业的记录
		entity = dao.searchUnFinishedProduction(entity);

		FactProductionFeatureForm respForm = null;

		if (entity != null) {
			respForm = new FactProductionFeatureForm();
			// 数据模型数据到表单
			BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respForm;

	}

	/**
	 * 删除现品作业信息
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void delete(String key, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		FactProductionFeatureMapper dao = conn.getMapper(FactProductionFeatureMapper.class);
		// 删除现品作业信息
		dao.delete(key);
	}

	/**
	 * 更新零件入库单 KEY
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void updateKey(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		FactProductionFeatureMapper dao = conn.getMapper(FactProductionFeatureMapper.class);

		FactProductionFeatureEntity entity = new FactProductionFeatureEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 更新零件入库单 KEY
		dao.updateKey(entity);
	}

	/**
	 * 零件待出库
	 *
	 * @param conn
	 * @return
	 */
	public List<FactProductionFeatureForm> searchWaitOutStorage(ActionForm form, SqlSession conn) {
		// 数据库连接对象
		FactProductionFeatureMapper dao = conn.getMapper(FactProductionFeatureMapper.class);

		FactProductionFeatureEntity entity = new FactProductionFeatureEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<FactProductionFeatureEntity> list = dao.searchWaitOutStorage(entity);

		List<FactProductionFeatureForm> respList = new ArrayList<FactProductionFeatureForm>();

		if (list != null && list.size() > 0) {
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, FactProductionFeatureForm.class);
		}

		return respList;
	}

}
