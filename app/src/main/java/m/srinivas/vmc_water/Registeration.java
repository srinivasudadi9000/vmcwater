package m.srinivas.vmc_water;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class Registeration extends Activity implements View.OnClickListener {
    TextView title_tv, officer_name, depart_name;
    ImageView back_img, capture_img;
    Spinner department_spinner, ward_spinner;
    ArrayList<Department> departments;
    ArrayList<Ward> wards;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> dep, ward;
    String array[] = {"asdlfk", "dsd"};
    SharedPreferences sharedPreferences;
    String userChoosenTask, depart_str, ward_str, ward_no_str, image_str = "notcaptured", response_str = "dadi";
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    Bitmap scaledBitmap = null, bitmap;
    Button submit;
    ProgressDialog progress, progress_before;
    EditText input_location, input_des;
    Bitmap afterEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registeration);
        title_tv = (TextView) findViewById(R.id.title_tv);
        depart_name = (TextView) findViewById(R.id.depart_name);
        submit = (Button) findViewById(R.id.submit);
        input_location = (EditText) findViewById(R.id.input_location);
        input_des = (EditText) findViewById(R.id.input_des);
        officer_name = (TextView) findViewById(R.id.officer_name);
        back_img = (ImageView) findViewById(R.id.back_img);
        capture_img = (ImageView) findViewById(R.id.capture_img);
        capture_img.setOnClickListener(this);
        back_img.setOnClickListener(this);
        submit.setOnClickListener(this);
        title_tv.setText("Water Grievance Registration");
        department_spinner = (Spinner) findViewById(R.id.department_spinner);
        ward_spinner = (Spinner) findViewById(R.id.ward_spinner);

        sharedPreferences = getSharedPreferences("Userinfo", MODE_PRIVATE);
        officer_name.setText(sharedPreferences.getString("username", ""));
        depart_name.setText(sharedPreferences.getString("DepartmentName", ""));
        depart_str = sharedPreferences.getString("intDepartmentid", "");

        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin);
        title_tv.startAnimation(slideUp);
        departments = new ArrayList<Department>();
        wards = new ArrayList<Ward>();
        dep = new ArrayList<String>();
        ward = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE}, 0);
        }
        if (validations.isConnectedToInternet(Registeration.this)) {
            // new Registeration.getDepartment().execute();
            progress_before = new ProgressDialog(this);
            progress_before.setMessage("Wards Fetching..");
            progress_before.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress_before.setIndeterminate(true);
            progress_before.setCancelable(false);
            progress_before.show();
            new Registeration.getWard().execute();
        } else {
            showalert("Please Check Your Internet Connection", "hai");
        }
        department_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                depart_str = departments.get(position).getDep_id();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                depart_str = "";
            }
        });
        ward_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ward_str = wards.get(position).getIntWardid();
                ward_no_str = wards.get(position).getWardNo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ward_str = "";
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                finish();
                break;
            case R.id.capture_img:
                selectImage();
                break;
            case R.id.submit:
                if (ward_no_str.length() == 0) {
                    showalert("Select Ward No", "d");
                } else if (image_str.toString().equals("notcaptured")) {
                    showalert("Take Grievance Image", "d");
                } else if (input_location.getText().toString().length() == 0) {
                    showalert("Location Should Not Be Empty", "d");
                } else if (input_des.getText().toString().length() == 0) {
                    showalert("Description Should Not Be Empty", "d");
                } else {
                    progress = new ProgressDialog(this);
                    progress.setMessage("Request Processing..");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.setCancelable(false);
                    progress.show();
                    if (validations.isConnectedToInternet(Registeration.this)) {
                        GPSTracker gpsTracker = new GPSTracker(Registeration.this);
                        Location location = gpsTracker.getLocation();
                        if (afterEdit != null) {
                            new Registeration.upload().execute();

                        }
                    } else {
                        showalert("Please Check Your Internet Connection", "hai");
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    private class getDepartment extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;
        private ArrayList<NameValuePair> nameValuePairs;
        private JSONObject json;
        String id, stage;


        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... arg0) {
            nameValuePairs = new ArrayList<NameValuePair>();

            json = JSONParser.makeServiceCall("http://www.vmc103.org/Water/wsgetdeptdata.aspx", 1, nameValuePairs);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            // Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_SHORT).show();
            //  progress.dismiss();
            try {
                if (json.getString("status").equals("1")) {
                    JSONArray jsonObject = json.getJSONArray("result");
                    for (int i = 0; i < jsonObject.length(); i++) {
                        JSONObject value = jsonObject.getJSONObject(i);
                        dep.add(value.getString("DepartmentName"));
                        departments.add(new Department(value.getString("intDepartmentid"), value.getString("DepartmentName")));
                    }
                    arrayAdapter = new ArrayAdapter<String>(Registeration.this, android.R.layout.simple_spinner_dropdown_item,
                            dep);
                    department_spinner.setAdapter(arrayAdapter);
                } else {
                    //showalert("Server Busy At This Moment !!","hai");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class getWard extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;
        private ArrayList<NameValuePair> nameValuePairs;
        private JSONObject json;
        String id, stage;


        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... arg0) {
            nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("intOfficerid", sharedPreferences.getString("intOfficerid", "")));
            json = JSONParser.makeServiceCall("http://www.vmc103.org/Water/wsgetWarddata.aspx", 1, nameValuePairs);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            // Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_SHORT).show();
            progress_before.dismiss();
            try {
                if (json.getString("status").equals("1")) {
                    JSONArray jsonObject = json.getJSONArray("result");
                    for (int i = 0; i < jsonObject.length(); i++) {
                        JSONObject value = jsonObject.getJSONObject(i);
                        ward.add(value.getString("WardNo"));
                        wards.add(new Ward(value.getString("intWardid"), value.getString("WardNo")));
                    }
                    arrayAdapter = new ArrayAdapter<String>(Registeration.this, android.R.layout.simple_spinner_dropdown_item,
                            ward);
                    ward_spinner.setAdapter(arrayAdapter);
                } else {
                    //showalert("Server Busy At This Moment !!","hai");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(Registeration.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {

                    userChoosenTask = "Take Photo";
                    cameraIntent();
                } else if (options[item].equals("Choose from Gallery")) {
                    userChoosenTask = "Choose from Library";
                    galleryIntent();
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        GPSTracker gpsTracker = new GPSTracker(Registeration.this);
        Location location = gpsTracker.getLocation();
        String lat = "0.0", lat_long = "0.0";
        if (location != null) {
            lat = String.valueOf(location.getLatitude() + "  ");
            lat_long = String.valueOf(location.getLongitude());
        }
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c.getTime());

        afterEdit = drawTextToBitmap(Registeration.this, lat, lat_long, strDate, thumbnail);
        // bin_photo_img.setMaxWidth(300);
        //bin_photo_img.setMaxHeight(300);
        capture_img.setScaleType(ImageView.ScaleType.FIT_XY);
        capture_img.setImageBitmap(afterEdit);
        image_str = "selected";
     /* *//*  Uri tempUri = getImageUri(updatestatus.this, thumbnail);

        // CALL THIS METHOD TO GET THE ACTUAL PATH
        File finalFile = new File(getRealPathFromURI(tempUri));*//*
        bitmap = (Bitmap) data.getExtras().get("data");
        // compressImage(finalFile.getAbsolutePath().toString());
        // ivImage.setImageBitmap(scaledBitmap);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // capture_img.setMaxWidth(150);
        //capture_img.setMaxHeight(150);
        capture_img.setScaleType(ImageView.ScaleType.FIT_XY);
        capture_img.setImageBitmap(bitmap);
        scaledBitmap = bitmap;*/

    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());

              /*  Uri tempUri = getImageUri(getApplicationContext(), bitmap);

                File finalFile = new File(getRealPathFromURI(tempUri));
                //  compressImage(finalFile.getAbsolutePath().toString());*/

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // capture_img.setMaxWidth(150);
                //capture_img.setMaxHeight(150);
                // new MainActivity.JSONParsedoitfast(scaledBitmap,"one").execute();
                capture_img.setScaleType(ImageView.ScaleType.FIT_XY);
                scaledBitmap = bitmap;
                capture_img.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.d("myimage", encodedImage.toString());
        return encodedImage;


    }

    private class upload extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;
        private ArrayList<NameValuePair> nameValuePairs;
        private JSONObject json;
        String id, stage;


        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... arg0) {
            GPSTracker gpsTracker = new GPSTracker(Registeration.this);
            Location location = gpsTracker.getLocation();
            String lat = "0.0", lat_long = "0.0";
            if (location != null) {
                lat = String.valueOf(location.getLatitude() + "  ");
                lat_long = String.valueOf(location.getLongitude());
            }
            nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("intWardid", ward_str));
            nameValuePairs.add(new BasicNameValuePair("intDepartmentid", depart_str));
            nameValuePairs.add(new BasicNameValuePair("WardNo", ward_no_str));
            nameValuePairs.add(new BasicNameValuePair("LocalityName", input_location.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("GrievanceDesc", input_des.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("GBLatitude", lat));
            nameValuePairs.add(new BasicNameValuePair("GBLangitude", lat_long));
            nameValuePairs.add(new BasicNameValuePair("intOfficerid", sharedPreferences.getString("intOfficerid", "")));
            nameValuePairs.add(new BasicNameValuePair("GPhotoName", "file"));
            nameValuePairs.add(new BasicNameValuePair("GPhotoPath", getStringImage(afterEdit)));


            json = JSONParser.makeServiceCall("http://www.vmc103.org/Water/InsertofGreivance.aspx", 2, nameValuePairs);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            // Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_SHORT).show();
            progress.dismiss();
            try {
                JSONArray jsonObject = json.getJSONArray("result");
                for (int i = 0; i < jsonObject.length(); i++) {
                    JSONObject jsn = jsonObject.getJSONObject(i);
                    response_str = jsn.getString("status");
                    if (jsn.getString("status").equals("1")) {
                        showalert("Successfully Grievance Registered", response_str);
                    } else {
                        showalert("Server Busy At This Moment !!", "hai");
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void showalert(String alert_msg, final String show) {
        // {"result":[{"CreateEvent":"success","status":1,"createevent_id":"2018-VMCW-1000"}]}

        final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(Registeration.this);
        alertDialogBuilder.setTitle("VMC Water Grievance");
        alertDialogBuilder.setIcon(R.drawable.aplogo);
        alertDialogBuilder.setMessage(alert_msg);
        // set dialog message
        alertDialogBuilder.setMessage(alert_msg).setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (response_str.equals("1")) {
                            Intent refresh = new Intent(Registeration.this, Registeration.class);
                            startActivity(refresh);
                            finish();
                        }
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

    public Bitmap drawTextToBitmap(Context mContext, String lat, String longitude, String cdate, Bitmap bitmap) {
        try {
            String mText = "lat: " + lat + "long: " + longitude;
            Resources resources = mContext.getResources();
            float scale = resources.getDisplayMetrics().density;
            Bitmap.Config bitmapConfig = bitmap.getConfig();
            // set default bitmap config if none
            if (bitmapConfig == null) {
                bitmapConfig = Bitmap.Config.ARGB_8888;
            }
            // resource bitmaps are imutable,
            // so we need to convert it to mutable one
            bitmap = bitmap.copy(bitmapConfig, true);
            Canvas canvas = new Canvas(bitmap);
            // new antialised Paint
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            // text color - #3D3D3D
            paint.setColor(Color.RED);
            // text size in pixels
            paint.setTextSize((int) (4 * scale));
            paint.setTextAlign(Paint.Align.LEFT);
            // text shadow
            //  paint.setShadowLayer(1f, 0f, 1f, Color.RED);
            // draw text to the Canvas center
            Rect bounds = new Rect();
            paint.getTextBounds(mText, 0, mText.length(), bounds);
            int x = (bitmap.getWidth() - bounds.width()) / 2;
            int y = (bitmap.getHeight() + bounds.height()) / 2;
            //change height
            // y = y - y / 2;
            canvas.drawText(" LAT: " + lat, x * scale / 55, y / 7, paint);
            canvas.drawText(" LONG: " + longitude, x * scale / 55, y / 3, paint);
            canvas.drawText("     " + cdate, x * scale / 45, y / 2, paint);
            return bitmap;
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }
}
