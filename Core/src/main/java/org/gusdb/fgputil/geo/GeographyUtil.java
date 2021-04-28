package org.gusdb.fgputil.geo;

import java.util.function.Function;

import org.gusdb.fgputil.Tuples.ThreeTuple;

/**
 * Collection of spherical coordinate calculation utilities for conversion and
 * averaging of latitude/longitudinal data.
 *
 * @author rdoherty
 */
public class GeographyUtil {

  /** Constant multiplier to convert radians to degrees */
  public static final double RAD_TO_DEG = 180 / Math.PI;

  /** Constant muliplier to convert degrees to radians */
  public static final double DEG_TO_RAD = Math.PI / 180;

  /**
   * Units for values representing angles;
   * provides conversion methods between the two
   */
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

  /**
   * Utility class to encapsulate a latitude, longitude, and the units of the point
   */
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

  /**
   * Utility class to encapsulate a three dimensional point and/or a vector
   * from a known reference point.
   */
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

  /**
   * Converts Cartesian coordinates to a latitude in radians (-Pi/2, Pi/2]
   */
  public static double coordToLatRads(double x, double y, double z) {
    return Math.asin(z / Math.sqrt(x * x + y * y + z * z));
  }

  /**
   * Converts Cartesian coordinates to a latitude in radians (-Pi/2, Pi/2]
   */
  public static double coordToLatRads(CartesianCoordinates coords) {
    return coordToLatRads(coords.getX(), coords.getY(), coords.getZ());
  }

  /**
   * Converts Cartesian coordinates to a longitude in radians (-Pi, Pi]
   */
  public static double coordToLonRads(double x, double y) {
    return Math.atan2(-y, x);
  }

  /**
   * Converts Cartesian coordinates to a longitude in radians (-Pi, Pi]
   */
  public static double coordToLonRads(CartesianCoordinates coords) {
    return coordToLonRads(coords.getX(), coords.getY());
  }

  /**
   * Converts values of latitude and longitude into reference Cartesian
   * coordinates which can be converted back using the other utils in this class.
   *
   * @param latitudeRad latitude radians in range (-Pi/2, Pi/2]
   * @param longitudeRad longitude radians in range (-Pi, Pi]
   * @return
   */
  public static CartesianCoordinates toCartesianCoord(double latitudeRad, double longitudeRad) {
    double referenceLon = longitudeRad + (Math.PI / 2);
    double cosLatRad = Math.cos(latitudeRad);
    return new CartesianCoordinates(
      /* x */ cosLatRad * Math.sin(referenceLon),
      /* y */ cosLatRad * Math.cos(referenceLon),
      /* z */ Math.sin(latitudeRad));
  }
}
