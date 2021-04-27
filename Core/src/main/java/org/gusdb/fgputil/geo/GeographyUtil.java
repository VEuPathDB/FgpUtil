package org.gusdb.fgputil.geo;

import java.util.function.Function;

import org.gusdb.fgputil.Tuples.ThreeTuple;

public class GeographyUtil {

  public static final double RAD_TO_DEG = 180 / Math.PI;
  public static final double DEG_TO_RAD = Math.PI / 180;

  public enum Units {

    DEGREES(d -> d * DEG_TO_RAD, d -> d),
    RADIANS(r -> r, r -> RAD_TO_DEG * r);

    private final Function<Double,Double> _toRadians;
    private final Function<Double,Double> _toDegrees;

    private Units(Function<Double,Double> toRadians, Function<Double,Double> toDegrees) {
      _toRadians = toRadians;
      _toDegrees = toDegrees;
    }

    public double toDegrees(double value) {
      return _toDegrees.apply(value);
    }

    public double toRadians(double value) {
      return _toRadians.apply(value);
    }

    public double to(Units toUnits, double value) {
      switch(toUnits) {
        case DEGREES: return _toDegrees.apply(value);
        case RADIANS: default: return _toRadians.apply(value);
      }
    }
  }

  public static class GeographicPoint extends ThreeTuple<Double, Double, Units> {

    public GeographicPoint(double latitude, double longitude, Units units){
      super(latitude, longitude, units);
    }

    public double getLatitude() { return getFirst(); }
    public double getLongitude() { return getSecond(); }
    public Units getUnits() { return getThird(); }

    @Override
    public String toString() {
      return "[ " + getLatitude() + ", " + getLongitude() + " ]";
    }

  }

  public static class CartesianCoordinates extends ThreeTuple<Double, Double, Double> {

    public CartesianCoordinates(double x, double y, double z){
      super(x, y, z);
    }

    public double getX() { return getFirst(); }
    public double getY() { return getSecond(); }
    public double getZ() { return getThird(); }

    @Override
    public String toString() {
      return "[ " + getX() + ", " + getY() + ", " + getZ() +" ]";
    }
  }

  public static double coordToLatRads(double x, double y, double z) {
    return Math.asin(z / Math.sqrt(x * x + y * y + z * z));
    //return Math.atan2(z, Math.sqrt(x * x + y * y));
  }

  public static double coordToLonRads(double x, double y) {
    return Math.atan2(-y, x);
  }

  public static CartesianCoordinates toCartesianCoord(double latitudeRad, double longitudeRad) {
    double cosLatRad = Math.cos(latitudeRad);
    return new CartesianCoordinates(
      /* x */ cosLatRad * Math.sin(longitudeRad + (Math.PI / 2)),
      /* y */ cosLatRad * Math.cos(longitudeRad + (Math.PI / 2)),
      /* z */ Math.sin(latitudeRad));
    //double sinLatRad = Math.sin(latitudeRad);
    //return new CartesianCoordinates(
      /* x */ //sinLatRad * Math.cos(longitudeRad),
      /* y */ //sinLatRad * Math.sin(longitudeRad),
      /* z */ //Math.cos(latitudeRad));
  }

}
