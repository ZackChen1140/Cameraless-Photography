package com.example.cameralessphotography;

import java.util.HashMap;
import java.util.Map;

public class DataTypeConverter {
    private Map<String, Object> weatherInfoMap;
    public DataTypeConverter()
    {
        weatherInfoMap = new HashMap<>();
    }
    public Map<String, Object> getWeatherInfoMap4FirebaseQuery(HashMap<String, String> weatherInformation)
    {
        float wind_direction = Float.parseFloat(weatherInformation.get("windDirection"));
        float wind_speed = Float.parseFloat(weatherInformation.get("windSpeed"));
        float temperature = Float.parseFloat(weatherInformation.get("temperature"));
        int humidity = Integer.parseInt(weatherInformation.get("humidity"));
        float pressure = Float.parseFloat(weatherInformation.get("pressure"));
        float precipitation = Float.parseFloat(weatherInformation.get("dayRain"));
        float gust_speed = Float.parseFloat(weatherInformation.get("gustSpeed"));
        float gust_direction = Float.parseFloat(weatherInformation.get("gustDirection"));

        weatherInfoMap.clear();
        weatherInfoMap.put("station_name", weatherInformation.get("stationName"));
        weatherInfoMap.put("wind_direction", wind_direction);
        weatherInfoMap.put("wind_speed", wind_speed);
        weatherInfoMap.put("temperature", temperature);
        weatherInfoMap.put("humidity", humidity);
        weatherInfoMap.put("pressure", pressure);
        weatherInfoMap.put("precipitation", precipitation);
        weatherInfoMap.put("gust_speed", gust_speed);
        weatherInfoMap.put("gust_direction", gust_direction);
        weatherInfoMap.put("weather", weatherInformation.get("weather"));

        return weatherInfoMap;
    }
}
