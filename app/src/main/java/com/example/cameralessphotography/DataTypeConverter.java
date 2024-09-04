package com.example.cameralessphotography;

import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    public Map<String, Object> getShotResultMap4Firebase(Map<String, Object> shot_parameters, Map<String, Object> photograph_parameters)
    {
        Map<String, Object> shotResultMap = photograph_parameters;
        shotResultMap.put("R_station_name", shot_parameters.get("station_name"));
        shotResultMap.put("R_wind_direction", shot_parameters.get("wind_direction"));
        shotResultMap.put("R_wind_speed", shot_parameters.get("wind_speed"));
        shotResultMap.put("R_temperature", shot_parameters.get("temperature"));
        shotResultMap.put("R_humidity", shot_parameters.get("humidity"));
        shotResultMap.put("R_pressure", shot_parameters.get("pressure"));
        shotResultMap.put("R_precipitation", shot_parameters.get("precipitation"));
        shotResultMap.put("R_gust_speed", shot_parameters.get("gust_speed"));
        shotResultMap.put("R_gust_direction", shot_parameters.get("gust_direction"));
        shotResultMap.put("R_weather", shot_parameters.get("weather"));

        shotResultMap.put("R_latitude", shot_parameters.get("latitude"));
        shotResultMap.put("R_longitude", shot_parameters.get("longitude"));
        shotResultMap.put("R_pitch", shot_parameters.get("pitch"));
        shotResultMap.put("R_roll", shot_parameters.get("roll"));
        shotResultMap.put("R_yaw", shot_parameters.get("yaw"));

        shotResultMap.put("R_datetime", shot_parameters.get("datetime"));

        shotResultMap.put("photo_url", shot_parameters.get("photo_url"));
        shotResultMap.put("location_provider", shot_parameters.get("location_provider"));

        return shotResultMap;
    }
}
