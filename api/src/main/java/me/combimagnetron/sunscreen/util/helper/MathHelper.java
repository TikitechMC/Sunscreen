package me.combimagnetron.sunscreen.util.helper;

public class MathHelper {

    private MathHelper() {}

    public static boolean isPowerOfTwo(double x) {
        if (x <= 0) {
            return false;
        }
        double log2Value = Math.log(x) / Math.log(2);
        return Math.abs(log2Value - Math.round(log2Value)) < 1e-9;
    }

}
