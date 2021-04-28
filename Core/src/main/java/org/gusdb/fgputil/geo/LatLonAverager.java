package org.gusdb.fgputil.geo;

import static org.gusdb.fgputil.geo.GeographyUtil.coordToLatRads;
import static org.gusdb.fgputil.geo.GeographyUtil.coordToLonRads;
import static org.gusdb.fgputil.geo.GeographyUtil.toCartesianCoord;

import org.gusdb.fgputil.geo.GeographyUtil.CartesianCoordinates;
import org.gusdb.fgputil.geo.GeographyUtil.GeographicPoint;
import org.gusdb.fgputil.geo.GeographyUtil.Units;

/**
 * Provides a utility to average a set of latitude and longitude coordinates.
 * To do so, lat/lon must be converted to radians if not already, then converted
 * to Cartesian (xyz) coordinates (vectors), which are then averaged and
 * converted back to radians and returned, or back to degrees if necessary.
 */
public class LatLonAverager {

  private final Units _units;

  private double _xSum = 0;
  private double _ySum = 0;
  private double _zSum = 0;

  /**
   * Create a new averager with default input/output units (degrees)
   */
  public LatLonAverager() {
    this(Units.DEGREES);
  }

  /**
   * Create a new averager with the passed input/output units
   *
   * @param units units input lat/lon are sent in as
   */
  public LatLonAverager(Units units) {
    _units = units;
  }

  /**
   * Add a data point.  Values should be in the same units specified in the
   * constructor.
   *
   * @param latitude latitude point
   * @param longitude longitude point
   */
  public void addDataPoint(double latitude, double longitude) {
    CartesianCoordinates coords = toCartesianCoord(
        _units.toRadians(latitude),
        _units.toRadians(longitude)
    );
    _xSum += coords.getX();
    _ySum += coords.getY();
    _zSum += coords.getZ();
  }

  /**
   * Average of the points entered so far.  Units of the geographic point will
   * be the same as the input points.  Note this method can be called again if
   * more points are added and will create another average of all entered points.
   *
   * @return average of the points entered
   */
  public GeographicPoint getCurrentAverage() {
    double avgLatRads = coordToLatRads(_xSum, _ySum, _zSum);
    double avgLonRads = coordToLonRads(_xSum, _ySum);
    return new GeographicPoint(
        Units.RADIANS.to(_units, avgLatRads),
        Units.RADIANS.to(_units, avgLonRads),
        _units
    );
    
  }

}
