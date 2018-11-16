package com.osh.rvs.service.qa;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.qa.QualityAssuranceMapper;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

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
		String[] showLines = new String[5];
		showLines[0] = "最终检验";
		showLines[1] = "分解工程";
		showLines[2] = "NS 工程";
		showLines[3] = "总组工程";
		showLines[4] = "外科硬镜修理工程";

		MaterialService mService = new MaterialService();

		for (String showLine : showLines) {
			Map<String, String> fileTempl = PcsUtils.getXlsContents(showLine, mform.getModel_name(), null, mform.getMaterial_id(), getHistory, conn);

			if ("NS 工程".equals(showLine))
				mService.filterSolo(fileTempl, mform.getMaterial_id(), conn);

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
	 * 
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public void listRefresh(Map<String, Object> listResponse, String position_id, SqlSession conn) {
		QualityAssuranceMapper qDao = conn.getMapper(QualityAssuranceMapper.class);
		// 取得待品保处理对象一览 611
		List<MaterialEntity> waitings = qDao.getWaitings(position_id);

		// 取得今日已完成处理对象一览
		List<MaterialEntity> finished = qDao.getFinished(position_id);

		List<MaterialForm> waitingsForms = new ArrayList<MaterialForm>();
		List<MaterialForm> finishedForm = new ArrayList<MaterialForm>();

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
		
		
		BeanUtil.copyToFormList(finished, finishedForm, CopyOptions.COPYOPTIONS_NOEMPTY, MaterialForm.class);

		listResponse.put("waitings", waitingsForms);
		listResponse.put("finished", finishedForm);

	}

	/**
	 * 返回品保展示月份
	 * 
	 * @param selectedMonth
	 * @return
	 */
	public Map<String, Object> getYearMonthByDate(Calendar selectedMonth) {
		Map<String, Object> ret = new HashMap<String, Object>();
		int year = selectedMonth.get(Calendar.YEAR);

		ret.put("yOptions", "<option value=''/><option value='" + (year - 1) + "'>" + (year - 1)
				+ "</option><option value='" + year + "'>" + year + "</option>");

		List<String> years = new ArrayList<String>();
		List<String> months = new ArrayList<String>();
		RvsUtils.getMonthAxisInNatualYear(selectedMonth.getTime(), true, true, years, months);

		String mOptions = "<option value=''/>";
		for (String month : months) {
			mOptions += "<option value='" + month + "'>" + month + "月</option>";
		}
		ret.put("mOptions", mOptions);

		int sMonth = (selectedMonth.get(Calendar.MONTH) + 1);
		ret.put("sMonth", year + "年" + sMonth + "月（"
						+ RvsUtils.getBussinessHalfYearString(selectedMonth) + "）");
		ret.put("yearMonthValue", year + (sMonth >= 10 ? ""+sMonth : "0"+sMonth) );

		return ret;
	}

    SimpleDateFormat objSimpleDateFormat = new SimpleDateFormat("M月");

    SimpleDateFormat dfYm = new SimpleDateFormat("yyyyMM");

	/**
	 * 触发SAP修理完成接口
	 * @param material_id
	 */
	public void notifiSapShipping(String material_id) {

		// SAP同步
		String urlString = "http://localhost:8080/rvsIf/shipping/" + material_id;
		try {
			URL url = new URL(urlString);
			url.getQuery();
			URLConnection urlconn = url.openConnection();
			urlconn.setReadTimeout(1); // 不等返回
			urlconn.connect();
			urlconn.getContentType(); // 这个就能触发
		} catch (Exception e) {
			_log.error("Failed", e);
		}
	}
}
