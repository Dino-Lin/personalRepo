package com.dino.common.util;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateFormatUtils;

public class StringUtils extends org.apache.commons.lang.StringUtils {
    // Constants used by escapeHTMLTags
    private static final char[] QUOTE_ENCODE = "&quot;".toCharArray();
    private static final char[] AMP_ENCODE = "&amp;".toCharArray();
    private static final char[] LT_ENCODE = "&lt;".toCharArray();
    private static final char[] GT_ENCODE = "&gt;".toCharArray();
    private static final char[] zeroArray = "0000000000000000000000000000000000000000000000000000000000000000"
            .toCharArray();

    /**
     * Formats a Date as a fifteen character long String made up of the Date's
     * padded millisecond value.
     * 
     * @return a Date encoded as a String.
     */
    public static String dateToMillis(Date date) {
        return zeroPadString(Long.toString(date.getTime()), 15);
    }

    /**
     * Pads the supplied String with 0's to the specified length and returns the
     * result as a new String. For example, if the initial String is "9999" and
     * the desired length is 8, the result would be "00009999". This type of
     * padding is useful for creating numerical values that need to be stored
     * and sorted as character data. Note: the current implementation of this
     * method allows for a maximum <tt>length</tt> of 64.
     * 
     * @param string
     *            the original String to pad.
     * @param length
     *            the desired length of the new padded String.
     * @return a new String padded with the required number of 0's.
     */
    public static String zeroPadString(String string, int length) {
        if (string == null || string.length() > length) {
            return string;
        }
        StringBuilder buf = new StringBuilder(length);
        buf.append(zeroArray, 0, length - string.length()).append(string);
        return buf.toString();
    }

    /**
     * This method takes a string which may contain HTML tags (ie, &lt;b&gt;,
     * &lt;table&gt;, etc) and converts the '&lt'' and '&gt;' characters to
     * their HTML escape sequences. It will also replace LF with &lt;br&gt;.
     * 
     * @param in
     *            the text to be converted.
     * @return the input string with the characters '&lt;' and '&gt;' replaced
     *         with their HTML escape sequences.
     */
    public static String escapeHTMLTags(String in) {
        if (in == null) {
            return null;
        }
        char ch;
        int i = 0;
        int last = 0;
        char[] input = in.toCharArray();
        int len = input.length;
        StringBuilder out = new StringBuilder((int) (len * 1.3));
        for (; i < len; i++) {
            ch = input[i];
            if (ch > '>') {
            } else if (ch == '<') {
                if (i > last) {
                    out.append(input, last, i - last);
                }
                last = i + 1;
                out.append(LT_ENCODE);
            } else if (ch == '>') {
                if (i > last) {
                    out.append(input, last, i - last);
                }
                last = i + 1;
                out.append(GT_ENCODE);
            } else if (ch == '\n') {
                if (i > last) {
                    out.append(input, last, i - last);
                }
                last = i + 1;
                out.append("<br>");
            }
        }
        if (last == 0) {
            return in;
        }
        if (i > last) {
            out.append(input, last, i - last);
        }
        return out.toString();
    }

    /**
     * Format mills String to Date
     * 
     * @param millsStr
     * @return Date
     */
    public static Date stringToDate(String millsStr) {
        Calendar cal = Calendar.getInstance();
        if (StringUtils.contains(millsStr, "-")) {
            millsStr = "-" + StringUtils.substringAfter(millsStr, "-");
        }
        cal.setTimeInMillis(Long.parseLong(millsStr));
        return cal.getTime();
    }

    /**
     * Format mills String to Calendar
     * 
     * @param millsStr
     * @return Calendar
     */
    public static Calendar stringToCalendar(String millsStr) {
        Calendar cal = Calendar.getInstance();
        if (StringUtils.contains(millsStr, "-")) {
            millsStr = "-" + StringUtils.substringAfter(millsStr, "-");
        }
        cal.setTimeInMillis(Long.parseLong(millsStr));
        return cal;
    }

    /**
     * 除去字符串中的空格、回车、换行符、制表符
     * 
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(str);
        String after = m.replaceAll("");
        return after;
    }

    /**
     * js日历控件日期转换为字符串类型的mills
     * 
     * @param jsCalendar
     *            js日期 支持格式为20100520,2010-05-20
     * @param separator
     *            分隔符 如格式为20100520 则为空 格式为2010-05-20则为"-"
     * @return 如果jsCalendar为空则返回空字符串
     */
    public static String jsCalendarToString(String jsCalendar, String separator) {
        Calendar cal = Calendar.getInstance();
        if (StringUtils.isBlank(jsCalendar)) {
            return "";
        }
        if (separator != null && !separator.trim().equals("")) {
            String[] jsCalendarArr = StringUtils.split(jsCalendar, separator);
            cal.set(Integer.parseInt(jsCalendarArr[0]), Integer.parseInt(jsCalendarArr[1]) - 1,
                    Integer.parseInt(jsCalendarArr[2]));
        } else {
            cal.set(Integer.parseInt(StringUtils.substring(jsCalendar, 0, 4)),
                    Integer.parseInt(StringUtils.substring(jsCalendar, 4, 6)) - 1,
                    Integer.parseInt(StringUtils.substring(jsCalendar, 6, 8)));
        }
        return StringUtils.dateToMillis(cal.getTime());
    }

    /**
     * 字符串转换成日历控件日期
     * 
     * @param mills
     * @param pattern
     * @return
     */
    public static String stringToJsCalendar(String mills, String pattern) {
        if (StringUtils.isBlank(mills)) {
            return "";
        }
        return DateFormatUtils.format(StringUtils.stringToCalendar(mills), pattern);
    }

    public static boolean isNumeric(String num) {
        try {
            Double.parseDouble(num);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validateMoblie(String phone) {
        boolean rs = false;
        if (StringUtils.isEmpty(phone)) {
            return false;
        }
        if (matchingText("^0(([1-9]d)|([3-9]d{2}))d{8}$", phone)) {
            return true;
        }
        if (matchingText("^(13[0-9]|15[0-9]|18[7|8|9|6|5])\\d{4,8}$", phone)) {
            rs = true;
        }
        return rs;
    }

    private static boolean matchingText(String expression, String text) {
        Pattern p = Pattern.compile(expression); // 正则表达式
        Matcher m = p.matcher(text); // 操作的字符串
        boolean b = m.matches();
        return b;
    }

    /**
     * objectToString
     * 
     * @param obj
     * @return
     */
    public static String objectToString(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    /**
     * 
     * @param str
     * @param strFormatType
     * @param formatType
     * @return
     */
    public static String formatString(String str, String strFormatType, String formatType) {
        if (isEmpty(str)) {
            return "";
        }
        try {
            return new String(str.getBytes(strFormatType), formatType);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * encode
     * 
     * @param str
     * @param formatType
     *            GBK or UTF-8
     * @return
     */
    public static String encode(String str, String formatType) {
        String returnStr = "";
        if (isEmpty(str)) {
            return returnStr;
        }
        try {
            returnStr = URLEncoder.encode(str, formatType);
        } catch (Exception ex) {
            // ex.printStackTrace();
            returnStr = "";
        }
        return returnStr;
    }

    /**
     * isEmpty
     * 
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return null == str || str.trim().length() == 0;
    }

    /**
     * isNotEmpty
     * 
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static int getRandom(int min, int max) {
        Random r = new Random();
        int tmp = Math.abs(r.nextInt());
        return tmp % (max - min + 1) + min;
    }

    /**
     * 是否有中文字符
     * 
     * @param s
     * @return
     */
    public static boolean hasCn(String s) {
        if (s == null) {
            return false;
        }
        return countCn(s) > s.length();
    }

    /**
     * 计算GBK编码的字符串的字节数
     * 
     * @param s
     * @return
     */
    public static int countCn(String s) {
        if (s == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.codePointAt(i) < 256) {
                count++;
            } else {
                count += 2;
            }
        }
        return count;
    }

    public static String unescape(String src) {
        if (null == src)
            return null;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    public static String escape(String src) {
        if (null == src)
            return null;
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null)
            return null;
        if (fileName.equals("")) {
            return "";
        } else {
            String name = fileName.replaceAll("\\.(?![^.]+$)", "_");
            return name.replaceAll("\\/|\\/|\\||:|\\?|\\*|\"|<|>|\\p{Cntrl}", "_");
        }
    }
    
    public static String encodeGBK(String str){
        if(StringUtils.isNotBlank(str)){
            try {
                return new String(str.trim().getBytes(),"GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return str;
            }
        }
        return str;
    }
    
    public static String LastSiteCityCode(String getHostName){
		String cityCode="";
		String temp[] = getHostName.split("\\."); 
		String city=temp[0];
		if(city.equals("www")){
			cityCode="";
		}else if(city.equals("gd")){
			cityCode="44";
		}else if(city.equals("gz")){
			cityCode="4401";
		}
		return cityCode;
    }
}
