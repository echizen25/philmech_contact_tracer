
package com.example.philmechlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.philmechlocator.Connection.ConnectionClass;
import com.example.philmechlocator.Connection2.DtrConnect;
import com.example.philmechlocator.Session.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;




public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int FIRST_REQUEST_CODE = 1;
    private static final int SECODE_REQUEST_CODE =2 ;
    public static final String version = "version_3";
    private ImageView profilePic;
    public Uri imageUri;
    private StorageReference storageReference;
    Button scanBtn;
    SessionManager sessionManager;
    Connection con;
    EditText datetime;
    TextView lblUsername;
    TextView division;
    EditText memcode;
    EditText result;
    TextView status;
    Statement stmt;
    String username;
    Connection dtrdb;
    Connection dtrdb2;
    ImageButton btnFetch;
    ListView lstData;
    SimpleAdapter ADAhere;
    TextView stat;
    TextView gender;
    TextView log_Date;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        getSupportActionBar().  setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_logo);
        setContentView(R.layout.activity_main);
        imageUri = null;
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        result = findViewById(R.id.result);
        memcode = findViewById(R.id.memcode);
        division = findViewById(R.id.division);
        datetime = findViewById(R.id.datetime);
        stat = findViewById(R.id.stat);
        log_Date = findViewById(R.id.times);
        gender = findViewById(R.id.gender);
        ImageView sex = (ImageView)findViewById(R.id.sex);
        status = findViewById(R.id.status);
        lblUsername = findViewById(R.id.location);
        btnFetch = (ImageButton) findViewById(R.id.btnFetch);
        sessionManager = new SessionManager(getApplicationContext());
        sessionManager.checkLogin();
        scanBtn = findViewById(R.id.button);
        scanBtn.setOnClickListener(this);
        profilePic = findViewById(R.id.sex);
        //storage upload
        storageReference = FirebaseStorage.getInstance().getReference().child("images");

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        btnFetch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ConnectMySql connectMySql = new ConnectMySql();
                connectMySql.execute("");
            }
        });

        lstData = (ListView) findViewById(R.id.listlogs);

        String string2 = (String) sessionManager.getUserDetails().get((Object) "username");
        String string3 = (String) sessionManager.getUserDetails().get((Object) "memcode");
        String string4 = (String) sessionManager.getUserDetails().get((Object) "gender");
        if (string2 != null) {
            TextView textView = this.lblUsername;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Welcome, <b>");
            stringBuilder.append(string2);
            stringBuilder.append("</b>!");
            textView.setText((CharSequence) Html.fromHtml((String) stringBuilder.toString()));
            this.username = string2;
        }

        if (string3 != null) {
            memcode.setText(string3);
        }
        if (string4 != null){
            if(string4.equals("M")) {
                gender.setText("Male");
                sex.setImageResource(R.mipmap.avatar1);
            }
            else {
                gender.setText("Female");
                sex.setImageResource(R.mipmap.avatar2);
            }

        }

        FillTextBox();
        FillCurrentLoc();
        checkVersion();

        databaseHelper = new DatabaseHelper (this);
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType(("image/*"));
        intent.setAction((Intent.ACTION_GET_CONTENT));
        startActivityForResult(intent,3);
    }


    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Please wait...", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                dtrdb = connectionClass(DtrConnect.un.toString(), DtrConnect.pass.toString(), DtrConnect.db.toString(), DtrConnect.ip.toString());
                Statement st = dtrdb.createStatement();
                ResultSet rs = st.executeQuery("select log_date_time,specify_location from location_logs where memcode ='"+memcode.getText()+"' order by log_date_time DESC");
                ResultSetMetaData rsmd = rs.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                while (rs.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("A", rs.getString("log_date_time").toString());
                    datanum.put("B", rs.getString("specify_location").toString());

                    data.add(datanum);
                }

                String[] fromwhere = { "A", "B" };
                int[] viewswhere = { R.id.Datelog, R.id.Location };
                ADAhere = new SimpleAdapter(MainActivity.this, data,
                        R.layout.list_logs, fromwhere, viewswhere);

            } catch (Exception e) {
                e.printStackTrace();

            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {

            lstData.setAdapter(ADAhere);
        }
    }

    @Override
    public void onClick(View view) {
        FillTextBox();
        updateDb();
        scanCode();
    }
    public void checkVersion(){
        try {

            dtrdb2 = connectionClass(DtrConnect.un.toString(), DtrConnect.pass.toString(), DtrConnect.db.toString(), DtrConnect.ip.toString());

            String query = "select top 1 version from android_version order by version_id desc";
            PreparedStatement stmt = dtrdb2.prepareStatement(query);
            ResultSet rs3 = stmt.executeQuery();
            if (!rs3.next()) {

                Toast.makeText(getApplicationContext(), "Ok", Toast.LENGTH_LONG).show();
            }
            else{
                try {

                    if(rs3.getString("version").equals(version)){
                        Toast.makeText(getApplicationContext(), "Your app is up to Date", Toast.LENGTH_LONG).show();
                    }
                    else{
                        showCustomDialog();
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void showCustomDialog() {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.customdialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated

        builder.setView(dialogView);
        Button buttonOK = (Button) dialogView.findViewById(R.id.buttonOk);
        builder.setCancelable(false);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("https://www.philmech.gov.ph/download/android/tracer_app/PHilmech_Contact_Tracer_App.apk"));
                startActivity(viewIntent);
            }
        });
        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan QR Code");
        startActivityForResult(integrator.createScanIntent(),FIRST_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
            uploadPicture();
        }


        if (requestCode == 1) {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
            if (intentResult != null) {
                if (intentResult.getContents() == null) {
                    result.setText("");
                    datetime.setText("");
                    status.setText("");
                    stat.setText("Recent Location:");
                } else {
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    result.setText(intentResult.getContents());
                    datetime.setText(date);
                    stat.setText("You are Currently at:");

                    saveToDb();

                    ConnectMySql connectMySql = new ConnectMySql();
                    connectMySql.execute("");
                }
            }
        } else if (requestCode == 2) {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
            if (intentResult != null) {
                if (intentResult.getContents() == null) {
                    result.setText("");
                    datetime.setText("");
                    status.setText("");
                } else {
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    result.setText(intentResult.getContents());
                    datetime.setText(date);
                    stat.setText("");
                    result.setText("");
                    ConnectMySql connectMySql = new ConnectMySql();
                    connectMySql.execute("");
                }
            }
        } else {

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void uploadPicture() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image....");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/"+ randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Uploaded!", Toast.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed to Upload!", Toast.LENGTH_LONG).show();

                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pd.setMessage("Percentage: " + (int) progressPercent + "%");
                    }

                });

    }

    public void saveToDb() {
        try {
            dtrdb = connectionClass(DtrConnect.un.toString(), DtrConnect.pass.toString(), DtrConnect.db.toString(), DtrConnect.ip.toString());
            if (dtrdb == null ) {
                String memcodeTXT = memcode.getText().toString();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date datein = format.parse(datetime.getText().toString());
                String spec_location = result.getText().toString();
                String current_status = "True";
                int stat = 0;


                assert datein != null;
                Boolean checkinsertdata = databaseHelper.adduserdata(memcodeTXT, datein, spec_location, current_status,stat);
                if (checkinsertdata){
                    status.setTextColor(Color.RED);
                    status.setText("Saved offline");}
                else{
                    status.setTextColor(Color.RED);
                    status.setText("Error");
                }

            } else {
                            String sql = "INSERT INTO location_logs (memcode,log_date_time,specify_location,current_status) VALUES ('" + memcode.getText() + "','" + datetime.getText() + "','" + result.getText() + "','True')";
                            stmt = dtrdb.createStatement();
                            stmt.executeUpdate(sql);
                            status.setTextColor(Color.RED);
                            status.setText("Data has been saved to the server");
                            checkVersion();
                        }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void updateDb() {
        try {
            dtrdb = connectionClass(DtrConnect.un.toString(), DtrConnect.pass.toString(), DtrConnect.db.toString(), DtrConnect.ip.toString());
            if (dtrdb == null) {
                String memcodes = memcode.getText().toString();
                String currentdate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                String prevdate = (log_Date.getText().toString());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date newDate = format.parse(prevdate);
                format = new SimpleDateFormat("yyyy-MM-dd");
                String prevdates = format.format(newDate);

                String current_status = "False";

                if (prevdates.equals(currentdate)) {
                    Boolean updateinsertdata = databaseHelper.updateuserdata(memcodes,currentdate,current_status);
                    if (updateinsertdata) {
                        status.setTextColor(Color.RED);
                        status.setText("Saved offline");
                    } else {
                        status.setTextColor(Color.RED);
                        status.setText("Error");
                    }
                }
                else{
                    Boolean updateinsertdata = databaseHelper.updateuserdata(memcodes,currentdate,current_status);
                    if (updateinsertdata) {
                        status.setTextColor(Color.RED);
                        status.setText("Saved offline");
                    } else {
                        status.setTextColor(Color.RED);
                        status.setText("Error");
                    }
                }

            } else {

                String dates = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                String currentdate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                String prevdate = (log_Date.getText().toString());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date newDate = format.parse(prevdate);
                format = new SimpleDateFormat("yyyy-MM-dd");
                String prevdates = format.format(newDate);

                if (prevdates.equals(currentdate)){
                String sql = "UPDATE location_logs set log_out_date_time = '" + dates + "' ,current_status = 'False' where loc_id= ( SELECT TOP 1 loc_id FROM location_logs where memcode ='" + memcode.getText() + "'  ORDER BY log_date_time DESC) ";
                stmt = dtrdb.createStatement();
                stmt.executeUpdate(sql);
                status.setTextColor(Color.RED);
                status.setText("updated");

               }
                           else{
                   String sql = "UPDATE location_logs set log_out_date_time = NULL ,current_status = 'False' where loc_id= ( SELECT TOP 1 loc_id FROM location_logs where memcode ='" + memcode.getText() + "'  ORDER BY log_date_time DESC) ";
                   stmt = dtrdb.createStatement();
                   stmt.executeUpdate(sql);
                   status.setTextColor(Color.RED);
                   status.setText("updated");
               }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void FillTextBox() {
        try {

            con = connectionClass(ConnectionClass.un.toString(), ConnectionClass.pass.toString(), ConnectionClass.db.toString(), ConnectionClass.ip.toString());

            String query = "select memcode,full_name,GNAME,SNAME,division,gender from members where username ='" + username + "'";
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                try {
                     memcode.setText(rs.getString("memcode"));
                    division.setText(rs.getString("division"));
                    if(rs.getString("gender").equals("M")) {
                        gender.setText("Male");
                    }
                    else {
                        gender.setText("Female");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public void FillCurrentLoc() {
        try {

            dtrdb2 = connectionClass(DtrConnect.un.toString(), DtrConnect.pass.toString(), DtrConnect.db.toString(), DtrConnect.ip.toString());

            String query = "select top 1 specify_location,log_date_time from location_logs where memcode ='" + memcode.getText() + "' order by log_date_time DESC ";
            PreparedStatement stmt = dtrdb2.prepareStatement(query);
            ResultSet rs2 = stmt.executeQuery();
            if (!rs2.next()) {
                stat.setText("");
                Toast.makeText(getApplicationContext(), "Check in!", Toast.LENGTH_LONG).show();
            }
            else{
                try {
                    result.setText(rs2.getString("specify_location"));
                    log_Date.setText((rs2.getString("log_date_time")));
                    stat.setText("Recent Location:");


                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public Connection connectionClass(String user, String password, String database, String server) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String connectionURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connectionURL = "jdbc:jtds:sqlserver://" + server + "/" + database + ";user=" + user + ";password=" + password + ";";
            connection = DriverManager.getConnection(connectionURL);
        } catch (Exception e) {
            Log.e("SQL Connection Error : ", Objects.requireNonNull(e.getMessage()));
        }
        return connection;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                SessionManager sessionManager;
                sessionManager = new SessionManager(getApplicationContext());
                sessionManager.logoutUser();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

