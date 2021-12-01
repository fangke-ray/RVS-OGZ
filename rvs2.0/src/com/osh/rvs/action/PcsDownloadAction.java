package com.osh.rvs.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.ZipUtility;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.service.DownloadService;
import com.osh.rvs.service.qa.QualityAssuranceService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.common.util.CommonStringUtil;

public class PcsDownloadAction extends BaseAction {

	private static Logger logger = Logger.getLogger("Download");
	DownloadService service = new DownloadService();

	/**
	 * 作业完成
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void file(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("PcsDownloadAction.file start");

		String material_id = req.getParameter("material_id");
		String getHistory = req.getParameter("get_history");

		QualityAssuranceService service = new QualityAssuranceService();
		if (!CommonStringUtil.isEmpty(material_id)) {
			MaterialForm mform = service.getMaterialInfo(material_id, conn);
			String sorcNo = mform.getSorc_no();
			String subPath = "";
			if (sorcNo== null) { // If Manuf
				String finishDate = mform.getOutline_time();
				if (finishDate == null) finishDate = mform.getFinish_time();
				subPath = "MA" + mform.getModel_name().substring(0, 2) + "-" + finishDate.substring(1, 3) + mform.getSerial_no() + "________";
			} else if (sorcNo.length() < 8)
				subPath = "SAPD-" + sorcNo + "________";
			else if (sorcNo.length() == 8)
				subPath = "OMRN-" + sorcNo + "________";
			else 
				subPath = sorcNo;
			String sub8 = subPath.substring(0, 8);

			String packFilename = sorcNo;
			if (packFilename == null) packFilename = mform.getSerial_no();
			String folderPath = PathConsts.BASE_PATH + PathConsts.PCS + "\\" + sub8 + "\\" + packFilename;

			// 工程检查票Pdf生成
			service.makePdf(mform, folderPath, (getHistory != null), conn);

//			MaterialPartialService mpService = new MaterialPartialService();
//			try {
//				mpService.createArchireOfPartialRecept(mform, folderPath, conn);
//			}catch(Exception e) {
//				// 防错
//				logger.error(e.getMessage(), e);
//			}
			// 打包
			ZipUtility.zipper(folderPath, folderPath + ".zip", "UTF-8");

			// TODO  FileUtils.removeDir();

		}

		Map<String, Object> callback = new HashMap<String, Object>();
		// 返回Json格式响应信息
		returnJsonResponse(res, callback);

		log.info("PcsDownloadAction.file end");
	}
}
