package com.example.administrator.weathercool;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.weathercool.gson.Weathers;
import com.example.administrator.weathercool.util.HttpUtil;
import com.example.administrator.weathercool.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/27.
 */

public class WeatherActivity extends AppCompatActivity {

    private ScrollView mWeather_layout;
    private TextView mTitle_city;
    private TextView mTitle_update_time;
    private TextView mDegree_text;
    private TextView mWeather_info_text;
    private LinearLayout mForecast_layout;
    private TextView mApi_text;
    private TextView mPm2_5_text;
    private TextView mComfort_text;
    private TextView mWash_text;
    private TextView mSport_text;
    public DrawerLayout mDrawerLayout;
    private Button mNavButton;
    public SwipeRefreshLayout mSwipe_refresh;
    private String mWeatherId;
    private ImageView mPic_img;
    private String mWeatherString;
    private String mPic_img1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUi();
        setContentView(R.layout.activity_weather);
        initView();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavButton = (Button) findViewById(R.id.nav_button);
        mNavButton.setOnClickListener(mOnClickListener);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mWeatherString = preferences.getString("weather", null);
        mPic_img1 = preferences.getString("pic", null);
        decidePicExist();
        decideWeatherExist();
        mSwipe_refresh.setOnRefreshListener(mOnRefreshListener);
    }

    private void setUi() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorview = getWindow().getDecorView();
            decorview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

    }

    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=671286e873d343169d6844775cbaf95a";
        //String weatherUrl ="http://guolin.tech/api/weather?cityid=CN101280101&key=671286e873d343169d6844775cbaf95a";
        mWeatherId = weatherId;
        HttpUtil.senOkHttpRequest(weatherUrl, mCallback);
        loadpic();
    }

    private void loadpic() {
        String request = "http://guolin.tech/api/bing_pic";
        HttpUtil.senOkHttpRequest(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String pic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("pic", pic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(pic).into(mPic_img);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weathers weathers) {
        String cityName = weathers.getBasic().getCity();
        String updateTime = weathers.getBasic().getUpdate().getLoc().split(" ")[1];
        String degree = weathers.getNow().getTmp() + "℃";
        String weatherInfo = weathers.getNow().getCond().getTxt();
        Log.i("ccccccccccc", "showWeatherInfo: " + cityName + updateTime + degree + weatherInfo);

        // Log.i("dddddddddddd", "showWeatherInfo: "+weathers.daily_forecast.size());
        mTitle_city.setText(cityName);
        mTitle_update_time.setText(updateTime);
        mDegree_text.setText(degree);
        mWeather_info_text.setText(weatherInfo);
        mForecast_layout.removeAllViews();
       /* for (Weathers.DailyForecastBean forecastBean : weathers.daily_forecast) {
            View view = LayoutInflater.from(this).from(this).inflate(R.layout.forecast_item, mForecast_layout, false);
            TextView dataText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecastBean.getDate());
            infoText.setText(forecastBean.getCond().getTxt_d());
            maxText.setText(forecastBean.getTmp().getMax());
            minText.setText(forecastBean.getTmp().getMin());
            mForecast_layout.addView(view);
            Log.i("ccccccccccc", "showWeatherInfo: "+forecastBean.getDate()+forecastBean.getCond().getTxt_d()+forecastBean.getTmp().getMax()+forecastBean.getTmp().getMin());
        }
                */

        if (weathers.getAqi() != null) {
            mApi_text.setText(weathers.getAqi().getCity().getAqi());
            mPm2_5_text.setText(weathers.getAqi().getCity().getPm25());
        }
        String comfort = "舒适度:" + weathers.getSuggestion().getComf().getTxt();
        String carWash = "洗车指数:" + weathers.getSuggestion().getCw().getTxt();
        String sport = "运动指数:" + weathers.getSuggestion().getSport().getTxt();
        mComfort_text.setText(comfort);
        mWash_text.setText(carWash);
        mSport_text.setText(sport);
        mWeather_layout.setVisibility(View.VISIBLE);
    }

    private void initView() {
        mWeather_layout = (ScrollView) findViewById(R.id.weather_layout);
        mTitle_city = (TextView) findViewById(R.id.title_city);
        mTitle_update_time = (TextView) findViewById(R.id.title_update_time);
        mDegree_text = (TextView) findViewById(R.id.degree_text);
        mWeather_info_text = (TextView) findViewById(R.id.weather_info_text);
        mForecast_layout = (LinearLayout) findViewById(R.id.forecast_layout);
        mApi_text = (TextView) findViewById(R.id.api_text);
        mPm2_5_text = (TextView) findViewById(R.id.pm2_5_text);
        mComfort_text = (TextView) findViewById(R.id.comfort_text);
        mWash_text = (TextView) findViewById(R.id.wash_text);
        mSport_text = (TextView) findViewById(R.id.sport_text);
        mSwipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        mPic_img = (ImageView) findViewById(R.id.pic_img);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    };

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            requestWeather(mWeatherId);
        }
    };

    private Callback mCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WeatherActivity.this, "onFailure获取天气信息失败", Toast.LENGTH_LONG).show();
                    Log.i("AAAAA", "run: " + "onFailure获取天气信息失败");
                    mSwipe_refresh.setRefreshing(false);
                }
            });
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String responseText = response.body().string();
            Log.i("AAAAA", "run: " + responseText);
            final Weathers weather = Utility.handleWeatherResponse(responseText);
            Log.i("AAAAA", "run: " + weather.getStatus());
            // Log.i("ddddddddd", "showWeatherInfo: "+weather.getDaily_forecast().size());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (weather != null && "ok".equals(weather.getStatus())) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                        showWeatherInfo(weather);
                    } else {
                        Log.i("AAAAA", "获取天气信息失败");
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                    }
                    mSwipe_refresh.setRefreshing(false);
                }
            });
        }
    };


    private void decideWeatherExist() {
        if (mWeatherString != null) {
            Weathers weathers = Utility.handleWeatherResponse(mWeatherString);
            mWeatherId = weathers.getBasic().getId();
            showWeatherInfo(weathers);
        } else {
            mWeatherId = getIntent().getStringExtra("weather_id");
            mWeather_layout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
    }

    private void decidePicExist() {
        if (mPic_img1 != null) {
            Glide.with(WeatherActivity.this).load(mPic_img1).into(mPic_img);
        } else {
            loadpic();
        }
    }

}
