package com.osh.rvs.service.qa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.qa.QualityAssuranceMapper;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;

public class QualityAssuranceService {
	/**
	 * 得到维修对象信息 For 品保
	 * 
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public MaterialForm getMaterialInfo(String material_id, SqlSession conn) {
		MaterialForm materialForm = new MaterialForm();
		QualityAssuranceMapper dao = conn.getMapper(QualityAssuranceMapper.class);
		MaterialEntity materialEntity = dao.getMaterialDetail(material_id);
		BeanUtil.copyToForm(materialEntity, materialForm, CopyOptions.COPYOPTIONS_NOEMPTY);

		return materialForm;
	}

	private static Logger _log = Logger.getLogger(QualityAssuranceService.class);

	/**
	 * 工程检查票Pdf制造
	 * 
	 * @param mform
	 * @param folderPath 
	 * @param conn
	 * @throws IOException
	 */
	public void makePdf(MaterialForm mform, String folderPath, boolean getHistory, SqlSession conn) throws IOException {
		String[] showLines = {};

		if ("0".equals(mform.getLevel())) {
			showLines = new String[2];
			showLines[0] = "出荷检查表";
			showLines[1] = "检查工程";
		} else {
			showLines = new String[6];
			showLines[0] = "检查卡";
			showLines[1] = "最终检验";
			showLines[2] = "外科硬镜修理工程";
			showLines[3] = "分解工程";
			showLines[4] = "NS 工程";
			showLines[5] = "总组工程";
		}

		MaterialService mService = new MaterialService();

		for (String showLine : showLines) {
			Map<String, String> fileTempl = PcsUtils.getXlsContents(showLine, mform.getModel_name(), null, mform.getMaterial_id(), getHistory, conn);

			if ("NS 工程".equals(showLine))
				mService.filterSolo(fileTempl, mform.getMaterial_id(), mform.getLevel() ,conn);
			if ("总组工程".equals(showLine)) MaterialService.filterLight(fileTempl, mform.getMaterial_id(), mform.getLevel(), conn);

			String retEmpty = PcsUtils.toPdf(fileTempl, mform.getMaterial_id(), mform.getSorc_no(), mform.getModel_name(),
					mform.getSerial_no(), mform.getLevel(), null, folderPath, conn);
			if (retEmpty!= null && retEmpty.length() > 0) {
				Logger logger = Logger.getLogger("Download");
				logger.info(retEmpty + " MODEL: " + mform.getModel_name() + " PAT:" + mform.getPat_id());
			}
		}
	}

	/**
	 * 得到作业信息 For 品保
	 * 
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public boolean getProccessingData(Map<String, Object> listResponse, String material_id, ProductionFeatureEntity pf,
			LoginData user, SqlSession conn) throws Exception {
		// 取得维修对象信息。
		MaterialForm mform = getMaterialInfo(material_id, conn);
		listResponse.put("mform", mform);
		if (mform.getQa_check_time() == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 品保结果一览更新
	 * @param department 
	 * 
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public void listRefresh(Map<String, Object> listResponse, String position_id, Integer department, SqlSession conn) {
		QualityAssuranceMapper qDao = conn.getMapper(QualityAssuranceMapper.class);
		List<MaterialForm> waitingsForms = new ArrayList<MaterialForm>();
		List<MaterialForm> finishedForm = new ArrayList<MaterialForm>();

		// 取得待品保处理对象一览 611
		List<MaterialEntity> waitings = null;
		

		// 取得今日已完成处理对象一览
		List<MaterialEntity> finished = null;
		

		if (department.equals(RvsConsts.DEPART_MANUFACT)) {
			waitings = qDao.getManufatorWaitings(position_id);
			finished = qDao.getManufatorFinished(position_id);

			CopyOptions cos = new CopyOptions();
			cos.excludeNull();
			cos.dateConverter(DateUtil.ISO_DATE_TIME_PATTERN, "inline_time", "finish_time");
			BeanUtil.copyToFormList(waitings, waitingsForms, cos, MaterialForm.class);
		} else {
			waitings = qDao.getWaitings(position_id);
			finished = qDao.getFinished(position_id);

			for (MaterialEntity waiting : waitings) {
				MaterialForm waitingsForm = new MaterialForm();
				BeanUtil.copyToForm(waiting, waitingsForm, CopyOptions.COPYOPTIONS_NOEMPTY);
				String comment = "";
				if (waiting.getDirect_flg() != null && waiting.getDirect_flg()==1) {
					comment += "直送";
				}
				if (waiting.getService_repair_flg() != null) {
					comment += CodeListUtils.getValue("material_service_repair", ""+waiting.getService_repair_flg());
				}
				waitingsForm.setStatus(comment);
				waitingsForms.add(waitingsForm);
			}
		}

		BeanUtil.copyToFormList(finished, finishedForm, CopyOptions.COPYOPTIONS_NOEMPTY, MaterialForm.class);

		listResponse.put("waitings", waitingsForms);
		listResponse.put("finished", finishedForm);

	}
}
