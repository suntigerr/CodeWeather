package com.example.sun.codeweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sun.codeweather.gson.Forecast;
import com.example.sun.codeweather.gson.Weather;
import com.example.sun.codeweather.util.HttpUtil;
import com.example.sun.codeweather.util.JsonUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefresh;

    private ScrollView weatherLayout;
    private TextView titleCityText;
    private TextView titleUpdateTimeText;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;
    private SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //只有5.0以上支持  同时借助coordinatorLayout AppBarLayout CollapsingToolbarLayout 也能实现
        if (Build.VERSION.SDK_INT >= 21){
            //获取当前活动的DecorView
            View decorView = getWindow().getDecorView();
            //改变系统UI显示
            //表示当前活动显示在状态栏上
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //状态栏上设置透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //初始化控件
        weatherLayout = findViewById(R.id.weather_layout);

        titleCityText = findViewById(R.id.title_city);
        titleUpdateTimeText = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);

        forecastLayout = findViewById(R.id.forecast_layout);

        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        bingPicImg = findViewById(R.id.bing_pic_img);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            //有缓存直接解析天气
            Weather weather = JsonUtil.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
//            无缓存再去服务器查询
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPic = preferences.getString("bing_pic",null);
        if (bingPic!= null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
    }



    /**
     * 根据weather_id请求城市的天气信息
     *
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                "key=64feb105fdc44411aad68cb0e7d0a656";
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = JsonUtil.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor =
                                    PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                                            .edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 处理展示weather实体的中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temerature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCityText.setText(cityName);
        titleUpdateTimeText.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast: weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数："+weather.suggestion.carWash.info;
        String sport = "运动指数："+weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

    }
    /**
     * 加载Bing每日一图
     */

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String bingPic = response.body().string();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic)
                                .into(bingPicImg);
                    }
                });
            }
        });
    }


}

