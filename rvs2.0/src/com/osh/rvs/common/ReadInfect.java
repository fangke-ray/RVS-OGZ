package com.osh.rvs.common;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.osh.rvs.bean.master.DeviceCheckItemEntity;
import com.osh.rvs.mapper.master.CheckFileManageMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.copy.DateUtil;

public class ReadInfect {

	private static Map<String, String> styleclasses = new HashMap<String, String>();

	private static Map<String, String> styleidmap = new HashMap<String, String>();

	// ss:WrapText="1"
	@SuppressWarnings("unused")
	private static final String SS_WRAPTEXT_1 = "white-space:normal; word-break:break-all; overflow:hidden; ";

	private static Logger _logger = Logger.getLogger(ReadInfect.class); 
	private static ReadInfect instance = new ReadInfect(); 

	public static void main(String[] args) {

//		String[] nas = {
//				"QR-B31002-26"
//				};
//		String[] ids = {"14"};

//		String[] nas = {
//				"QR-B31002-24", "QR-B31002-2", "QR-B31002-29", "QR-B31003-5A", "QR-B31002-3A"
//				};
//		String[] ids = {"6", "1", "5", "4", "2"};

		File p = new File("E:\\rvsG\\DeviceInfection\\pos_multi\\test\\");
		List<String> fnames = new ArrayList<String>(); 
		List<String> lnas = new ArrayList<String>(); 
		for (File file : p.listFiles()) {
			if (file.isDirectory()) continue;
			String name = (file.getName());
			fnames.add(name);
			lnas.add(name.substring(0, 10));
		}

		String[] nas = lnas.toArray(new String[lnas.size()]);

		SqlSessionManager conn = getTempWritableConn();

		try {
			conn.startManagedSession();

			for (int i=0; i< nas.length;i++) {
				String na = nas[i];
				System.out.println("=======================" + na + "========================");
				String fromfile = "E:\\rvsG\\DeviceInfection\\pos_multi\\test\\xml\\"+fnames.get(i)+"";
				//				String fromfile = "H:\\3\\"+na+".xml";
				// H:\3

				String tofile = "E:\\rvsG\\DeviceInfection\\pos_multi\\test\\xml\\"+na+".html";
				instance.convert(fromfile, tofile, "" + (i+204), conn, 3, new ArrayList<MsgInfo>()); // ids[i]
			}

//			instance.convert("E:\\rvsG\\DeviceInfection\\xml\\MS0301-2.xml", 
//					"E:\\rvsG\\DeviceInfection\\xml\\MS0301-2.html", "0", conn, new ArrayList<MsgInfo>()); // ids[i]
			
			
			conn.commit();
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				_logger.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}
	}

	public void convert(String fromfile, String tofile, String check_file_manage_id, SqlSessionManager conn, Integer linage, List<MsgInfo> errors) {
		// Defines a factory API
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		String tablecontents = "";
		Map<String,String> referItems = new HashMap<String,String>();
		float countWidth = 0; // 每日点检时默认大宽度

		try {
			// DocumentBuilderFactoryからDocumentBuilderインスタンスを取得
			DocumentBuilder db = dbf.newDocumentBuilder();

			//File file = new File(basedir + "EL座-粗镜及电子细镜（150、180、VISERA除外）.xml");
			File file = new File(fromfile);
			// XML DOMを作成
			Document doc = db.parse(file);

			// 外形
			NodeList nl = doc.getElementsByTagName("Styles");
			if (nl==null || nl.getLength() == 0) return;
			Element styles = (Element) nl.item(0);
			NodeList stylelist = styles.getElementsByTagName("Style");
			boolean findDefault = false;
			for (int i =0;i<stylelist.getLength();i++) {
				Node style = stylelist.item(i);
				String styleid = style.getAttributes().getNamedItem("ss:ID").getNodeValue();
				if (!findDefault) {
					if ("Default".equalsIgnoreCase(styleid)) {
						// 生成Default css TODO
						@SuppressWarnings("unused")
						NodeList borders = ((Element)style).getElementsByTagName("Borders");

						findDefault = true;
					}
				} else {
					NodeList alignment = ((Element)style).getElementsByTagName("Alignment");
					NodeList borders = ((Element)style).getElementsByTagName("Borders");
					NodeList numberFormat = ((Element)style).getElementsByTagName("NumberFormat");
					styleidmap.put(styleid, getAlignmentCss(alignment) + getBorderCss(borders) + getNumberFormatCss(numberFormat));
				}
			}

			// 位置 + 内容
			NodeList tnl = doc.getElementsByTagName("Worksheet");
			if (tnl==null || tnl.getLength() == 0) return;
			int wsLength = tnl.getLength();
			Element refertable = null;

			if (wsLength > 1) { // 确定都只有1页有效
				Node refersheet = tnl.item(1);
				NodeList tables = ((Element) refersheet).getElementsByTagName("Table");
				// 取得参照值
				if (tables!=null && tables.getLength() > 0) {
					refertable = (Element) tables.item(0);
//					NodeList columns = refertable.getElementsByTagName("Column");
					NodeList rows = refertable.getElementsByTagName("Row");
					if (rows.getLength() > 0)
						for (int irow =0;irow<rows.getLength();irow++) { // 行的循环
							Element row = (Element)rows.item(irow);
							NodeList rowcells = row.getElementsByTagName("Cell");
							for (int irowcell = 0; irowcell<rowcells.getLength(); irowcell++) {
								Element rowcell = (Element)rowcells.item(irowcell);
								String cellText = getTextData(rowcell);
								String iRowIdx = rowcell.getAttribute("ss:Index");
								if (isEmpty(iRowIdx)) iRowIdx = "1";
								// 单元格的循环
								if (!isEmpty(cellText)) {
									String cellNum = XlsUtil.getExcelColCode(iRowIdx) + (irow + 1);
									// refer可以有空格
									cellText = cellText.replaceAll("&nbsp;", " ");
									referItems.put(cellNum, cellText);
								}
							}
						}
					;
				}
				wsLength = 1;
			}

			// sheet的循环
			Node worksheet = tnl.item(0);
//			String sheetname = worksheet.getAttributes().getNamedItem("ss:Name").getNodeValue();
			NodeList tables = ((Element) worksheet).getElementsByTagName("Table");
			if (tables!=null && tables.getLength() > 0) {
			Element table = (Element) tables.item(0);
//			int sheetrows = getAttributeIntValue(table.getAttributes().getNamedItem("ss:ExpandedRowCount"));
			int sheetcols = getAttributeIntValue(table.getAttributes().getNamedItem("ss:ExpandedColumnCount"));

			NodeList columns = table.getElementsByTagName("Column");
			//列的宽度
			float[] columnswidth = new float[sheetcols];
			//列宽度没有付给td 默认是付给了
			boolean[] columnswidthnotsetted = new boolean[sheetcols];

			int columnswidthcount = 0;
			// Default Width
			for (int j =0;j<columns.getLength();j++) { // 列的循环
				Element columsg = (Element)columns.item(j);
				int ispan = 1;
				ispan += getAttributeIntValue(columsg.getAttributes().getNamedItem("ss:Span"));
				int ssindex = getAttributeIntValue(columsg.getAttributes().getNamedItem("ss:Index"));
				if (ssindex != 0) columnswidthcount = ssindex - 1;
				float width = getAttributeFloatValue(columsg.getAttributes().getNamedItem("ss:Width"));
				for (int k=0;k<ispan;k++,columnswidthcount++) {
					columnswidth[columnswidthcount] = width;
					if (width > 0) {
						countWidth += width;
						columnswidthnotsetted[columnswidthcount] = true;
					}
				}
			} // 列的循环

			NodeList rows = table.getElementsByTagName("Row");
			int[] columnspanned = new int[sheetcols];

			CheckFileManageMapper mapper = conn.getMapper(CheckFileManageMapper.class);
			// 先删除项目
			mapper.deleteDevicesCheckItem(check_file_manage_id);

			// 合并归档时，复数行复制内容对象
			List<ShiftData> gapObjects = new ArrayList<ShiftData>();

			for (int irow =0;irow<rows.getLength();irow++) { // 行的循环
				String trcontents = "";
				boolean linespan = true;
				Integer irowspan = null;

				Element row = (Element)rows.item(irow);
//				int trheight = getAttributeIntValue(row.getAttributes().getNamedItem("ss:Height"));

				// ss:Span --> 纯空行
				Node ssSpan = row.getAttributes().getNamedItem("ss:Span");
				if (ssSpan != null) {
					int iSpanCount = getAttributeIntValue(ssSpan);
					for (int iSpan = 0; iSpan <= iSpanCount; iSpan++)
						tablecontents += "<tr/>";
					continue;
				}

				// tr border?
				trcontents+="<tr>";
				NodeList rowcells = row.getElementsByTagName("Cell");
				int irowcellExact = 0;
				//System.out.println("row:>>"+irow+">>cells"+rowcells.getLength());

				List<ShiftData> shifts = new ArrayList<ShiftData>();
				for (int irowcell = 0; irowcell<rowcells.getLength(); irowcell++, irowcellExact++) {  // 一行中单元格的循环
					Element rowcell = (Element)rowcells.item(irowcell);

					if (irowcellExact >= sheetcols) break;
					if (columnspanned[irowcellExact] < 0) { // 这是不正常的
						// 如果是被横向合并+纵向合并的单元格，就简单过去
						columnspanned[irowcellExact]++;
						//columnspanned[irowcellExact] = - columnspanned[irowcellExact] - 1;
					} else if (columnspanned[irowcellExact] > 0) {
						// 有被合并就不算是全行跨列了
						linespan = false;
						while (columnspanned[irowcellExact] != 0) {
							if (columnspanned[irowcellExact] < 0) {
								// 之后被横向合并+纵向合并的单元格
								columnspanned[irowcellExact]++;
							} else {
								// 如果是开始合并行的下方行
								columnspanned[irowcellExact]--;
							}
							irowcellExact++;
						}
					} else {
					}

					int mergeAcross = getAttributeIntValue(rowcell.getAttributes().getNamedItem("ss:MergeAcross"));
					int mergeDown = getAttributeIntValue(rowcell.getAttributes().getNamedItem("ss:MergeDown"));
					// 如果全格子同样跨列值，需要调整
					if (linespan) {
						if (irowspan != null && irowspan != mergeDown) {
							linespan = false;
						}
						irowspan = mergeDown;
					}

					int index = getAttributeIntValue(rowcell.getAttributes().getNamedItem("ss:Index")) - 1;

					// 如果指定了列数，则补上到指定列的td。
					if (index > 0) {
						while (irowcellExact < index) {
							if (columnspanned[irowcellExact] < 0) {
								linespan = false;
								// 之后被横向合并+纵向合并的单元格
								columnspanned[irowcellExact]++;
							} else if (columnspanned[irowcellExact] > 0) {
								linespan = false;
								// 如果是开始合并行的下方行
								columnspanned[irowcellExact]--;
							} else {
								trcontents+="<td></td>";
							}
							irowcellExact++;
						}
					}

					// 取得文本内容
					String cellText = getTextData(rowcell);

					// 解析标识符
					cellText = transTagInfect(cellText, referItems, check_file_manage_id, linage, gapObjects, mapper, errors);

					// 需要换行的保存
					// 跳行要求
					if (linage != null && cellText.indexOf(" gap='") >= 0) {
						ShiftData gap = new ShiftData();
						gap.content = cellText;
						gap.rowShift = new ShiftData();
						gap.rowShift.tab = Integer.parseInt(cellText.replaceAll(".*gap='(\\d+)'.*", "$1"));
						if (cellText.indexOf("shift='1'") >= 0) { // 有横向
							gap.seq = cellText.replaceAll(".*seq='(\\d+)'.*", "$1");
							gap.tab = Integer.parseInt(cellText.replaceAll(".*tab='(\\d+)'.*", "$1"));
							Integer cycle_type = Integer.parseInt(cellText.replaceAll(".*cycle_type='(\\d+)'.*", "$1"));
							int shiftCount = 0;
							switch (cycle_type) {
							case 1: shiftCount = 31;countWidth = 0;break;
							case 2: shiftCount = 5;break;
							case 3: shiftCount = 12;break;
							case 4: shiftCount = 2;break;
							}
							if (gap.tab != 0) {
								gap.cols = new Integer[shiftCount];
								for (int iSft=0;iSft<shiftCount;iSft++) {
									gap.cols[iSft] = irowcellExact + gap.tab * iSft; // shift.tab * (iSft + 1)
								}
							} else {
								gap.cols = new Integer[1];
								gap.cols[0] = irowcellExact;
							}
						} else {
							gap.cols = new Integer[1];
							gap.cols[0] = irowcellExact;
						}

						gap.rowShift.cols = new Integer[linage];
						for (int iSft=0;iSft<linage;iSft++) {
							gap.rowShift.cols[iSft] = irow + gap.rowShift.tab * iSft; // shift.tab * (iSft + 1)
						}

						gapObjects.add(gap);

					} else
					if (cellText.indexOf("shift='1'") >= 0) {
						ShiftData shift = new ShiftData();
						shift.content = cellText;
						shift.seq = cellText.replaceAll(".*seq='(\\d+)'.*", "$1");
						shift.tab = Integer.parseInt(cellText.replaceAll(".*tab='(\\d+)'.*", "$1"));
						Integer cycle_type = Integer.parseInt(cellText.replaceAll(".*cycle_type='(\\d+)'.*", "$1"));
						int shiftCount = 0;
						switch (cycle_type) {
						case 1: shiftCount = 31;countWidth = 0;break;
						case 2: shiftCount = 5;break;
						case 3: shiftCount = 12;break;
						case 4: shiftCount = 2;break;
						}
						if (shift.tab != 0) {
							shift.cols = new Integer[shiftCount];
							for (int iSft=0;iSft<shiftCount;iSft++) {
								shift.cols[iSft] = irowcellExact + shift.tab * iSft; // shift.tab * (iSft + 1)
							}
						}
						shifts.add(shift);
					}

					// 生成td
					if (mergeAcross>0 || mergeDown >0) { // 如果存在跨行列
						String html = "";
						String css = getcss(rowcell);
						if (css.contains(" NBk")) {
							cellText = cellText.replaceAll("^\\-(\\d*)$", "($1)");
							css.replaceAll(" NBk", "");
						}
						if (isEmpty(cellText)) { // 空格平移
							for (ShiftData shift : shifts) {
								for (int iFl = 0; iFl < shift.cols.length ; iFl++) {
									Integer col = shift.cols[iFl];
									if (col == irowcellExact) {
										cellText = shift.content.replaceAll("shift='1'", "shift='"+(1+iFl)+"'");
										break;
									}
								}
							}
							for (ShiftData gap : gapObjects) {
								for (int iRw = 0; iRw < gap.rowShift.cols.length ; iRw++) {
									Integer spRow = gap.rowShift.cols[iRw];
									if (spRow == irow) {
										if (gap.cols.length > 1) { // 有行展开
											for (int iFl = 0; iFl < gap.cols.length ; iFl++) {
												Integer col = gap.cols[iFl];
												if (col == irowcellExact) {
													cellText = gap.content.replaceAll("shift='1'", "shift='"+(1+iFl)+"'");
													break;
												}
											}
											cellText = cellText.replaceAll("line='0'", "line='"+(iRw)+"'");
										} else {
											if (gap.cols[0] == irowcellExact) {
												cellText = gap.content.replaceAll("line='0'", "line='"+(iRw)+"'");
											}
										}

										break;
									}
								}
							}
						}
						columnspanned[irowcellExact] = mergeDown;
						for (int iAcross = 1; iAcross <= mergeAcross; iAcross++) {
							columnspanned[++irowcellExact] = - mergeDown;
						}

						html+="<td " + css;
						if (mergeAcross > 0) {
							html+="colspan='"+ (mergeAcross+1) +"' ";
						}
						if (mergeDown > 0) {
							html+="linespan='"+ (mergeDown+1) +"' ";
						}
						if (css.contains(" IT")) {
							html+=		"><span>" + cellText + "</span></td>";
						} else {
							html+=		">" + cellText + "</td>";
						}

						trcontents+=html;
					} else {
						String sWidth = "";
						if (columnswidthnotsetted[irowcellExact] && mergeAcross == 0) {
							sWidth = " style=\"width:" + columnswidth[irowcellExact] + "px;\"";
							columnswidthnotsetted[irowcellExact] = false;
						}
						String css = getcss(rowcell);
						if (css.contains(" NBk")) {
							cellText = cellText.replaceAll("^\\-(\\d*)$", "($1)");
							//css.replaceAll(" NBk", "");
						}
						if (isEmpty(cellText)) { // 空格平移
							for (ShiftData shift : shifts) {
								for (int iFl = 0; iFl < shift.cols.length ; iFl++) {
									Integer col = shift.cols[iFl];
									if (col == irowcellExact) {
										cellText = shift.content.replaceAll("shift='1'", "shift='"+(1+iFl)+"'");
										break;
									}
								}
							}
							for (ShiftData gap : gapObjects) {
								for (int iRw = 0; iRw < gap.rowShift.cols.length ; iRw++) {
									Integer spRow = gap.rowShift.cols[iRw];
									if (spRow == irow) {
										if (gap.cols.length > 1) { // 有行展开
											for (int iFl = 0; iFl < gap.cols.length ; iFl++) {
												Integer col = gap.cols[iFl];
												if (col == irowcellExact) {
													cellText = gap.content.replaceAll("shift='1'", "shift='"+(1+iFl)+"'");
													break;
												}
											}
											cellText = cellText.replaceAll("line='0'", "line='"+(iRw)+"'");
										} else {
											if (gap.cols[0] == irowcellExact) {
												cellText = gap.content.replaceAll("line='0'", "line='"+(iRw)+"'");
											}
										}

										break;
									}
								}
							}
						}
						if (css.contains(" IT")) {
							trcontents+="<td " + css + sWidth + "><span>" + cellText + "</span></td>";
						} else {
							trcontents+="<td " + css + sWidth + ">" + cellText + "</td>";
						}
					}
					
				}  // 单元格的循环

				trcontents+="</tr>";
				if (linespan && irowspan != null && irowspan != 0) {
					trcontents = trcontents.replaceAll("linespan='(\\d+)'", "");
					for (int icols=0; icols < columnspanned.length;icols++){
						columnspanned[icols] = 0;
					}
				} else {
					trcontents = trcontents.replaceAll("linespan='(\\d+)'", "rowspan='$1'");
				}
				tablecontents += trcontents;
				//System.out.println(irow + "Line:"); //arraystring(columnspanned )
				for (int iremain = irowcellExact; iremain < sheetcols; iremain++) {
					if (columnspanned[iremain] > 0)
						columnspanned[iremain]--;
				}
			}  // 行的循环

			tablecontents = tablecontents.replaceAll("  ", " ").replaceAll(" >", ">").replaceAll(" ' ", "' ");
					//System.out.println(style.getAttributes().getNamedItem("ss:ID").getNodeValue());
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 生成模板文件
		File f = new File(tofile);

		BufferedWriter output = null;
		try {
			if(!f.exists()) {
				try {
					f.createNewFile();
				} catch (Exception e) {
					System.out.println(tofile);
					throw e;
				}
			}

			output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			output.write("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
			output.write("<style>td{font-size:12px;}.HC{text-align:center}.HL{text-align:left}.HR{text-align:right}");
			output.write(".VT{valign:top}.VC{valign:middle}.VB{valign:bottom}");
			output.write(".WT{white-space:normal; word-break:break-all; overflow:hidden;}.IT{width:1em;}.IT span{width:1em;letter-spacing: 1em;}");
			// 页面宽度
			if (countWidth > 0 && countWidth <= 1100) {
				output.write(".tcs_sheet table {min-width: 1100px;}");
			} else if (countWidth > 1100 && countWidth < 1280) {
				output.write(".tcs_sheet table {min-width: " + countWidth + "px;}");
			}

			String styleHtml = "";
			for (String stylekey : styleclasses.keySet()) {
				styleHtml += "." + stylekey + "{" + styleclasses.get(stylekey) + "}";
			}
			output.write(styleHtml);
			output.write("</style></head><table style=\"border-collapse:collapse;\">");
			output.write(tablecontents);
			output.write("</table>");
			if (!referItems.isEmpty()) {
				String referContent = "<refers>";
				for (String referItemKey : referItems.keySet()) {
					referContent += "<refer type='extra' id='"+referItemKey+"' value='"+referItems.get(referItemKey)+"'/>";
				}
				referContent += "</refers>";
				output.write(referContent);
			}
			output.write("</html>");
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			output = null;
			f = null;
		}
	}
	private static String getTextData(Element rowcell) {
		String cellText = "";
		NodeList data = rowcell.getElementsByTagName("Data");
		for (int idatum = 0;idatum < data.getLength();idatum++) {
			Element textdata = (Element) data.item(idatum);
			// Data ss:Type="DateTime" TODO??
			cellText += decodeHtmlText(textdata.getTextContent());
		}
		data = rowcell.getElementsByTagName("ss:Data");
		for (int idatum = 0;idatum < data.getLength();idatum++) {
			Element textdata = (Element) data.item(idatum);
			cellText += decodeHtmlText(textdata.getTextContent());
		}
		return cellText;
	}

	// 解析标识符
	private static String transTagInfect(String cellText, Map<String, String> referItems, String check_file_manage_id, 
			Integer linage, List<ShiftData> gapObjects, CheckFileManageMapper mapper, List<MsgInfo> errors) {

		if ("".equals(cellText) || cellText.indexOf("#") < 0) return cellText;
		// 管理编号
		cellText = cellText.replaceAll("#G\\[MANAGENO#" , "<manageNo/>");
		cellText = cellText.replaceAll("#G\\[MANAGENO\\[R#" , "<manageNo replacable/>");

		if (linage != null && cellText.matches("^[^#]*#G\\[MANAGENO\\[U(.*)#[^#]*$")) {

			cellText = cellText.replaceAll("#G\\[MANAGENO\\[U(.*)#" , "<manageNo gap='$1' line='0'/>");

			return cellText;
		}

		if (linage != null && cellText.matches("^[^#]*#G\\[NO\\[U(.*)#[^#]*$")) {
			cellText = cellText.replaceAll("#G\\[NO\\[U(.*)#" , "<nodo gap='$1' line='0'/>");

			return cellText;
		}

		// 型号
		cellText = cellText.replaceAll("#G\\[MODEL#" , "<model/>");
		if (linage != null && cellText.matches("^[^#]*#G\\[MODEL\\[U(.*)#[^#]*$")) {
			cellText = cellText.replaceAll("#G\\[MODEL\\[U(.*)#" , "<model gap='$1' line='0'/>");

			return cellText;
		}

		// 半期
		cellText = cellText.replaceAll("#G\\[PERIOD#" , "<period type='full'/>");
		// 半期（没有P）
		cellText = cellText.replaceAll("#G\\[PERIODC#" , "<period type='num'/>");
		// 年
		cellText = cellText.replaceAll("#G\\[YEAR#" , "<date type='year'/>");
		// 月
		cellText = cellText.replaceAll("#G\\[MONTH#" , "<date type='month'/>");
		// 工程
		cellText = cellText.replaceAll("#G\\[LINE#" , "<line/>");
		// 工位
		cellText = cellText.replaceAll("#G\\[POSITION#" , "<position/>");
		// 名称
		cellText = cellText.replaceAll("#G\\[NAME#" , "<name/>");
		if (linage != null && cellText.matches("^[^#]*#G\\[NAME\\[U(.*)#[^#]*$")) {
			cellText = cellText.replaceAll("#G\\[NAME\\[U(.*)#" , "<name gap='$1' line='0'/>");

			return cellText;
		}

		// 责任者
		cellText = cellText.replaceAll("#G\\[RESPONCOR#" , "<responcor/>");

		// 页数
		if (linage != null && cellText.indexOf("#G[PAGE") > 0) {
			cellText = cellText.replaceAll("#G\\[PAGE#" , "<page type='current'/>");
			cellText = cellText.replaceAll("#G\\[PAGE\\[M#" , "<page type='max'/>");

			return cellText;
		} else {
			cellText = cellText.replaceAll("#G\\[PAGE#" , "１");
			cellText = cellText.replaceAll("#G\\[PAGE\\[M#" , "１");
		}
		

		// cellText = cellText.replaceAll("#.*#" , "");

		// 管理编号（可更换）
		cellText = cellText.replaceAll("#G\\[MANAGENO\\[P(\\d){0,}#" , "<manageNo/><input type='text' class='new_manage_no'><input type='button' id='new_device_button' value='新到设备/工具'>");

		// 点检项目
		// 	  	#DM[S01[EC[T1[U1#	日	序号	确认	确认	竖行间隔	
		//			U1	
		//				
		//日	点检人	竖版	间隔		
		Pattern pCheckData = Pattern.compile("#D.*#");
		Matcher mCheckData = pCheckData.matcher(cellText);

		if (mCheckData.find()) {
			String matchTag = mCheckData.group();
			String matchText = matchTag.replaceAll("#", "");
			try {
				// System.out.println(cellText + "=>" + matchText);
				String[] tags = matchText.split("\\[");
				DeviceCheckItemEntity itemEntity = new DeviceCheckItemEntity();
				itemEntity.setTab(1); // 默认单元格中的跳动
				itemEntity.setData_type(1); // 默认点检方式

				Integer ul = null;
				for (String tag : tags) {

					if (tag.startsWith("D")) {
						// 取得周期信息
						String cycle = tag.substring(1, 2);
						switch (cycle) {
						case "D" : itemEntity.setCycle_type(1); itemEntity.setTrigger_state(1); break; // 日
						case "W" : itemEntity.setCycle_type(2); itemEntity.setTrigger_state(2); break; // 周
						case "M" : itemEntity.setCycle_type(3); itemEntity.setTrigger_state(3); break; // 月
						case "P" : itemEntity.setCycle_type(4); itemEntity.setTrigger_state(4); break; // 半期
						case "Y" : itemEntity.setCycle_type(5); itemEntity.setTrigger_state(5); break; // 全年
						default : throw new Exception("不合法的标签：" + cellText);
						}
						System.out.print(cycle + "\t");
						if (tag.length() >= 3) {
							cycle = tag.substring(2, 3);
							switch (cycle) {
							case "F" : itemEntity.setTrigger_state(0); break; // 无限制
							case "W" : itemEntity.setTrigger_state(2); break; // 周
							case "M" : itemEntity.setTrigger_state(3); break; // 月
							case "P" : itemEntity.setTrigger_state(4); break; // 半期
							case "Y" : itemEntity.setTrigger_state(5); break; // 全年
							default : throw new Exception("不合法的标签：" + cellText);
							}
						}
					} else if (tag.startsWith("S")) {
						// 取得序列信息
						String seq = tag.substring(1, 3);
						itemEntity.setItem_seq(seq);
						System.out.print(seq + "\t");
					} else if (tag.startsWith("K")) {
						// 取得输入项目信息信息
						// 确认  KC
						// 数字  KDU0.2L0.6
						// 参照  KP
						int index = 2;
						String triggerStateAndDataType = tag.substring(1, 2);
						if("C".equals(triggerStateAndDataType)) {
							System.out.print("Input\t");
							itemEntity.setData_type(1);
						} else if ("D".equals(triggerStateAndDataType)) {
							System.out.print("Number\t");
							itemEntity.setData_type(2);
						} else if ("P".equals(triggerStateAndDataType)) {
							System.out.print("Refer\t");
							itemEntity.setData_type(2);
							// 通过参考取值
							itemEntity.setRefer_upper_from("P");
							itemEntity.setRefer_lower_from("P");
						} else {
							throw new Exception("输入项目格式不正确");
						}

						while (index < tag.length()) {
							String vValue = getOfAct(tag, index);
							System.out.print(vValue + "\t");
							if (vValue.startsWith("U")) {
								// 上限
								String uValue = vValue.substring(1);
								if ("".equals(uValue)) {
									// 通过型号取值
									index += 2;
									String referTags = tag.substring(index-1,index+1);
									String referLimits = referItems.get(referTags);
									itemEntity.setRefer_upper_from(referLimits);
								} else {
									// 直接取值
									BigDecimal bValue = new BigDecimal(uValue);
									itemEntity.setUpper_limit(bValue);
								}
							} else if (vValue.startsWith("L")) {
								// 下限
								String lValue = vValue.substring(1);
								if ("".equals(lValue)) {
									// 通过型号取值
									index += 2;
									String referTags = tag.substring(index-1,index+1);
									String referLimits = referItems.get(referTags);
									itemEntity.setRefer_lower_from(referLimits);
								} else {
									// 直接取值
									BigDecimal bValue = new BigDecimal(lValue);
									itemEntity.setLower_limit(bValue);
								}
							} else if (vValue.startsWith("B")) {
								itemEntity.setRefer_upper_from(vValue.substring(1));
								itemEntity.setRefer_lower_from(vValue.substring(1));
							} else if (vValue.startsWith("T")) {
								// 定点
								Date dValue = DateUtil.toDate(vValue.substring(1), "HH:mm");
								itemEntity.setAct_refer_time(dValue);
							}
							if (index < tag.indexOf(vValue) + vValue.length())
								index = tag.indexOf(vValue) + vValue.length();
						}
					} else if (tag.startsWith("M")) {
						// 型号特定需要参照
						itemEntity.setModel_relative(getReferData(referItems, tag.substring(1)));
					} else if (tag.startsWith("B")) {
						// 数据需要参照
						itemEntity.setData_relative(tag.substring(1));
					} else if (tag.startsWith("T")) {
						// 单元格中的跳动
						itemEntity.setTab(Integer.parseInt(tag.substring(1)));
					} else if (tag.startsWith("U")) {
						if(linage != null) ul = Integer.parseInt(tag.substring(1)); // 换行页面也处理
						itemEntity.setGap(ul);
					} else {
						throw new Exception("不合法的标签：" + cellText);
					}
				}
				itemEntity.setCheck_file_manage_id(check_file_manage_id);
				mapper.addSeqItem(itemEntity);

				// 替换成标签
				String replaceTag = matchTag.replaceAll("\\[", "\\\\[").replaceAll("\\+", "\\\\+").replaceAll("-", "\\\\-");
				cellText = cellText.replaceAll(replaceTag, itemEntity.toXmlTag());
			} catch (Exception e) {
				_logger.error("解析"+cellText+"时发生错误:" + e.getMessage(), e);
				MsgInfo error = new MsgInfo();
				error.setComponentid("sheet_file_name");
				error.setErrmsg("解析"+cellText+"时发生错误:" + e.getMessage());
				errors.add(error);
			}
			System.out.println(cellText);
		}

		//	参照项目	#P[I01#	I01	输入内容
		//	 参照项目（组）	#P[I01[U2#						
		//	 可选择参照项目	#P[C01[V26#	选项序号					
		Pattern pReferData = Pattern.compile("#P.*#");
		Matcher mReferData = pReferData.matcher(cellText);
		if (mReferData.find()) {
//			String matchTag = mReferData.group();
//			String matchText = matchTag.replaceAll("#", "");
			try {
				cellText = cellText.replaceAll("#P\\[CL([\\-\\d\\.]*)U([\\-\\d\\.]*)#", "<refer type='choose' lower_limit='$1' upper_limit='$2'/>");
//				if (matchText.charAt(2) == 'I') {
//					// 输入
//					cellText = matchTag.replaceAll("#P\\[I(\\d{2}).*#", "<refer type='input' item_seq='$1'/>");
//				} else if (matchText.charAt(2) == 'C') {
//					// 选择
//					cellText = cellText.replaceAll("#P\\[C(\\d{2})\\[V([\\-\\d\\.]*)#", "<refer type='choose' item_q='$1' value='$2'/>");
//					cellText = cellText.replaceAll("#P\\[C(\\d{2})\\[V([\\-\\d\\.]*)L([\\-\\d\\.]*)#", "<refer type='choose' item_seq='$1' value='$2' lower_limit='$3'/>");
//					cellText = cellText.replaceAll("#P\\[C(\\d{2})\\[V([\\-\\d\\.]*)U([\\-\\d\\.]*)#", "<refer type='choose' item_seq='$1' value='$2' upper_limit='$3'/>");
//					cellText = cellText.replaceAll("#P\\[C(\\d{2})\\[V([\\-\\d\\.]*)L([\\-\\d\\.]*)U([\\-\\d\\.]*)#", "<refer type='choose' item_seq='$1' value='$2' lower_limit='$3' upper_limit='$4'/>");
//				}
			} catch (Exception e) {
				_logger.error("解析"+cellText+"时发生错误:" + e.getMessage(), e);
			}
			System.out.println(cellText);
		}

//	 	  盖章	#NP[I[T3#	#ND	I	V	T3		
//	  		周	上级		全范围		
//	  	#NP[L[T3#	#NW	L		T0		
//	  		月					
//	  		#NM					
//	  		半期					
//	  		#NP					
		Pattern pSignData = Pattern.compile("#N.*#");
		Matcher mSignData = pSignData.matcher(cellText);
		if (mSignData.find()) {
			String matchTag = mSignData.group();
			String matchText = matchTag.replaceAll("#", "");
			try {
				String[] tags = matchText.split("\\[");
				DeviceCheckItemEntity itemEntity = new DeviceCheckItemEntity();
				itemEntity.setTrigger_state(1);
				itemEntity.setTab(1);
				for (String tag : tags) {
					if (tag.startsWith("N")) {
						// 取得周期信息
						String cycle = tag.substring(1, 2);
						switch (cycle) {
						case "D" : itemEntity.setCycle_type(1); itemEntity.setTrigger_state(1); break;
						case "W" : itemEntity.setCycle_type(2); itemEntity.setTrigger_state(2); break;
						case "M" : itemEntity.setCycle_type(3); itemEntity.setTrigger_state(3); break;
						case "P" : itemEntity.setCycle_type(4); itemEntity.setTrigger_state(4); break;
						case "Y" : itemEntity.setCycle_type(5); itemEntity.setTrigger_state(5); break; // 全年
						default : throw new Exception("不合法的标签：" + cellText);
						}
						System.out.print(cycle + "\t");
						if (tag.length() >= 3) {
							cycle = tag.substring(2, 3);
							switch (cycle) {
							case "F" : itemEntity.setTrigger_state(0); break; // 无限制
							case "W" : itemEntity.setTrigger_state(2); break; // 周
							case "M" : itemEntity.setTrigger_state(3); break; // 月
							case "P" : itemEntity.setTrigger_state(4); break; // 半期
							case "Y" : itemEntity.setTrigger_state(5); break; // 全年
							default : throw new Exception("不合法的标签：" + cellText);
							}
						}
					} else if (tag.startsWith("T")) {
						// 单元格中的跳动
						itemEntity.setTab(Integer.parseInt(tag.substring(1)));
					} else if (tag.startsWith("I")) {
						// 操作者
						itemEntity.setData_type(1);
					} else if (tag.startsWith("L")) {
						// 线长
						itemEntity.setData_type(2);
					} else if (tag.startsWith("V")) {
						itemEntity.setFile_cycle_type(-1);
					} else if (tag.startsWith("U")) {
						if(linage != null) // 换行页面也处理
							itemEntity.setGap(Integer.parseInt(tag.substring(1)));
					} else if (tag.startsWith("M")) {
						itemEntity.setModel_relative(getReferData(referItems, tag.substring(1)));
					} else {
						throw new Exception("不合法的标签：" + cellText);
					}
				}
				// 替换成标签
				cellText = cellText.replaceAll(matchTag.replaceAll("\\[", "\\\\["), itemEntity.toXmlButtonTag());

				if (itemEntity.getData_type() == 2) {
					// 线长确认周期
					itemEntity.setCheck_file_manage_id(check_file_manage_id);
					mapper.setConfirmCycle(itemEntity);
				}

			} catch (Exception e) {
				_logger.error("解析"+cellText+"时发生错误:" + e.getMessage(), e);
				MsgInfo error = new MsgInfo();
				error.setComponentid("sheet_file_name");
				error.setErrmsg("解析"+cellText+"时发生错误:" + e.getMessage());
				errors.add(error);
			}
			System.out.println(cellText);
		}

//		 	  点检日期	#TPD[I[T6#	#TPD	6/21				
//		 	  	#TPC[I[T6#	#TPC	146P 6月21日				
		Pattern pCDateData = Pattern.compile("#T.*#");
		Matcher mCDateData = pCDateData.matcher(cellText);
		if (mCDateData.find()) {
			String matchTag = mCDateData.group();
			String matchText = matchTag.replaceAll("#", "");
			try {
				String[] tags = matchText.split("\\[");
				DeviceCheckItemEntity itemEntity = new DeviceCheckItemEntity();
				itemEntity.setTab(1);

				for (int i=0 ; i < tags.length; i++) {
					String tag = tags[i];
					if (i == 0) {
						// 取得周期信息
						String cycle = tag.substring(1, 2);
						switch (cycle) {
						case "D" : itemEntity.setCycle_type(1); break;
						case "W" : itemEntity.setCycle_type(2); break;
						case "M" : itemEntity.setCycle_type(3); break;
						case "P" : itemEntity.setCycle_type(4); break;
						default : throw new Exception("不合法的标签：" + cellText);
						}
						String format = tag.substring(2);
						switch (format) {
						case "D" : itemEntity.setData_type(1); break;
						case "C" : itemEntity.setData_type(2); break;
						case "L" : itemEntity.setData_type(3); break;
						default : throw new Exception("不合法的标签：" + cellText);
						}
						System.out.print(cycle + "\t");
					} else if (tag.startsWith("T")) {
						// 单元格中的跳动
						itemEntity.setTab(Integer.parseInt(tag.substring(1)));
					} else if (tag.startsWith("I")) {
						// 操作者
						itemEntity.setTrigger_state(1);
					} else if (tag.startsWith("L")) {
						// 单元格中的跳动
						itemEntity.setTrigger_state(2);
					} else if (tag.startsWith("V")) {
					} else if (tag.startsWith("U")) { // 换行页面不处理
						if(linage != null) // 换行页面也处理
							itemEntity.setGap(Integer.parseInt(tag.substring(1)));
					} else if (tag.startsWith("M")) {
						itemEntity.setModel_relative(getReferData(referItems, tag.substring(1)));
					} else {
						throw new Exception("不合法的标签：" + cellText);
					}
				}
				// 替换成标签
				cellText = cellText.replaceAll(matchTag.replaceAll("\\[", "\\\\["), itemEntity.toXmlDateTag());
			} catch (Exception e) {
				_logger.error("解析"+cellText+"时发生错误:" + e.getMessage(), e);
				MsgInfo error = new MsgInfo();
				error.setComponentid("sheet_file_name");
				error.setErrmsg("解析"+cellText+"时发生错误:" + e.getMessage());
				errors.add(error);
			}
			System.out.println(cellText);
		}
					
//		 	  使用开始	#G[USESTART[U1#	年　  月  　日					
//		 	  使用期限	#G[USELIMIT[U1#
		Pattern pGDateData = Pattern.compile("#G\\[USE.*#");
		Matcher mGDateData = pGDateData.matcher(cellText);
		if (mGDateData.find()) {
			String matchTag = mGDateData.group();
			String matchText = matchTag.replaceAll("#", "");
			try {
				String[] tags = matchText.split("\\[");
				for (String tag : tags) {
					if (tag.startsWith("USESTART")) {
						// 替换成标签
						cellText = cellText.replaceAll(matchTag.replaceAll("\\[", "\\\\["), "<useStart/>");
					} else if (tag.startsWith("USELIMIT")) {
						// 替换成标签
						cellText = cellText.replaceAll(matchTag.replaceAll("\\[", "\\\\["), "<useEnd/>");
						String period = "1";
						if (tag.length() > 8) {
							period = tag.substring(8);
						}
						cellText = cellText.replaceAll("<useEnd/>", "<useEnd period='" + period + "'/>");
					} else if (tag.startsWith("U")) {
						if(linage != null) {
							int ul = Integer.parseInt(tag.substring(1)); // 换行页面也处理
							cellText = cellText.replaceAll("/>", " gap='" + ul + "' line='0'/>");
						}
					}
				}
			} catch (Exception e) {
				_logger.error("解析"+cellText+"时发生错误:" + e.getMessage(), e);
			}
			// System.out.println(cellText);
		}
	 	  							
//		 	  签章	#J[DE100005#						
		Pattern pPJobData = Pattern.compile("#J\\[(.*)#");
		Matcher mPJobData = pPJobData.matcher(cellText);
		if (mPJobData.find()) {
			String matchTag = mPJobData.group();
			String matchText = mPJobData.group(1);
			cellText = cellText.replaceAll(matchTag.replaceAll("\\[", "\\\\["), "<img src=\"/images/sign/"+matchText+"\">");
		}

		cellText = cellText.replaceAll("#IMAGE\\[([^#]*)#", "<img src=\"/images/tcs/$1\"/>");

		return cellText;
	}

	private static String getReferData(Map<String, String> referItems, String substring) {
		return referItems.get(substring);
	}

	private static String getOfAct(String tag, int index) {
		String ret = "";
		while (index < tag.length()) {
			if(ret.length() == 0) {
				ret+=tag.charAt(index);
			} else {
				char c = tag.charAt(index);
				if (c >= 'A' && c <= 'Z') {
					break;
				} else {
					ret+=c;
				}
			}
			index ++;
		}
		return ret;
	}

	private static int getAttributeIntValue(Node attribute) {
		if (attribute == null) return 0;
		try {
			return Integer.parseInt(attribute.getNodeValue());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static float getAttributeFloatValue(Node attribute) {
		if (attribute == null) return 0;
		try {
			return Float.parseFloat(attribute.getNodeValue());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static String getAttributeStringValue(Node attribute) {
		if (attribute == null) return null;
		return attribute.getNodeValue();
	}

	private static String getAlignmentCss(NodeList alignment) {
		String ret="";
		if (alignment != null && alignment.getLength() > 0) {
			NamedNodeMap alignmentattributes = alignment.item(0).getAttributes();
			if (alignmentattributes.getNamedItem("ss:Horizontal") != null) {
				// Left, Center, Right, Fill
				String ss_Horizontal = alignmentattributes.getNamedItem("ss:Horizontal").getNodeValue();
				if ("Center".equals(ss_Horizontal) || "Right".equals(ss_Horizontal)) // "Left".equals(ss_Horizontal) ||默认
					ret += "H" + ss_Horizontal.substring(0, 1) + " ";
				if ("Fill".equals(ss_Horizontal))
					ret += "HC ";
			}
			if (alignmentattributes.getNamedItem("ss:Vertical") != null) {
				String ss_Vertical = alignmentattributes.getNamedItem("ss:Vertical").getNodeValue();
				if (!"Center".equals(ss_Vertical))// "Center".equals(ss_Vertical) ||默认
					ret += "V" + ss_Vertical.substring(0, 1) + " ";
			}
			if (alignmentattributes.getNamedItem("ss:WrapText") != null && !"0".equals(alignmentattributes.getNamedItem("ss:WrapText").getNodeValue())) {
				ret += "WT ";
			}
			if (alignmentattributes.getNamedItem("ss:VerticalText") != null
					&& "1".equals(alignmentattributes.getNamedItem("ss:VerticalText").getNodeValue()))  {// VerticalText
				ret += "IT ";
			}
		}
		return ret;
	}

	private static String getNumberFormatCss(NodeList numberFormat) {
		if (numberFormat != null && numberFormat.getLength() > 0) {
			NamedNodeMap attributes = numberFormat.item(0).getAttributes();
			Node ssFormat = attributes.getNamedItem("ss:Format");
			if (ssFormat != null && "0_);\\(0\\)".equals(ssFormat.getNodeValue())) {
				return " NBk";
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	private static String getBorderCss(NodeList borders) {
		Map<String, String> borderTypes = new HashMap<String, String>();
		borderTypes.put("Top", "T0");
		borderTypes.put("Bottom", "B0");
		borderTypes.put("Left", "L0");
		borderTypes.put("Right", "R0");

		if (borders != null && borders.getLength() > 0) {
			NodeList borderlist = ((Element) borders.item(0)).getElementsByTagName("Border");
			for (int j=0;j<borderlist.getLength();j++) {
				NamedNodeMap attributes = borderlist.item(j).getAttributes();
				String position = attributes.getNamedItem("ss:Position").getNodeValue();
				if (position != null) {
					Node LineWeight = attributes.getNamedItem("ss:Weight");
					String sLineWeight = "1";
					if (LineWeight != null) sLineWeight = LineWeight.getNodeValue();
					String styletext = position.substring(0, 1) 
								+ simplfy(attributes.getNamedItem("ss:LineStyle").getNodeValue()) 
								+ sLineWeight;
//					} catch(Exception e) {
//						System.out.println("NullPointerException");
//						throw e;
//					}
					if (!styleclasses.containsKey(styletext)) {
						styleclasses.put(styletext, generateBorderCss(position, attributes.getNamedItem("ss:LineStyle").getNodeValue(),  sLineWeight));
					}
					borderTypes.put(position, styletext);
				}
			}
		}
		return borderTypes.get("Top") + " " + borderTypes.get("Bottom") + " " + borderTypes.get("Left") + " " + borderTypes.get("Right") + " ";
	}

	private static String simplfy(String lineStyle) {
		if ("Continuous".equalsIgnoreCase(lineStyle)) {
			return "Sd";
		} else if ("Double".equalsIgnoreCase(lineStyle)) {
			return "Db";
		} else if ("Dash".equalsIgnoreCase(lineStyle)) {
			return "Ds";
		} else if ("Dot".equalsIgnoreCase(lineStyle) || "DashDot".equalsIgnoreCase(lineStyle) || "DashDotDot".equalsIgnoreCase(lineStyle) || "SlantDashDot".equalsIgnoreCase(lineStyle)) {
			return "Dt";
		} else {
			return "No";
		}
	}

	private static String generateBorderCss(String position, String lineStyle, String weight) {
		String retCss = "border-";
		retCss += position.toLowerCase() + ": " + weight + "px ";
		// None, Continuous, Dash, Dot, DashDot, DashDotDot, SlantDashDot, Double
		if ("Continuous".equalsIgnoreCase(lineStyle)) {
			retCss += "solid ";
		} else if ("Double".equalsIgnoreCase(lineStyle)) {
			retCss += "double ";
		} else if ("Dash".equalsIgnoreCase(lineStyle)) {
			retCss += "dashed ";
		} else if ("Dot".equalsIgnoreCase(lineStyle) || "DashDot".equalsIgnoreCase(lineStyle) || "DashDotDot".equalsIgnoreCase(lineStyle) || "SlantDashDot".equalsIgnoreCase(lineStyle)) {
			retCss += "dotted ";
		}
		retCss += ";";
		return retCss;
	}

	private static String getcss(Node style) {
		String styleID = getAttributeStringValue(style.getAttributes().getNamedItem("ss:StyleID"));

		if (styleID == null || !styleidmap.containsKey(styleID)) {
			return "";
		} else {
			return " class='" + styleidmap.get(styleID) + "' ";
		}
	}

    public static String decodeHtmlText(String text) {
    	if (text == null) return text;

    	return text.replaceAll("<(.+?)>", "&lt;$1&gt;").replaceAll(" ", "&nbsp;").replaceAll("\n", "<br>").replaceAll("T00:00:00\\.000", "");
    }

	private class ShiftData {
		private String content;
		private String seq = "";
		private Integer tab = 0;
		private Integer[] cols = {};
		private ShiftData rowShift = null;
		public ShiftData clone(){
			ShiftData ret = new ShiftData();
			ret.content = this.content;
			ret.seq = this.seq;
			ret.tab = new Integer(this.tab);
			ret.cols = new Integer[this.cols.length];
			for (int iv = 0 ; iv < this.cols.length ; iv++) {
				ret.cols[iv] = new Integer(this.cols[iv]);
			}
			if (this.rowShift != null) {
				ret.rowShift = this.rowShift.clone();
			};
			return ret;
		}
	}

	public static SqlSessionManager getTempWritableConn() {
		_logger.info("new Connnection");
		@SuppressWarnings("static-access")
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}
}

