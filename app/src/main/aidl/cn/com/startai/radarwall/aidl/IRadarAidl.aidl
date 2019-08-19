// IRadarAidl.aidl
package cn.com.startai.radarwall.aidl;

// Declare any non-default types here with import statements

interface IRadarAidl {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


     int add(int arg1, int arg2);
}
