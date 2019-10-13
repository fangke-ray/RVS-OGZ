/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：维修对象机种系统管理事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action.qf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.service.DownloadService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.ProductionFeatureService;
import com.osh.rvs.service.qf.AcceptanceService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;


public class AcceptanceAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	private AcceptanceService service = new AcceptanceService();
	private ModelService modelService = new ModelService();
	private ProductionFeatureService featureService = new ProductionFeatureService();
	
	/**
	 * 受理画面初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={100})
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("AcceptanceAction.init start");
		
		String mReferChooser = modelService.getRepairOptions(conn);
		req.getSession().setAttribute("mReferChooser", mReferChooser);

		// 配送区域信息
		req.setAttribute("g_bound_out_ocm", CodeListUtils.getGridOptions("material_direct_ocm"));
		req.setAttribute("g_area", CodeListUtils.getGridOptions("material_large_area"));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("AcceptanceAction.init end");
	}

	/**
	 * 受理添加记录实行处理
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={107})
	public void doinsert(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("AcceptanceAction.doinsert start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		v.delete("level", "ocm", "ocm_rank", "area", "bound_out_ocm");
		v.add("level", v.integerType());
		v.add("ocm", v.integerType());
		v.add("ocm_rank", v.integerType());
		v.add("area", v.integerType());
		v.add("bound_out_ocm", v.integerType());
		List<MsgInfo> errors = v != null ? v.validate(): new ArrayList<MsgInfo>();
		MaterialForm materialForm = ((MaterialForm)form);
		String id = materialForm.getMaterial_id();
		MaterialService mservice = new MaterialService();

		mservice.checkModelDepacy(materialForm, conn, errors);

		// 手工处理维修编号可与已修品重复
		materialForm.setTicket_flg("1");
		mservice.checkRepeatNo(id, form, conn, errors);
		
		if (errors.size() == 0) {
			if ("".equals(id) || id == null) {
				service.insert(form, req.getSession(), conn, errors);
			} else {
				service.update(form, req.getSession(), conn, errors);
			}
		}
		
		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);
		
		log.info("AcceptanceAction.doinsert end");
	}

	/**
	 * 批量数据实际导入
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={107})
	public void doimport(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("AcceptanceAction.doimport start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		service.batchinsert(req.getParameterMap(), req.getSession(), conn, errors);

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);
		
		log.info("AcceptanceAction.doimport end");
	}	
	/**
	 * 取得维修对象一览信息
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={100})
	public void loadData(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("AcceptanceAction.loadData start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MaterialForm> lResultForm = service.getMaterialDetail(conn);
		
		// 查询结果放入Ajax响应对象
		listResponse.put("list", lResultForm);
		
		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("AcceptanceAction.loadData end");
	}
	
	/**
	 * 实物受理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={107})
	public void doAccept(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("AcceptanceAction.doAccept start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		service.accept(req.getParameterMap(), req.getSession(), conn, errors);

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);
		
		log.info("AcceptanceAction.doAccept end");
	}

	/**
	 * 发送至消毒
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={107})
	public void doDisinfection(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		String ids = req.getParameter("ids");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String section_id = user.getSection_id();

		String[] split = ids.split(",");
		for (int i = 0; i < split.length; i++) {
			ProductionFeatureEntity entity = new ProductionFeatureEntity();
			entity.setMaterial_id(split[i]);
			entity.setPosition_id("10");
			entity.setSection_id(section_id);
			entity.setPace(0);
			entity.setOperate_result(0);
			entity.setRework(0);
			featureService.insert(entity, conn);
			
			// 更新受理（111）工位完成时间
			entity = new ProductionFeatureEntity();
			entity.setMaterial_id(split[i]);
			entity.setPosition_id(RvsConsts.POSITION_ACCEPTANCE);
			entity.setPace(0);
			entity.setSection_id("00000000001");
			entity.setRework(0);
			featureService.updateFinishTime(entity, conn);
		}

		// 发送完毕后，受理时间覆盖导入时间
		service.updateFormalReception(split, conn);
	}
	
	/**
	 * 发送至灭菌
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={107})
	public void doSterilization(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		String ids = req.getParameter("ids");
	
		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String section_id = user.getSection_id();

		String[] split = ids.split(",");
		for (int i = 0; i < split.length; i++) {
		
			ProductionFeatureEntity entity = new ProductionFeatureEntity();
			entity.setMaterial_id(split[i]);
			entity.setPosition_id("11");
			entity.setPace(0);
			entity.setSection_id(section_id);
			entity.setOperate_result(0);
			entity.setRework(0);
			featureService.insert(entity, conn);
			
			// 更新受理（111）工位完成时间
			entity = new ProductionFeatureEntity();
			entity.setMaterial_id(split[i]);
			entity.setPosition_id(RvsConsts.POSITION_ACCEPTANCE);
			entity.setPace(0);
			entity.setSection_id("00000000001");
			entity.setRework(0);
			featureService.updateFinishTime(entity, conn);
		}

		// 发送完毕后，受理时间覆盖导入时间
		service.updateFormalReception(split, conn);
	}

	/**
	 * 小票打印
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={100})
	public void doPrintTicket(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{ 
		log.info("AcceptanceAction.printTicket start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 取得所选维修对象信息
		MaterialService mService = new MaterialService();
		List<String> ids = mService.getIds(req.getParameterMap());
		List<MaterialEntity> mBeans = mService.loadMaterialDetailBeans(ids, conn);

		DownloadService dService = new DownloadService();
		String filename = dService.printTickets(mBeans, conn);
		callbackResponse.put("tempFile", filename);

		// 更新维修对象小票打印标记
		MaterialMapper mdao = conn.getMapper(MaterialMapper.class);
		mdao.updateMaterialTicket(ids);

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("AcceptanceAction.printTicket end");
	}

	/**
	 * 不修理发还
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={107})
	public void doReturn(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{ 
		log.info("AcceptanceAction.return start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 取得所选维修对象信息
		MaterialService mService = new MaterialService();
		List<String> ids = mService.getIds(req.getParameterMap());

		// 受理处返还，不需要出货
		service.acceptReturn(form, ids, conn);
		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("AcceptanceAction.return end");
	}
	
}
