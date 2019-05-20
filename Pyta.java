class Point {
	double x;
	double y;
}

class Rect {
	Point leftUp;
	Point leftDown;
	Point rightUp;
	Point rightDown;
	
	Rect() {
		leftUp = new Point();
		leftDown = new Point();
		rightUp = new Point();
		rightDown = new Point();
	}
}


public class Pyta {
	
	static double wRatio, hRatio, wBias, hBias;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double lux, luy, ldx, ldy, rux, ruy, rdx, rdy;
		double realWidth, realHeight, mapWidth, mapHeight;		
		
		lux = 98652;
		luy = 53475;
		ldx = 94079;
		ldy = 53475;
		rux = 98652;
		ruy = 63818;
		rdx = 94079;
		rdy = 63818;
		
		realWidth = ruy - luy;
		realHeight = lux - ldx;
		
		mapWidth = 173.1;
		mapHeight = 97.1;
		
		wRatio = realWidth / mapWidth;
		hRatio = realHeight / mapHeight;
		
		Rect map = new Rect();
		map.leftUp.x = lux / hRatio;
		map.leftUp.y = luy / wRatio;
		map.leftDown.x = ldx / hRatio;
		map.leftDown.y = ldy / wRatio;
		map.rightUp.x = rux / hRatio;
		map.rightUp.y = ruy / wRatio;
		map.rightDown.x = rdx / hRatio;
		map.rightDown.y = rdy / wRatio;
		
		wBias = map.leftDown.y;
		hBias = map.leftDown.x;
		
		map.leftUp.x -= hBias;
		map.leftUp.y -= wBias;
		map.rightUp.x -= hBias;
		map.rightUp.y -= wBias;
		map.rightDown.x -= hBias;
		map.rightDown.y -= wBias;
		map.leftDown.x -= hBias;
		map.leftDown.y -= wBias;
		
		System.out.println("hRatio : " + hRatio);
		System.out.println("wRatio : " + wRatio);
		System.out.println("hBias : " + hBias);
		System.out.println("wBias : " + wBias);
		System.out.println(map.leftUp.x + ", " + map.leftUp.y + "     " + map.rightUp.x + ", " + map.rightUp.y);
		System.out.println(map.leftDown.x + ", " + map.leftDown.y + "     " + map.rightDown.x + ", " + map.rightDown.y);
		
		double lat, lon, x, z;
		lat = 37.497062;
		lon = 126.955112;
		z = getUnityZ(lat);
		x = getUnityX(lon);
		System.out.print(x + " " + z);
	}
	
	static double getUnityZ(double lat) {
		return lat * 1000000 % 100000 / hRatio - hBias;
	}
	
	static double getUnityX(double lon) {
		return lon * 1000000 % 100000 / wRatio - wBias;
	}

}
