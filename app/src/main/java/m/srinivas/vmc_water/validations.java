package m.srinivas.vmc_water;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class validations {
	public String validuser = "false";
	private static final String TAG = null;
	private static int internetStatus;
	private static String internetType;
	public static String deviceid = "";
	public static boolean signoutFlag;
	// public static String finalurl = "http://43.229.62.192/";
	public static String finalurl = "http://admin-mob.zonehigh.com";
	public static String playurl = finalurl + "/public/files/";
	// before
	public static String adddevice = "http://admin-mob.zonehigh.com/service/adddevice";

	public static String allmindsharpeners = finalurl
			+ "/service/allmindsharpeners";

	public static String allwhysharpners = "http://admin-mob.zonehigh.com/service/allwhysharpners";

	public static String addlogurl = finalurl + "/service/addlog";
	public static String getlogurl = finalurl + "/service/getlogs";
	public static String getcouponamount = finalurl
			+ "/service/getcouponamount";

	public static String addpaypal = finalurl + "/service/addpaypaltransaction";

	public static String getintroduction = finalurl
			+ "/service/getintroduction";
	public static String getInstruction = finalurl + "/service/getInstruction";
	public static String getbonus = finalurl + "/service/getbonus";
	public static String zonetipssubscribe = finalurl + "/service/subscribenew";
	public static String aboutUrl = finalurl + "/service/getaboutus";
	public static String getwhyupgrade = finalurl + "/service/getwhyupgrade";
	public static String addreminder = finalurl + "/service/addreminder";
	public static String getreminders = finalurl + "/service/getreminders";
	public static String getpdf = playurl + "/sellinginthezonev2.pdf";

	public static void MyAlertBox(final Context ctx, String alert_msg) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);

		// set title
		alertDialogBuilder.setTitle("e-Sangareddy");

		// set dialog message
		alertDialogBuilder.setMessage(alert_msg).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity

					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public static void hideKeyboard(Activity activity) {
		if (activity != null && activity.getWindow() != null
				&& activity.getWindow().getDecorView() != null) {
			InputMethodManager imm = (InputMethodManager) activity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(activity.getWindow().getDecorView()
					.getWindowToken(), 0);
		}
	}



	public static void MyAlertBoxIntent(final Context activity,
                                        String alert_msg, final Class inclass) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				activity);

		// set title
		alertDialogBuilder.setTitle(alert_msg);
		alertDialogBuilder.setIcon(R.drawable.aplogo);
		// set dialog message
		alertDialogBuilder.setMessage(alert_msg).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity

						Intent in = new Intent(activity, inclass);
						activity.startActivity(in);
						((Activity) activity).finish();

					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public static void MyAlertAlaramIntent(final Context activity,
                                           String alert_msg, final Class inclass) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				activity);

		// set title
		alertDialogBuilder.setTitle("MESSAGE");

		// set dialog message
		alertDialogBuilder.setMessage(alert_msg).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity

						Intent in = new Intent(activity, inclass);
						activity.startActivity(in);

					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public static boolean isConnectedToInternet(Context context) {
		Log.i(TAG, "Checking Internet Connection...");

		boolean found = false;

		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				found = true;
				internetStatus = 0;
			}

			NetworkInfo wifi = cm
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo _3g = cm
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (wifi.isConnected())
				internetType = "WiFi";

			if (_3g.isConnected())
				internetType = "3G";

		} catch (Exception e) {
			//Log.e("CheckConnectivity Exception", e.getMessage(), e);
		}

		if (found)
			Log.i(TAG, "Internet Connection found.");
		else
			Log.i(TAG, "Internet Connection not found.");

		return found;
	}

	public static boolean email(String name) {
		String spl = "!#$%^&*()+=-[]\\\';,/{}|\":<>?";
		if (name.length() > 2) {
			boolean flag = true;
			for (int i = 0; i < name.length(); i++) {
				if (spl.indexOf(name.charAt(i)) != -1) {
					flag = false;
					break;
				}
			}
			if (flag) {
				if (name.contains("@") && name.contains(".")) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getCurrentTime() {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
		// /SimpleDateFormat sdf = new SimpleDateFormat(str);
		String currentDateTimeString = sdf.format(d);
		System.out.println("currentDateTimeString: " + currentDateTimeString);
		return currentDateTimeString;
	}

	public static String convertTo12Hour(String Time) {
		System.out.println("Time: " + Time);
		String x = Time;
		if (Time.contains(":")) {

			DateFormat f1 = new SimpleDateFormat("HH:mm"); // "23:00"
			Date d = null;
			try {
				d = f1.parse(Time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DateFormat f2 = new SimpleDateFormat("hh:mm a");
			x = f2.format(d); // 11:00 pm
		}
		return x;
	}

	public static String convertTo24Hour(String Time) {
		String x = Time;
		if (Time.contains(":")) {
			DateFormat f1 = new SimpleDateFormat("hh:mm a"); // "23:00"
			Date d = null;
			try {
				d = f1.parse(Time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DateFormat f2 = new SimpleDateFormat("HH:mm");
			x = f2.format(d); // 11:00 pm
		}
		return x;
	}

	public static boolean usPhoneno(String PhoneNo) {
		// String PhoneNo = "+123-456 7890";
		boolean flag = false;
		String Regex = "[^\\d]";
		String PhoneDigits = PhoneNo.replaceAll(Regex, "");

		if (PhoneDigits.length() < 6 || PhoneDigits.length() > 13) {

			flag = true;
			// error message
		} else {
			PhoneNo = "+";
			PhoneNo = PhoneNo.concat(PhoneDigits); // adding the plus sign

			// validation successful

		}
		return flag;
	}

}
