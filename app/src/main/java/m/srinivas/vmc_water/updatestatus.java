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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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

public class updatestatus extends Activity implements View.OnClickListener {
    TextView application_no, dep_name, officer_name_display, ward_number, locality_name_display, description, updat_logout;
    EditText remarks;
    ImageView image_display, redress_img_display, back, image_display_one, redress_img_display_one, image_display_two, redress_img_display_two;
    Spinner status_spinner;
    SharedPreferences sharedPreferences;
    String userChoosenTask, status_str, response_str = "33", image_str = "notcaptured";
    String lat = "0.0", lat_long = "0.0";
    ProgressDialog progress;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    Bitmap scaledBitmap = null, bitmap;
    Button update;
    Bitmap afterEdit = null, afterEdit_one = null, afterEdit_two = null;
    ;
    String captured_image = "others";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updatestatus);
        update = (Button) findViewById(R.id.update);
        back = (ImageView) findViewById(R.id.back);
        application_no = (TextView) findViewById(R.id.application_no);
        dep_name = (TextView) findViewById(R.id.dep_name);
        officer_name_display = (TextView) findViewById(R.id.officer_name_display);
        ward_number = (TextView) findViewById(R.id.ward_number);
        locality_name_display = (TextView) findViewById(R.id.locality_name_display);
        description = (TextView) findViewById(R.id.description);
        remarks = (EditText) findViewById(R.id.remarks);

        image_display_one = (ImageView) findViewById(R.id.image_display_one);
        redress_img_display_one = (ImageView) findViewById(R.id.redress_img_display_one);
        image_display_two = (ImageView) findViewById(R.id.image_display_two);
        redress_img_display_two = (ImageView) findViewById(R.id.redress_img_display_two);

        image_display = (ImageView) findViewById(R.id.image_display);
        redress_img_display = (ImageView) findViewById(R.id.redress_img_display);
        status_spinner = (Spinner) findViewById(R.id.status_spinner);
        updat_logout = (TextView) findViewById(R.id.updat_logout);
        back.setOnClickListener(this);
        updat_logout.setOnClickListener(this);
        update.setOnClickListener(this);
        redress_img_display.setOnClickListener(this);
        redress_img_display_one.setOnClickListener(this);
        redress_img_display_two.setOnClickListener(this);
        getIntent().getStringExtra("intGrivanceid");
        sharedPreferences = getSharedPreferences("Userinfo", MODE_PRIVATE);
        sharedPreferences.getString("intOfficerid", "");
        progress = new ProgressDialog(updatestatus.this);
        progress.setMessage("Fetching data from server..");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        if (validations.isConnectedToInternet(updatestatus.this)) {
            new updatestatus.getstatus(getIntent().getStringExtra("intGrivanceid")).execute();
        } else {
            progress.dismiss();
            showalert("Please Select Your Internet Connection", "hai");
        }

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        status_spinner.setAdapter(adapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE}, 0);
        }
        status_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                status_str = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                status_str = "--Select--";
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update:
                if (remarks.getText().toString().length() == 0) {
                    showalert("Please Enter Remarks", "hai");
                } else if (image_str.equals("notcaptured")) {
                    showalert("Please Select Image ", "hai");
                } else if (status_str.equals("--Select--")) {
                    showalert("Please Select Status", "hai");
                } else {
                    if (validations.isConnectedToInternet(updatestatus.this)) {
                        progress = new ProgressDialog(updatestatus.this);
                        progress.setMessage("Fetching data from server..");
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.setIndeterminate(true);
                        progress.setCancelable(false);
                        progress.show();
                        GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
                        Location location = gpsTracker.getLocation();
                        if (afterEdit != null) {
                            new updatestatus.upload().execute();
                        } else {
                            gpsTracker.showSettingsAlert();
                        }
                    } else {
                        showalert("Please Check Your Internet Connection", "hai");
                    }
                }
                break;
            case R.id.redress_img_display:
                captured_image = "one";
                selectImage();
                break;
            case R.id.redress_img_display_one:
                captured_image = "two";
                selectImage();
                break;
            case R.id.redress_img_display_two:
                captured_image = "three";
                selectImage();
                break;
            case R.id.back:
                finish();
                break;
            case R.id.updat_logout:
                SharedPreferences sharedPreferences = getSharedPreferences("Userinfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("intUserid", "");
                editor.putString("username", "");
                editor.putString("user_id", "");
                editor.putString("userlevel", "");
                editor.putString("intDepartmentid", "");
                editor.putString("DepartmentName", "");
                editor.putString("intOfficerid", "");
                editor.commit();
                Intent login = new Intent(updatestatus.this, Login.class);
                startActivity(login);
                break;
        }
    }

    private class getstatus extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;
        private ArrayList<NameValuePair> nameValuePairs;
        private JSONObject json;
        String id;

        public getstatus(String grievanceid) {
            this.id = grievanceid;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... arg0) {
            nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("intGrievanceid", id));
            nameValuePairs.add(new BasicNameValuePair("intOfficerid", sharedPreferences.getString("intOfficerid", "")));
            json = JSONParser.makeServiceCall("http://www.vmc103.org/Water/GrievanceDetbyNumber.aspx", 1, nameValuePairs);
            //asdfasdfsad
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            // Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_SHORT).show();
            progress.dismiss();
            try {
                if (json.getString("status").equals("1")) {
                    JSONArray jsonObject = json.getJSONArray("users");
                    for (int i = 0; i < jsonObject.length(); i++) {
                        JSONObject value = jsonObject.getJSONObject(i);
                        //    Toast.makeText(getApplicationContext(), value.getString("intUserid").toString(), Toast.LENGTH_SHORT).show();
                        application_no.setText(value.getString("App_No"));
                        // grievance_app_no = value.getString("App_No");
                        dep_name.setText(value.getString("DepartmentName"));
                        officer_name_display.setText(value.getString("OfficerName"));
                        ward_number.setText(value.getString("WardNo"));
                        locality_name_display.setText(value.getString("LocalityName"));
                        description.setText(value.getString("GrievanceDesc"));
                        remarks.setText(value.getString("remarks"));
                        if (value.getString("Status").equals("Pending")) {
                            // update.setVisibility(View.GONE);
                        } else {
                            update.setVisibility(View.GONE);
                            remarks.setEnabled(false);
                            if (value.getString("Status").equals("Redressed")) {
                                status_spinner.setSelection(1);
                                status_spinner.setEnabled(false);
                            } else {
                                status_spinner.setSelection(2);
                                status_spinner.setEnabled(false);
                            }
                        }
                        //
                        image_display.setScaleType(ImageView.ScaleType.FIT_XY);
                        Picasso.with(updatestatus.this)
                                // .load("http://" + value.getString("GrievancePhotoPath1"))
                                .load(value.getString("GPhotoPath"))
                                //this is also optional if some error has occurred in downloading the image this image would be displayed
                                .into(image_display);
                        if (value.getString("GrievancePhotoPath1").contains(".jpg")) {
                            redress_img_display.setScaleType(ImageView.ScaleType.FIT_XY);
                            Picasso.with(updatestatus.this)
                                    // .load("http://" + value.getString("GrievancePhotoPath1"))
                                    .load(value.getString("GrievancePhotoPath1"))
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    //this is also optional if some error has occurred in downloading the image this image would be displayed
                                    .into(redress_img_display);
                        } else {
                            redress_img_display.setImageDrawable(getResources().getDrawable(R.drawable.dummy));
                        }


                        //
                        image_display_one.setScaleType(ImageView.ScaleType.FIT_XY);
                        Picasso.with(updatestatus.this)
                                // .load("http://" + value.getString("GrievancePhotoPath1"))
                                .load(value.getString("GPhotoPath1"))
                                //this is also optional if some error has occurred in downloading the image this image would be displayed
                                .into(image_display_one);

                        if (value.getString("GrievancePhotoPath2").contains(".jpg")) {
                            redress_img_display_one.setScaleType(ImageView.ScaleType.FIT_XY);
                            Picasso.with(updatestatus.this)
                                    // .load("http://" + value.getString("GrievancePhotoPath1"))
                                    .load(value.getString("GrievancePhotoPath2"))
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    //this is also optional if some error has occurred in downloading the image this image would be displayed
                                    .into(redress_img_display_one);
                        } else {
                            redress_img_display_one.setImageDrawable(getResources().getDrawable(R.drawable.dummy));
                        }

                        //
                        image_display_two.setScaleType(ImageView.ScaleType.FIT_XY);
                        Picasso.with(updatestatus.this)
                                // .load("http://" + value.getString("GrievancePhotoPath1"))
                                .load(value.getString("GPhotoPath2"))
                                //this is also optional if some error has occurred in downloading the image this image would be displayed
                                .into(image_display_two);
                        if (value.getString("GrievancePhotoPath3").contains(".jpg")) {
                            redress_img_display_two.setScaleType(ImageView.ScaleType.FIT_XY);
                            Picasso.with(updatestatus.this)
                                    // .load("http://" + value.getString("GrievancePhotoPath1"))
                                    .load(value.getString("GrievancePhotoPath3"))
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    //this is also optional if some error has occurred in downloading the image this image would be displayed
                                    .into(redress_img_display_two);
                        } else {
                            redress_img_display_two.setImageDrawable(getResources().getDrawable(R.drawable.dummy));
                        }


                        //exactstatus = value.getString("Status");

                        SharedPreferences sharedPreferences = getSharedPreferences("app_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("intGrivanceid", value.getString("intGrivanceid"));
                        editor.putString("App_No", value.getString("App_No"));
                        editor.commit();


                    }
                } else {
                    // showalert("No Data Found For This Grievance Id", "show");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                //    Toast.makeText(getBaseContext(),"exception",Toast.LENGTH_SHORT).show();
                // showalert("Record Not Found !!! For This Grievance Id " + search.getText().toString(), "show");
            }
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(updatestatus.this);
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


        if (captured_image.equals("one")) {
            GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
            Location location = gpsTracker.getLocation();
            // String lat = "0.0", lat_long = "0.0";
            if (location != null) {
                lat = String.valueOf(location.getLatitude() + "  ");
                lat_long = String.valueOf(location.getLongitude());
            }
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = sdf.format(c.getTime());

            afterEdit = drawTextToBitmap(updatestatus.this, lat, lat_long, strDate, thumbnail);
            // bin_photo_img.setMaxWidth(300);
            //bin_photo_img.setMaxHeight(300);
            redress_img_display.setScaleType(ImageView.ScaleType.FIT_XY);
            redress_img_display.setImageBitmap(afterEdit);
            image_str = "captured";
        } else if (captured_image.equals("two")) {
            GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
            Location location = gpsTracker.getLocation();
            // String lat = "0.0", lat_long = "0.0";
            if (location != null) {
                lat = String.valueOf(location.getLatitude() + "  ");
                lat_long = String.valueOf(location.getLongitude());
            }
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = sdf.format(c.getTime());

            afterEdit_one = drawTextToBitmap(updatestatus.this, lat, lat_long, strDate, thumbnail);
            // bin_photo_img.setMaxWidth(300);
            //bin_photo_img.setMaxHeight(300);
            redress_img_display_one.setScaleType(ImageView.ScaleType.FIT_XY);
            redress_img_display_one.setImageBitmap(afterEdit_one);
            image_str = "captured";
        } else if (captured_image.equals("three")) {
            GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
            Location location = gpsTracker.getLocation();
            // String lat = "0.0", lat_long = "0.0";
            if (location != null) {
                lat = String.valueOf(location.getLatitude() + "  ");
                lat_long = String.valueOf(location.getLongitude());
            }
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = sdf.format(c.getTime());

            afterEdit_two = drawTextToBitmap(updatestatus.this, lat, lat_long, strDate, thumbnail);
            // bin_photo_img.setMaxWidth(300);
            //bin_photo_img.setMaxHeight(300);
            redress_img_display_two.setScaleType(ImageView.ScaleType.FIT_XY);
            redress_img_display_two.setImageBitmap(afterEdit_two);
            image_str = "captured";
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
               /* GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
                Location location = gpsTracker.getLocation();
                // String lat = "0.0", lat_long = "0.0";
                if (location != null) {
                    lat = String.valueOf(location.getLatitude() + "  ");
                    lat_long = String.valueOf(location.getLongitude());
                }
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf.format(c.getTime());

                afterEdit = drawTextToBitmap(updatestatus.this, lat, lat_long, strDate, bitmap);
                // bin_photo_img.setMaxWidth(300);
                //bin_photo_img.setMaxHeight(300);
                redress_img_display.setScaleType(ImageView.ScaleType.FIT_XY);
                redress_img_display.setImageBitmap(afterEdit);
                image_str = "captured";*/
                if (captured_image.equals("one")) {
                    GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
                    Location location = gpsTracker.getLocation();

                    if (location != null) {
                        lat = String.valueOf(location.getLatitude() + "  ");
                        lat_long = String.valueOf(location.getLongitude());
                    }
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String strDate = sdf.format(c.getTime());
                    afterEdit = drawTextToBitmap(updatestatus.this, lat, lat_long, strDate, bitmap);
                    redress_img_display.setScaleType(ImageView.ScaleType.FIT_XY);
                    redress_img_display.setImageBitmap(afterEdit);
                    image_str = "selected";
                } else if (captured_image.equals("two")) {
                    GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
                    Location location = gpsTracker.getLocation();

                    if (location != null) {
                        lat = String.valueOf(location.getLatitude() + "  ");
                        lat_long = String.valueOf(location.getLongitude());
                    }
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String strDate = sdf.format(c.getTime());
                    afterEdit_one = drawTextToBitmap(updatestatus.this, lat, lat_long, strDate, bitmap);
                    redress_img_display_one.setScaleType(ImageView.ScaleType.FIT_XY);
                    redress_img_display_one.setImageBitmap(afterEdit_one);
                    image_str = "selected";
                } else if (captured_image.equals("three")) {
                    GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
                    Location location = gpsTracker.getLocation();

                    if (location != null) {
                        lat = String.valueOf(location.getLatitude() + "  ");
                        lat_long = String.valueOf(location.getLongitude());
                    }
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String strDate = sdf.format(c.getTime());
                    afterEdit_two = drawTextToBitmap(updatestatus.this, lat, lat_long, strDate, bitmap);
                    redress_img_display_two.setScaleType(ImageView.ScaleType.FIT_XY);
                    redress_img_display_two.setImageBitmap(afterEdit_two);
                    image_str = "selected";
                }

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
            nameValuePairs = new ArrayList<NameValuePair>();

            GPSTracker gpsTracker = new GPSTracker(updatestatus.this);
            Location location = gpsTracker.getLocation();
            /*String lat = "0.0", lat_long = "0.0";
            if (location != null) {
                lat = String.valueOf(location.getLatitude() + "  ");
                lat_long = String.valueOf(location.getLongitude());
            }*/
            nameValuePairs.add(new BasicNameValuePair("intGrivanceid", getIntent().getStringExtra("intGrivanceid")));
            nameValuePairs.add(new BasicNameValuePair("App_No", application_no.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("Status", status_str));
            nameValuePairs.add(new BasicNameValuePair("remarks", remarks.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("GLatitude", lat));
            nameValuePairs.add(new BasicNameValuePair("GLangitude", lat_long));
            nameValuePairs.add(new BasicNameValuePair("intOfficerid", sharedPreferences.getString("intOfficerid", "")));
            nameValuePairs.add(new BasicNameValuePair("GrievancePhotoFile1", "file1"));
            nameValuePairs.add(new BasicNameValuePair("GrievancePhotoFile2", "file2"));
            nameValuePairs.add(new BasicNameValuePair("GrievancePhotoFile3", "file3"));
            if (afterEdit == null) {
                nameValuePairs.add(new BasicNameValuePair("GrievancePhotoPath1", "sdfsd"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("GrievancePhotoPath1", getStringImage(afterEdit)));
            }
            if (afterEdit_one == null) {
                nameValuePairs.add(new BasicNameValuePair("GrievancePhotoPath2", "dasdf"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("GrievancePhotoPath2", getStringImage(afterEdit_one)));
            }
            if (afterEdit_two == null) {
                nameValuePairs.add(new BasicNameValuePair("GrievancePhotoPath3", "dsdf"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("GrievancePhotoPath3", getStringImage(afterEdit_two)));
            }

            json = JSONParser.makeServiceCall("http://www.vmc103.org/Water/UpdateStatusofGreivance.aspx", 2, nameValuePairs);

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
                        showalert("Successfully Grievance Updated", "hai");
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

        final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(updatestatus.this);
        alertDialogBuilder.setTitle("VMC Water Grievance");
        alertDialogBuilder.setIcon(R.drawable.aplogo);
        alertDialogBuilder.setMessage(alert_msg);
        // set dialog message
        alertDialogBuilder.setMessage(alert_msg).setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (response_str.equals("1")) {
                            finish();
                        }
                    }
                });
        /*alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });*/
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
