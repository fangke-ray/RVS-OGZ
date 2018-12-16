package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.PartialEntity;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.PartialForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.master.PartialMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;

public class PartialService {

	public List<PartialForm> searchPartial(PartialEntity partialEntity, SqlSession conn) {
		PartialMapper dao = conn.getMapper(PartialMapper.class);
		List<PartialForm> resultForm = new ArrayList<PartialForm>();
		/* 判断数据为null的情况 */
		if (partialEntity != null) {
			List<PartialEntity> resultBean = dao.searchPartial(partialEntity);
			BeanUtil.copyToFormList(resultBean, resultForm, null, PartialForm.class);
			return resultForm;
		} else {
			return null;
		}
	}

	/* 验证零件编码和零件 */
	public void customValidate(ActionForm partialForm, SqlSession conn, List<MsgInfo> errors) {
		PartialMapper dao = conn.getMapper(PartialMapper.class);
		PartialEntity conditionBean = new PartialEntity();
		/* 数据复制 */
		BeanUtil.copyToBean(partialForm, conditionBean, (new CopyOptions()).include("partial_id", "code"));

		List<String> resultBean = dao.checkPartial(conditionBean);
		if (resultBean != null && resultBean.size() > 0) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("code");
			error.setErrcode("dbaccess.columnNotUnique");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "零件编码", conditionBean.getCode(), "零件"));
			errors.add(error);
		}
	}

	public String insert(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		PartialEntity insertBean = new PartialEntity();
		BeanUtil.copyToBean(form, insertBean, null);
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		insertBean.setUpdated_by(user.getOperator_id());

		/* partail表插入数据 */
		PartialMapper dao = conn.getMapper(PartialMapper.class);
		dao.insertPartial(insertBean);

		CommonMapper cDao = conn.getMapper(CommonMapper.class);
		String partial_id = cDao.getLastInsertID();// //取得本连接最后取得的自增ID

		return partial_id;
		/*
		 * CommonMapper cDao = conn.getMapper(CommonMapper.class); String
		 * partial_id = cDao.getLastInsertID();
		 *
		 * insertBean.setPartial_id(partial_id);
		 * dao.insertPartialPrice(insertBean);
		 */
	}

	public PartialForm getDetail(PartialEntity partialEntity, SqlSession conn, List<MsgInfo> errors) {
		String partial_id = partialEntity.getPartial_id();

		PartialMapper dao = conn.getMapper(PartialMapper.class);
		PartialEntity partial = dao.getPartialByID(partial_id);
		if (partial == null) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("partial_id");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "零件对象型号"));
			errors.add(error);
			return null;
		} else {
			PartialForm pf = new PartialForm();
			BeanUtil.copyToForm(partial, pf, null);
			return pf;
		}
	}

	public PartialForm getDetail(String code, SqlSession conn, List<MsgInfo> errors) {
		PartialMapper dao = conn.getMapper(PartialMapper.class);

		List<PartialEntity> list = dao.getPartialByCode(code);

		if (list == null || list.size() == 0) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("partial_id");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "零件对象型号"));
			errors.add(error);
			return null;
		} else {
			PartialForm pf = new PartialForm();
			BeanUtil.copyToForm(list.get(0), pf, null);
			return pf;
		}

	}

	public void delete(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		PartialEntity updateBean = new PartialEntity();
		BeanUtil.copyToBean(form, updateBean, null);
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		updateBean.setUpdated_by(user.getOperator_id());

		PartialMapper dao = conn.getMapper(PartialMapper.class);
		dao.deletePartial(updateBean);
	}

	/* 双击修改页面内容 */

	public void update(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		PartialEntity updateBean = new PartialEntity();
		BeanUtil.copyToBean(form, updateBean, null);
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		// 更新人
		updateBean.setUpdated_by(user.getOperator_id());

		PartialMapper dao = conn.getMapper(PartialMapper.class);
		dao.updatePartial(updateBean);

		// 清空反查缓存
		ReverseResolution.partialRever.clear();
		/*
		 * //如果返回的是false则只执行更新PartialPrice表 if ("false".equals(priceNotChanged))
		 * { dao.updatePartialPrice(updateBean); }
		 */
	}

	/* 零件partial的code和name更新 */
	public void updatePartialCodeName(ActionForm form, String judgeHistorylimitdate, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		PartialEntity updateBean = new PartialEntity();
		PartialEntity updateBeanForPrice = new PartialEntity();
		BeanUtil.copyToBean(form, updateBean, CopyOptions.COPYOPTIONS_NOEMPTY);
		BeanUtil.copyToBean(form, updateBeanForPrice, CopyOptions.COPYOPTIONS_NOEMPTY);

		/* 获取的操作者的ID */
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		updateBean.setUpdated_by(user.getOperator_id());

		PartialMapper dao = conn.getMapper(PartialMapper.class);

		/* 从页面传递有效截至日期和选择的日期进行比较大小 */
		if ("false".equals(judgeHistorylimitdate)) {
			/* 新建插入partial表的code和name */
			dao.insertPartialCodeName(updateBean);
		}
	}

	/** 零件集合 **/
	public List<Map<String, String>> getPartialAutoCompletes(String code, SqlSession conn) {
		PartialMapper dao = conn.getMapper(PartialMapper.class);
		return dao.getAllPartial(code);
	}
}
