package com.aethercoder.misc.qtum.walletTransaction;

import java.math.RoundingMode;
import java.text.DecimalFormat;

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
}
