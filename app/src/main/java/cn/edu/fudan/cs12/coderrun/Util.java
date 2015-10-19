package cn.edu.fudan.cs12.coderrun;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Li on 2015/10/5.
 */
public class Util {

	public static boolean isMobileNum(String mobiles) {
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	public static boolean validPassword(String password) {
		return password.length() >= 6;
	}
}
