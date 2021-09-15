package com.example.xingliansdk.service.work

object WeatherUtil {

    fun getWeatherCode(currentCode: String): Int {
        return when (currentCode.toInt()) {
            150,//晴	Clear	×	•
            100 -> 1//晴	Sunny	•	×
            153, //晴间多云	Partly Cloudy	×	•
            102, //少云	Few Clouds	•
            103,//晴间多云	Partly Cloudy	•	×
            101 -> 2  //	多云	Cloudy	•	•
            104, //阴	Overcast	•	×
            154 -> 3 // 阴	Overcast	×	•
            300, //	阵雨	Shower Rain	•	×
            301, //	强阵雨	Heavy Shower Rain	•	×
            302,//	雷阵雨	Thundershower	•	•
            303,//	强雷阵雨	Heavy Thunderstorm	•	•
            304, //雷阵雨伴有冰雹	Thundershower with hail	•	•
            305,//	小雨	Light Rain	•	•
            306,//	中雨	Moderate Rain	•	•
            307,//	大雨	Heavy Rain	•	•
            308,//极端降雨	Extreme Rain	•	•
            309,//毛毛雨/细雨	Drizzle Rain	•	•
            310,//暴雨	Storm	•	•
            311,//大暴雨	Heavy Storm	•	•
            312,//	特大暴雨	Severe Storm	•	•
            313,//冻雨	Freezing Rain	•	•
            314,//小到中雨	Light to moderate rain	•	•
            315,//	中到大雨	Moderate to heavy rain	•	•
            316,//	大到暴雨	Heavy rain to storm	•	•
            317,//	暴雨到大暴雨	Storm to heavy storm	•	•
            318,//	大暴雨到特大暴雨	Heavy to severe storm	•	•
            399,//	雨	Rain	•	•
            350,//	阵雨	Shower Rain	×	•
            351 -> 4//	强阵雨	Heavy Shower Rain	×	•
            400,//	小雪	Light Snow	•	•
            401,//	中雪	Moderate Snow	•	•
            402,//	大雪	Heavy Snow	•	•
            403,//	暴雪	Snowstorm	•	•
            404,//	雨夹雪	Sleet	•	•
            405,//	雨雪天气	Rain And Snow	•	•
            406,//	阵雨夹雪	Shower Snow	•	×
            407,//	阵雪	Snow Flurry	•	×
            408,//	小到中雪	Light to moderate snow	•	•
            409,//	中到大雪	Moderate to heavy snow	•	•
            410,//	大到暴雪	Heavy snow to snowstorm	•	•
            499,//	雪	Snow	•	•
            456,//	阵雨夹雪	Shower Snow	×	•
            457 -> 5//	阵雪	Snow Flurry	×	•
//            500,//	薄雾	Mist	•	•
//            501,//	雾	Foggy	•	•
//            502,//	霾	Haze	•	•
//            503,//	扬沙	Sand	•	•
//            504,//	浮尘	Dust	•	•
//            507,//	沙尘暴	Duststorm	•	•
//            508,//	强沙尘暴	Sandstorm	•	•
//            509,//	浓雾	Dense fog	•	•
//            510,//	强浓雾	Strong fog	•	•
//            511,//	中度霾	Moderate haze	•	•
//            512,//	重度霾	Heavy haze	•	•
//            513,//	严重霾	Severe haze	•	•
//            514,//	大雾	Heavy fog	•	•
//            515,//	特强浓雾	Extra heavy fog	•	•
//            900,//	热	Hot	•	•
//            901,//	冷	Cold	•	•
//            999,//	未知	Unknown	•	•
            else -> {
                255
            }
        }
    }

}