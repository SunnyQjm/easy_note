package cn.yml.note.utils;

import java.util.Calendar;

/**
 * Created by Sunny on 2017/10/24 0024.
 */

public class ConstellationUtil {
    /**
     * ♈
     * 白羊座
     * Aries
     * 3月21日~4月19日
     * 牡羊座
     * ♉
     * 金牛座
     * Taurus
     * 4月20日～5月20日
     * 金牛座
     * ♊
     * 双子座
     * Gemini
     * 5月21日～6月21日
     * 双子座
     * ♋
     * 巨蟹座
     * Cancer
     * 6月22日～7月22日
     * 巨蟹座
     * ♌
     * 狮子座
     * Leo
     * 7月23日～8月22日
     * 狮子座
     * ♍
     * 处女座
     * Virgo
     * 8月23日～9月22日
     * 室女座
     * ♎
     * 天秤座
     * Libra
     * 9月23日～10月23日
     * 天平座
     * ♏
     * 天蝎座
     * Scorpio
     * 10月24日～11月22日
     * 天蝎座
     * ♐
     * 射手座
     * Sagittarius
     * 11月23日～12月21日
     * 人马座
     * ♑
     * 摩羯座
     * Capricorn
     * 12月22日～1月19日
     * 山羊座
     * ♒
     * 水瓶座
     * Aquarius
     * 1月20日～2月18日
     * 宝瓶座
     * ♓
     * 双鱼座
     * Pisces
     * 2月19日～3月20日
     * 双鱼座
     */

    private static final String BaseUrl = "http://xueban3.oureda.cn:8000/stars/";
    private static final String ARIES = BaseUrl + "aries.png";
    private static final String TAURUS = BaseUrl + "taurus.png";
    private static final String GEMINI = BaseUrl + "gemini.png";
    private static final String CANCER = BaseUrl + "cancer.png";
    private static final String LEO = BaseUrl + "leo.png";
    private static final String VIRGO = BaseUrl + "virgo.png";
    private static final String LIBRA = BaseUrl + "libra.png";
    private static final String SCORPIO = BaseUrl + "scorpio.png";
    private static final String SAGITTARIUS = BaseUrl + "sagittarius.png";
    private static final String CAPRICORN = BaseUrl + "capricorn.png";
    private static final String AQUARIUS = BaseUrl + "aquarius.png";
    private static final String PISCES = BaseUrl + "pisces.png";

    public enum Type {
        ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS,
        PISCES
    }

    public static String getUrls(){
        switch (getType(getTime())){
            case ARIES:
                return ARIES;
            case TAURUS:
                return TAURUS;
            case GEMINI:
                return GEMINI;
            case LEO:
                return LEO;
            case CANCER:
                return CANCER;
            case VIRGO:
                return VIRGO;
            case LIBRA:
                return LIBRA;
            case SCORPIO:
                return SCORPIO;
            case SAGITTARIUS:
                return SAGITTARIUS;
            case CAPRICORN:
                return CAPRICORN;
            case AQUARIUS:
                return AQUARIUS;
            case PISCES:
                return PISCES;
            default:
                return SCORPIO;
        }
    }



    private static Time getTime() {
        return new Time(Calendar.getInstance()
                .get(Calendar.YEAR),
                Calendar.getInstance()
                        .get(Calendar.MONTH) + 1,
                Calendar.getInstance()
                        .get(Calendar.DAY_OF_MONTH));
    }

    public static Type getType(Time t) {
        switch (t.month) {
            case 1:
                if (t.day > 19)
                    return Type.AQUARIUS;
                return Type.CAPRICORN;
            case 2:
                if (t.day > 18)
                    return Type.PISCES;
                return Type.AQUARIUS;
            case 3:
                if (t.day > 20)
                    return Type.ARIES;
                return Type.PISCES;
            case 4:
                if (t.day > 19)
                    return Type.TAURUS;
                return Type.ARIES;
            case 5:
                if (t.day > 20)
                    return Type.GEMINI;
                return Type.TAURUS;
            case 6:
                if (t.day > 21)
                    return Type.CANCER;
                return Type.GEMINI;
            case 7:
                if (t.day > 22)
                    return Type.LEO;
                return Type.CANCER;
            case 8:
                if (t.day > 22)
                    return Type.VIRGO;
                return Type.LEO;
            case 9:
                if (t.day > 22)
                    return Type.LIBRA;
                return Type.VIRGO;
            case 10:
                if (t.day > 23)
                    return Type.SCORPIO;
                return Type.LIBRA;
            case 11:
                if (t.day > 22)
                    return Type.SAGITTARIUS;
                return Type.SCORPIO;
            case 12:
                if (t.day > 21)
                    return Type.CAPRICORN;
                return Type.SAGITTARIUS;
            default:
                return Type.SCORPIO;
        }
    }



    private static class Time {
        private final int year;
        private final int month;
        private final int day;

        Time(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        private Time(Builder builder) {
            year = builder.year;
            month = builder.month;
            day = builder.day;
        }

        static final class Builder {
            private int year;
            private int month;
            private int day;

            public Builder() {
            }

            public Builder year(int val) {
                year = val;
                return this;
            }

            public Builder month(int val) {
                month = val;
                return this;
            }

            public Builder day(int val) {
                day = val;
                return this;
            }

            public Time build() {
                return new Time(this);
            }
        }
    }

}
