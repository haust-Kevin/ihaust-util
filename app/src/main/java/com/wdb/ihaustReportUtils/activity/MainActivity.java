package com.wdb.ihaustReportUtils.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.qmuiteam.qmui.arch.QMUIActivity;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.tab.QMUIBasicTabSegment;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;
import com.wdb.ihaustReportUtils.R;
import com.wdb.ihaustReportUtils.ui.main.tab.adapter.TabFragmentAdapter;

import java.util.List;

public class MainActivity extends QMUIActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static Context mContext;
    private static LocationManager m_locationManager;
    private static String m_provider;

    private QMUITabSegment tabSegment;
    private QMUIViewPager viewpager;

    private final static int LOCATION_CODE = 100;

    private Location location;

    public Location getLocation() {
        return location;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QMUIStatusBarHelper.translucent(this);


        tabSegment = findViewById(R.id.tab_segment);
        viewpager = findViewById(R.id.viewpager);

        findViewById(R.id.btn_more).setOnClickListener(v -> {
            new QMUIBottomSheet.BottomListSheetBuilder(this)
                    .addItem(R.drawable.github, "源码地址", "github")
                    .addItem(R.drawable.qq, "联系我", "qq")
                    .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                        @Override
                        public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                            switch (tag) {
                                case "github": {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    Uri content_url = Uri.parse("https://github.com/haust-Kevin/ihaust-util");
                                    intent.setData(content_url);
                                    startActivity(intent);
                                    break;
                                }
                                case "qq": {
                                    try {
                                        String url = "mqqwpa://im/chat?chat_type=wpa&uin=3364917990";
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                default:

                            }
                        }
                    })
                    .build()
                    .show();

        });

        initViewPager();
        initPermission();
    }

    private void initViewPager() {
        viewpager.setAdapter(new TabFragmentAdapter(getSupportFragmentManager()));
        tabSegment.setupWithViewPager(viewpager, true);
        tabSegment.selectTab(0);
    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location l) {
            if (l != null) {
                System.out.println(l);
                location = l;
            }
        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    };

//    @SuppressLint("MissingPermission")
    public void startLocation() {
        Context context = this;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mContext = context;
        //获取定位服务
        m_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = m_locationManager.getProviders(true);
        if (list.contains(LocationManager.GPS_PROVIDER)) {
            //是否为GPS位置控制器
            m_provider = LocationManager.NETWORK_PROVIDER;//NETWORK_PROVIDER GPS_PROVIDER
        } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            //是否为网络位置控制器
            m_provider = LocationManager.NETWORK_PROVIDER;

        }
        if (m_provider != null) {
            m_locationManager.requestLocationUpdates(m_provider, 3000, 1, locationListener);
        }
    }

    public void initPermission() {
        Context context = this;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
        } else {
            startLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) {
            return;
        }
        if (requestCode == LOCATION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationListener != null) {
            m_locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }
}