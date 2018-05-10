package com.example.sun.codeweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tem")
    public String temerature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
