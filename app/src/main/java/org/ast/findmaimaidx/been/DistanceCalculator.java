package org.ast.findmaimaidx.been;
public class DistanceCalculator {

    // 地球椭球参数（WGS-84）
    private static final double a = 6378137.0; // 长半轴
    private static final double b = 6356752.314245; // 短半轴
    private static final double f = 1 / 298.257223563; // 扁率

    // 计算两点之间的距离
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将角度转换为弧度
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        // 计算纬度差和经度差
        double L = lon2 - lon1;
        double U1 = Math.atan((1 - f) * Math.tan(lat1));
        double U2 = Math.atan((1 - f) * Math.tan(lat2));

        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double lambda = L;
        double lambdaP = 2 * Math.PI;
        double iterLimit = 20;

        double cosSqAlpha = 0;
        double sinSigma = 0;
        double cosSigma = 0;
        double cos2SigmaM = 0;
        double sigma = 0;
        while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0) {
            double sinLambda = Math.sin(lambda);
            double cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) +
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0) {
                return 0; // 两点重合
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0; // 两点位于赤道上
            }
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        }

        if (iterLimit == 0) {
            return 0; // 迭代次数超过限制
        }

        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

        double s = b * A * (sigma - deltaSigma);

        return s / 1000; // 返回距离（单位：公里）
    }

    public static void main(String[] args) {
        double lat1 = 39.9042; // 北京
        double lon1 = 116.4074;
        double lat2 = 31.2304; // 上海
        double lon2 = 121.4737;

        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        System.out.println("Distance between Beijing and Shanghai: " + distance + " km");
    }
}
