package framework.huiqing.common.util;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonStringUtil {

	/**
	* 文字列の両端から空白を除去する。<br>
	* 半角空白だけでなく、全角空白も除去する。<br>
	* 対象文字列がNULLまたは空文字の場合は何もせず、そのまま返す。<br>
	*
	* @param inTarget String 対象文字列
	* @return String 結果文字列
	*/
	@SuppressWarnings("nls")
	public static String trim(String inTarget) {
	    String target = inTarget;
		if (target == null || target.length() == 0) {
			return "";
		}
		// 前頭の空白を除去
		while (target.startsWith(" ") || target.startsWith("　")) {
			target = target.substring(1, target.length());
		}
		// 末尾の空白を除去
		while (target.endsWith(" ") || target.endsWith("　")) {
			target = target.substring(0, target.length() - 1);
		}
		return target;
	}

	/**
	* 文字列のバイト数を取得する。<br>
	* 対象文字列がNULLの場合はゼロ返す。<br>
	*
	* @param target String 対象文字列
	* @return int バイト数
	*/
	public static int byteLength(String target) {
		if (target == null) {
			return 0;
		} else {
		    try {
				return target.getBytes(BaseConst.UTF_8).length;
		    } catch (UnsupportedEncodingException e) {
		        throw new RuntimeException(e);
		    }
		}
	}

	/**
	* 文字列のバイト数を取得する。<br>
	* 対象文字列がNULLの場合はゼロ返す。<br>
	*
	* @param target String 対象文字列
	* @return int バイト数
	*/
	public static int byteLengthSystem(String target) {
		if (target == null) {
			return 0;
		} else {
			return target.getBytes().length;
		}
	}

	/**
	* 文字列が入力されているかどうかをチェックする。<br>
	* チェックの前に内部的にトリミングを行う。<br>
	* 文字列がNULLまたは空文字のとき、trueを返す。<br>
	*
	* @param target String チェックする文字列
	* @return boolean
	*/
	@SuppressWarnings("nls")
	public static boolean isEmpty(String target) {
		if (target == null || target.equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * Nullまたは空文字を替え字に変換する。
	 * 引数がNullじゃない場合は何もしない。
	 *
	 * @param inStr 操作対象文字列
	 * @param inStr 替え字
	 * @return alterしたString
	 */
	@SuppressWarnings("nls")
	public static String nullToAlter(String inStr,String alter) {
	    String str = inStr;
		if (str == null || str.equals("")) {
			str = alter;
		}
		return str;
	}

    /**
     * Decapitalizes the text according to JavaBeans specification.
     * 
     * @param text
     *            the text
     * 
     * @return the decapitalized text
     */
    public static String capitalize(String text) {
        if (isEmpty(text)) {
            return text;
        }
        char chars[] = text.toCharArray();

        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * Decapitalizes the text according to JavaBeans specification.
     * 
     * @param text
     *            the text
     * 
     * @return the decapitalized text
     */
    public static String decapitalize(String text) {
        if (isEmpty(text)) {
            return text;
        }
        char chars[] = text.toCharArray();
        if (chars.length >= 2
            && Character.isUpperCase(chars[0])
            && Character.isUpperCase(chars[1])) {
            return text;
        }
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * 
     * @param text
     * @return ((text.replace(/<(.+?)>/gi,"&lt;$1&gt;")).replace(/ /gi,"&nbsp;")).replace(/\n/gi,"<br>");
     */
    public static String decodeHtmlText(String text) {
    	if (isEmpty(text)) return text;

    	return text.replaceAll("<(.+?)>", "&lt;$1&gt;").replaceAll(" ", "&nbsp;").replaceAll("\n", "<br>");
    }

	public static String joinBy(String connectString, String... spareStrings) {
		StringBuffer sb = new StringBuffer();
		for (String spareString : spareStrings) {
			if (!isEmpty(spareString)) {
				sb.append(spareString).append(connectString);
			}
		}
		if (connectString.length() > 0) {
			int del = sb.lastIndexOf(connectString);
			if (del >=0 ) {
				sb.delete(del, sb.length());
			}
		}
		return sb.toString();
	}

	/**
	 * 文字宽度截断
	 * @throws UnsupportedEncodingException 
	 */
    public static String cutTextOnByte(String str, String encoding, int chkLen) throws UnsupportedEncodingException {
        byte[] bs = str.getBytes(encoding);
        if (bs.length <= chkLen) {
            return str;
        }
        String returnStr = new String(bs, 0, chkLen + 1, encoding);
        return returnStr.substring(0, returnStr.length() - 1);
    }
    
    
    /**
	 * check是否是数字
	 * @param strNumber
	 * @return
	 */
    public static boolean checkNumber(String strNumber) {

        String strRegex = "^[0-9]+$";

        return startCheck(strRegex, strNumber);
    }
    
    /**
     * check是否是浮点型数据
     * @param strNumber
     * @return
     */
    public static boolean checkDecimal(String strNumber){
    	
    	 String strRegex = "^[0-9]*(.[0-9]+){0,1}$";

         return startCheck(strRegex, strNumber);
    }
	
	/**
	 * check是否是日期以及各式
	 * @param strCalendar
	 * @param strFormat
	 * @return
	 */
	public static boolean checkCalendar(String strCalendar, String strFormat) {

        boolean blResult = true;

        try {
            SimpleDateFormat objSimpleDateFormat = new SimpleDateFormat(strFormat);
            objSimpleDateFormat.setLenient(false);
            objSimpleDateFormat.format(objSimpleDateFormat.parse(strCalendar));

        } catch (Exception e) {

            blResult = false;
        }

        return blResult;
    }
	
	
	public static boolean startCheck(String reg, String string) {

        boolean blResult = false;

        Pattern objPattern = Pattern.compile(reg);
        Matcher objMatcher = objPattern.matcher(string);

        blResult = objMatcher.matches();

        return blResult;
    }


    /**
     * 补字符串到指定长度
     * @param source
     * @param filler
     * @param length
     * @param before true:前方补位 false:后方补位
     * @return
     */
	public static String fillChar(String source, char filler, int length,
			boolean before) {
		if (source == null) {
			return null;
		}
		while (source.length() < length) {
			if (before) {
				source = filler + source;
			} else {
				source += filler;
			}
		}
		return source;
	}
}
