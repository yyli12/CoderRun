package cn.edu.fudan.cs12.coderrun;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

	// Leancloud config
	public final static String APPID = "Sbt4q1ytJ6hNlbA9hMlmgTiN";
	public final static String APPKEY = "6KhG7YTKcrqq8icVJFzsmWg0";

	public static String[] GENDER_STRINGS = {"male", "female"};


	public final static int SUCCESS = 0;
	// Error code
	public final static int DEFAULT_FAIL = -1;             // fail for other reason

	public final static int RUN_SERVICE_START = 1;
	public final static int RUN_SERVICE_PAUSE = 2;
	public final static int RUN_SERVICE_STOP = 3;


}
