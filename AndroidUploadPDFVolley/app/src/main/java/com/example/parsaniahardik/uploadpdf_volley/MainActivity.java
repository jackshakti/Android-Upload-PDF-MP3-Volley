package com.example.parsaniahardik.uploadpdf_volley;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    private TextView tv;
    private String upload_URL = "https://demonuts.com/Demonuts/JsonTest/Tennis/uploadfile.php?";
    //private String upload_URL = "http://192.168.40.92/api/phpapi/uploadfile.php?";
    private RequestQueue rQueue;
    private ArrayList<HashMap<String, String>> arraylist;
    String url = "https://www.google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btn);
        tv = findViewById(R.id.tv);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                //intent.setType("audio/mpeg");
                startActivityForResult(intent,1);
            }
        });

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Get the Uri of the selected file
            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        Log.d("nameeeee>>>>  ",displayName);

                        uploadPDF(displayName,uri);
                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
                Log.d("nameeeee>>>>  ",displayName);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

   }

   private void uploadPDF(final String pdfname, Uri pdffile){

       InputStream iStream = null;
       try {

           iStream = getContentResolver().openInputStream(pdffile);
           final byte[] inputData = getBytes(iStream);

           VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, upload_URL,
                   new Response.Listener<NetworkResponse>() {
                       @Override
                       public void onResponse(NetworkResponse response) {
                           Log.d("ressssssoo",new String(response.data));
                           rQueue.getCache().clear();
                           try {
                               JSONObject jsonObject = new JSONObject(new String(response.data));
                               Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                               jsonObject.toString().replace("\\\\","");

                               if (jsonObject.getString("status").equals("true")) {
                                    Log.d("come::: >>>  ","yessssss");
                                   arraylist = new ArrayList<HashMap<String, String>>();
                                   JSONArray dataArray = jsonObject.getJSONArray("data");


                                   for (int i = 0; i < dataArray.length(); i++) {
                                       JSONObject dataobj = dataArray.getJSONObject(i);
                                       url = dataobj.optString("pathToFile");
                                       tv.setText(url);
                                   }


                               }
                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                       }
                   },
                   new Response.ErrorListener() {
                       @Override
                       public void onErrorResponse(VolleyError error) {
                           Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                   }) {

               /*
                * If you want to add more parameters with the image
                * you can do it here
                * here we have only one parameter with the image
                * which is tags
                * */
               @Override
               protected Map<String, String> getParams() throws AuthFailureError {
                   Map<String, String> params = new HashMap<>();
                   // params.put("tags", "ccccc");  add string parameters
                   return params;
               }

               /*
                *pass files using below method
                * */
               @Override
               protected Map<String, DataPart> getByteData() {
                   Map<String, DataPart> params = new HashMap<>();

                   params.put("filename", new DataPart(pdfname ,inputData));
                   return params;
               }
           };


           volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                   0,
                   DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                   DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
           rQueue = Volley.newRequestQueue(MainActivity.this);
           rQueue.add(volleyMultipartRequest);



       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }


   }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}

