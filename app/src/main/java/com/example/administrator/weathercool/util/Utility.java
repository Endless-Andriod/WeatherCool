package com.example.administrator.weathercool.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.administrator.weathercool.db.City;
import com.example.administrator.weathercool.db.County;
import com.example.administrator.weathercool.db.Province;
import com.example.administrator.weathercool.gson.Weathers;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/21.
 */

public class Utility {
    /**
     * 解析省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        try {
            if (!TextUtils.isEmpty(response)) {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    province.save();
                }
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解析城市数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        try {
            if (!TextUtils.isEmpty(response)) {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setCityName(jsonObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解析县级别的数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        try {
            if (!TextUtils.isEmpty(response)) {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Weathers handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            Log.i("bbbbbbbbbbbbbbbbb", "handleWeatherResponse: "+jsonArray.length());
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Log.i("bbbbbbbbbbbbbbbbb", "handleWeatherResponse: "+weatherContent);
            Gson gson = new Gson();
            Weathers weathers = gson.fromJson(weatherContent,Weathers.class);
         //   Log.i("bbbbbbbbbbbbbbbbb", "handleWeatherResponse: "+weathers.getStatus().toString());
            return weathers;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
