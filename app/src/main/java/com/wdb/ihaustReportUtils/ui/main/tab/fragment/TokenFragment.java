package com.wdb.ihaustReportUtils.ui.main.tab.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wdb.ihaustReportUtils.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TokenFragment extends Fragment {

    private View view;
    private EditText et_stuNo;
    private EditText et_stuPwd;
    private Button button;
    private TextView tv_token;
    private Handler handler;

    private final static String SP_ACCOUNT_NAME = "SP_ACCOUNT_NAME";
    private final static String FIELD_USERNAME = "stuNo";
    private final static String FIELD_PASSWORD = "stuPwd";

    SharedPreferences accountInfo;
    SharedPreferences.Editor editor;


    public TokenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_token, container, false);

        accountInfo = view.getContext().getSharedPreferences(SP_ACCOUNT_NAME, Context.MODE_PRIVATE);
        editor = accountInfo.edit();

        handler = new Handler(view.getContext().getMainLooper());

        et_stuNo = view.findViewById(R.id.stuNo);
        et_stuPwd = view.findViewById(R.id.stuPwd);
        button = view.findViewById(R.id.button);
        tv_token = view.findViewById(R.id.token);

        loadAccount();

        button.setOnClickListener(v -> {
            tv_token.setText("");
            String urlPattern = "https://token.haust.edu.cn/password/passwordLogin?username=%s&password=%s&appId=com.lantu.MobileCampus.haust&geo=&deviceId=";
            String stuNo = et_stuNo.getText().toString().trim();
            String stuPwd = et_stuPwd.getText().toString().trim();

            if ("".equals(stuNo) || "".equals(stuPwd)) {
                toast("请输入账号密码");
                return;
            }
            String url = String.format(urlPattern, stuNo, stuPwd);

            OkHttpClient client = new OkHttpClient.Builder().build();
            Call call = client.newCall(new Request.Builder().url(url).post(RequestBody.create(null, "")).build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    toast(e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        JSONObject object = new JSONObject(Objects.requireNonNull(response.body()).string());
                        System.out.println(object);
                        if (response.code() != 200) {
                            toast("账号或密码错误");
                        } else {
                            saveAccount();
                            String token = object.getJSONObject("data").getString("idToken");
                            handler.post(() -> {
                                hideKeyboard();
                                tv_token.setText(token);
                            });

                            //获取剪贴板管理器：
                            ClipboardManager cm = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            // 创建普通字符型ClipData
                            ClipData mClipData = ClipData.newPlainText("token", token);
                            // 将ClipData内容放到系统剪贴板里。
                            cm.setPrimaryClip(mClipData);
                            toast("已复制token");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        return view;
    }


    private void saveAccount() {
        editor.clear();
        editor.putString(FIELD_USERNAME, et_stuNo.getText().toString());
        editor.putString(FIELD_PASSWORD, et_stuPwd.getText().toString());
        editor.commit();
    }

    private void loadAccount() {
        et_stuNo.setText(accountInfo.getString(FIELD_USERNAME, ""));
        et_stuPwd.setText(accountInfo.getString(FIELD_PASSWORD, ""));
    }

    // 隐藏软键盘
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(((Activity) view.getContext()).getWindow().getDecorView().getWindowToken(), 0);
    }


    private void toast(String msg) {
        handler.post(() -> {
            Toast.makeText(view.getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}