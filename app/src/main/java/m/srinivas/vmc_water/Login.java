package m.srinivas.vmc_water;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Login extends Activity implements View.OnClickListener {
    Button btn_login;
    EditText input_usename, input_password;
    LinearLayout title_ll;
    ImageView applogo;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grievance__register);
        btn_login = (Button) findViewById(R.id.btn_login);
        applogo = (ImageView) findViewById(R.id.applogo);
        btn_login.setOnClickListener(this);
        input_usename = (EditText) findViewById(R.id.input_usename);
        input_password = (EditText) findViewById(R.id.input_password);
        title_ll = (LinearLayout) findViewById(R.id.title_ll);
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin);
        title_ll.startAnimation(slideUp);
        applogo.startAnimation(
                AnimationUtils.loadAnimation(Login.this, R.anim.rotation));


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (input_usename.getText().length() == 0) {
                    showalert("UserId should not be empty", "d");

                } else if (input_password.getText().length() == 0) {
                    showalert("Password should not be empty", "d");

                } else {
                    progress = new ProgressDialog(this);
                    progress.setMessage("Authenticating User..");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.setCancelable(false);
                    progress.show();
                    if (validations.isConnectedToInternet(Login.this)) {
                       // new Login.getstatus("AEW-1119", "AEW-1119").execute();
                        new Login.getstatus(input_usename.getText().toString(), input_password.getText().toString()).execute();
                        // Toast.makeText(getBaseContext(),"internet connected",Toast.LENGTH_SHORT).show();
                    } else {
                        progress.dismiss();
                        showalert("Please Check Your Internet Connection...!!", "d");
                    }
                }
                break;
        }
    }

    void hidekeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input_usename, InputMethodManager.SHOW_IMPLICIT);
    }

    private class getstatus extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;
        private ArrayList<NameValuePair> nameValuePairs;
        private JSONObject json;
        String id, stage;

        public getstatus(String officerid, String stage) {
            this.id = officerid;
            this.stage = stage;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... arg0) {
            nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("OffUserid", id));
            nameValuePairs.add(new BasicNameValuePair("OffPassword", stage));
            json = JSONParser.makeServiceCall("http://www.vmc103.org/Water/LoginService.aspx", 1, nameValuePairs);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            // Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_SHORT).show();
            //  progress.dismiss();
            try {
                if (json.getString("status").equals("1")) {
                    JSONArray jsonObject = json.getJSONArray("users");
                    for (int i = 0; i < jsonObject.length(); i++) {
                        JSONObject value = jsonObject.getJSONObject(i);
                        SharedPreferences sharedPreferences = getSharedPreferences("Userinfo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("intUserid", value.getString("intUserid"));
                        editor.putString("username", value.getString("username"));
                        editor.putString("user_id", value.getString("user_id"));
                        editor.putString("userlevel", value.getString("userlevel"));
                        editor.putString("intDepartmentid", value.getString("intDepartmentid"));
                        editor.putString("DepartmentName", value.getString("DepartmentName"));
                        editor.putString("intOfficerid", String.valueOf(value.getInt("intOfficerid")));
                        editor.commit();
                        Intent registration = new Intent(Login.this, Home.class);
                        startActivity(registration);
                    }
                 /*   RecyclerView.Adapter adapter = new DrilldownRecycler(drilldowns,DashboardView.this);
                    dashboardDril_list.setAdapter(adapter);*/
                } else {
                    //showalert("Server Busy At This Moment !!","hai");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean internet() {
        boolean connected = false;
        try
        { ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                //we are connected to a network
                connected = true;
            } else
                connected = false;


        }catch (Exception e){
           connected = true;
        }

        return connected;
    }

    void showalert(String alert_msg, final String show) {
        // {"result":[{"CreateEvent":"success","status":1,"createevent_id":"2018-VMCW-1000"}]}

        final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(Login.this);
        alertDialogBuilder.setTitle("VMC Water Grievance");
        alertDialogBuilder.setMessage(alert_msg);
        alertDialogBuilder.setIcon(R.drawable.aplogo);
        // set dialog message

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        // create alert dialog
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }


}
