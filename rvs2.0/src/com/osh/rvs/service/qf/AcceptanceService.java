package com.osh.rvs.service.qf;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.common.FseBridgeUtil;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.qf.AcceptanceMapper;
import com.osh.rvs.service.CustomerService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ProductionFeatureService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.validator.Validators;

public class AcceptanceService {

	private ProductionFeatureService pfService = new ProductionFeatureService();
	
	/**
	 * 建立维修对象信息
	 * @param newId
	 * @param session
	 * @param conn
	 * @param errors
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public void insert(ActionForm form, HttpSession session, SqlSession conn, List<MsgInfo> errors) throws NumberFormatException, Exception {

		MaterialEntity insertBean = new MaterialEntity();
		BeanUtil.copyToBean(form, insertBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		if (!isEmpty(insertBean.getCustomer_name())) {
			CustomerService cservice = new CustomerService();
			insertBean.setCustomer_id(cservice.getCustomerStudiedId(insertBean.getCustomer_name(), insertBean.getOcm(), conn));
		}

		Date dAgreedDate = insertBean.getAgreed_date();

		if (dAgreedDate != null) {
			// 计算同意日->预定纳期
			Date dSchedulePlan = RvsUtils.getTimeLimit(dAgreedDate, 
					insertBean.getLevel(), null, conn, false)[0];
			insertBean.setScheduled_date(dSchedulePlan);
		}

		if (insertBean.getBreak_back_flg() == null) {
			insertBean.setBreak_back_flg(0);
		}

		AcceptanceMapper dao = conn.getMapper(AcceptanceMapper.class);
		dao.insertMaterial(insertBean);
	}

	/**
	 * 建立受理的作业记录
	 * @param newId
	 * @param session
	 * @param conn
	 * @throws Exception
	 */
	public void accept(String newId, HttpSession session, SqlSession conn) throws Exception {
		ProductionFeatureEntity entity = new ProductionFeatureEntity();
		entity.setMaterial_id(newId);
		entity.setPosition_id(RvsConsts.POSITION_ACCEPTANCE);
		entity.setPace(0);
		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		entity.setOperator_id(loginData.getOperator_id());
		entity.setSection_id(loginData.getSection_id()); // TODO user
		entity.setSection_id("00000000001");
		entity.setOperate_result(2);
		entity.setRework(0);

		// 标准作业时间
		Integer use_seconds = 0;
		entity.setUse_seconds(use_seconds);
		pfService.insertAcceptance(entity, conn);

	}

	/**
	 * 批量插入数据-从Excel
	 * @param parameterMap
	 * @param session
	 * @param conn
	 * @param errors
	 * @throws Exception
	 */
	public void batchinsert(Map<String, String[]> parameterMap,HttpSession session,
			SqlSessionManager conn, List<MsgInfo> errors) throws Exception {

		List<MaterialForm> materialForms = new AutofillArrayList<MaterialForm>(MaterialForm.class);
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		
		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("materials".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("sorc_no".equals(column)) {
						materialForms.get(icounts).setSorc_no(value[0]);
					} else if ("model_id".equals(column)) {
						materialForms.get(icounts).setModel_id(value[0]);
					} else if ("model_name".equals(column)) {
						materialForms.get(icounts).setModel_name(value[0]);
					} else if ("serial_no".equals(column)) {
						materialForms.get(icounts).setSerial_no(value[0]);
					} else if ("customer_name".equals(column)) {
						materialForms.get(icounts).setCustomer_name(value[0]);
					} else if ("agreed_date".equals(column)) {
						materialForms.get(icounts).setAgreed_date(value[0]);
					} else if ("ocm".equals(column)) {
						materialForms.get(icounts).setOcm(value[0]);
					} else if ("ocm_rank".equals(column)) {
						materialForms.get(icounts).setOcm_rank(value[0]);
					} else if ("level".equals(column)) {
						materialForms.get(icounts).setLevel(value[0]);
					} else if ("direct_flg".equals(column)) {
						materialForms.get(icounts).setDirect_flg(value[0]);
					} else if ("area".equals(column)) {
						materialForms.get(icounts).setArea(value[0]);
					} else if ("bound_out_ocm".equals(column)) {
						materialForms.get(icounts).setBound_out_ocm(value[0]);
					} else if ("ocm_deliver_date".equals(column)) {
						materialForms.get(icounts).setOcm_deliver_date(value[0]);
					} else if ("osh_deliver_date".equals(column)) {
						materialForms.get(icounts).setOsh_deliver_date(value[0]);
					} else if ("selectable".equals(column)) {
						materialForms.get(icounts).setSelectable(value[0]);
					} else if ("service_repair_flg".equals(column)) {
						materialForms.get(icounts).setService_repair_flg(value[0]);
					} else if ("fix_type".equals(column)) {
						materialForms.get(icounts).setFix_type(value[0]);
					}
				}
			}
		}

		MaterialService mservice = new MaterialService();

		// 检查每个Form
		for (MaterialForm materialForm : materialForms) {
			Validators v = BeanUtil.createBeanValidators(materialForm, BeanUtil.CHECK_TYPE_ALL);
			v.delete("level", "ocm", "ocm_rank", "area", "bound_out_ocm");
			v.add("level", v.integerType());
			v.add("ocm", v.integerType());
			v.add("ocm_rank", v.integerType());
			v.add("area", v.integerType());
			v.add("bound_out_ocm", v.integerType());
			List<MsgInfo> thisErrors = v.validate();
			if (thisErrors.size() > 0) {
				for (MsgInfo thisError : thisErrors) {
					thisError.setErrmsg("型号" + materialForm.getModel_name() + "机身号" + materialForm.getSerial_no() + "的维修对象：" + thisError.getErrmsg());
					errors.add(thisError);
				}
			}

			// 对比客户ID
			mservice.checkRepeatNo("", materialForm, conn, errors);
		}

		// 放入数据库
		if (errors.size() == 0) {
			for (MaterialForm materialForm : materialForms) {
				insert(materialForm, session, conn, errors);
			}
		}
	}

	/**
	 * 批量受理数据
	 * @param parameterMap
	 * @param session
	 * @param conn
	 * @param errors
	 * @throws Exception
	 */
	public void accept(Map<String, String[]> parameterMap,HttpSession session,
			SqlSessionManager conn, List<MsgInfo> errors) throws Exception {

		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("materials".equals(entity)) {
					String column = m.group(2);

					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("material_id".equals(column)) {
						accept(value[0], session, conn);
					}
				}
			}
		}
	}

	/**
	 * 更新维修对象数据（受理完成前）
	 * @param form
	 * @param session
	 * @param conn
	 * @param errors
	 * @throws Exception 
	 */
	public void update(ActionForm form, HttpSession session, SqlSession conn, List<MsgInfo> errors) throws Exception {
		CustomerService cservice = new CustomerService();

		MaterialEntity insertBean = new MaterialEntity();
		BeanUtil.copyToBean(form, insertBean, CopyOptions.COPYOPTIONS_NOEMPTY);
	
		if (!isEmpty(insertBean.getCustomer_name())) {
			insertBean.setCustomer_id(cservice.getCustomerStudiedId(insertBean.getCustomer_name(), insertBean.getOcm(), conn));
		}

		AcceptanceMapper dao = conn.getMapper(AcceptanceMapper.class);
		dao.updateMaterial(insertBean);
	}

	/**
	 * 取得维修对象详细信息
	 * @param conn
	 * @return
	 */
	public List<MaterialForm> getMaterialDetail(SqlSession conn) {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);

		List<MaterialEntity> lResultBean = dao.getMaterialDetailForRecept(null);

		List<MaterialForm> lResultForm = new ArrayList<MaterialForm>();

		CopyOptions cos = new CopyOptions();
		cos.dateConverter(DateUtil.DATE_TIME_PATTERN, "reception_time", "finish_time");
		cos.fieldRename("finish_time", "doreception_time");
		BeanUtil.copyToFormList(lResultBean, lResultForm, cos, MaterialForm.class);

		return lResultForm;
	}

	/**
	 * 未修理返还（受理完成前）
	 * @param form
	 * @param ids
	 * @param conn
	 * @throws Exception
	 */
	public void acceptReturn(ActionForm form, List<String> ids, SqlSessionManager conn) throws Exception{
		MaterialForm materialForm = (MaterialForm)form;

		if ("OSH".equals(materialForm.getReturn_ocm())) {
			AcceptanceMapper dao = conn.getMapper(AcceptanceMapper.class);
			// 发还给OSH维修对象状态更改为已发还
			dao.changeSorc(ids);
		} else {
			MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
			mDao.updateMaterialReturn(ids);
		}

		// FSE 数据同步
		try{
			for (String id: ids) {
				FseBridgeUtil.toUpdateMaterial(id, "br111");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新受理时间（数据获取时间->正式受理完成时间）
	 * @param ids
	 * @param conn
	 * @throws Exception
	 */
	public void updateFormalReception(String[] material_ids, SqlSessionManager conn) throws Exception {
		AcceptanceMapper dao = conn.getMapper(AcceptanceMapper.class);
		dao.updateFormalReception(material_ids);
		try {
		for (String material_id : material_ids) {
			FseBridgeUtil.toUpdateMaterial(material_id, "111");
			FseBridgeUtil.toUpdateMaterialProcess(material_id, "111");
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
