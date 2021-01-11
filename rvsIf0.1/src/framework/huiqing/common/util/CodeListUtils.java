package framework.huiqing.common.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CodeListUtils {

	private static final String NODE_NAME = "name";
	private static final String NODE_VALUE = "value";
	private static final String NODE_CODE = "code";

	private static Map<String, Map<String, String>> codelists;

	private static Logger logger = Logger.getLogger("CODE_LIST");

	static {
		readFile();
	}

	/**
	 * 
	 * @param codeGroup
	 *            コードリスト名
	 * @param codeId
	 *            コード
	 * @param nullOption
	 *            空白項目のコード （nullの場合ブランク項目がいらない）
	 * @param isSelectUp
	 *            選択した項目はトップにするか
	 * @return
	 */
	public static String getSelectOptions(String codeGroup, String codeId, String nullOption, boolean isSelectUp) {
		// 選択項をトップにする、IDはnullの場合
		if (codeId == null && isSelectUp) {
			// 選択肢を返す
			return "<option value=\"\">Code id is not selected!</option>";
		}

		// 選択肢
		String output = "";

		String nullCode = "";
		// 空白項目を追加
		if (nullOption != null) {
			if (nullOption.contains("::")) {
				String[] nullSet = nullOption.split("::");
				nullCode = nullSet[0];
				nullOption = nullSet[1];
			}

			if (codeId != null && codeId.equals(nullOption)) {
				// 選択した空白な選択肢
				output += "<option value=\""+nullCode+"\" selected>" + nullOption + "</option>";
			} else {
				// 選択しない空白な選択肢
				output += "<option value=\""+nullCode+"\">" + nullOption + "</option>";
			}
		}

		// コードリストを取得
		Map<String, String> group = getList(codeGroup);

		if (group == null) {
			return output;
		}

		if (codeId != null) {
			for (String code : group.keySet()) {

				if (codeId.equals(code)) {
					// 選択した選択肢
					if (isSelectUp) {
						output = "<option value=\"" + code + "\" selected>" + group.get(code) + "</option>" + output;
					} else {
						output += "<option value=\"" + code + "\" selected>" + group.get(code) + "</option>";
					}
				} else {
					// 選択しない選択肢
					output += "<option value=\"" + code + "\">" + group.get(code) + "</option>";
				}

			}
		} else {
			for (String code : group.keySet()) {
				// 選択しない選択肢
				output += "<option value=\"" + code + "\">" + group.get(code) + "</option>";
			}
		}
		return output;
	}

	/**
	 * 
	 * @param codeMap
	 *            コードMap名
	 * @param codeId
	 *            コード
	 * @param nullOption
	 *            空白項目のコード （nullの場合ブランク項目がいらない）
	 * @param isSelectUp
	 *            選択した項目はトップにするか
	 * @return
	 */
	public static String getSelectOptions(Map<String, String> codeMap, String codeId, String nullOption,
			boolean isSelectUp) {
		// 選択項をトップにする、IDはnullの場合
		if (codeId == null && isSelectUp) {
			// 選択肢を返す
			return "<option value=\"\">Code id is not selected!</option>";
		}

		// 選択肢
		String output = "";

		// 空白項目を追加
		if (nullOption != null) {
			if (codeId != null && codeId.equals(nullOption)) {
				// 選択した空白な選択肢
				output += "<option value=\"\" selected>" + nullOption + "</option>";
			} else {
				// 選択しない空白な選択肢
				output += "<option value=\"\">" + nullOption + "</option>";
			}
		}

		// コードリストを取得
		Map<String, String> group = codeMap;

		if (group == null) {
			return output;
		}

		if (codeId != null) {
			for (String code : group.keySet()) {

				if (codeId.equals(code)) {
					// 選択した選択肢
					if (isSelectUp) {
						output = "<option value=\"" + code + "\" selected>"
								+ CommonStringUtil.decodeHtmlText(group.get(code)) + "</option>" + output;
					} else {
						output += "<option value=\"" + code + "\" selected>"
								+ CommonStringUtil.decodeHtmlText(group.get(code)) + "</option>";
					}
				} else {
					// 選択しない選択肢
					output += "<option value=\"" + code + "\">" + CommonStringUtil.decodeHtmlText(group.get(code))
							+ "</option>";
				}

			}
		} else {
			for (String code : group.keySet()) {
				// 選択しない選択肢
				output += "<option value=\"" + code + "\">" + CommonStringUtil.decodeHtmlText(group.get(code))
						+ "</option>";
			}
		}
		return output;
	}

	public static String getSelectOptions(String codeGroup, String codeId, String nullOption) {
		return getSelectOptions(codeGroup, codeId, nullOption, false);
	}

	public static String getSelectOptions(String codeGroup, String codeId, boolean isSelectUp) {
		return getSelectOptions(codeGroup, codeId, null, isSelectUp);
	}

	public static String getSelectOptions(String codeGroup, String codeId) {
		return getSelectOptions(codeGroup, codeId, null, false);
	}

	public static String getSelectOptions(String codeKey) {
		return getSelectOptions(codeKey, null, null, false);
	}

	/**
	 * @param codeGroup
	 *            コードリスト名
	 * @param codeId
	 *            コード
	 * @param nullText
	 *            nullの場合の出力
	 * @return
	 */
	public static String getValue(String codeGroup, String codeId, String nullText) {
		// 表示する文字を設定
		String output = null;

		// コードリストを取得
		Map<String, String> group = getList(codeGroup);

		if (group == null) {
			// からストリングを返す
			return "";
		}

		for (String code : group.keySet()) {
			if (code.equals(codeId)) {
				// 選択した値
				if (group.get(code) != null) {
					output = group.get(code);
				}
				break;
			}
		}

		// 選択しない場合
		if (output == null) {
			return nullText;
		}

		return output;
	}

	public static String getValue(String codeGroup, String codeId) {
		return getValue(codeGroup, codeId, "");
	}

	/**
	 * @param codeGroup
	 *            コードリスト名
	 * @param codeId
	 *            コード
	 * @param nullText
	 *            nullの場合の出力
	 * @return
	 */
	public static String getKeyByValue(String codeGroup, String value, String nullText) {
		// 表示する文字を設定
		String output = null;

		// コードリストを取得
		Map<String, String> group = getList(codeGroup);

		if (group == null) {
			// からストリングを返す
			return "";
		}

		for (String code : group.keySet()) {
			if (group.get(code).equals(value)) {
				output = code;
				break;
			}
		}

		// 選択しない場合
		if (output == null) {
			return nullText;
		}

		return output;
	}

	public static Map<String, String> getList(String codeGroup) {
		return codelists.get(codeGroup);
	}

	private static void readFile() {
		// Defines a factory API
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			// DocumentBuilderFactoryからDocumentBuilderインスタンスを取得
			DocumentBuilder db = dbf.newDocumentBuilder();
			String basedir = CodeListUtils.class.getResource("/").getPath().replaceAll("%20", " ");
			logger.info("CodeList's basedir:" + basedir);
			File file = new File(basedir + "codelist.xml");

			logger.info("file.exists():" + file.exists());

			// XML DOMを作成
			Document doc = db.parse(file);

			// codelistノードを取得
			NodeList nl = doc.getElementsByTagName("codelist");

			int nlLen = nl.getLength();

			codelists = new HashMap<String, Map<String, String>>();
			for (int i = 0; i < nlLen; i++) {
				Map<String, String> codeListDTO = new TreeMap<String, String>();
				Element eltGroup = (Element) nl.item(i);
				int nLen = eltGroup.getElementsByTagName(NODE_VALUE).getLength();
				for (int j = 0; j < nLen; j++) {
					Node eltValue = eltGroup.getElementsByTagName(NODE_VALUE).item(j);

					String nodeValue = "";
					Node fchild = eltValue.getFirstChild();
					if (fchild != null) {
						nodeValue = fchild.getNodeValue();
					}
					codeListDTO.put(eltValue.getAttributes().getNamedItem(NODE_CODE).getNodeValue(), nodeValue);

				}
				codelists.put(eltGroup.getAttributes().getNamedItem(NODE_NAME).getNodeValue(), codeListDTO);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} catch (SAXException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}

	public static String getGridOptions(String codeGroup) {
		Map<String, String> map = codelists.get(codeGroup);
		String ret = "";
		for (String key : map.keySet()) {
			ret += key + ":" + CommonStringUtil.decodeHtmlText(map.get(key)) + ";";
		}
		if (ret.length() > 0)
			ret = ret.substring(0, ret.length() - 1);

		return ret;
	}

	public static String getReferChooser(List<String[]> lines) {

		StringBuffer ret = new StringBuffer("");

		for (String[] line : lines) {
			if (line == null || line.length < 2)
				return "";
			ret = ret.append("<tr><td class='referId' style='display:none'>" + CommonStringUtil.decodeHtmlText(line[0]) + "</td>");
			ret = ret.append("<td><nobr>" + CommonStringUtil.decodeHtmlText(line[1]) + "</nobr></td>");
			for (int i = 2; i < line.length; i++) {
				ret = ret.append("<td>" + CommonStringUtil.decodeHtmlText(line[i]) + "</td>");
			}

			ret = ret.append("</tr>");
		}

		return ret.toString();
	}

	// public static Map<String, String> getList(String codeGroup) { TODO cached
	// QueryCache q = QueryCache.getInstance();
	// Map<String, String> ret = q.findCodeListFromCache(codeGroup);
	// if (ret == null) {
	// readFile();
	// ret = q.findCodeListFromCache(codeGroup);
	// }
	// return ret;
	// }
	//
	// private static void readFile() {
	// // Defines a factory API
	// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	//
	// try {
	// // DocumentBuilderFactoryからDocumentBuilderインスタンスを取得
	// DocumentBuilder db = dbf.newDocumentBuilder();
	// String basedir = CodeListUtils.class.getResource("/").getPath();
	// File file = new File(basedir + "codelist.xml");
	// // XML DOMを作成
	// Document doc = db.parse(file);
	// // codelistノードを取得
	// NodeList nl = doc.getElementsByTagName("codelist");
	// int nlLen = nl.getLength();
	// QueryCache q = QueryCache.getInstance();
	// for (int i = 0; i < nlLen; i++) {
	// Map<String, String> codeListDTO = new TreeMap<String, String>();
	// Element eltGroup = (Element) nl.item(i);
	// int nLen = eltGroup.getElementsByTagName(NODE_VALUE).getLength();
	// for (int j = 0; j < nLen; j++) {
	// Node eltValue = eltGroup.getElementsByTagName(NODE_VALUE)
	// .item(j);
	//
	// codeListDTO.put(eltValue.getAttributes().getNamedItem(NODE_CODE).getNodeValue(),
	// eltValue.getFirstChild().getNodeValue());
	//
	// }
	// q.putCodeListInCache(eltGroup.getAttributes().getNamedItem(NODE_NAME).getNodeValue(), codeListDTO);
	// }
	// } catch (ParserConfigurationException e) {
	// e.printStackTrace();
	// } catch (SAXException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
}
