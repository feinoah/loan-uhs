package com.jhh.jhs.loan.common.util;

import java.util.regex.Pattern;

/**
 * @author xingmin
 */
public class RegexUtil {
    private static Pattern isNumberPattern = Pattern.compile("^-?[0-9]+");

    public static boolean isNumber(String param) {
        if(isNumberPattern.matcher(param).matches()){
            return true;
        } else {
            return false;
        }
    }
}
