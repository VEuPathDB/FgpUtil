package org.gusdb.fgputil;

import static org.gusdb.fgputil.geo.GeographyUtil.DEG_TO_RAD;
import static org.gusdb.fgputil.geo.GeographyUtil.RAD_TO_DEG;
import static org.gusdb.fgputil.geo.GeographyUtil.coordToLatRads;
import static org.gusdb.fgputil.geo.GeographyUtil.coordToLonRads;
import static org.gusdb.fgputil.geo.GeographyUtil.toCartesianCoord;

import org.gusdb.fgputil.geo.GeographyUtil.CartesianCoordinates;
import org.gusdb.fgputil.geo.GeographyUtil.GeographicPoint;
import org.gusdb.fgputil.geo.GeographyUtil.Units;
import org.gusdb.fgputil.geo.LatLonAverager;
import org.junit.Assert;
import org.junit.Test;

public class LatLonAvgTest {

  // some random data I found on the internet :)
  private static final double[] POINTS = new double[] {
      42.66666667, -73.75, //"Albany,N.Y."
      35.08333333, -106.65, //"Albuquerque,N.M."
      35.18333333, -101.8333333, //"Amarillo,Tex."
      61.21666667, -149.9, //"Anchorage,Alaska"
      33.75, -84.38333333, //"Atlanta,Ga."
      30.26666667, -97.73333333, //"Austin,Tex."
      44.78333333, -117.8333333, //"Baker,Ore."
      39.3, -76.63333333, //"Baltimore,Md."
      44.8, -68.78333333, //"Bangor,Maine"
      33.5, -86.83333333, //"Birmingham,Ala."
      46.8, -100.7833333, //"Bismarck,N.D."
      43.6, -116.2166667, //"Boise,Idaho"
      42.35, -71.08333333, //"Boston,Mass."
      42.91666667, -78.83333333, //"Buffalo,N.Y."
      51.01666667, -114.0166667, //"Calgary,Alba.,Can."
      32.43333333, -104.25, //"Carlsbad,N.M."
      32.78333333, -79.93333333, //"Charleston,S.C."
      38.35, -81.63333333, //"Charleston,W.Va."
      35.23333333, -80.83333333, //"Charlotte,N.C."
      41.15, -104.8666667, //"Cheyenne,Wyo."
      41.83333333, -87.61666667, //"Chicago,Ill."
      39.13333333, -84.5, //"Cincinnati,Ohio"
      41.46666667, -81.61666667, //"Cleveland,Ohio"
      34, -81.03333333, //"Columbia,S.C."
      40, -83.01666667, //"Columbus,Ohio"
      32.76666667, -96.76666667, //"Dallas,Tex."
      39.75, -105, //"Denver,Colo."
      41.58333333, -93.61666667, //"DesMoines,Iowa"
      42.33333333, -83.05, //"Detroit,Mich."
      42.51666667, -90.66666667, //"Dubuque,Iowa"
      46.81666667, -92.08333333, //"Duluth,Minn."
      44.9, -67, //"Eastport,Maine"
      53.56666667, -113.4666667, //"Edmonton,Alb.,Can."
      32.63333333, -115.55, //"ElCentro,Calif."
      31.76666667, -106.4833333, //"ElPaso,Tex."
      44.05, -123.0833333, //"Eugene,Ore."
      46.86666667, -96.8, //"Fargo,N.D."
      35.21666667, -111.6833333, //"Flagstaff,Ariz."
      32.71666667, -97.31666667, //"FortWorth,Tex."
      36.73333333, -119.8, //"Fresno,Calif."
      39.08333333, -108.55, //"GrandJunction,Colo."
      42.96666667, -85.66666667, //"GrandRapids,Mich."
      48.55, -109.7166667, //"Havre,Mont."
      46.58333333, -112.0333333, //"Helena,Mont."
      21.3, -157.8333333, //"Honolulu,Hawaii"
      34.51666667, -93.05, //"HotSprings,Ark."
      29.75, -95.35, //"Houston,Tex."
      43.5, -112.0166667, //"IdahoFalls,Idaho"
      39.76666667, -86.16666667, //"Indianapolis,Ind."
      32.33333333, -90.2, //"Jackson,Miss."
      30.36666667, -81.66666667, //"Jacksonville,Fla."
      58.3, -134.4, //"Juneau,Alaska"
      39.1, -94.58333333, //"KansasCity,Mo."
      24.55, -81.8, //"KeyWest,Fla."
      44.25, -76.5, //"Kingston,Ont.,Can."
      42.16666667, -121.7333333, //"KlamathFalls,Ore."
      35.95, -83.93333333, //"Knoxville,Tenn."
      36.16666667, -115.2, //"LasVegas,Nev."
      46.4, -117.0333333, //"Lewiston,Idaho"
      40.83333333, -96.66666667, //"Lincoln,Neb."
      43.03333333, -81.56666667, //"London,Ont.,Can."
      33.76666667, -118.1833333, //"LongBeach,Calif."
      34.05, -118.25, //"LosAngeles,Calif."
      38.25, -85.76666667, //"Louisville,Ky."
      43, -71.5, //"Manchester,N.H."
      35.15, -90.05, //"Memphis,Tenn."
      25.76666667, -80.2, //"Miami,Fla."
      43.03333333, -87.91666667, //"Milwaukee,Wis."
      44.98333333, -93.23333333, //"Minneapolis,Minn."
      30.7, -88.05, //"Mobile,Ala."
      32.35, -86.3, //"Montgomery,Ala."
      44.25, -72.53333333, //"Montpelier,Vt."
      45.5, -73.58333333, //"Montreal,Que.,Can."
      50.61666667, -105.5166667, //"MooseJaw,Sask.,Can."
      36.16666667, -86.78333333, //"Nashville,Tenn."
      49.5, -117.2833333, //"Nelson,B.C.,Can."
      40.73333333, -74.16666667, //"Newark,N.J."
      41.31666667, -72.91666667, //"NewHaven,Conn."
      29.95, -90.06666667, //"NewOrleans,La."
      40.78333333, -73.96666667, //"NewYork,N.Y."
      64.41666667, -165.5, //"Nome,Alaska"
      37.8, -122.2666667, //"Oakland,Calif."
      35.43333333, -97.46666667, //"OklahomaCity,Okla."
      41.25, -95.93333333, //"Omaha,Neb."
      45.4, -75.71666667, //"Ottawa,Ont.,Can."
      39.95, -75.16666667, //"Philadelphia,Pa."
      33.48333333, -112.0666667, //"Phoenix,Ariz."
      44.36666667, -100.35, //"Pierre,S.D."
      40.45, -79.95, //"Pittsburgh,Pa."
      43.66666667, -70.25, //"Portland,Maine"
      45.51666667, -122.6833333, //"Portland,Ore."
      41.83333333, -71.4, //"Providence,R.I."
      46.81666667, -71.18333333, //"Quebec,Que.,Can."
      35.76666667, -78.65, //"Raleigh,N.C."
      39.5, -119.8166667, //"Reno,Nev."
      38.76666667, -112.0833333, //"Richfield,Utah"
      37.55, -77.48333333, //"Richmond,Va."
      37.28333333, -79.95, //"Roanoke,Va."
      38.58333333, -121.5, //"Sacramento,Calif."
      45.3, -66.16666667, //"St.John,N.B.,Can."
      38.58333333, -90.2, //"St.Louis,Mo."
      40.76666667, -111.9, //"SaltLakeCity,Utah"
      29.38333333, -98.55, //"SanAntonio,Tex."
      32.7, -117.1666667, //"SanDiego,Calif."
      37.78333333, -122.4333333, //"SanFrancisco,Calif."
      37.33333333, -121.8833333, //"SanJose,Calif."
      18.5, -66.16666667, //"SanJuan,P.R."
      35.68333333, -105.95, //"SantaFe,N.M."
      32.08333333, -81.08333333, //"Savannah,Ga."
      47.61666667, -122.3333333, //"Seattle,Wash."
      32.46666667, -93.7, //"Shreveport,La."
      43.55, -96.73333333, //"SiouxFalls,S.D."
      57.16666667, -135.25, //"Sitka,Alaska"
      47.66666667, -117.4333333, //"Spokane,Wash."
      39.8, -89.63333333, //"Springfield,Ill."
      42.1, -72.56666667, //"Springfield,Mass."
      37.21666667, -93.28333333, //"Springfield,Mo."
      43.03333333, -76.13333333, //"Syracuse,N.Y."
      27.95, -82.45, //"Tampa,Fla."
      41.65, -83.55, //"Toledo,Ohio"
      43.66666667, -79.4, //"Toronto,Ont.,Can."
      36.15, -95.98333333, //"Tulsa,Okla."
      49.21666667, -123.1, //"Vancouver,B.C.,Can."
      48.41666667, -123.35, //"Victoria,B.C.,Can."
      36.85, -75.96666667, //"VirginiaBeach,Va."
      38.88333333, -77.03333333, //"Washington,D.C."
      37.71666667, -97.28333333, //"Wichita,Kan."
      34.23333333, -77.95, //"Wilmington,N.C."
      49.9, -97.11666667, //"Winnipeg,Man.,Can."
  };

  @Test
  public void manyPointAvgTest() {
    doTest(POINTS);
  }

  @Test
  public void farSideOfTheEarthAvgTest() {
    double[] farSidePoints = new double[] {
        45, -179,
        -45, 179
    };
    doTest(farSidePoints);
    
  }

  @Test
  public void noInputsAvgTest() {
    double[] divZeroPoints = new double[] { };
    doTest(divZeroPoints);
  }

  public void doTest(double[] inputs) {
    int numDataPoints = inputs.length / 2;
    System.out.println("Averaging " + numDataPoints + " data points...");
    double lat, latTotal = 0;
    double lon, lonTotal = 0;
    LatLonAverager runningAvg = new LatLonAverager();
    for (int i = 0; i < inputs.length; i+=2) {
      lat = inputs[i];
      lon = inputs[i+1];
      latTotal += lat;
      lonTotal += lon;
      runningAvg.addDataPoint(lat, lon);
    }
    // mostly this is an eyeball thing; in some cases shows why geometric is better
    System.out.println("Simple Average:    " + new GeographicPoint(latTotal / numDataPoints, lonTotal / numDataPoints, Units.DEGREES));
    System.out.println("Geometric Average: " + runningAvg.getCurrentAverage());
  }

  @Test
  public void inverseFunctionTest() {
    double[] latOptions = new double[] {
      5, 27, -8, -33, 0, 90, -90
    };
    double[] lonOptions = new double[] {
      35, 127, -56, -127, 0, 180, -180
    };
    double PRECISION_THRESHOLD = 0.000000000001;
    for (double lat : latOptions) {
      for (double lon : lonOptions) {
        // convert to x,y,z
        CartesianCoordinates coords = toCartesianCoord(DEG_TO_RAD * lat, DEG_TO_RAD * lon);
        // convert back to lat/lon
        double newLat = RAD_TO_DEG * coordToLatRads(coords);
        double newLon = RAD_TO_DEG * coordToLonRads(coords);
        // make sure new values are within some (small) precision threshold of the original
        Assert.assertTrue(Math.abs(lat - newLat) < PRECISION_THRESHOLD);
        Assert.assertTrue(Math.abs(lon - newLon) < PRECISION_THRESHOLD);
      }
    }
  }
}
