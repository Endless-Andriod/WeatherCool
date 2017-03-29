package com.example.administrator.weathercool;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.weathercool.db.City;
import com.example.administrator.weathercool.db.County;
import com.example.administrator.weathercool.db.Province;
import com.example.administrator.weathercool.util.HttpUtil;
import com.example.administrator.weathercool.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/3/26.
 */

public class AreaFragment extends Fragment {
    @BindView(R.id.title_text)
    TextView mTitleText;
    @BindView(R.id.back_button)
    Button mBackButton;
    @BindView(R.id.list_view)
    ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> mDataList = new ArrayList<>();
    private int currrentLevel;  //当前选中的级别
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private Province mProvince;
    private City mCity;
    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;
    protected ProgressDialog mProgressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        ButterKnife.bind(this, view);
        mTitleText = (TextView) view.findViewById(R.id.title_text);
        mBackButton = (Button) view.findViewById(R.id.back_button);
        mListView = (ListView) view.findViewById(R.id.list_view);
        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(mOnItemClickListener);
        mBackButton.setOnClickListener(mOnClickListener);
        queryProvinces();
    }


    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (currrentLevel == LEVEL_PROVINCE) {
                mProvince = mProvinceList.get(position);
                queryCities();
            } else if (currrentLevel == LEVEL_CITY) {
                mCity = mCityList.get(position);
                queryCounties();
            } else if (currrentLevel == LEVEL_COUNTY) {
                String weatherId = mCountyList.get(position).getWeatherId();
                if (getActivity() instanceof MainActivity) {
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                    Log.i("aaaaaaaa", "queryCounties: " + weatherId);
                } else if (getActivity() instanceof WeatherActivity) {
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    activity.mDrawerLayout.closeDrawers();
                    activity.mSwipe_refresh.setRefreshing(true);
                    activity.requestWeather(weatherId);
                }
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currrentLevel == LEVEL_COUNTY) {
                queryCities();
            } else if (currrentLevel == LEVEL_CITY) {
                queryProvinces();
            }

        }
    };

    private void queryProvinces() {
        mTitleText.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            mDataList.clear();
            for (Province province : mProvinceList) {
                mDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currrentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCounties() {
        mTitleText.setText(mCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountyList = DataSupport.where("cityid=?", String.valueOf(mCity.getId())).find(County.class);

        if (mCountyList.size() > 0) {
            mDataList.clear();
            for (County county : mCountyList) {
                mDataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currrentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = mProvince.getProvinceCode();
            int cityCode = mCity.getCityCode();
            Log.i(TAG, "queryCounties: " + cityCode);
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }

    }

    private void queryCities() {
        mTitleText.setText(mProvince.getProvinceName());
        mBackButton.setVisibility(View.VISIBLE);
        mCityList = DataSupport.where("provinceid= ?", String.valueOf(mProvince.getId())).find(City.class);
        if (mCityList.size() > 0) {
            mDataList.clear();
            for (City city : mCityList) {
                mDataList.add(city.getCityName());

            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currrentLevel = LEVEL_CITY;
        } else {
            int provinceCode = mProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.senOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();

                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, mCity.getId());
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, mProvince.getId());
                }

                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            } else if ("city".equals(type)) {
                                queryCities();
                            }
                        }
                    });
                }
            }
        });


    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();

        }

    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
}
