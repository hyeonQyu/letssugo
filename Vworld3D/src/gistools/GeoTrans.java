//// http://javaexpert.tistory.com/142 참고

package gistools;

public class GeoTrans {
	
	public static final int GEO=0;
	public static final int KATEC=1;
	public static final int TM=2;
	public static final int GRS80=3;
	public static final int UTMK=4;
	public static final int EPSG3857=5;
	
	private static double[] m_Ind = new double[6];
	private static double[] m_Es = new double[6];
	private static double[] m_Esp = new double[6];
	private static double[] src_m = new double[6];
	private static double[] dst_m = new double[6];

	private static double EPSLN = 0.0000000001;
	private static double[] m_arMajor = new double[6];
	private static double[] m_arMinor = new double[6];

	private static double[] m_arScaleFactor = new double[6];
	private static double[] m_arLonCenter = new double[6];
	private static double[] m_arLatCenter = new double[6];
	private static double[] m_arFalseNorthing = new double[6];
	private static double[] m_arFalseEasting = new double[6];
	
	private static double[] datum_params = new double[3];
	
	static {
		m_arScaleFactor[GEO] = 1;
		m_arLonCenter[GEO] = 0.0;
		m_arLatCenter[GEO] = 0.0;
		m_arFalseNorthing[GEO] = 0.0;
		m_arFalseEasting[GEO] = 0.0;
		m_arMajor[GEO] = 6378137.0;
		m_arMinor[GEO] = 6356752.3142;
		
		
		//네비게이션용 카텍 좌표
		m_arScaleFactor[KATEC] = 0.9999;
		m_arLonCenter[KATEC] = 2.23402144255274; // 128
		m_arLatCenter[KATEC] = 0.663225115757845;
		m_arFalseNorthing[KATEC] = 600000.0;
		m_arFalseEasting[KATEC] = 400000.0;
		m_arMajor[KATEC] = 6377397.155;
		m_arMinor[KATEC] = 6356078.9633422494;
		
		m_arScaleFactor[TM] = 1.0;
		//this.m_arLonCenter[TM] = 2.21656815003280; // 127
		m_arLonCenter[TM] = 2.21661859489671; // 127.+10.485 minute
		m_arLatCenter[TM] = 0.663225115757845;
		m_arFalseNorthing[TM] = 500000.0;
		m_arFalseEasting[TM] = 200000.0;
		m_arMajor[TM] = 6377397.155;
		m_arMinor[TM] = 6356078.9633422494;
		
		m_arScaleFactor[GRS80] = 1.0;//0.9999;
		m_arLonCenter[GRS80] = 2.21656815003280; // 127
		//m_arLonCenter[GRS80] = 2.21661859489671; // 127.+10.485 minute
		m_arLatCenter[GRS80] = 0.663225115757845;
		m_arFalseNorthing[GRS80] = 500000.0;
		m_arFalseEasting[GRS80] = 200000.0;
		m_arMajor[GRS80] = 6378137.;
		m_arMinor[GRS80] = 6356752.3142;
		
		m_arScaleFactor[UTMK] = 0.9996;//0.9999;
		//m_arLonCenter[UTMK] = 2.22534523630815; // 127.502890
		m_arLonCenter[UTMK] = 2.22529479629277; // 127.5
		m_arLatCenter[UTMK] = 0.663225115757845;
		m_arFalseNorthing[UTMK] = 2000000.0;
		m_arFalseEasting[UTMK] = 1000000.0;
		m_arMajor[UTMK] = 6378137.;
		m_arMinor[UTMK] = 6356752.3141403558;

		m_arScaleFactor[EPSG3857] = 1;
		m_arLonCenter[EPSG3857] = 0.0;
		m_arLatCenter[EPSG3857] = 0.0;
		m_arFalseNorthing[EPSG3857] = 0.0;
		m_arFalseEasting[EPSG3857] = 0.0;
		m_arMajor[EPSG3857] = 6378137.0;
		m_arMinor[EPSG3857] = 6378137.0;
		
		datum_params[0] = -146.43;
		datum_params[1] = 507.89;
		datum_params[2] = 681.46;

		double tmp = m_arMinor[GEO] / m_arMajor[GEO];
		m_Es[GEO] = 1.0 - tmp * tmp;
		m_Esp[GEO] = m_Es[GEO] / (1.0 - m_Es[GEO]);

		if (m_Es[GEO] < 0.00001) {
			m_Ind[GEO] = 1.0;
		} else {
			m_Ind[GEO] = 0.0;
		}
		
		
		tmp = m_arMinor[KATEC] / m_arMajor[KATEC];
		m_Es[KATEC] = 1.0 - tmp * tmp;
		m_Esp[KATEC] = m_Es[KATEC] / (1.0 - m_Es[KATEC]);
		
		if (m_Es[KATEC] < 0.00001) {
			m_Ind[KATEC] = 1.0;
		} else {
			m_Ind[KATEC] = 0.0;
		}
		
		tmp = m_arMinor[TM] / m_arMajor[TM];
		m_Es[TM] = 1.0 - tmp * tmp;
		m_Esp[TM] = m_Es[TM] / (1.0 - m_Es[TM]);

		if (m_Es[TM] < 0.00001) {
			m_Ind[TM] = 1.0;
		} else {
			m_Ind[TM] = 0.0;
		}
		
		tmp = m_arMinor[UTMK] / m_arMajor[UTMK];
		m_Es[UTMK] = 1.0 - tmp * tmp;
		m_Esp[UTMK] = m_Es[UTMK] / (1.0 - m_Es[UTMK]);

		if (m_Es[UTMK] < 0.00001) {
			m_Ind[UTMK] = 1.0;
		} else {
			m_Ind[UTMK] = 0.0;
		}
		
		tmp = m_arMinor[GRS80] / m_arMajor[GRS80];
		m_Es[GRS80] = 1.0 - tmp * tmp;
		m_Esp[GRS80] = m_Es[GRS80] / (1.0 - m_Es[GRS80]);

		if (m_Es[GRS80] < 0.00001) {
			m_Ind[GRS80] = 1.0;
		} else {
			m_Ind[GRS80] = 0.0;
		}
		

		tmp = m_arMinor[EPSG3857] / m_arMajor[EPSG3857];
		m_Es[EPSG3857] = 1.0 - tmp * tmp;
		m_Esp[EPSG3857] = m_Es[EPSG3857] / (1.0 - m_Es[EPSG3857]);

		if (m_Es[EPSG3857] < 0.00001) {
			m_Ind[EPSG3857] = 1.0;
		} else {
			m_Ind[EPSG3857] = 0.0;
		}
		
		src_m[GEO] = m_arMajor[GEO] * mlfn(e0fn(m_Es[GEO]), e1fn(m_Es[GEO]), e2fn(m_Es[GEO]), e3fn(m_Es[GEO]), m_arLatCenter[GEO]);
		dst_m[GEO] = m_arMajor[GEO] * mlfn(e0fn(m_Es[GEO]), e1fn(m_Es[GEO]), e2fn(m_Es[GEO]), e3fn(m_Es[GEO]), m_arLatCenter[GEO]);
		src_m[KATEC] = m_arMajor[KATEC] * mlfn(e0fn(m_Es[KATEC]), e1fn(m_Es[KATEC]), e2fn(m_Es[KATEC]), e3fn(m_Es[KATEC]), m_arLatCenter[KATEC]);
		dst_m[KATEC] = m_arMajor[KATEC] * mlfn(e0fn(m_Es[KATEC]), e1fn(m_Es[KATEC]), e2fn(m_Es[KATEC]), e3fn(m_Es[KATEC]), m_arLatCenter[KATEC]);
		src_m[TM] = m_arMajor[TM] * mlfn(e0fn(m_Es[TM]), e1fn(m_Es[TM]), e2fn(m_Es[TM]), e3fn(m_Es[TM]), m_arLatCenter[TM]);
		dst_m[TM] = m_arMajor[TM] * mlfn(e0fn(m_Es[TM]), e1fn(m_Es[TM]), e2fn(m_Es[TM]), e3fn(m_Es[TM]), m_arLatCenter[TM]);
		src_m[GRS80] = m_arMajor[GRS80] * mlfn(e0fn(m_Es[GRS80]), e1fn(m_Es[GRS80]), e2fn(m_Es[GRS80]), e3fn(m_Es[GRS80]), m_arLatCenter[GRS80]);
		dst_m[GRS80] = m_arMajor[GRS80] * mlfn(e0fn(m_Es[GRS80]), e1fn(m_Es[GRS80]), e2fn(m_Es[GRS80]), e3fn(m_Es[GRS80]), m_arLatCenter[GRS80]);
		src_m[UTMK] = m_arMajor[UTMK] * mlfn(e0fn(m_Es[UTMK]), e1fn(m_Es[UTMK]), e2fn(m_Es[UTMK]), e3fn(m_Es[UTMK]), m_arLatCenter[UTMK]);
		dst_m[UTMK] = m_arMajor[UTMK] * mlfn(e0fn(m_Es[UTMK]), e1fn(m_Es[UTMK]), e2fn(m_Es[UTMK]), e3fn(m_Es[UTMK]), m_arLatCenter[UTMK]);
		src_m[EPSG3857] = m_arMajor[EPSG3857] * mlfn(e0fn(m_Es[EPSG3857]), e1fn(m_Es[EPSG3857]), e2fn(m_Es[EPSG3857]), e3fn(m_Es[EPSG3857]), m_arLatCenter[EPSG3857]);
		dst_m[EPSG3857] = m_arMajor[EPSG3857] * mlfn(e0fn(m_Es[EPSG3857]), e1fn(m_Es[EPSG3857]), e2fn(m_Es[EPSG3857]), e3fn(m_Es[EPSG3857]), m_arLatCenter[EPSG3857]);
		
	}

	private static double D2R(double degree) {
		return degree* Math.PI / 180.0;
	}

	private static double R2D(double radian) {
		return radian * 180.0 / Math.PI;
	}

	private static double e0fn(double x) {
		return 1.0 - 0.25 * x * (1.0 + x / 16.0 * (3.0 + 1.25 * x));
	}

	private static double e1fn(double x) {
		return 0.375 * x * (1.0 + 0.25 * x * (1.0 + 0.46875 * x));
	}

	private static double e2fn(double x) {
		return 0.05859375 * x * x * (1.0 + 0.75 * x);
	}

	private static double e3fn(double x) {
		return x * x * x * (35.0 / 3072.0);
	}

	private static double mlfn(double e0, double e1, double e2, double e3, double phi) {
		return e0 * phi - e1 * Math.sin(2.0 * phi) + e2 * Math.sin(4.0 * phi) - e3 * Math.sin(6.0 * phi);
	}

	private static double asinz(double value) {
		if (Math.abs(value) > 1.0) value = (value > 0 ? 1: -1);
		return Math.asin(value);
	}
	
	public static GeoPoint convert(int srctype, int dsttype, GeoPoint in_pt) {
		GeoPoint tmpPt = new GeoPoint();
		GeoPoint out_pt = new GeoPoint();

		if (srctype == GEO) {
			tmpPt.x = D2R(in_pt.x);
			tmpPt.y = D2R(in_pt.y);
		} else {
			tm2geo(srctype, in_pt, tmpPt);
		}

		if (dsttype == GEO) {
			out_pt.x = R2D(tmpPt.x);
			out_pt.y = R2D(tmpPt.y);
		} else {
			geo2tm(dsttype, tmpPt, out_pt);
			//out_pt.x = Math.round(out_pt.x);
			//out_pt.y = Math.round(out_pt.y);
		}

		return out_pt;
	}
	
	public static void geo2tm(int dsttype, GeoPoint in_pt, GeoPoint out_pt) {
		double x, y;
		
		transform(GEO, dsttype, in_pt);
		double delta_lon = in_pt.x - m_arLonCenter[dsttype];
		double sin_phi = Math.sin(in_pt.y);
		double cos_phi = Math.cos(in_pt.y);

		if (m_Ind[dsttype] != 0) {
			double b = cos_phi * Math.sin(delta_lon);

			if ((Math.abs(Math.abs(b) - 1.0)) < EPSLN) {
				//Log.d("무한대 에러");
				//System.out.println("무한대 에러");
			}
		} else {
			double b = 0;
			x = 0.5 * m_arMajor[dsttype] * m_arScaleFactor[dsttype] * Math.log((1.0 + b) / (1.0 - b));
			double con = Math.acos(cos_phi * Math.cos(delta_lon) / Math.sqrt(1.0 - b * b));

			if (in_pt.y < 0) {
				con = con * -1;
				y = m_arMajor[dsttype] * m_arScaleFactor[dsttype] * (con - m_arLatCenter[dsttype]);
			}
		}

		double al = cos_phi * delta_lon;
		double als = al * al;
		double c = m_Esp[dsttype] * cos_phi * cos_phi;
		double tq = Math.tan(in_pt.y);
		double t = tq * tq;
		double con = 1.0 - m_Es[dsttype] * sin_phi * sin_phi;
		double n = m_arMajor[dsttype] / Math.sqrt(con);
		double ml = m_arMajor[dsttype] * mlfn(e0fn(m_Es[dsttype]), e1fn(m_Es[dsttype]), e2fn(m_Es[dsttype]), e3fn(m_Es[dsttype]), in_pt.y);

		out_pt.x = m_arScaleFactor[dsttype] * n * al * (1.0 + als / 6.0 * (1.0 - t + c + als / 20.0 * (5.0 - 18.0 * t + t * t + 72.0 * c - 58.0 * m_Esp[dsttype]))) + m_arFalseEasting[dsttype];
		out_pt.y = m_arScaleFactor[dsttype] * (ml - dst_m[dsttype] + n * tq * (als * (0.5 + als / 24.0 * (5.0 - t + 9.0 * c + 4.0 * c * c + als / 30.0 * (61.0 - 58.0 * t + t * t + 600.0 * c - 330.0 * m_Esp[dsttype]))))) + m_arFalseNorthing[dsttype];
	}


	public static void tm2geo(int srctype, GeoPoint in_pt, GeoPoint out_pt) {
		GeoPoint tmpPt = new GeoPoint(in_pt.getX(), in_pt.getY());
		//System.out.println(tmpPt.x+","+tmpPt.y);
		int max_iter = 6;

		if (m_Ind[srctype] != 0) {
			double f = Math.exp(in_pt.x / (m_arMajor[srctype] * m_arScaleFactor[srctype]));
			double g = 0.5 * (f - 1.0 / f);
			double temp = m_arLatCenter[srctype] + tmpPt.y / (m_arMajor[srctype] * m_arScaleFactor[srctype]);
			double h = Math.cos(temp);
			double con = Math.sqrt((1.0 - h * h) / (1.0 + g * g));
			out_pt.y = asinz(con);

			if (temp < 0) out_pt.y *= -1;

			if ((g == 0) && (h == 0)) {
				out_pt.x = m_arLonCenter[srctype];
			} else {
				out_pt.x = Math.atan(g / h) + m_arLonCenter[srctype];
			}
		}

		tmpPt.x -= m_arFalseEasting[srctype];
		tmpPt.y -= m_arFalseNorthing[srctype];

		double con = (src_m[srctype] + tmpPt.y / m_arScaleFactor[srctype]) / m_arMajor[srctype];
		double phi = con;

		int i = 0;

		while (true) {
			double delta_Phi = ((con + e1fn(m_Es[srctype]) * Math.sin(2.0 * phi) - e2fn(m_Es[srctype]) * Math.sin(4.0 * phi) + e3fn(m_Es[srctype]) * Math.sin(6.0 * phi)) / e0fn(m_Es[srctype])) - phi;
			phi = phi + delta_Phi;

			if (Math.abs(delta_Phi) <= EPSLN) break;

			if (i >= max_iter) {
				//Log.d("무한대 에러");
				//System.out.println("무한대 에러");
				break;
			}

			i++;
		}

		if (Math.abs(phi) < (Math.PI / 2)) {
			double sin_phi = Math.sin(phi);
			double cos_phi = Math.cos(phi);
			double tan_phi = Math.tan(phi);
			double c = m_Esp[srctype] * cos_phi * cos_phi;
			double cs = c * c;
			double t = tan_phi * tan_phi;
			double ts = t * t;
			double cont = 1.0 - m_Es[srctype] * sin_phi * sin_phi;
			double n = m_arMajor[srctype] / Math.sqrt(cont);
			double r = n * (1.0 - m_Es[srctype]) / cont;
			double d = tmpPt.x / (n * m_arScaleFactor[srctype]);
			double ds = d * d;
			out_pt.y = phi - (n * tan_phi * ds / r) * (0.5 - ds / 24.0 * (5.0 + 3.0 * t + 10.0 * c - 4.0 * cs - 9.0 * m_Esp[srctype] - ds / 30.0 * (61.0 + 90.0 * t + 298.0 * c + 45.0 * ts - 252.0 * m_Esp[srctype] - 3.0 * cs)));
			out_pt.x = m_arLonCenter[srctype] + (d * (1.0 - ds / 6.0 * (1.0 + 2.0 * t + c - ds / 20.0 * (5.0 - 2.0 * c + 28.0 * t - 3.0 * cs + 8.0 * m_Esp[srctype] + 24.0 * ts))) / cos_phi);
		} else {
			out_pt.y = Math.PI * 0.5 * Math.sin(tmpPt.y);
			out_pt.x = m_arLonCenter[srctype];
		}
		transform(srctype, GEO, out_pt);
	}

	public static double getDistancebyGeo(GeoPoint pt1, GeoPoint pt2) {
		double lat1 = D2R(pt1.y);
		double lon1 = D2R(pt1.x);
		double lat2 = D2R(pt2.y);
		double lon2 = D2R(pt2.x);

		double longitude = lon2 - lon1;
		double latitude = lat2 - lat1;

		double a = Math.pow(Math.sin(latitude / 2.0), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(longitude / 2.0), 2);
		return 6376.5 * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
	}

	public static double getDistancebyKatec(GeoPoint pt1, GeoPoint pt2) {
		pt1 = convert(KATEC, GEO, pt1);
		pt2 = convert(KATEC, GEO, pt2);

		return getDistancebyGeo(pt1, pt2);
	}
	
	public static double getDistancebyTm(GeoPoint pt1, GeoPoint pt2) {
		pt1 = convert(TM, GEO, pt1);
		pt2 = convert(TM, GEO, pt2);

		return getDistancebyGeo(pt1, pt2);
	}
	
	public static double getDistancebyUTMK(GeoPoint pt1, GeoPoint pt2) {
		pt1 = convert(UTMK, GEO, pt1);
		pt2 = convert(UTMK, GEO, pt2);

		return getDistancebyGeo(pt1, pt2);
	}
	
	public static double getDistancebyGrs80(GeoPoint pt1, GeoPoint pt2) {
		pt1 = convert(GRS80, GEO, pt1);
		pt2 = convert(GRS80, GEO, pt2);

		return getDistancebyGeo(pt1, pt2);
	}

	private static long getTimebySec(double distance) {
		return Math.round(3600 * distance / 4);
	}

	public static long getTimebyMin(double distance) {
		return (long)(Math.ceil(getTimebySec(distance) / 60));
	}
	
	/*
	Author:       Richard Greenwood rich@greenwoodmap.com
	License:      LGPL as per: http://www.gnu.org/copyleft/lesser.html
	*/
	
	/**
	 * convert between geodetic coordinates (longitude, latitude, height)
	 * and gecentric coordinates (X, Y, Z)
	 * ported from Proj 4.9.9 geocent.c
	*/

	// following constants from geocent.c
	private static final double HALF_PI = 0.5 * Math.PI;
	private static final double COS_67P5  = 0.38268343236508977;  /* cosine of 67.5 degrees */
	private static final double AD_C      = 1.0026000 ;
	/* Toms region 1 constant */
	
	private static void transform(int srctype, int dsttype, GeoPoint point) {
		if (srctype == dsttype)
			return;
		
		if ((srctype != 0 && srctype != GRS80 && srctype != UTMK ) || (dsttype != 0 && dsttype != GRS80 && dsttype != UTMK)) {
			// Convert to geocentric coordinates.
			geodetic_to_geocentric(srctype, point);
			
			// Convert between datums
			if (srctype != 0 && srctype != GRS80 && srctype != UTMK ) {
				geocentric_to_wgs84(point);
			}
			
			if (dsttype != 0 && dsttype != GRS80 && dsttype != UTMK ) {
				geocentric_from_wgs84(point);
			}
			
			// Convert back to geodetic coordinates
			geocentric_to_geodetic(dsttype, point);
		}
	}

	private static boolean geodetic_to_geocentric (int type, GeoPoint p) {

	/*
	 * The function Convert_Geodetic_To_Geocentric converts geodetic coordinates
	 * (latitude, longitude, and height) to geocentric coordinates (X, Y, Z),
	 * according to the current ellipsoid parameters.
	 *
	 *    Latitude  : Geodetic latitude in radians                     (input)
	 *    Longitude : Geodetic longitude in radians                    (input)
	 *    Height    : Geodetic height, in meters                       (input)
	 *    X         : Calculated Geocentric X coordinate, in meters    (output)
	 *    Y         : Calculated Geocentric Y coordinate, in meters    (output)
	 *    Z         : Calculated Geocentric Z coordinate, in meters    (output)
	 *
	 */

	  double Longitude = p.x;
	  double Latitude = p.y;
	  double Height = p.z;
	  double X;  // output
	  double Y;
	  double Z;

	  double Rn;            /*  Earth radius at location  */
	  double Sin_Lat;       /*  Math.sin(Latitude)  */
	  double Sin2_Lat;      /*  Square of Math.sin(Latitude)  */
	  double Cos_Lat;       /*  Math.cos(Latitude)  */

	  /*
	  ** Don't blow up if Latitude is just a little out of the value
	  ** range as it may just be a rounding issue.  Also removed longitude
	  ** test, it should be wrapped by Math.cos() and Math.sin().  NFW for PROJ.4, Sep/2001.
	  */
	  if (Latitude < -HALF_PI && Latitude > -1.001 * HALF_PI )
	      Latitude = -HALF_PI ;
	  else if (Latitude > HALF_PI && Latitude < 1.001 * HALF_PI )
	      Latitude = HALF_PI;
	  else if ((Latitude < -HALF_PI) || (Latitude > HALF_PI)) { /* Latitude out of range */
		  return true;
	  }

	  /* no errors */
	  if (Longitude > Math.PI)
		  Longitude -= (2*Math.PI);
	  Sin_Lat = Math.sin(Latitude);
	  Cos_Lat = Math.cos(Latitude);
	  Sin2_Lat = Sin_Lat * Sin_Lat;
	  Rn = m_arMajor[type] / (Math.sqrt(1.0e0 - m_Es[type] * Sin2_Lat));
	  X = (Rn + Height) * Cos_Lat * Math.cos(Longitude);
	  Y = (Rn + Height) * Cos_Lat * Math.sin(Longitude);
	  Z = ((Rn * (1 - m_Es[type])) + Height) * Sin_Lat;

	  p.x = X;
	  p.y = Y;
	  p.z = Z;
	  return false;
	} // cs_geodetic_to_geocentric()


	/** Convert_Geocentric_To_Geodetic
	 * The method used here is derived from 'An Improved Algorithm for
	 * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb 1996
	 */
	private static void geocentric_to_geodetic (int type, GeoPoint p) {

	  double X = p.x;
	  double Y = p.y;
	  double Z = p.z;
	  double Longitude;
	  double Latitude = 0.;
	  double Height;

	  double W;        /* distance from Z axis */
	  double W2;       /* square of distance from Z axis */
	  double T0;       /* initial estimate of vertical component */
	  double T1;       /* corrected estimate of vertical component */
	  double S0;       /* initial estimate of horizontal component */
	  double S1;       /* corrected estimate of horizontal component */
	  double Sin_B0;   /* Math.sin(B0), B0 is estimate of Bowring aux doubleiable */
	  double Sin3_B0;  /* cube of Math.sin(B0) */
	  double Cos_B0;   /* Math.cos(B0) */
	  double Sin_p1;   /* Math.sin(phi1), phi1 is estimated latitude */
	  double Cos_p1;   /* Math.cos(phi1) */
	  double Rn;       /* Earth radius at location */
	  double Sum;      /* numerator of Math.cos(phi1) */
	  boolean At_Pole;  /* indicates location is in polar region */

	  At_Pole = false;
	  if (X != 0.0) {
	      Longitude = Math.atan2(Y,X);
	  }
	  else {
	      if (Y > 0) {
	          Longitude = HALF_PI;
	      }
	      else if (Y < 0) {
	          Longitude = -HALF_PI;
	      }
	      else {
	          At_Pole = true;
	          Longitude = 0.0;
	          if (Z > 0.0) {  /* north pole */
	              Latitude = HALF_PI;
	          }
	          else if (Z < 0.0) {  /* south pole */
	              Latitude = -HALF_PI;
	          }
	          else {  /* center of earth */
	              Latitude = HALF_PI;
	              Height = -m_arMinor[type];
	              return;
	          }
	      }
	  }
	  W2 = X*X + Y*Y;
	  W = Math.sqrt(W2);
	  T0 = Z * AD_C;
	  S0 = Math.sqrt(T0 * T0 + W2);
	  Sin_B0 = T0 / S0;
	  Cos_B0 = W / S0;
	  Sin3_B0 = Sin_B0 * Sin_B0 * Sin_B0;
	  T1 = Z + m_arMinor[type] * m_Esp[type] * Sin3_B0;
	  Sum = W - m_arMajor[type] * m_Es[type] * Cos_B0 * Cos_B0 * Cos_B0;
	  S1 = Math.sqrt(T1*T1 + Sum * Sum);
	  Sin_p1 = T1 / S1;
	  Cos_p1 = Sum / S1;
	  Rn = m_arMajor[type] / Math.sqrt(1.0 - m_Es[type] * Sin_p1 * Sin_p1);
	  if (Cos_p1 >= COS_67P5) {
	      Height = W / Cos_p1 - Rn;
	  }
	  else if (Cos_p1 <= -COS_67P5) {
	      Height = W / -Cos_p1 - Rn;
	  }
	  else {
	      Height = Z / Sin_p1 + Rn * (m_Es[type] - 1.0);
	  }
	  if (At_Pole == false) {
	      Latitude = Math.atan(Sin_p1 / Cos_p1);
	  }

	  p.x = Longitude;
	  p.y =Latitude;
	  p.z = Height;
	  return;
	} // geocentric_to_geodetic()



	/****************************************************************/
	// geocentic_to_wgs84(defn, p )
   //  defn = coordinate system definition,
	//  p = point to transform in geocentric coordinates (x,y,z)
	private static void geocentric_to_wgs84(GeoPoint p) {

	  //if( defn.datum_type == PJD_3PARAM )
	  {
	    // if( x[io] == HUGE_VAL )
	    //    continue;
	    p.x += datum_params[0];
	    p.y += datum_params[1];
	    p.z += datum_params[2];
	  }
	} // geocentric_to_wgs84

	/****************************************************************/
	// geocentic_from_wgs84()
	//  coordinate system definition,
	//  point to transform in geocentric coordinates (x,y,z)
	private static void geocentric_from_wgs84(GeoPoint p) {

	  //if( defn.datum_type == PJD_3PARAM ) 
	  {
	    //if( x[io] == HUGE_VAL )
	    //    continue;
	    p.x -= datum_params[0];
	    p.y -= datum_params[1];
	    p.z -= datum_params[2];

	  }
	} //geocentric_from_wgs84()

}
