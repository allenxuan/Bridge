package com.allenxuan.xuanyihuang.bridge.core;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

public class Bridge {
    private static String TAG = "Bridge";
    private volatile ConcurrentHashMap<Class, Object> implMap;

    private Bridge() {
        implMap = new ConcurrentHashMap<Class, Object>();
    }

    public static <T> T getImpl(Class<T> interfaceClass) {
        if (interfaceClass == null) {
            Log.e(TAG, "getImpl->interface class passed in is null", new Throwable("interface class passed in is null"));
            return null;
        }
        T impl = null;
        try {
            impl = SingletonHolder.singleton.getImplInner(interfaceClass);
        } catch (Throwable throwable) {
            Log.e(TAG, String.format("getImpl error, cause:%s, message:%s", throwable.getCause(), throwable.getMessage()), throwable);
        }
        return impl;
    }

    private <T> T getImplInner(Class<T> interfaceClass) {
        Object impl = implMap.get(interfaceClass);
        if (impl == null) {
            synchronized (interfaceClass) {
                impl = implMap.get(interfaceClass);
                if (impl == null) {
                    String generatedImpBuilderClassSimpleName = String.format("%s$$$$ImplBuilder", interfaceClass.getName());
                    try {
                        Class<?> generatedImplBuilderClass = Class.forName(generatedImpBuilderClassSimpleName);
                        Constructor defaultConstructor = generatedImplBuilderClass.getConstructor();
                        defaultConstructor.setAccessible(true);
                        Object generatedImplBuilder = defaultConstructor.newInstance();
                        if (generatedImplBuilder instanceof ImplBuilder) {
                            impl = ((ImplBuilder) generatedImplBuilder).buildImpl();
                            implMap.put(interfaceClass, impl);
                        }
                    } catch (Throwable throwable) {
                        Log.e(TAG, String.format("getImplInner error, cause:%s, message:%s", throwable.getCause(), throwable.getMessage()), throwable);
                    }
                }
            }
        }

        T implT = null;
        try {
            implT = (T) impl;
        } catch (Throwable throwable) {
            Log.e(TAG, String.format("getImplInner error, cause:%s, message:%s", throwable.getCause(), throwable.getMessage()), throwable);
        }

        return implT;
    }

    public static <T> boolean removeImpl(Class<T> interfaceClass) {
        if (interfaceClass == null) {
            Log.e(TAG, "removeImpl->interface class passed in is null", new Throwable("interface class passed in is null"));
            return false;
        }
        boolean result = false;
        try {
            result = SingletonHolder.singleton.removeImplInner(interfaceClass);
        } catch (Throwable throwable) {
            Log.e(TAG, String.format("removeImpl error, cause:%s, message:%s", throwable.getCause(), throwable.getMessage()), throwable);
        }
        return result;
    }

    private <T> boolean removeImplInner(Class<T> interfaceClass) {
        Object impl = implMap.remove(interfaceClass);
        return (impl != null);
    }

    private static class SingletonHolder {
        private static Bridge singleton = new Bridge();
    }
}
