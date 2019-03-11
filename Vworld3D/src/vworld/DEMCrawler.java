package vworld;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import gistools.GeoPoint;
import gistools.GeoTrans;
 
/**
 * vworld로부터 DEM(수치표고모형)을 긁어온다.
 * @author vuski@github
 *
 */
public class DEMCrawler {
	
	static int nn = 0;
	
	static String url3 = "http://xdworld.vworld.kr:8080/XDServer/requestLayerNode?APIKey=";
	static String apiKey = "3A5BC79F-2161-3C0B-A0AD-7F66EF5856D5";
	
	//중복 다운이나 변환하지 않도록 저장할 폴더
	static String storageDirectory  = "C:\\Users\\hgKim\\Documents\\College\\3Grade\\1stSemester\\SoftwareProject\\Let'SSUgo\\Vworld3D\\Terrain\\Origin\\";
	
	//얻고자 하는 영역을 그때그때 다르게 설정해주면 좋다. obj파일들만 저장됨
	static String targetDirectory = "C:\\Users\\hgKim\\Documents\\College\\3Grade\\1stSemester\\SoftwareProject\\Let'SSUgo\\Vworld3D\\Terrain\\Sample\\";
	
	//아래에서 값 참고
	//https://github.com/nasa/World-Wind-Java/blob/master/WorldWind/src/gov/nasa/worldwind/globes/Earth.java
	public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 6356752.3; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.00669437999013; // eccentricity squared, semi-major axis / 이심률 제곱 / 이심률 = Math.sqrt(1-(장반경제곱/단반경제곱))

    public static final double ELEVATION_MIN = -11000d; // Depth of Marianas trench
    public static final double ELEVATION_MAX = 8500d; // Height of Mt. Everest.
	
	static int level = 13;
	/*
	level 15 = 1.5m grid (대략적으로)
	level 14 = 3m grid
	level 13 = 6m grid
	level 12 = 12m grid
	level 11 = 24m grid
	level 10 = 48m grid
	level 9 = 96m grid
	level 8 = 192m grid
	level 7 = 284m grid
	*/
	static double unit = 360 / (Math.pow(2, level) * 10); //15레벨의 격자 크기(단위:경위도)
	
	private static String[] getCoordination() {
		
		String minmax = "37.495729, 126.954277, 37.497108, 126.961208"; //얻고자 하는 영역의  {좌하단 위도, 좌하단 경도, 우상단 위도, 우상단 경도} 순서 
		String[] temp1 = minmax.replaceAll(" ", "").split(",");
		return new String[]{temp1[1],temp1[0], temp1[3],temp1[2]};
	}

	public static void main(String[] args) throws IOException {
		
		//필요한 subfolder를 만든다. 이미 있으면 건너뛴다.
		String[] folders1 = {"DEM bil","DEM txt_Cartesian","DEM txt_latlon","DEM txt_UTMK","DEM dds"};
		makeSubFolders(storageDirectory, folders1);
		String[] folders2 = {"DEM obj","DEM obj_UTMK"};
		makeSubFolders(targetDirectory,folders2);
		
		String layerName = "dem";
		String layerName2 = "tile";
		
		String[] latlon = getCoordination(); //어떤 영역을 가져올지 정한다.
		String minLon = latlon[0]; //경도
		String minLat = latlon[1]; //위도	 
		String maxLon = latlon[2];
		String maxLat = latlon[3];
		
		//idx와 idy를 받는 1단계 단계를 생략하고 여기서 직접 계산한다.
		int minIdx = (int)Math.floor((Double.parseDouble(minLon)+180)/unit);
		int minIdy = (int)Math.floor((Double.parseDouble(minLat)+90)/unit);
		int maxIdx = (int)Math.floor((Double.parseDouble(maxLon)+180)/unit);
		int maxIdy = (int)Math.floor((Double.parseDouble(maxLat)+90)/unit);
		System.out.println(minIdx+" , "+minIdy+" | "+maxIdx+" , "+maxIdy);		
 
		String[][] idxIdyList = new String[(maxIdx-minIdx+1)*(maxIdy-minIdy+1)][2];
		int index = 0;
		for (int i=minIdx ; i<=maxIdx ; i++) {
			for (int j=minIdy ; j<=maxIdy; j++) {
				idxIdyList[index][0] = i+"";
				idxIdyList[index][1] = j+"";
				index++;
			}
		}		
		
		//중복 다운로드를 피하기 위해 현재 있는 파일들 목록을 구한다.
		HashSet<String> fileExistBil = getFileNames(storageDirectory+"DEM bil\\", ".bil");
		HashSet<String> fileExistTxt = getFileNames(storageDirectory+"DEM txt_latlon\\", ".txt");	
		HashSet<String> fileExistObj = getFileNames(targetDirectory+"DEM obj\\", ".obj");	
		HashSet<String> fileNamesDds = getFileNames(storageDirectory+"DEM dds\\", ".dds");
		
		//단위 구역들을 차례차례 처리한다.
		L1 : for (int i=0 ; i<idxIdyList.length ; i++) {
			
			System.out.println("file :"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+"세션 시작....."+(i+1)+"/"+idxIdyList.length);			
			
			//tile 이미지를 받아온다.
			String fileNameDds = "tile_"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+".dds";			
			
			if (!fileNamesDds.contains(fileNameDds)) {
				
				String address3_1= url3 + apiKey +"&Layer=" + layerName2 + "&Level=" + level 
						+ "&IDX=" + idxIdyList[i][0] + "&IDY=" + idxIdyList[i][1];
				sendQueryForBin(address3_1,"DEM dds\\"+fileNameDds);
			} 
			System.out.println("tile ok");
			
			//만약 이미 bil 파일이 존재하면 건너뛴다.
			String fileNameBil = "terrain file_"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+".bil";			
			if (!fileExistBil.contains(fileNameBil)) { //존재하지 않으면 다운받는다.				
				String address3 = url3 + apiKey +"&Layer=" + layerName + "&Level=" + level 
						+ "&IDX=" + idxIdyList[i][0] + "&IDY=" + idxIdyList[i][1];				
				int size = sendQueryForBin(address3, "DEM bil\\"+fileNameBil);   //IDX와 IDY 및 nodeLevel을 보내서 bil목록들을 받아 bil에 저장한다.
				if (size < 16900) { //제대로 된 파일이 아니면.
					System.out.println("file :"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+"세션 건너뜀(용량부족)....."+(i+1)+"/"+idxIdyList.length);
					continue L1;
				}
			} 
				
			String fileNameParsedTxt = "terrain file_"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+".txt";
			if (!fileExistTxt.contains(fileNameParsedTxt)) {
				bilParser(idxIdyList[i], fileNameBil, fileNameParsedTxt); //dat를 다시 읽고 txt에 파싱한다.
				bilParserUTMK(idxIdyList[i], fileNameBil, fileNameParsedTxt); //dat를 다시 읽고 txt에 파싱한다.
			}
			
			
			String fileNameObj = "obj file_"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+".obj";	
			if (!fileExistObj.contains(fileNameObj)) {
				mtlWriter(idxIdyList[i], "DEM obj\\");
				objWriter(idxIdyList[i], fileNameParsedTxt, fileNameObj, "DEM txt_Cartesian\\", "DEM obj\\");
				mtlWriter(idxIdyList[i], "DEM obj_UTMK\\");
				objWriter(idxIdyList[i], fileNameParsedTxt, fileNameObj, "DEM txt_UTMK\\", "DEM obj_UTMK\\"); //dat를 다시 읽고 txt에 파싱한다.
			}
			
			System.out.println(fileNameParsedTxt+"저장완료....."+(i+1)+"/"+idxIdyList.length);
			
		} //for L1
	}
	

	private static void mtlWriter(String[] idxidy, String subFolder) throws IOException {
		
		FileWriter fw = new FileWriter(targetDirectory+subFolder+"mtl_"+idxidy[0]+"_"+idxidy[1]+".mtl");
		BufferedWriter bw = new BufferedWriter(fw);	
		
		bw.write("# Rhino");
		bw.newLine();
		bw.write("newmtl "+idxidy[0]+"_"+idxidy[1]);
		bw.newLine();
		bw.write("Ka 0.0000 0.0000 0.0000");
		bw.newLine();
		bw.write("Kd 1.0000 1.0000 1.0000");
		bw.newLine();
		bw.write("Ks 1.0000 1.0000 1.0000");
		bw.newLine();
		bw.write("Tf 0.0000 0.0000 0.0000");
		bw.newLine();
		bw.write("d 1.0000");
		bw.newLine();
		bw.write("Ns 0");
		bw.newLine();
		bw.write("map_Kd tile_"+idxidy[0]+"_"+idxidy[1]+".dds");
		bw.newLine();
		bw.close();	
		
	}
		
	private static void objWriter(String[] idxidy, String fileNameParsedTxt, String fileNameObj
			, String sourceSubfolder, String targetSubfolder) throws IOException {
		
		FileReader fr = new FileReader(storageDirectory+sourceSubfolder+fileNameParsedTxt);
		BufferedReader br = new BufferedReader(fr);
		
		ArrayList<Double[]> coordinates = new ArrayList<Double[]>();
		String line;
		
		while ((line=br.readLine())!=null) {
			String[] coorStr = line.split(",");
			Double[] coorDb = new Double[3];
			coorDb[0] = Double.parseDouble(coorStr[0]);
			coorDb[1] = Double.parseDouble(coorStr[1]);
			coorDb[2] = Double.parseDouble(coorStr[2]);			
			coordinates.add(coorDb);
		}
		
		br.close();
		
		FileWriter fw = new FileWriter(targetDirectory+targetSubfolder+ fileNameObj);
		BufferedWriter bw = new BufferedWriter(fw);	
		
		bw.write("# Rhino");
		bw.newLine();
		bw.newLine();
		bw.write("mtllib mtl_"+idxidy[0]+"_"+idxidy[1]+".mtl");
		bw.newLine();
		bw.write("g "+idxidy[0]+"_"+idxidy[1]);
		bw.newLine();
		bw.write("usemtl "+idxidy[0]+"_"+idxidy[1]);
		bw.newLine();
		
		for (int i = 0 ; i<coordinates.size() ; i++) {
			bw.write("v "+coordinates.get(i)[0]+" "+coordinates.get(i)[1]+" "+coordinates.get(i)[2]);
			bw.newLine();
		}
		
		for (int i = 0 ; i< 65 ; i++) {
			float v = 1.0f-(i*1.0f/64.0f);
			for (int j=0 ; j<65 ; j++) {
				float u = j*(1.0f/64.0f);
				bw.write("vt "+u+" "+v);
				bw.newLine();				
			}
		}
		
		for (int i = 0 ; i< 64 ; i++) {
			for (int j=1 ; j<65 ; j++) {
				int v = j+(i*65);
				bw.write("f ");
				bw.write(v+"/"+v+" "+(v+65)+"/"+(v+65)+" "+(v+66)+"/"+(v+66));
				bw.newLine();
				bw.write("f ");
				bw.write(v+"/"+v+" "+(v+66)+"/"+(v+66)+" "+(v+1)+"/"+(v+1));
				bw.newLine();
			}			
		}
		
		bw.close();
		
		fileCopy(storageDirectory+"DEM dds\\"+"tile_"+idxidy[0]+"_"+idxidy[1]+".dds", targetDirectory+targetSubfolder+"tile_"+idxidy[0]+"_"+idxidy[1]+".dds");
		
	}
	
	

	
	/**
	 * httpRequest를 보내고 바이너리 파일을 받아 저장한다.
	 * @param address
	 * @param xdofileName
	 */
	private static int sendQueryForBin(String address, String fileName) {
		
		int size = 0;
		
		try {
			
			//이 부분을 쓰려면 아파치에서 httpClient를 다운받아 설치하여야 한다.
			CloseableHttpClient httpClient = HttpClients.createDefault();
			
			HttpGet httpGet = new HttpGet(address);	
			httpGet.addHeader("Referer","http://localhost:4141");
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();	
			BufferedInputStream bis = null;
			
			if (entity != null) {
			    long len = entity.getContentLength();
			    bis = new BufferedInputStream(entity.getContent());
			}
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(storageDirectory+fileName)));
			int inByte;
			
			while((inByte = bis.read()) != -1) {
				bos.write(inByte);
				size++;
			}
			
			bis.close();
			bos.close();			
			httpResponse.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return size;
	}



	/**
	 * xdo 리스트가 있는 dat를 읽고 txt에 파싱하여 저장한다.
	 */
	private static void bilParser(String[] idxIdy, String fileName, String fileNameW) throws IOException {
		
		double idx = Double.parseDouble(idxIdy[0]);
		double idy = Double.parseDouble(idxIdy[1]);
		
		double x = unit * (idx - (Math.pow(2, level-1)*10)); //타일의 좌하단 x좌표(경도) unit= 0.0010986328125 (대략값)
		double y = unit * (idy - (Math.pow(2, level-2)*10));  //타일의 좌하단 y좌표(위도)
		//idx idy에서 먼저 빼 주는 수는 서경 180도, 혹은 남위 90도만큼
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(storageDirectory+"DEM bil\\"+fileName)));
		
		//경위도와 높이로 기록한다.
		FileWriter fw = new FileWriter(storageDirectory+"DEM txt_latlon\\"+ fileNameW);
		BufferedWriter bw = new BufferedWriter(fw);		
		
		//둥근 지구 상의 3차원 좌표로 기록한다.
		FileWriter fwc = new FileWriter(storageDirectory+"DEM txt_Cartesian\\"+ fileNameW);
		BufferedWriter bwc = new BufferedWriter(fwc);	
		
		//terrain height
		//vworld에서 제공하는 DEM이 65x65개의 점으로 되어 있다.
		for (int yy=64 ; yy>=0 ; yy--) {			
			for (int xx=0 ; xx<65 ; xx++) {				
				double xDegree = x+(unit/64)*xx;
				double yDegree = y+(unit/64)*yy;
				float height = pFloat(bis);
				Vec4 coor = geodeticToCartesian(xDegree, yDegree, height);
				
				//65x65의 격자이지만 후속 작업을 고려하여 일렬로 기록한다.
				bwc.write(coor.x+","+coor.y+","+coor.height);
				bwc.newLine();
				bw.write(xDegree+","+(yDegree)+","+height); //이것을 쓰면 경도,위도,높이로 기록
				bw.newLine();	
			}		
		}		
		bis.close();
		bw.close();
		bwc.close();
	}
	
	private static void bilParserUTMK(String[] idxIdy, String fileName, String fileNameW) throws IOException {
		
		double idx = Double.parseDouble(idxIdy[0]);
		double idy = Double.parseDouble(idxIdy[1]);
		
		double x = unit * (idx - (Math.pow(2, level-1)*10)); //타일의 좌하단 x좌표(경도) unit= 0.0010986328125 (대략값)
		double y = unit * (idy - (Math.pow(2, level-2)*10));  //타일의 좌하단 y좌표(위도)
		//idx idy에서 먼저 빼 주는 수는 서경 180도, 혹은 남위 90도만큼
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(storageDirectory+"DEM bil\\"+fileName)));
		
		FileWriter fwc = new FileWriter(storageDirectory+"DEM txt_UTMK\\"+ fileNameW);
		BufferedWriter bwc = new BufferedWriter(fwc);	
		
		//terrain height
		//vworld에서 제공하는 DEM이 65x65개의 점으로 되어 있다.
		for (int yy=64 ; yy>=0 ; yy--) {
			
			for (int xx=0 ; xx<65 ; xx++) {
				
				double xDegree = x+(unit/64)*xx;
				double yDegree = y+(unit/64)*yy;
				float height = pFloat(bis);
				GeoPoint xy_ = new GeoPoint(xDegree,yDegree);
				GeoPoint xy = GeoTrans.convert(GeoTrans.GEO, GeoTrans.UTMK, xy_);
				
				//65x65의 격자이지만 후속 작업을 고려하여 일렬로 기록한다.
				bwc.write(xy.getX()+","+xy.getY()+","+height);
				bwc.newLine();
			}		
		}
		
		bis.close();
		bwc.close();		
	}

	//World Wind source에서 참고하고 vworld에 맞게 수정
	//https://github.com/nasa/World-Wind-Java/blob/master/WorldWind/src/gov/nasa/worldwind/globes/EllipsoidalGlobe.java
	private static Vec4 geodeticToCartesian(double longitude, double latitude, double metersElevation){
        
        double cosLat = Math.cos(latitude * (Math.PI/180));
        double sinLat = Math.sin(latitude * (Math.PI/180));
        double cosLon = Math.cos(longitude * (Math.PI/180));
        double sinLon = Math.sin(longitude * (Math.PI/180));

        double rpm = // getRadius (in meters) of vertical in prime meridian
        		WGS84_EQUATORIAL_RADIUS / Math.sqrt(1.0 - WGS84_ES * sinLat * sinLat);
     
        //vworld의 좌표계산법은 world wind 방식이 아니라 그냥 모두 장반경으로 계산한 약식임
        double x = (WGS84_EQUATORIAL_RADIUS + metersElevation) * cosLat * cosLon;
        double y = (WGS84_EQUATORIAL_RADIUS + metersElevation) * cosLat * sinLon;
        double z = (WGS84_EQUATORIAL_RADIUS + metersElevation) * sinLat;
        
        return new Vec4(x, y, z);
    }
	
	
	private static HashSet<String> getFileNames(String fileLocation, String extension) {
		
		HashSet<String> fileNames = new HashSet<String>(); 		
		File[] files = (new File(fileLocation)).listFiles(); 
		
		// 디렉토리가 비어 있지 않다면 
		if(!(files.length <= 0)){ 			
			for (int i = 0; i < files.length; i++) { 
				if(files[i].isFile() && files[i].getName().endsWith(extension)){ 
				fileNames.add(files[i].getName());
            } 
         } 
       }
		return fileNames;
	}
	
	//4바이트 읽어서 float로 저장 
	private static float pFloat(BufferedInputStream bis) throws IOException {		
		byte[] b = new byte[4];
		int readByteNo = bis.read(b);	
		ArrayUtils.reverse(b);
		return ByteBuffer.wrap(b).getFloat();		
	}
	
	private static void makeSubFolders(String fileLocation, String[] subfolders) {
		
		File file = new File(fileLocation);
		String[] files = file.list(); 
		
		for (String subfolder : subfolders) {
			
			boolean isExist = false;
			
			if (files != null ) {
				for (int j = 0; j < files.length; j++) { 
					if(files[j].equals(subfolder)) {
						isExist = true; //같은 이름이 있으면 빠져나오고
						break;
					}		
	            } //for
			}			
			if (!isExist) {				
				File newDir = new File(fileLocation+subfolder);					
				newDir.mkdirs();		
			}
		}
	}
		
	private static void fileCopy(String inFileName, String outFileName) throws IOException {
		
		//http://fruitdev.tistory.com/87 	
		FileInputStream inputStream = new FileInputStream(inFileName);        
		FileOutputStream outputStream = new FileOutputStream(outFileName);
		  
		FileChannel fcin =  inputStream.getChannel();
		FileChannel fcout = outputStream.getChannel();
		  
		long size = fcin.size();
		fcin.transferTo(0, size, fcout);
		  
		fcout.close();
		fcin.close();		  
		outputStream.close();
		inputStream.close();
	}
	
	
}
