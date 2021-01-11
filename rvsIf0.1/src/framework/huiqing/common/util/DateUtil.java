package framework.huiqing.common.util;

/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import framework.huiqing.common.util.CommonStringUtil;


/**
 * A utility class for {@link Date}.
 * 
 * @author higa
 * @version 3.0
 */
public final class DateUtil {

    /**
     * The date pattern for ISO-8601.
     */
    public static final String ISO_DATE_PATTERN = "yyyy-MM-dd";

    /**
     * The time pattern for ISO-8601.
     */
    public static final String ISO_TIME_PATTERN = "HH:mm:ss";

    /**
     * The date time pattern for ISO-8601.
     */
    public static final String ISO_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * The datetime pattern for SYSTEM.
     */
    public static final String DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";

    /**
     * The date pattern for SYSTEM.
     */
    public static final String DATE_PATTERN = "yyyy/MM/dd";

    /**
     * The date pattern for SYSTEM.
     */
    public static final String DATE_PATTERN_YYYYMMDD = "yyyyMMdd";

    /**
     * The date pattern for SYSTEM.
     */
    public static final String TIME_PATTERN_HHMMSS = "HHmmss";

    /**
     * The date pattern for SYSTEM.
     */
    public static final String DATE_PATTERN_YYYYMMDDHHMMSS = "yyyyMMdd HHmmss";

    /**
     * Converts the text to {@link Date}.
     * 
     * @param text
     *            the text
     * @param pattern
     *            the pattern for {@link SimpleDateFormat}
     * @return the converted value
     * @throws NullPointerException
     *             if the pattern parameter is null
     * @throws WrapRuntimeException
     *             if an error occurred while parsing the text
     */
    public static Date toDate(String text, String pattern)
            throws NullPointerException {
        if (pattern == null) {
            throw new NullPointerException("The pattern parameter is null.");
        }
        if (CommonStringUtil.isEmpty(text)) {
            return null;
        }
        try {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            df.setTimeZone(TimeZone.getDefault()); // TODO
            return df.parse(text);
        } catch (ParseException cause) {
        	return null;
        }
    }

    /**
     * Converts the {@link Date} value to text.
     * 
     * @param value
     *            the {@link Date} value
     * @param pattern
     *            the pattern for {@link SimpleDateFormat}
     * @return the converted value
     * @throws NullPointerException
     *             if the pattern parameter is null
     */
    public static String toString(Date value, String pattern)
            throws NullPointerException {
        if (pattern == null) {
            throw new NullPointerException("The pattern parameter is null.");
        }
        if (value == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getDefault()); // TODO
        return df.format(value);
    }

    public static Integer compareDate(Date value1, Date value2) throws NullPointerException {
    	if (value1 == null || value2 == null) return null;
    	return toString(value1, ISO_DATE_PATTERN).compareTo(toString(value2, ISO_DATE_PATTERN));
    }

    private DateUtil() {
    }
}