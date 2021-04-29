package framework.huiqing.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XssShieldUtil {

    private static List<Pattern> patterns = null;

    static {
        List<Pattern> list = new ArrayList<Pattern>();

        String regex = null;
        Integer flag = null;
        int arrLength = 0;

        for(Object[] arr : getXssPatternList()) {
            arrLength = arr.length;
            for(int i = 0; i < arrLength; i++) {
                regex = (String)arr[0];
                flag = (Integer)arr[1];
                list.add(Pattern.compile(regex, flag));
            }
        }

        patterns = list;
    }

    private static List<Object[]> getXssPatternList() {
        List<Object[]> ret = new ArrayList<Object[]>();

        ret.add(new Object[]{"eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
        ret.add(new Object[]{"expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
        ret.add(new Object[]{"(javascript:|vbscript:|view-source:)*", Pattern.CASE_INSENSITIVE});
        return ret;
    }

    public static String stripXss(String value) {
        if(!CommonStringUtil.isEmpty(value)) {

            Matcher matcher = null;

            for(Pattern pattern : patterns) {
                matcher = pattern.matcher(value);
                // 匹配
                if(matcher.find()) {
                    // 删除相关字符串
                    value = matcher.replaceAll("");
                }
            }

            value = value.replaceAll("<", "＜").replaceAll(">", "＞").replaceAll("&", "＆");
        }

        return value;
    }
}
