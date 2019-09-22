package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResultActivity extends AppCompatActivity {
    private TextView mTextViewResult;
    private RequestQueue requestQueue;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        final String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        textView = findViewById(R.id.textView);
        textView.setText(message);

        mTextViewResult = findViewById(R.id.text_view_result);

        requestQueue = Volley.newRequestQueue(this);

        jsonParse(message);
    }

    private void jsonParse(String address) {
        String url = "https://data.gov.sg/api/action/datastore_search?resource_id=139a3035-e624-4f56-b63f-89ae28d4ae4c&q=" + address;

        JsonObjectRequest carparkInfoRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            JSONArray records = result.getJSONArray("records");
                            JSONObject recordsObj = records.getJSONObject(0);
                            String carParkNo = recordsObj.getString("car_park_no");
                            String fullAddress = recordsObj.getString("address");
                            String freeParking = recordsObj.getString("free_parking");

                            String resultString = "Free parking availability: " + freeParking + "\n\n";

                            textView.setText(fullAddress);

                            mTextViewResult.append(resultString);

                            searchForAvailability(carParkNo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(carparkInfoRequest);
    }

    private void searchForAvailability(String carParkNo) {
        final String carParkNo2 = carParkNo;
        String url = "https://api.data.gov.sg/v1/transport/carpark-availability";

        JsonObjectRequest availabilityRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray records = response.getJSONArray("items");
                            JSONObject jsonObject = records.getJSONObject(0);
                            JSONArray carparkData = jsonObject.getJSONArray("carpark_data");
                            for (int i = 0; i < carparkData.length(); i++) {
                                JSONObject carpark = carparkData.getJSONObject(i);
                                if (carpark.getString("carpark_number").equals(carParkNo2)) {
                                    JSONObject carparkInfo = carpark.getJSONArray("carpark_info").getJSONObject(0);
                                    String totalSpace = carparkInfo.getString("total_lots");
                                    String availableSpace = carparkInfo.getString("lots_available");
                                    String availabilityResult = "Total: " + totalSpace + "\nAvailable: " + availableSpace;

                                    mTextViewResult.append(availabilityResult);
                                    break;
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
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(availabilityRequest);
    }

    public void startNavigating(View view) {
        String address = textView.getText().toString().replace(" ", "+");
        String addressUri = "google.navigation:q=" + address + ",+Singapore";
        Uri gmmIntentUri = Uri.parse(addressUri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

}
