package com.osh.rvsif.phenomenon;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;
import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.DateUtil;
import global.olympus.cna.rvs.rvs005.DT_RVS005;
import global.olympus.cna.rvs.rvs005.DT_RVS005_Return;
import global.olympus.cna.rvs.rvs005.InspectionResult_type0;
import global.olympus.cna.rvs.rvs005.MT_RVS005;
import global.olympus.cna.rvs.rvs005.Monitoring;
import global.olympus.cna.rvs.rvs005.RVS005_RVS2SFDC_Syn_OutServiceStub;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvsif.common.CommonMapper;
import com.osh.rvsif.common.bean.IfSapMessageContentEntity;
import com.osh.rvsif.common.bean.IfSapMessageEntity;
import com.osh.rvsif.common.mapper.IfSapMessageContentMapper;
import com.osh.rvsif.common.mapper.IfSapMessageMapper;
import com.osh.rvsif.phenomenon.bean.NewPhenomenonEntity;
import com.osh.rvsif.phenomenon.mapper.NewPhenomenonMapper;

public class SendNewPhenomenonTrigger extends HttpServlet {

	private static final long serialVersionUID = 9102066237003874576L;

	static Logger _logger = Logger.getLogger("TriggerServlet");
	private static String REMOTE_SERVICE = "http://10.220.4.81:8000/sap/bc/srt/xip/sap/yrvs004/200/rvs004/rvs004";
	private static String USER_NAME = "rvs_if";
	private static String PASSWORD = "abc123";
	private static String SUCCESS = "S";

	protected static JSON json = new JSON();
	static {
		json.setSuppressNull(true);
	}

	public void init(ServletConfig arg0) throws ServletException {
		String remoteService = arg0.getInitParameter("REMOTE_NEW_PHENON_SERVER");
		String userName = arg0.getInitParameter("USER_NAME");
		String password = arg0.getInitParameter("PASSWORD");
		String success = arg0.getInitParameter("SUCCESS");
		if (!isEmpty(remoteService)) REMOTE_SERVICE = remoteService;
		if (!isEmpty(userName)) USER_NAME = userName;
		if (!isEmpty(password)) PASSWORD = password;
		if (!isEmpty(success)) SUCCESS = success;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		RVS005_RVS2SFDC_Syn_OutServiceStub stub = new RVS005_RVS2SFDC_Syn_OutServiceStub(REMOTE_SERVICE);
		MT_RVS005 mT_RVS005 = new MT_RVS005();

		DT_RVS005 param = new DT_RVS005();
		InspectionResult_type0 arr = new InspectionResult_type0();
		arr.setSAPRepairNotificationNo("100222111");
		arr.setRVSDetailNo("OSH-SORC-00000011789");
		arr.setLocationGroupDesc("操作部");
		arr.setLocationDesc("EL管变形");
		arr.setDescription("-");
		arr.setDetermineDate("2020-09-17");
		arr.setDetermineTime("123001");
		arr.setDeterminePerson("DE110032");

		param.setInspectionResult(arr);

		mT_RVS005.setMT_RVS005(param);

		DT_RVS005_Return vaildResponse = stub.RVS005_RVS2SFDC_Syn_Out(mT_RVS005).getMT_RVS005_Return();
		_logger.info(vaildResponse.getStatus());
		_logger.info(vaildResponse.getErrItem());
		_logger.info(vaildResponse.getErrMsg());
	}

	protected void service(HttpServletRequest req, HttpServletResponse arg1) throws ServletException, IOException {

		String uri = req.getRequestURI();
		uri = uri.replaceFirst(req.getContextPath(), "");
		uri = uri.replaceFirst(req.getServletPath(), "");
		String addr = req.getRemoteAddr();
		_logger.info("Get finger on :" + uri + " from " + addr);

		// 只有本机可以访问
		if (!"0:0:0:0:0:0:0:1".equals(addr) && !"127.0.0.1".equals(addr)) {
			_logger.warn("推送只限服务器本机触发, but" + addr);
			// return;
		}

		String[] parameters = uri.split("\\/");
		if (parameters.length > 1) {
			String alarmMessageId = parameters[1];

			@SuppressWarnings("static-access")
			SqlSessionFactory factory = SqlSessionFactorySingletonHolder
					.getInstance().getFactory();
			SqlSessionManager conn = SqlSessionManager.newInstance(factory);
			conn.startManagedSession(ExecutorType.BATCH,
					TransactionIsolationLevel.REPEATABLE_READ);

			NewPhenomenonMapper sMapper = conn.getMapper(NewPhenomenonMapper.class);
			_logger.info("alarmMessageId=" + alarmMessageId);
			
			// 取得维修对象信息
			String omr_notifi_no = null;
			NewPhenomenonEntity newPhenomenon = null;
					
			try {
				newPhenomenon = sMapper.getNewPhenomenon(alarmMessageId);

				// 没有Phenomenon不发送
				if (newPhenomenon == null || newPhenomenon.getOmr_notifi_no() == null) {
					_logger.warn("Phenomenon not set for " + newPhenomenon);
					if (conn != null && conn.isManagedSessionStarted()) {
						conn.close();
					}
					conn = null;
					return;
				} else {
					omr_notifi_no = newPhenomenon.getOmr_notifi_no();
					_logger.info("Omr_notifi_no=" + omr_notifi_no);
				}
			} catch (Exception e) {
				_logger.error(e.getMessage(), e);
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.close();
				}
				conn = null;
				return;
			}

			// 设定提交对象
			DT_RVS005 param = new DT_RVS005();
			InspectionResult_type0 arr = new InspectionResult_type0();
			arr.setSAPRepairNotificationNo(CommonStringUtil.fillChar(omr_notifi_no, '0', 12, true));
			arr.setRVSDetailNo("OGZ-SORC-" + newPhenomenon.getKey());
			arr.setLocationGroupDesc(newPhenomenon.getLocation_group_desc());
			arr.setLocationDesc(newPhenomenon.getLocation_desc());
			arr.setDescription(newPhenomenon.getDescription());
			if (arr.getDescription() == null) {
				arr.setDescription("");
			}

			Calendar nowCal = Calendar.getInstance();
			Date now = nowCal.getTime();

			arr.setDetermineDate(DateUtil.toString(now, DateUtil.DATE_PATTERN_YYYYMMDD));
			arr.setDetermineTime(DateUtil.toString(now, DateUtil.TIME_PATTERN_HHMMSS));
			arr.setDeterminePerson(newPhenomenon.getJob_no());

			String prefix4MessageGroupNumber = "52" + nowCal.get(Calendar.YEAR);
			String lastMessageGroupNumber = sMapper.getLastMessageGroupNumber(prefix4MessageGroupNumber);
			if (lastMessageGroupNumber == null) {
				lastMessageGroupNumber = prefix4MessageGroupNumber + "00001";
			} else {
				String sSeq = lastMessageGroupNumber.substring(6);
				if (sSeq.equals("99999")) {
					lastMessageGroupNumber = prefix4MessageGroupNumber + "00001";
				} else {
					int iSeq = 1;
					try {
						iSeq = Integer.parseInt(sSeq);
					} catch (NumberFormatException e) {
						
					}
					lastMessageGroupNumber = prefix4MessageGroupNumber + 
							CommonStringUtil.fillChar("" + (iSeq + 1), '0', 5, true);
				}
			}

			Monitoring mon = new Monitoring();
			mon.setTag("MSGH");
			mon.setSender("RVS");
			mon.setReceiver("SFDC");
			mon.setMessageType("RVS005");
			mon.setMessageGroupNumber(lastMessageGroupNumber);
			mon.setNumberOfRecord("1");
			mon.setTransmissionDateTime(DateUtil.toString(now, "yyyyMMddHHmm"));
			mon.setText("");

			RVS005_RVS2SFDC_Syn_OutServiceStub stub = new RVS005_RVS2SFDC_Syn_OutServiceStub(REMOTE_SERVICE);

			// client.get 'application/x-www-form-urlencoded';
			// getLinkAuth
			MT_RVS005 mT_RVS005 = new MT_RVS005();
			param.setInspectionResult(arr);
			param.setMonitoring(mon);
			mT_RVS005.setMT_RVS005(param);

//			String value = sMapper.searchUserDefineCodes();
//			if ("0".equals(value)) {
//				return;
//			}

			// configure and engage Rampart
			ServiceClient client = stub._getServiceClient();

			Options options = client.getOptions();
			_logger.info("to=" + options.getTo().getAddress());
			_logger.info("action=" + options.getAction());

			options.setUserName(USER_NAME);
			options.setPassword(PASSWORD);
			HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();

			basicAuthentication.setUsername(USER_NAME);
			basicAuthentication.setPassword(PASSWORD);
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
					basicAuthentication);
			basicAuthentication.setPreemptiveAuthentication(true);

			OMFactory fac = OMAbstractFactory.getOMFactory();
			String tns = "http://cna.olympus.global/rvs/RVS005";
			OMNamespace omNs = fac.createOMNamespace(tns, "nsl");
			OMElement header = fac.createOMElement("AuthenticationToken", omNs);
			OMElement ome_user = fac.createOMElement("Username", omNs);
			OMElement ome_pass = fac.createOMElement("Password", omNs);
			ome_user.setText(USER_NAME);
			ome_pass.setText(PASSWORD);

			_logger.info(header.getNamespaceURI() + "|" + header.toString() + "|" + header.getLocalName() + "|" + header.getText());
			header.addChild(ome_user);
			header.addChild(ome_pass);

			client.addHeader(header);

			DT_RVS005_Return vaildResponse = null;
			try {
				vaildResponse = stub.RVS005_RVS2SFDC_Syn_Out(mT_RVS005)
						.getMT_RVS005_Return();
			} catch (Exception e) {
				vaildResponse = new DT_RVS005_Return();
				vaildResponse.setStatus("EX") ;
				vaildResponse.setErrItem(e.getClass().getName());
				vaildResponse.setErrMsg(e.getMessage());
				if (e.getMessage().length() > 125) {
					vaildResponse.setErrMsg(e.getMessage().substring(0, 120));
				}
				_logger.error(e.getMessage(), e);
			}

			// 保存处理结果
			try {
				IfSapMessageMapper mapper = conn.getMapper(IfSapMessageMapper.class);
				IfSapMessageEntity entity = new IfSapMessageEntity();
				entity.setForward(2);
				entity.setKind("NewPhenomenon");
				entity.setResponse_message(json.format(vaildResponse));
				entity.setCheck_status((SUCCESS.equals(vaildResponse.getStatus()) ? 1 : 0));
				mapper.insert(entity);

				CommonMapper commonMapper = conn.getMapper(CommonMapper.class);
				String if_sap_message_key = commonMapper.getLastInsertID();

				IfSapMessageContentMapper contentMapper = conn.getMapper(IfSapMessageContentMapper.class);
				IfSapMessageContentEntity contentEntity = null;

				contentEntity = new IfSapMessageContentEntity();
				contentEntity.setIf_sap_message_key(Integer.parseInt(if_sap_message_key));
				contentEntity.setSeq(1);
				contentEntity.setContent(json.format(mT_RVS005));
				contentEntity.setResolved(3);
				contentEntity.setInvalid_message(vaildResponse.getErrMsg());
				contentMapper.insert(contentEntity);

				// 更新RVS中发送状态
				newPhenomenon.setReturn_status(vaildResponse.getStatus());
				newPhenomenon.setLast_sent_message_number(lastMessageGroupNumber);
				sMapper.setReturnStatus(newPhenomenon);

				conn.commit();
			} catch (Exception e) {
				_logger.info(e.getMessage(), e);
				conn.rollback();
			} finally {
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.close();
				}
				conn = null;
			}
		}
	}
}
