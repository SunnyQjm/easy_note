package cn.yml.note.utils;

import com.cunoraz.tagview.Tag;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Sunny on 2017/4/19 0019.
 */
public class GsonUtil {
    public static Gson instance = null;

    public static Gson getInstance() {
        if (instance == null) {
            instance = new Gson();
        }
        return instance;
    }

    public static <T> T json2Bean(String json, Class<T> beanClass) {
        T bean = null;
        try {
            bean = getInstance().fromJson(json, beanClass);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public static <T> T json2Bean(String json) {
        T bean = null;
        Type type = new TypeToken<ArrayList<T>>(){}.getType();
        try {
            bean = getInstance().fromJson(json, type);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public static ArrayList<Tag> json2TagList(String json) {
        Type type = new TypeToken<ArrayList<Tag>>(){}.getType();
        ArrayList<Tag> bean = new ArrayList<>();
        try {
            bean = getInstance().fromJson(json, type);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public static String bean2Json(Object object) {
        return getInstance().toJson(object);
    }
}