package com.example.sun.codeweather.gson;


import com.google.gson.annotations.SerializedName;

/**
 * 对应json数据的basic对象
 * 使用@SerializedName注解建立json字段和java字段的联系
 */

public class Basic {
    //    城市名
    @SerializedName("city")
    public String cityName;
    //    天气对应得ID
    @SerializedName("id")
    public String weatherId;
    @SerializedName("update")
    public Update update;

    //内部update对象
    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
