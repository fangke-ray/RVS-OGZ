package com.osh.rvs.service.partial;

import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.partial.FactProductionCommentEntity;
import com.osh.rvs.mapper.partial.FactProductionCommentMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 现品作业备注
 *
 * @author liuxb
 *
 */
public class FactProductionCommentService {

	/**
	 * 新建记录
	 *
	 * @param form
	 * @param conn
	 */
	public void insert(ActionForm form, SqlSessionManager conn) {
		// 数据库连接对象
		FactProductionCommentMapper dao = conn.getMapper(FactProductionCommentMapper.class);
		FactProductionCommentEntity entity = new FactProductionCommentEntity();

		// 复制表单数据到模型对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		dao.insert(entity);
	}

}
