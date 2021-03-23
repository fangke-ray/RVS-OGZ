package com.osh.rvs.service.qf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;

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
					checkLocationInDailyPlan(todayString, tcEntity.getLocation(), conn);

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
				List<String> emptyLocations = new ArrayList<String>();
				getEmptyLocations(mBean.getKind(), emptyLocations, 1, true, conn, false);
				tcEntity.setExecute(0);
				String orgLocation = tcEntity.getLocation();
				tcEntity.setLocation(emptyLocations.get(0));
				mapper.putin(tcEntity);
				checkLocationInDailyPlan(todayString, tcEntity.getLocation(), conn);

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
}
