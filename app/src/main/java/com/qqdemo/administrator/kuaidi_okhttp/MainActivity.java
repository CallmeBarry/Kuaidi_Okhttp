package com.qqdemo.administrator.kuaidi_okhttp;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AAAAAAAAA";
    @InjectView(R.id.spinner)
    Spinner mSpinner;
    @InjectView(R.id.ednum)
    EditText mEdnum;
    @InjectView(R.id.btn)
    Button mBtn;
    @InjectView(R.id.listview)
    ListView mListview;
    private String mHttpArg;
    private String mHttpUrl;
    Gson mGson;
    InformationBean mInformationBean;
    private String mCom;
    public String[] map = {"sf", "sto", "yt", "yd", "tt", "ems", "zto", "ht"};
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


        mGson = new Gson();
        mListview.setAdapter(mBaseAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCom = map[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCom = map[0];
            }
        });

    }
    private void loadData(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: " + e.getLocalizedMessage());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "查询失败重新查询", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Response response) throws IOException {
                mInformationBean=new InformationBean();
                String result = response.body().string();
                mInformationBean = mGson.fromJson(result, InformationBean.class);

                if (mInformationBean.getResultcode().equals("200")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBaseAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "查询不到此快递信息", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

        });
    }


    BaseAdapter mBaseAdapter = new BaseAdapter() {

        private ViewHolder mHolder = null;

        @Override
        synchronized public int getCount() {
            if (mInformationBean == null) {
                return 0;
            }
            if (mInformationBean.getResultcode().equals("200")) {
                return mInformationBean.getResult().getList().size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.list_item, null);
                mHolder = new ViewHolder(convertView);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }
            InformationBean.ResultBean.ListBean listBean = mInformationBean.getResult().getList().get(position);
            mHolder.txt.setText("时间：" + listBean.getDatetime() + "\n地址：" + listBean.getRemark() + "\n");

            return convertView;
        }
    };

    @OnClick(R.id.btn)
    public void onClick() {
        hidKeyboard();
        String no = mEdnum.getText().toString();
        mHttpUrl = "http://v.juhe.cn/exp/index?key=64b973df262d45cd6f1cb17bb323a755&com=" + mCom + "&no=" + no;
        loadData(mHttpUrl);
    }

    public class ViewHolder {
        private TextView txt;


        ViewHolder(View root) {
            txt = (TextView) root.findViewById(R.id.txt);

        }

    }

    void hidKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

}

