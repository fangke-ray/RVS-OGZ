package com.osh.rvs.service.qf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.qa.QualityAssuranceMapper;
import com.osh.rvs.mapper.qf.ShippingMapper;
import com.osh.rvs.service.inline.PositionPanelService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.IntegerConverter;
import framework.huiqing.common.util.message.ApplicationMessage;

public class ShippingService {
	private Logger _log = Logger.getLogger(getClass());

	public List<MaterialEntity> getWaitingMaterial(String position_id, SqlSession conn) {
		// TODO Auto-generated method stub

		ShippingMapper sDao = conn.getMapper(ShippingMapper.class);

		// 取得待品保处理对象一览 711
		List<MaterialEntity> waitings = sDao.getWaitings(position_id);

		return waitings;
	}

	public List<MaterialEntity> getFinishedMaterial(String position_id, SqlSession conn) {

		ShippingMapper sDao = conn.getMapper(ShippingMapper.class);

		// 取得今日已完成处理对象一览
		List<MaterialEntity> finished = sDao.getFinished(position_id);

		return finished;
	}

	public void updateMaterial(HttpServletRequest req, SqlSessionManager conn) throws Exception {
		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		PositionPanelService ppService = new PositionPanelService();
		// 取得当前作业中作业信息
		ProductionFeatureEntity workingPf = ppService.getWorkingPf(user, conn);

		// 检查维修对象表单 TODO
		MaterialEntity bean = new MaterialEntity();
		// 进行中的维修对象
		bean.setMaterial_id(workingPf.getMaterial_id());
		bean.setOutline_time(new Date());
		bean.setBound_out_ocm(IntegerConverter.getInstance().getAsObject(req.getParameter("bound_out_ocm")));
		bean.setPackage_no(req.getParameter("package_no"));
		bean.setOcm_shipping_date(new Date());

		// 更新维修对象。
		QualityAssuranceMapper dao = conn.getMapper(QualityAssuranceMapper.class);
		dao.updateMaterial(bean);

//		// FSE 数据同步
//		try{
//			FseBridgeUtil.toUpdateMaterialProcess(workingPf.getMaterial_id(), "711");
//		} catch (Exception e) {
//			_log.error(e.getMessage(), e);
//		}

		// 工时按标准工时：
		Integer use_seconds = ppService.getTotalTimeByRework(workingPf, conn);

		// 作业信息状态改为，作业完成
		ProductionFeatureMapper pfdao = conn.getMapper(ProductionFeatureMapper.class);
		workingPf.setOperate_result(RvsConsts.OPERATE_RESULT_FINISH);
		workingPf.setUse_seconds(use_seconds);
		// Dummy
		workingPf.setPcs_inputs(req.getParameter("pcs_inputs"));
		workingPf.setPcs_comments(req.getParameter("pcs_comments"));
		// Dummy
		pfdao.finishProductionFeatureSetFinish(workingPf);
		
	}

	public void scanMaterial(SqlSession conn, String material_id, HttpServletRequest req, List<MsgInfo> errors,
			Map<String, Object> listResponse) throws Exception {
		PositionPanelService ppService = new PositionPanelService();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String section_id = user.getSection_id();// TODO
		user.setSection_id(null);
//		user.setPosition_id("00000000047");
//		user.setProcess_code("711");
//		user.setLine_id("00000000011");

		// 判断维修对象已经完成出货
		ProductionFeatureMapper pfMapper = conn.getMapper(ProductionFeatureMapper.class);
		if (pfMapper.checkPositionDid(material_id, user.getPosition_id(), "2", null)) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("material_id");
			error.setErrcode("info.linework.shipped");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.shipped"));
			errors.add(error);
		} else {
		
			// 判断维修对象在等待区，并返回这一条作业信息
			ProductionFeatureEntity waitingPf = ppService.checkMaterialId(material_id, user, errors, conn);

			if (errors.size() == 0) {
				String fileContent = getProccessingData(listResponse, material_id, waitingPf, user, conn);
				listResponse.put("fileContent", fileContent);

				// 作业信息状态改为，作业中
				ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
				waitingPf.setOperator_id(user.getOperator_id());
				dao.startProductionFeature(waitingPf);
			}
		}

		user.setSection_id(section_id); // TODO
	}

	public String getProccessingData(Map<String, Object> listResponse, String material_id, ProductionFeatureEntity pf,
			LoginData user, SqlSession conn) throws Exception {
		// 取得维修对象信息。
		MaterialForm mform = getMaterialInfo(material_id, conn);
		listResponse.put("mform", mform);

		String templatePath = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\package\\" + mform.getModel_name() + ".htm";

		if (new File(templatePath).exists()) {
			return getContent(templatePath);
		}
		return null;
	}

	public MaterialForm getMaterialInfo(String material_id, SqlSession conn) {
		MaterialForm materialForm = new MaterialForm();
		ShippingMapper dao = conn.getMapper(ShippingMapper.class);
		MaterialEntity materialEntity = dao.getMaterialDetail(material_id);
		BeanUtil.copyToForm(materialEntity, materialForm, CopyOptions.COPYOPTIONS_NOEMPTY);

		return materialForm;
	}

	private String getContent(String filename) {
		File xmlfile = new File(filename);
		if (xmlfile.exists()) {
			if (xmlfile.isFile()) {
				BufferedReader input = null;
				try {
					input = new BufferedReader(new InputStreamReader(new FileInputStream(xmlfile),"UTF-8"));
					StringBuffer buffer = new StringBuffer();
					String text;

					while ((text = input.readLine()) != null)
						buffer.append(text);

					String content = buffer.toString();

					return content;
				} catch (IOException ioException) {
				} finally {
					try {
						input.close();
					} catch (IOException e) {
					}
					input = null;
				}
			}
		}
		return null;
	}

}
