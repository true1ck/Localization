package com.example.loc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String API_URL = "https://mllocalizationmodel-1.onrender.com/predict";

    private WifiManager wifiManager;
    private Spinner spinnerHotspot1, spinnerHotspot2, spinnerHotspot3;
    private TextView tvSignalStrength1, tvSignalStrength2, tvSignalStrength3, tvStatus;
    private Button btnStart;
    private GridLayout gridLayout;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initUI();

        // Initialize WifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Request location permission if not granted
        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
            loadHotspotData();
        }

        // Button click listener for signal collection
        btnStart.setOnClickListener(v -> startSignalCollection());
    }

    private void initUI() {
        spinnerHotspot1 = findViewById(R.id.spinnerHotspot1);
        spinnerHotspot2 = findViewById(R.id.spinnerHotspot2);
        spinnerHotspot3 = findViewById(R.id.spinnerHotspot3);

        tvSignalStrength1 = findViewById(R.id.tvSignalStrength1);
        tvSignalStrength2 = findViewById(R.id.tvSignalStrength2);
        tvSignalStrength3 = findViewById(R.id.tvSignalStrength3);
        tvStatus = findViewById(R.id.tvStatus);

        btnStart = findViewById(R.id.btnStart);

        gridLayout = findViewById(R.id.gridLayout);
        initializeGrid();
    }

    private void initializeGrid() {
        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(3);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                TextView cell = new TextView(this);
                cell.setText(String.format("a%d%d", row + 1, col + 1));
                cell.setGravity(android.view.Gravity.CENTER);
                cell.setPadding(16, 16, 16, 16);
                cell.setBackgroundResource(android.R.drawable.btn_default);
                gridLayout.addView(cell);
            }
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private void loadHotspotData() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(this, "Enabling Wi-Fi...", Toast.LENGTH_SHORT).show();
        }

        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission is required to scan Wi-Fi networks.", Toast.LENGTH_SHORT).show();
            return;
        }

        wifiManager.startScan();
        List<ScanResult> results = wifiManager.getScanResults();
        List<String> hotspotList = new ArrayList<>();

        for (ScanResult result : results) {
            if (!result.SSID.isEmpty()) {
                hotspotList.add(result.SSID);
            }
        }

        if (hotspotList.isEmpty()) {
            Toast.makeText(this, "No Wi-Fi hotspots found.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hotspotList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerHotspot1.setAdapter(adapter);
        spinnerHotspot2.setAdapter(adapter);
        spinnerHotspot3.setAdapter(adapter);
    }

    private void startSignalCollection() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission is required to start signal collection.", Toast.LENGTH_SHORT).show();
            return;
        }

        String hotspot1 = getSelectedHotspot(spinnerHotspot1);
        String hotspot2 = getSelectedHotspot(spinnerHotspot2);
        String hotspot3 = getSelectedHotspot(spinnerHotspot3);

        if (hotspot1 == null || hotspot2 == null || hotspot3 == null) {
            Toast.makeText(this, "Please select a valid hotspot.", Toast.LENGTH_SHORT).show();
            return;
        }

        wifiManager.startScan();
        List<ScanResult> results = wifiManager.getScanResults();

        int strength1 = getSignalStrength(hotspot1, results);
        int strength2 = getSignalStrength(hotspot2, results);
        int strength3 = getSignalStrength(hotspot3, results);

        displaySignalStrength(tvSignalStrength1, "Hotspot 1", strength1);
        displaySignalStrength(tvSignalStrength2, "Hotspot 2", strength2);
        displaySignalStrength(tvSignalStrength3, "Hotspot 3", strength3);

        sendSignalDataToApi(strength1, strength2, strength3);
    }

    private String getSelectedHotspot(Spinner spinner) {
        return spinner.getSelectedItem() != null ? spinner.getSelectedItem().toString() : null;
    }

    private int getSignalStrength(String ssid, List<ScanResult> results) {
        for (ScanResult result : results) {
            if (result.SSID.equals(ssid)) {
                return result.level;
            }
        }
        return Integer.MIN_VALUE;
    }

    private void displaySignalStrength(TextView textView, String label, int strength) {
        textView.setText(String.format("%s: %s", label, formatSignalStrength(strength)));
    }

    private String formatSignalStrength(int strength) {
        return (strength != Integer.MIN_VALUE) ? strength + " dBm" : "Not Found";
    }

    private void sendSignalDataToApi(int signal1, int signal2, int signal3) {
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("Signal_1", signal1);
        jsonPayload.addProperty("Signal_2", signal2);
        jsonPayload.addProperty("Signal_3", signal3);

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonPayload.toString(), JSON);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> tvStatus.setText("API call failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> handleApiResponse(responseBody));
                } else {
                    runOnUiThread(() -> tvStatus.setText("API call failed with code: " + response.code()));
                }
            }
        });
    }

    private void handleApiResponse(String responseBody) {
        tvStatus.setText("API Response: " + responseBody);
        if (responseBody.contains("predicted_class")) {
            String predictedClass = responseBody.split(":")[1].replaceAll("[^a-zA-Z0-9]", "");
            highlightPredictedSpot(predictedClass);
        }
    }

    private void highlightPredictedSpot(String predictedClass) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            TextView cell = (TextView) gridLayout.getChildAt(i);
            if (cell.getText().toString().equals(predictedClass)) {
                cell.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            } else {
                cell.setBackgroundResource(android.R.drawable.btn_default);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadHotspotData();
            } else {
                Toast.makeText(this, "Permission denied. Unable to scan Wi-Fi.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
