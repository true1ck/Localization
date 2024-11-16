package com.example.loc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private WifiManager wifiManager;
    private Spinner spinnerHotspot1, spinnerHotspot2, spinnerHotspot3;
    private TextView tvSignalStrength1, tvSignalStrength2, tvSignalStrength3, tvStatus;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        spinnerHotspot1 = findViewById(R.id.spinnerHotspot1);
        spinnerHotspot2 = findViewById(R.id.spinnerHotspot2);
        spinnerHotspot3 = findViewById(R.id.spinnerHotspot3);

        tvSignalStrength1 = findViewById(R.id.tvSignalStrength1);
        tvSignalStrength2 = findViewById(R.id.tvSignalStrength2);
        tvSignalStrength3 = findViewById(R.id.tvSignalStrength3);
        tvStatus = findViewById(R.id.tvStatus);

        btnStart = findViewById(R.id.btnStart);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            loadHotspotData();
        }

        btnStart.setOnClickListener(v -> startSignalCollection());
    }

    private void loadHotspotData() {
        List<String> hotspotList = new ArrayList<>();
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<ScanResult> results = wifiManager.getScanResults();

        for (ScanResult result : results) {
            hotspotList.add(result.SSID);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hotspotList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerHotspot1.setAdapter(adapter);
        spinnerHotspot2.setAdapter(adapter);
        spinnerHotspot3.setAdapter(adapter);
    }

    private void startSignalCollection() {
        String hotspot1 = spinnerHotspot1.getSelectedItem().toString();
        String hotspot2 = spinnerHotspot2.getSelectedItem().toString();
        String hotspot3 = spinnerHotspot3.getSelectedItem().toString();

        wifiManager.startScan();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle the case where permission is not granted
            return;
        }
        List<ScanResult> results = wifiManager.getScanResults();

        int strength1 = getSignalStrength(hotspot1, results);
        int strength2 = getSignalStrength(hotspot2, results);
        int strength3 = getSignalStrength(hotspot3, results);

        tvSignalStrength1.setText("Strength 1: " + (strength1 != Integer.MIN_VALUE ? strength1 + " dBm" : "Not Found"));
        tvSignalStrength2.setText("Strength 2: " + (strength2 != Integer.MIN_VALUE ? strength2 + " dBm" : "Not Found"));
        tvSignalStrength3.setText("Strength 3: " + (strength3 != Integer.MIN_VALUE ? strength3 + " dBm" : "Not Found"));

        // Concatenate the results and show them in tvStatus
        String statusText = "Hotspot 1: " + (strength1 != Integer.MIN_VALUE ? strength1 + " dBm" : "Not Found") + "\n"
                + "Hotspot 2: " + (strength2 != Integer.MIN_VALUE ? strength2 + " dBm" : "Not Found") + "\n"
                + "Hotspot 3: " + (strength3 != Integer.MIN_VALUE ? strength3 + " dBm" : "Not Found");

        tvStatus.setText(statusText);
    }

    private int getSignalStrength(String ssid, List<ScanResult> results) {
        for (ScanResult result : results) {
            if (result.SSID.equals(ssid)) {
                return result.level;
            }
        }
        return Integer.MIN_VALUE; // Return a default value if SSID is not found
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadHotspotData();
            } else {
                Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
