package com.wdb.ihaustReportUtils.ui.main.tab.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wdb.ihaustReportUtils.R;
import com.wdb.ihaustReportUtils.activity.MainActivity;
import com.yuyh.jsonviewer.library.JsonRecyclerView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class JsonFragment extends Fragment {

    private View view;
    private EditText et_age;
    private EditText et_phone;
    private RadioGroup rg_isLuo;
    private TextView tv_position;
    private JsonRecyclerView tv_json;
    private Button button;

    private final static String SP_REPORT_NAME = "SP_REPORT_NAME";
    private final static String FIELD_AGE = "age";
    private final static String FIELD_PHONE = "phone";

    private Location location = null;

    SharedPreferences reportInfo;
    SharedPreferences.Editor editor;
    private Handler handler;

    public JsonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_json, container, false);
        handler = new Handler(getContext().getMainLooper());

        reportInfo = getContext().getSharedPreferences(SP_REPORT_NAME, Context.MODE_PRIVATE);
        editor = reportInfo.edit();
        et_age = view.findViewById(R.id.et_age);
        et_phone = view.findViewById(R.id.et_phone);
        rg_isLuo = view.findViewById(R.id.rg_isLuo);
        tv_position = view.findViewById(R.id.tv_position);
        tv_json = view.findViewById(R.id.tv_json);
        button = view.findViewById(R.id.btn_json);
        loadReport();
        locate();
        button.setOnClickListener(v -> {
            if (location == null) {
                toast("定位失败，请尝试重启应用");
                return;
            }
            String ageStr = et_age.getText().toString().trim();
            String phone = et_phone.getText().toString().trim();
            if ("".equals(ageStr) || "".equals(phone)) {
                toast("请填写联系方式和年龄");
                return;
            }
            int age = Integer.parseInt(ageStr);

            saveReport();

            String json = makeJson(age, phone);
            tv_json.bindJson(json);
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("json", json);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            toast("已复制json");
        });

        return view;
    }

    private String makeJson(int age, String phone) {
        String pattern = "{\"address\":\"%s\",\"age\":\"%d\",\"bodyTemperature\":%.1f,\"communityName\":null,\"communityPhone\":null,\"createTime\":\"2099-02-06 00:00:00\",\"latitude\":\"%f\",\"longitude\":\"%f\",\"homieAddress\":null,\"isCommunityRemark\":null,\"isRiskSite\":null,\"isStayLocal\":0,\"isTouchIll\":null,\"isolationType\":null,\"journeyAddress\":null,\"journeyDate\":null,\"journeyType\":null,\"onceIllDate\":null,\"onceTreatHospital\":null,\"otherSymptomRemark\":null,\"phone\":\"%s\",\"recoveryDate\":null,\"remark\":null,\"touchDate\":null,\"touchIllAddress\":null,\"touchIllDetail\":null,\"treatDetail\":null,\"unusualIllDate\":null,\"unusualSymptomList\":[],\"vehicleDetail\":null,\"vehicleType\":null,\"villageAddress\":null,\"needUpdate\":0,\"isAgree\":true}";
        return String.format(pattern,
                getDetailPosition(location),
                age,
                new Random().nextDouble() + 36,
                location.getLatitude(),
                location.getLongitude(),
                phone
        );
    }

    private void saveReport() {
        editor.clear();
        editor.putString(FIELD_PHONE, et_phone.getText().toString().trim());
        editor.putInt(FIELD_AGE, Integer.parseInt(et_age.getText().toString().trim()));
        editor.commit();
    }

    private void loadReport() {
        et_phone.setText(reportInfo.getString(FIELD_PHONE, ""));
        int age = reportInfo.getInt(FIELD_AGE, 999);
        if (age != 999) {
            et_age.setText(String.valueOf(age));
        }
    }


    private void locate() {

        new Thread(() -> {
            for (int i = 0; i < 6; i++) {
                location = ((MainActivity) getContext()).getLocation();
                if (location == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    handler.post(this::locationSuccess);
                    break;
                }
            }
        }).start();
    }

    private void locationSuccess() {
        getDetailPosition(location);
        tv_position.setText(getDetailPosition(location));
        if (isLuo(location)) rg_isLuo.check(R.id.yes);
        else rg_isLuo.check(R.id.no);
    }

    private void toast(String msg) {
        handler.post(() -> {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private String getDetailPosition(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder gc = new Geocoder(getContext(), Locale.CHINESE);
        List<Address> locationList = null;
        try {
            locationList = gc.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address = locationList.get(0);//得到Address实例

        StringBuilder ret = new StringBuilder();
        int i = 0;
        do {
            ret.append(address.getAddressLine(i++));
        } while (address.getAddressLine(i) != null);
        return ret.toString();
    }

    public boolean isLuo(Location location) {
        Geocoder gc = new Geocoder(getContext(), Locale.CHINESE);
        List<Address> locationList = null;
        try {
            locationList = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address = locationList.get(0);
        return "洛阳".equals(address.getLocality());
    }
}