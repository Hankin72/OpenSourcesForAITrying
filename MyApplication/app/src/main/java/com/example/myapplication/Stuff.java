package com.example.myapplication;

import android.Manifest;

public class Stuff {
    private final static String TAG = getTAG(Stuff.class);
    public final static String FILE = "[GesturesTest]";

    public static String getTAG(Class<?> cls) {
        return String.format("[%s]", cls.getSimpleName());
    }

    public static String[] PERMISSIONS_NEED = {
            Manifest.permission.CAMERA
    };

}