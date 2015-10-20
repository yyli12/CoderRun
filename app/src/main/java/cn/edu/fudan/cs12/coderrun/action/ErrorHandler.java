package cn.edu.fudan.cs12.coderrun.action;

import android.annotation.TargetApi;
import android.os.Build;

import com.avos.avoscloud.AVException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import cn.edu.fudan.cs12.coderrun.event.GeneralEvent;

/**
 * Created by Li on 2015/10/5.
 */
class ErrorHandler {

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static GeneralEvent getErrorEvent(AVException event, Class<? extends GeneralEvent> clazz) {
		if (event == null)
			return null;
		if (hasEventCode(event.getCode(), clazz)) {
			try {
				Constructor<? extends GeneralEvent> constructor = clazz.getConstructor(int.class);
				return constructor.newInstance(event.getCode());
			} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Constructor<? extends GeneralEvent> constructor = clazz.getConstructor(String.class);
				return constructor.newInstance(event.getMessage());
			} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressWarnings("unchecked")
	private static boolean hasEventCode(int code, Class<? extends GeneralEvent> clazz) {
		try {
			Field field = clazz.getField("errorSet");
			Set<Integer> set = (Set<Integer>) field.get(null);
			return set.contains(code);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
	}
}
