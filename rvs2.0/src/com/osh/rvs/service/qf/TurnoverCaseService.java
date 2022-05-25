package com.osh.rvs.service.qf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.qf.TurnoverCaseEntity;
import com.osh.rvs.form.qf.TurnoverCaseForm;
import com.osh.rvs.mapper.qf.TurnoverCaseMapper;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class TurnoverCaseService {

	public List<TurnoverCaseForm> searchTurnoverCase(ActionForm form,
			SqlSession conn) {
		TurnoverCaseEntity condition = new TurnoverCaseEntity();
		BeanUtil.copyToBean(form, condition, CopyOptions.COPYOPTIONS_NOEMPTY);

		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		List<TurnoverCaseEntity> result = mapper.searchTurnoverCase(condition);

		List<TurnoverCaseForm> lcf = new ArrayList<TurnoverCaseForm>();
		BeanUtil.copyToFormList(result, lcf, 
				CopyOptions.COPYOPTIONS_NOEMPTY, TurnoverCaseForm.class);

		return lcf;
	}

	public List<String> getStorageHeaped(ActionForm form, SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		return mapper.getStorageHeaped();
	}

	public void changelocation(SqlSessionManager conn, String material_id,
			String location) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);

		boolean putinByPda = false;
		if (location.endsWith("+")) {
			location = location.substring(0, location.length() - 1);
			putinByPda = true;
		}
		// 寻找原来位置
		TurnoverCaseEntity condition = new TurnoverCaseEntity();
		condition.setMaterial_id(material_id);
		List<TurnoverCaseEntity> result = mapper.searchTurnoverCase(condition);

		TurnoverCaseEntity updEntity = new TurnoverCaseEntity();
		if (result.size() == 0) {
			updEntity.setMaterial_id(material_id);
			updEntity.setStorage_time(new Date());
			updEntity.setExecute(0);
			updEntity.setLocation(location);
			mapper.putin(updEntity);
		} else {
			updEntity = result.get(0);
			mapper.warehousing(updEntity.getLocation());
			updEntity.setLocation(location);
			if (putinByPda) {
				updEntity.setStorage_time(new Date());
				updEntity.setExecute(1);
			}
			mapper.putin(updEntity);
		}
	}

	public void warehousing(SqlSessionManager conn, String location) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		
		mapper.warehousing(location);
	}

	public TurnoverCaseEntity getStorageByMaterial(String material_id, SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		TurnoverCaseEntity condition = new TurnoverCaseEntity();
		condition.setMaterial_id(material_id);
		List<TurnoverCaseEntity> result = mapper.searchTurnoverCase(condition);
		if (result.size() == 0) {
			return null;
		} else {
			return result.get(0);
		}
	}

	public List<TurnoverCaseForm> getWarehousingPlanList(SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		List<TurnoverCaseEntity> result = mapper.getWarehousingPlan();

		List<TurnoverCaseForm> lcf = new ArrayList<TurnoverCaseForm>();
		for (TurnoverCaseEntity turnoverCaseEntity :result) {
			TurnoverCaseForm turnoverCaseForm = new TurnoverCaseForm();

			BeanUtil.copyToForm(turnoverCaseEntity, turnoverCaseForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			turnoverCaseForm.setBound_out_ocm(CodeListUtils.getValue("material_direct_ocm", "" + turnoverCaseEntity.getBound_out_ocm()));

			lcf.add(turnoverCaseForm);
		}

		return lcf;
	}

	public List<String> warehousing(SqlSessionManager conn,
			Map<String, String[]> parameterMap) {
		List<String> locations = new AutofillArrayList<String> (String.class); 
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String table = m.group(1);
				if ("turnover_case".equals(table)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameterMap.get(parameterKey);
					if ("location".equals(column)) {
						locations.set(icounts, value[0]);
					}
				}
			}
		}

		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		for (String location : locations) {
			mapper.warehousing(location);
		}

		return locations;
	}

	/**
	 * 取得待入库镜箱一览
	 * @param conn
	 * @return
	 */
	public List<TurnoverCaseForm> getStoragePlanList(SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		List<TurnoverCaseEntity> result = mapper.getStoragePlan();

		List<TurnoverCaseForm> lcf = new ArrayList<TurnoverCaseForm>();

		for (TurnoverCaseEntity turnoverCaseEntity :result) {
			TurnoverCaseForm turnoverCaseForm = new TurnoverCaseForm();

			BeanUtil.copyToForm(turnoverCaseEntity, turnoverCaseForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			turnoverCaseForm.setBound_out_ocm(CodeListUtils.getValue("material_direct_ocm", "" + turnoverCaseEntity.getBound_out_ocm()));

			lcf.add(turnoverCaseForm);
		}
		return lcf;
	}

	public void checkStorage(SqlSessionManager conn, String location) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		mapper.checkStorage(location);
	}

	public List<String> checkStorage(SqlSessionManager conn,
			Map<String, String[]> parameterMap) {
		List<String> locations = new AutofillArrayList<String> (String.class); 
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String table = m.group(1);
				if ("turnover_case".equals(table)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameterMap.get(parameterKey);
					if ("location".equals(column)) {
						locations.set(icounts, value[0]);
					}
				}
			}
		}

		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		for (String location : locations) {
			mapper.checkStorage(location);
		}

		return locations;
	}

	public TurnoverCaseEntity getEntityByLocation(String location,
			SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		return mapper.getEntityByLocation(location);
	}

	public TurnoverCaseEntity getEntityByLocationForStorage(String location,
			SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		return mapper.getEntityByLocationForStorage(location);
	}

	public TurnoverCaseEntity getEntityByLocationForShipping(String location,
			SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		return mapper.getEntityByLocationForShipping(location);
	}

	public String getShelfMap(String shelf, List<TurnoverCaseForm> planListOnShelf, SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		List<TurnoverCaseEntity> list = mapper.getListOnShelf(shelf);

		StringBuffer sb = new StringBuffer();
		Integer comLayer = null;
		sb.append("<tr>");
		for (TurnoverCaseEntity entity : list) {
			Integer iLayer = entity.getLayer();
			if (comLayer != iLayer) {
				if (comLayer != null) sb.append("</tr><tr>");
				comLayer = iLayer;
			}
//			if ("M".equals(shelf) && iLayer > 1) {
//				sb.append("<td colspan=\"3\" location=\""); // 不规则M TODO
//			} else {
				sb.append("<td location=\"");
//			}
			sb.append(entity.getLocation());
			sb.append("\" class=\"");
			Date storage_time = entity.getStorage_time();
			if (storage_time == null) {
				sb.append("status-empty");
			} else if (storage_time.before(entity.getStorage_time_start())) {
				sb.append("status-overtime");
			} else {
				sb.append("status-storaged");
			}

			// 对象
			for (TurnoverCaseForm planTcForm : planListOnShelf) {
				if (entity.getLocation().equals(planTcForm.getLocation())) {
					sb.append(" mapping");
					break;
				}
			}
			sb.append("\">■</td>");
		}
		sb.append("</tr>");
		return sb.toString();
	}

	public List<TurnoverCaseForm> filterOnShelf(
			List<TurnoverCaseForm> storagePlanList, String shelf) {
		List<TurnoverCaseForm> ret = 		new ArrayList<TurnoverCaseForm>();
		for (TurnoverCaseForm storagePlan : storagePlanList) {
			if (shelf.equals(storagePlan.getShelf())) {
				ret.add(storagePlan);
			}
		}
		return ret;
	}

	public List<TurnoverCaseForm> getIdleMaterialList(SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		List<TurnoverCaseEntity> list = mapper.getIdleMaterialList();

		List<TurnoverCaseForm> lcf = new ArrayList<TurnoverCaseForm>();
		BeanUtil.copyToFormList(list, lcf, CopyOptions.COPYOPTIONS_NOEMPTY, TurnoverCaseForm.class);

		return lcf;
	}

	public void putinManual(String location, String material_id, SqlSessionManager conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		TurnoverCaseEntity entity = new TurnoverCaseEntity();
		entity.setLocation(location);
		entity.setMaterial_id(material_id);
		entity.setExecute(1);
		entity.setStorage_time(new Date());
		mapper.putin(entity);
	}

	public TurnoverCaseEntity checkEmptyLocation(String location,
			SqlSessionManager conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		return mapper.checkEmpty(location);
	}

	public void triggerUndoStorage(String material_id) throws IOReactorException, InterruptedException {
		HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
		httpclient.start();
		try {  
			HttpGet request = new HttpGet("http://localhost:8080/rvspush/trigger/assign_tc_space/" 
				+ material_id + "/UNDO/");
			httpclient.execute(request, null);
		} catch (Exception e) {
		} finally {
			Thread.sleep(80);
			httpclient.shutdown();
		}
	}

	public List<TurnoverCaseEntity> getTrolleyStacks(SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		return mapper.getTrolleyStacks();
	}

	private static final String TYPE_SHELF = "SHELF";
	private static final String TYPE_LOCATION = "LOCATION";
//	private static final String TYPE_NORMAL_LAYER = "NORMAL_LAYER";

	private static Map<String, List<TurnoverCaseEntity>> locationNormalSets = new HashMap<String, List<TurnoverCaseEntity>>();
	private static Map<String, List<TurnoverCaseEntity>> locationEndoeyeSets = new HashMap<String, List<TurnoverCaseEntity>>();

	/**
	 * 连续取得空置的通箱库位
	 * 
	 * @param kind 6 = Endoeye
	 * @param count 取得数量
	 * @param realPutin 实际分配
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public void getEmptyLocations(String kind, List<String> ret, int count, boolean realPutin,
			SqlSession conn, boolean bHitAgain) throws Exception {
//		List<String> ret = new ArrayList<String>();

		boolean isEndoeye = kind.equals("06");

		Map<String, List<TurnoverCaseEntity>> locationSets = null;

		if (isEndoeye) {
			locationSets = locationEndoeyeSets;
		} else {
			locationSets = locationNormalSets;
		}

		synchronized (locationSets) {
			String todayString = DateUtil.toString(new Date(), DateUtil.DATE_PATTERN);
			if (!locationSets.containsKey(todayString)) {
				List<TurnoverCaseEntity> locationSetsToday = getLocationSetsToday(kind, conn);
				locationSets.put(todayString, locationSetsToday);
			}

			List<TurnoverCaseEntity> locationSetsToday = locationSets.get(todayString);

			for (TurnoverCaseEntity shelf : locationSetsToday) {
				if (shelf.getLocation() != null) {
					List<String> retShelf = getEmptyLocation(shelf.getShelf(), conn);
					ret.addAll(retShelf);
					if (ret.size() > count) {
						break;
					}
				}
			}

			if (ret.size() < count) {
				if (bHitAgain) {
					throw new Exception("递归安排也无法找到库位！");
				}
				// 重新计算空余位置并且递归安排
				getEmptyLocations(kind, ret, count, realPutin, conn, true);
			}
		}
	}

	private List<TurnoverCaseEntity> getLocationSetsToday(String kind, SqlSession conn) throws Exception {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);

		if (kind == null || (!kind.equals("06"))) {
			List<TurnoverCaseEntity> storageEmptyList = mapper.countNowStorageEmpty();
			int length = storageEmptyList.size();

			if (length > 3) {
				TurnoverCaseEntity deepClone = new TurnoverCaseEntity();
				deepClone.setShelf(storageEmptyList.get(0).getShelf());
				deepClone.setExecute(storageEmptyList.get(0).getExecute());
				storageEmptyList.add(deepClone);

				deepClone = new TurnoverCaseEntity();
				deepClone.setShelf(storageEmptyList.get(1).getShelf());
				deepClone.setExecute(storageEmptyList.get(1).getExecute());
				storageEmptyList.add(deepClone);

				int maxEmptyCnt = 0; int maxCursor = -1;
				for (int i = 0 ;i < length; i++) {
					int iCnt = storageEmptyList.get(i).getExecute() + storageEmptyList.get(i+1).getExecute() + storageEmptyList.get(i+2).getExecute() ;
					if (iCnt > maxEmptyCnt) {
						maxEmptyCnt = iCnt;
						maxCursor = i;
					}
				}

				storageEmptyList.remove(length); storageEmptyList.remove(length);
				for (int i = 0; i < maxCursor; i++) {
					storageEmptyList.add(storageEmptyList.remove(0));
				}

			}

			return storageEmptyList;

//			// 取得非Endoeye
//			String mostSpacialShelf = mapper.getFirstSpaceShelf("01", shelf); 
//
//			String startLocation = null;
//			if (mostSpacialShelf == null) {
//				if (shelf != null) {
//					mostSpacialShelf = mapper.getFirstSpaceShelf("01", null);
//				}
//
//				if (mostSpacialShelf == null) {
//					throw new Exception("通箱（非Endoeye）库位满了！");
//				}
//			} else {
//				startLocation = mapper.getFirstSpaceInShelf(mostSpacialShelf, null, null); // (mostSpacialShelf, "1", null)
//				result.put(TYPE_NORMAL_LAYER, "1");
//			}
//			result.put(TYPE_SHELF, mostSpacialShelf);
//			result.put(TYPE_LOCATION, startLocation);
		}

		if (kind == null || kind.equals("06")) {
//			// 取得Endoeye
//			String mostSpacialShelf = mapper.getMostSpacialShelf(06, null);
//			String startLocation = null;
//			if (mostSpacialShelf == null) {
//				throw new Exception("通箱（Endoeye）库位满了！");
//			} else {
//				startLocation = mapper.getFirstSpaceInShelf(mostSpacialShelf, null, null); // (mostSpacialShelf, "1", null)
//			}
//			result.put(TYPE_SHELF, mostSpacialShelf);
//			result.put(TYPE_LOCATION, startLocation);
		}

		return new ArrayList<TurnoverCaseEntity>();
	}

	private List<String> getEmptyLocation(String shelf, SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		return mapper.getSpaceInShelf(shelf); // (shelf, layer, locationStart)
	}

	/**
	 * 更新推车放置
	 * 
	 * @param parameterMap
	 * @param conn
	 */
	public void trolleyUpdate(Map<String, String[]> parameterMap,
			SqlSessionManager conn) {
		List<TurnoverCaseEntity> list = new AutofillArrayList<TurnoverCaseEntity> (TurnoverCaseEntity.class); 
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String table = m.group(1);
				if ("trolley_stack".equals(table)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameterMap.get(parameterKey);
					if ("trolley_code".equals(column)) {
						list.get(icounts).setTrolley_code(value[0]);
					} else if ("layer".equals(column)) {
						list.get(icounts).setLayer(Integer.parseInt(value[0]));
					} else if ("material_id".equals(column)) {
						list.get(icounts).setMaterial_id(value[0]);
					}
				}
			}
		}		

		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		mapper.removeTrolleyStacks();
		mapper.insertTrolleyStacks(list);
	}

	public String assignLocation(Map<String, String[]> parameterMap,
			SqlSessionManager conn) throws Exception {
		String retMessage = "";

		List<TurnoverCaseEntity> list = new AutofillArrayList<TurnoverCaseEntity> (TurnoverCaseEntity.class); 
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String table = m.group(1);
				if ("assign_location".equals(table)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameterMap.get(parameterKey);
					if ("location".equals(column)) {
						list.get(icounts).setLocation(value[0]);
					} else if ("material_id".equals(column)) {
						list.get(icounts).setMaterial_id(value[0]);
					} else if ("kind_key".equals(column)) {
						list.get(icounts).setKey(value[0]);
					}
				}
			}
		}		

		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		String todayString = DateUtil.toString(new Date(), DateUtil.DATE_PATTERN);

		for (TurnoverCaseEntity tcEntity : list) {
			// 判断维修品是否已经分配或入库
			TurnoverCaseEntity maEntity = new TurnoverCaseEntity();
			maEntity.setMaterial_id(tcEntity.getMaterial_id());
			if (mapper.searchTurnoverCase(maEntity).size() > 0) {
				tcEntity.setExecute(-1);
			} else {
				// 判断库位是否已分配
				TurnoverCaseEntity entity = mapper.checkEmpty(tcEntity.getLocation());
				if (entity != null) {
					tcEntity.setExecute(0);
					mapper.putin(tcEntity);
					//checkLocationInDailyPlan(todayString, tcEntity.getLocation(), conn);
					checkLocationInDailyPlanSet(todayString, tcEntity.getKey(), tcEntity.getLocation(), conn);

					// 清除推车
					mapper.clearTrolleyStacks(tcEntity.getMaterial_id());
				}
			}
		}

		for (TurnoverCaseEntity tcEntity : list) {
			MaterialService mService = new MaterialService();

			// 已经占用的重新分配并且加入提示
			if (tcEntity.getExecute() == null) {
				MaterialEntity mBean = mService.loadSimpleMaterialDetailEntity(conn, tcEntity.getMaterial_id());

				Map<String, Set<String>> kindAgreeEmptyLocations = getKindAgreeEmptyLocations(1, conn);
				Set<String> emptyLocations = kindAgreeEmptyLocations.get(tcEntity.getKey());
				if (emptyLocations.isEmpty()) {
					throwLeakException(tcEntity.getKey());
				}

				tcEntity.setExecute(0);
				String orgLocation = tcEntity.getLocation();
				tcEntity.setLocation(emptyLocations.iterator().next());
				mapper.putin(tcEntity);
				//checkLocationInDailyPlan(todayString, tcEntity.getLocation(), conn);
				checkLocationInDailyPlanSet(todayString, tcEntity.getKey(), tcEntity.getLocation(), conn);

				retMessage += "维修品" + (mBean.getSorc_no() == null ? "" : mBean.getSorc_no()) 
						+ "(" + mBean.getModel_name() + " " + mBean.getSerial_no() + ")"
						+ "原先分配的[" + orgLocation + "]库位已被分配，另行分配到[" + tcEntity.getLocation() + "]。\n";

				// 清除推车
				mapper.clearTrolleyStacks(tcEntity.getMaterial_id());
			} else if (tcEntity.getExecute() == -1) {
				MaterialEntity mBean = mService.loadSimpleMaterialDetailEntity(conn, tcEntity.getMaterial_id());

				retMessage += "维修品" + (mBean.getSorc_no() == null ? "" : mBean.getSorc_no()) 
						+ "(" + mBean.getModel_name() + " " + mBean.getSerial_no() + ")"
						+ "可能已经被其他用户完成分配。建议重新开启分配窗口操作。\n";
				// 清除推车
				mapper.clearTrolleyStacks(tcEntity.getMaterial_id());
			}
		}

		return retMessage;
	}

	private void checkLocationInDailyPlan(String todayString, String location, SqlSession conn) {
		try {
			if (!locationNormalSets.containsKey(todayString)) {
				List<TurnoverCaseEntity> locationSetsToday = getLocationSetsToday(null, conn);
				locationNormalSets.put(todayString, locationSetsToday);
			}

			List<TurnoverCaseEntity> todayList = locationNormalSets.get(todayString);
			for (TurnoverCaseEntity tcEntity : todayList) {
				if (location.equals(tcEntity.getLocation())) {
					tcEntity.setLocation(null);
					break;
				}
			}

			// locationEndoeyeSets
			
		} catch (Exception e) {
			
		}
	}

	public TurnoverCaseForm getStoargeByKey(String key, SqlSession conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);

		TurnoverCaseEntity ret = mapper.getEntityByKey(key);
		if (ret != null) {
			TurnoverCaseForm retForm = new TurnoverCaseForm();
			BeanUtil.copyToForm(ret, retForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			return retForm;
		}

		return null;
	}

	public void create(ActionForm form, List<MsgInfo> msgInfos, SqlSessionManager conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);

		TurnoverCaseEntity entity = new TurnoverCaseEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		if (checkLocationDuplicate(entity.getLocation(), entity.getKey(), mapper)) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("location");
			error.setErrcode("dbaccess.recordDuplicated");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", "库位"));
			msgInfos.add(error);
			return;
		}

		if (entity.getShelf() == null) entity.setShelf("");
		mapper.create(entity);
	}


	public void changeSetting(ActionForm form, List<MsgInfo> msgInfos, SqlSessionManager conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);

		TurnoverCaseEntity entity = new TurnoverCaseEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		TurnoverCaseEntity target = mapper.getEntityByKey(entity.getKey());

		if (checkLocationDuplicate(entity.getLocation(), entity.getKey(), mapper)) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("location");
			error.setErrcode("dbaccess.recordDuplicated");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", "库位"));
			msgInfos.add(error);
			return;
		}

		if (target == null) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("location");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "库位"));
			msgInfos.add(error);
			return;
		}

		if (entity.getShelf() == null) entity.setShelf("");
		mapper.changeSetting(entity);
	}

	public void remove(ActionForm form, List<MsgInfo> msgInfos, SqlSessionManager conn) {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);

		TurnoverCaseEntity entity = new TurnoverCaseEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		TurnoverCaseEntity target = mapper.getEntityByKey(entity.getKey());

		if (target == null) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("location");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "库位"));
			msgInfos.add(error);
			return;
		} else {
			if (target.getMaterial_id() != null) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("location");
				error.setErrcode("info.turnoverCase.notSpacingWhileRemove");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.turnoverCase.notSpacingWhileRemove"));
				msgInfos.add(error);
				return;
			}
		}

		mapper.remove(entity);
	}

	private boolean checkLocationDuplicate(String location, String key, TurnoverCaseMapper mapper) {
		TurnoverCaseEntity hit = mapper.getEntityByLocation(location);
		if (key == null) {
			return hit != null;
		} else {
			return (hit != null && !key.equals(hit.getKey()));
		}
	}

	public void getLocationMap(ActionForm form,
			Map<String, Object> lResponseResult, SqlSession conn) {

		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);

		TurnoverCaseEntity entity = new TurnoverCaseEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		StringBuffer retSb = new StringBuffer("<style>.wip-table td.storage-empty {cursor :pointer;}#wip_pop .storage-table td[kind=\"1\"]{border-color: #92D050;outline:3px solid #92D050;}#wip_pop .storage-table td[kind=\"2\"]{border-color: #C4C400;outline:3px solid #C4C400;}#wip_pop .storage-table td[kind=\"4\"]{border-color: #FFC000;outline:3px solid yellow;}#wip_pop .storage-table td[kind=\"6\"]{border-color: #606060;outline:3px solid #D8D8D8;}#wip_pop .storage-table td[f_ag]{border-radius:8px;}</style>");
		retSb.append("<div class=\"ui-widget-header ui-corner-top ui-helper-clearfix areaencloser\"><span class=\"areatitle\">通箱库位区域一览</span></div><div class=\"ui-widget-content\">");

		List<TurnoverCaseEntity> storageMap = mapper.getStorageMap(entity);

		Map<String, List<TurnoverCaseEntity>> storageByShelf = new TreeMap<String, List<TurnoverCaseEntity>>();

		for (TurnoverCaseEntity storage : storageMap) {
			if (!storageByShelf.containsKey(storage.getShelf())) {
				storageByShelf.put(storage.getShelf(), new ArrayList<TurnoverCaseEntity>());
			}
			storageByShelf.get(storage.getShelf()).add(storage);
		}

		for (String shelf : storageByShelf.keySet()) {
			int curLayer = -999;
			int maxItemCnt = 0;
			int lineCnt = 0;
			StringBuffer shelfSb = new StringBuffer("<tbody>");

			for (TurnoverCaseEntity storage : storageByShelf.get(shelf)) {
				if (storage.getLayer() != curLayer) {
					if (shelfSb.length() > 0) shelfSb.append("</tr>");
					shelfSb.append("<tr>");
					curLayer = storage.getLayer();
					if (lineCnt > maxItemCnt) {
						maxItemCnt = lineCnt;
					}
					lineCnt = 0;
				}
				shelfSb.append("<td class=\"");
				if (storage.getMaterial_id() == null) {
					shelfSb.append("storage-empty");
				} else {
					shelfSb.append("ui-storage-highlight storage-heaped");
				}
				shelfSb.append("\" kind=\"");
				shelfSb.append(storage.getKind());
				if (storage.getFor_agreed() == 1) {
					shelfSb.append("\" f_ag");	
				} else {
					shelfSb.append("\"");
				}
				shelfSb.append(" location=\"");
				shelfSb.append(storage.getLocation());
				shelfSb.append("\">");
				shelfSb.append(slimLocation(shelf, storage.getLocation()));
				shelfSb.append("</td>");
				lineCnt++;
			}
			if (!storageByShelf.get(shelf).isEmpty()) {
				shelfSb.append("</tr>");
				if (lineCnt > maxItemCnt) {
					maxItemCnt = lineCnt;
				}
			}
			if (maxItemCnt == 0) {
				maxItemCnt = 3;
			}
			retSb.append("<div style=\"margin: 15px 0 0 15px; float: left;\"><div class=\"ui-widget-header\" style=\"width: " + (maxItemCnt * 20.4) + "px; text-align: center;\"> ");
			retSb.append(shelf);
			retSb.append(" 货架</div><table class=\"condform storage-table\" style=\"width: " + (maxItemCnt * 20) + "px;\">");
			retSb.append(shelfSb);
			retSb.append("</table></div>");
		}
		retSb.append("<div class=\"clear\"></div></div>");

		lResponseResult.put("storageHtml", retSb.toString());
	}

	private String slimLocation(String shelf, String location) {
		if (CommonStringUtil.isEmpty(location)) {
			return "-";
		}
		if (location.startsWith(shelf)) {
			location = location.substring(shelf.length());
		}
		if (location.length() > 2) {
			location = location.substring(location.length() - 2);
		}
		return location;
	}

	private static String updateDate = "";
	private static Map<String, String> locationKindAgreesStarts = new HashMap<String, String>();
	private static Map<String, Set<String>> locationKindAgrees = new HashMap<String, Set<String>>();

	public Map<String, Set<String>> getKindAgreeEmptyLocations(
			int count, SqlSession conn) throws Exception {
		String todayString = DateUtil.toString(new Date(), DateUtil.DATE_PATTERN);
		if (!updateDate.equals(todayString)) {
			getStartsToday(conn);
			updateDate = todayString;
		}

		for (String kindAgreed : locationKindAgrees.keySet()) {
			Set<String> locList = locationKindAgrees.get(kindAgreed);
			if (locList.size() >= count) {
				continue;
			}
			getForKindAgreed(kindAgreed, locList, count, conn);
		}

		return locationKindAgrees;
	}

	private void getForKindAgreed(String kindAgreed,
			Set<String> locList, int count, SqlSession conn) throws Exception {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		TurnoverCaseEntity condi = new TurnoverCaseEntity();
		condi.setLayer(count);
		if (locList.isEmpty()) {
			condi.setLocation(locationKindAgreesStarts.get(kindAgreed));
			condi.setExecute(0);
		} else {
			int size = locList.size();
			condi.setLocation(locList.toArray(new String[size])[size - 1]);
			condi.setExecute(1);
		}

		Set<String> set = locationKindAgrees.get(kindAgreed);
		List<String> l = mapper.getNextLocationsOnKindForAgreed(condi);
		for (String ele : l) {
			set.add(ele);
		}

		if (set.size() < count) {
			condi.setExecute(-1);
		}
		l = mapper.getNextLocationsOnKindForAgreed(condi);
		for (String ele : l) {
			set.add(ele);
		}

		if (set.size() < (count / 2)) {
			throwLeakException(kindAgreed);
		}
	}

	private void throwLeakException(String kindAgreed) throws Exception {
		switch (kindAgreed) {
		case "1_0": throw new Exception("260 系列未同意库位不足。");
		case "1_1": throw new Exception("260 系列已同意库位不足。");
		case "4_0": throw new Exception("290 系列未同意库位不足。");
		case "4_1": throw new Exception("290 系列已同意库位不足。");
		case "2_0": throw new Exception("细镜 + 纤维镜未同意库位不足。");
		case "2_1": throw new Exception("细镜 + 纤维镜已同意库位不足。");
		case "6_0": throw new Exception("硬性镜未同意库位不足。");
		case "6_1": throw new Exception("硬性镜已同意库位不足。");
		default : throw new Exception("发现库位不足。");
		}
	}

	private void getStartsToday(SqlSession conn) throws Exception {
		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);
		List<TurnoverCaseEntity> list = mapper.getStartLocationsOnKindForAgreed();
		synchronized (locationKindAgreesStarts) {
			locationKindAgreesStarts.clear();
			for (TurnoverCaseEntity locationByKindAgree : list) {
				String key = locationByKindAgree.getKind() + "_" + locationByKindAgree.getFor_agreed();
				locationKindAgreesStarts.put(key, locationByKindAgree.getLocation());
				locationKindAgrees.put(key, new LinkedHashSet<String>());
			}
		}
		if (locationKindAgreesStarts.size() < 8) {
			String emptyLocationsMessage = "";
			if (!locationKindAgreesStarts.containsKey("1_0")) {
				emptyLocationsMessage += "260 系列未同意库位不足。\n";
			}
			if (!locationKindAgreesStarts.containsKey("1_1")) {
				emptyLocationsMessage += "260 系列已同意库位不足。\n";
			}
			if (!locationKindAgreesStarts.containsKey("4_0")) {
				emptyLocationsMessage += "290 系列未同意库位不足。\n";
			}
			if (!locationKindAgreesStarts.containsKey("4_1")) {
				emptyLocationsMessage += "290 系列已同意库位不足。\n";
			}
			if (!locationKindAgreesStarts.containsKey("2_0")) {
				emptyLocationsMessage += "细镜 + 纤维镜未同意库位不足。\n";
			}
			if (!locationKindAgreesStarts.containsKey("2_1")) {
				emptyLocationsMessage += "细镜 + 纤维镜已同意库位不足。\n";
			}
			if (!locationKindAgreesStarts.containsKey("6_0")) {
				emptyLocationsMessage += "硬性镜未同意库位不足。\n";
			}
			if (!locationKindAgreesStarts.containsKey("6_1")) {
				emptyLocationsMessage += "硬性镜已同意库位不足。\n";
			}
			throw new Exception(emptyLocationsMessage);
		}
	}

	private void checkLocationInDailyPlanSet(String todayString, String kindAndForAgree, String location, SqlSession conn) throws Exception {
		if (!updateDate.equals(todayString)) {
			getStartsToday(conn);
			updateDate = todayString;
		}

		Set<String> locations = locationKindAgrees.get(kindAndForAgree);
		if (locations == null) return;
		if (locations.contains(location)) {
			locations.remove(location);
		}
	}
}
