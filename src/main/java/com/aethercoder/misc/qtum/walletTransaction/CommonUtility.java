package com.aethercoder.misc.qtum.walletTransaction;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CommonUtility {
    /**
     * 按四舍五入保留指定小数位数，小数点后仅保留有效位数
     *
     * @param d 格式化前的小数
     * @return 格式化后的小数
     */
    public static String formatQTUMDecimal(double d) {
        return formatDecimal(d, Constants.QTUM_DECIMAL);
    }

    public static String formatDecimal(double d, int newScale) {
        if (newScale != 0) {
            String pattern = "#.";
            for (int i = 0; i < newScale; i++) {
                pattern += "#";
            }
            DecimalFormat df = new DecimalFormat(pattern);
            df.setRoundingMode(RoundingMode.FLOOR);
            return df.format(d);
        } else {
            return "0";
        }
    }

    private static String hashPattern = "0000000000000000000000000000000000000000000000000000000000000000";
    public static String paddingAfter(String _value) {
        if(_value.length() > 64){
            StringBuilder sb = new StringBuilder();
            int length = _value.length();
            int padding = length % 64;
            sb.append(_value);
            sb.append(hashPattern.substring(padding));
            return sb.toString();
        }
        return _value + hashPattern.substring(_value.length());
    }

    public static String paddingBefore(String _value){
        if(_value.length() > 64){
            StringBuilder sb = new StringBuilder();
            int length = _value.length();
            int padding = length % 64;
            sb.append(hashPattern.substring(padding));
            sb.append(_value);
            return sb.toString();
        }
        return hashPattern.substring(_value.length()) + _value;
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString
     *            原始字符串
     * @param length
     *            指定长度
     * @return
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString
     *            原始字符串
     * @param length
     *            指定长度
     * @param size
     *            指定列表大小
     * @return
     */
    public static List<String> getStrList(String inputString, int length,
                                          int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     *
     * @param str
     *            原始字符串
     * @param f
     *            开始位置
     * @param t
     *            结束位置
     * @return
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }


}
