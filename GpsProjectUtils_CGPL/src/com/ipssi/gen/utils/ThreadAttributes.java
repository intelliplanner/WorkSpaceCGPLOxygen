package com.ipssi.gen.utils;
import java.util.HashMap;
import java.util.Map;

public class ThreadAttributes {
    private static ThreadLocal<Map<String, String>> threadAttrs = new ThreadLocal<Map<String, String>>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<String, String>();
        }
    };

    public static String get(String key) {
        return threadAttrs.get().get(key);
    }

    public static void set(String key, String value) {
        threadAttrs.get().put(key, value);
    }
}