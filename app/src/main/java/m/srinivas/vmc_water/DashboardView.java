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
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DashboardView extends Activity implements View.OnClickListener {
    RecyclerView dashboardDril_list;
    RelativeLayout logout;
    ArrayList<Drilldown> drilldowns;
    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_view);
        clearPreferences();
        back = (ImageView)findViewById(R.id.back);
        dashboardDril_list = (RecyclerView)findViewById(R.id.dashboardDril_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        dashboardDril_list.setLayoutManager(layoutManager);

        drilldowns = new ArrayList<Drilldown>();
        //dashboardDril_list.addOnItemTouchListener(new DrawerItemClickListener());
        SharedPreferences sharedPreferences = getSharedPreferences("Userinfo", MODE_PRIVATE);
        if (internet()){
            // Toast.makeText(getBaseContext(),"internet connected",Toast.LENGTH_SHORT).show();
            new DashboardView.getstatus(sharedPreferences.getString("intOfficerid", ""),getIntent().getStringExtra("stage").toString()).execute();

        }else {
            showalert("Please Check Your Internet Connection...!!","notshow");
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DashboardView.this,Dashboard.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
           /* case R.id.logout:
                Intent i = new Intent(DashboardView.this,Login.class);
                startActivity(i);
                break;*/
        }
    }

/*
    private class DrawerItemClickListener implements RecyclerView.OnItemTouchListener {
        GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if(child != null && gestureDetector.onTouchEvent(e)) {
                int position = rv.getChildAdapterPosition(child);
              //  Toast.makeText(getBaseContext(),drilldowns.get(position).getApplicationno(),Toast.LENGTH_SHORT).show();
                Intent i = new Intent(DashboardView.this,updatestatus.class);
                i.putExtra("class","dashboardview");
                i.putExtra("app_no",drilldowns.get(position).getApplicationno().toString());
                startActivity(i);
                finish();

            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
*/

    private class getstatus extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;
        private ArrayList<NameValuePair> nameValuePairs;
        private JSONObject json;
        String id,stage;

        public getstatus(String officerid, String stage) {
            this.id = officerid;this.stage  = stage;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... arg0) {
            nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("intOfficerid", id));
            nameValuePairs.add(new BasicNameValuePair("Status", stage));
            json = JSONParser.makeServiceCall("http://www.vmc103.org/Water/GrievanceDashboardDrildownService.aspx", 1, nameValuePairs);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            // Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_SHORT).show();
            //  progress.dismiss();
            try {
                if (json.getString("status").equals("1")){
                    JSONArray jsonObject = json.getJSONArray("result");
                    for (int i = 0; i < jsonObject.length(); i++) {
                        JSONObject value = jsonObject.getJSONObject(i);
                        String xx = String.valueOf(jsonObject.length());
                          //  Toast.makeText(getApplicationContext(), xx.toString(), Toast.LENGTH_SHORT).show();

                        drilldowns.add(new Drilldown(value.getString("App_No"),value.getString("WardNo"),
                                value.getString("intGrivanceid")));

                    }
                    RecyclerView.Adapter adapter = new DrilldownRecycler(drilldowns,DashboardView.this);
                    dashboardDril_list.setAdapter(adapter);
                }else {
                    showalert("Server Busy At This Moment !!","hai");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                showalert("Server Busy At This Moment !!","hai");
            }
        }
    }
    void showalert(String alert_msg, final String show) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(DashboardView.this);
        alertDialogBuilder.setTitle("VMC Water Grievance");
        // alertDialogBuilder.setIcon(R.drawable.aplogo);
        // set dialog message
        alertDialogBuilder.setMessage(alert_msg).setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
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
    public Boolean internet(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

        return connected;
    }
    private void clearPreferences() {
        try {
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear YOUR_APP_PACKAGE_GOES HERE");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
