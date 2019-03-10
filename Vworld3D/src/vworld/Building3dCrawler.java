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
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

public class Building3dCrawler {
	 
	static int nn = 0;
	static int nnP = 0;
	
	static String url3 = "http://xdworld.vworld.kr:8080/XDServer/requestLayerNode?APIKey=";	
	static String url4 = "http://xdworld.vworld.kr:8080/XDServer/requestLayerObject?APIKey=";	
	static String apiKey = "----apikey를 신청하여 받은 후 이곳에 복사한다.-----";
	static String referer = "http://localhost:4141"; //apikey를 신청할 때 입력하는 호스트 주소
	
	static String storageFolder  = "x:\\vworld\\";	 // 한번 받으면 계속 저장해 둘 폴더
	static String targetFolder = "x:\\vworld\\#obj_sample\\"; //그때그때 필요한 영역을 추출할 폴더, 요청 영역이 달라질때마다 바꾸어줘도 서버 요청 부하는 없다.
	
	static String csName1 = "EPSG:4326";
	static String csName2 = "EPSG:5179";	    
	static CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
	static CRSFactory csFactory = new CRSFactory();	    
	static CoordinateReferenceSystem crs1 = csFactory.createFromName(csName1);
	static CoordinateReferenceSystem crs2 = csFactory.createFromName(csName2);
	static CoordinateTransform trans = ctFactory.createTransform(crs1, crs2);
	static ProjCoordinate p1 = new ProjCoordinate();
	static ProjCoordinate p2 = new ProjCoordinate();
	
	//교량은 레벨 14에서 받아와야 한다.
	//static String layerName = "facility_bridge";
	//static int level = 14;
	
	//건물은 레벨 15에서 받아와야 한다.
	static String layerName = "facility_build";
	static int level = 15;	
	
	static double unit = 360 / (Math.pow(2, level) * 10); //15레벨의 격자 크기(단위:경위도)
	
	static HashSet<String> jpgList;
	static HashSet<String> fileNamesXdo;
	
	private static String[] getCoordination() {		
		
		String minmax = "37.560639, 126.991816,37.571219, 126.999605"; //sample 좌표
		String[] temp1 = minmax.replaceAll(" ", "").split(",");
		return new String[]{temp1[1],temp1[0], temp1[3],temp1[2]};
	}

	public static void main(String[] args) throws IOException {	
		
		//필요한 subfolder를 만든다. 이미 있으면 건너뛴다.
		String[] folders1 = {"jpg","xdo_dat","xdo_Files","xdo_List",};
		makeSubFolders(storageFolder, folders1);
		String[] folders2 = {"xdo_obj","xdo_obj_UTMK"};
		makeSubFolders(targetFolder,folders2);
		
		//불필요한 중복파일 다운로드를 하지 않기 위해 기존에 받아놓았던 파일 목록을 읽어들인다. 
		HashSet<String> fileNamesDAT = getFileNames(storageFolder+"xdo_dat\\", ".dat");
		HashSet<String> fileNamesXdoList = getFileNames(storageFolder+"xdo_List\\", ".txt");
		jpgList = getFileNames(storageFolder+"jpg\\", ".jpg");
		fileNamesXdo = getFileNames(storageFolder+"xdo_Files\\", ".xdo");
		
		//앞에서 설정한 수집 영역 좌표값을 받아온다.
		String[] latlon = getCoordination();
		String minLon = latlon[0];   //경도
		String minLat = latlon[1];	 // 위도
		String maxLon = latlon[2];
		String maxLat = latlon[3];		
		
		//원래는 request와 response를 통해 idx idy 목록들을 받아와야 하지만, 간단한 계산을 통해 구할 수 있으므로 직접 한다.
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
		
		
		L1 : for (int i=0; i<idxIdyList.length ; i++) {
			
			System.out.println("file :"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+"세션 시작....."+(i+1)+"/"+idxIdyList.length);
			
			//request를 위한 주ㅗ 생성 
			String address3 = url3 + apiKey +"&Layer=" + layerName + "&Level=" + level 
					+ "&IDX=" + idxIdyList[i][0] + "&IDY=" + idxIdyList[i][1];
			String fileNameXdo = "xdoList"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+".dat";
			
			//IDX와 IDY 및 nodeLevel을 보내서 xdo목록들을 받아 dat에 저장한다.
			if (!fileNamesDAT.contains(fileNameXdo)) {
				sendQueryForBin(address3, storageFolder+"xdo_dat\\"+fileNameXdo);   			
			}
			
			//쿼리를 보낸 영역에 건물들이 없을 경우 datChecker는 false를 반환한다. 이때 해당 루프는 건너뛴다.
			//위에서 일단 dat 파일을 저장한 후, datChecker에서 다시 읽어온다. 
			if (!datChecker(storageFolder+"xdo_dat\\"+fileNameXdo)) {
				System.out.println("자료 없음. 건너뜀");
				continue L1; 			
			}
			
			String fileNameParsedXdo = "xdoList_parsed"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+".txt";			
			
			if (!fileNamesXdoList.contains(fileNameParsedXdo)) {
				datParser(storageFolder+"xdo_dat\\"+fileNameXdo, storageFolder+"xdo_List\\"+fileNameParsedXdo); //dat를 다시 읽고 txt에 파싱한다.
			}
			
			//obj가 있거나 없거나 그냥 진행한다. 어차피 dat만 쿼리를 보내는 것이고 obj는 내 컴퓨터에서 약간의 계산만 하면 되기 때문이다. 결과 파일을 위에서 설정하면 target로 솎아내준다.
			//텍스쳐를 입히기 위해 obj와 mtl을 기록한다.
			String fileNameObj = "final_object file"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+".obj";
			String fileNameMtl = "final_object file"+idxIdyList[i][0]+"_"+idxIdyList[i][1]+".mtl";
			
			System.out.println(fileNameObj+"저장시도....."+(i+1)+"/"+idxIdyList.length);			
			xdosToObj(fileNameParsedXdo, fileNameObj, fileNameMtl ,idxIdyList[i][0],idxIdyList[i][1] ); //개별적인 xdo들을 호출하여 obj 파일로 만든다.			
			System.out.println(fileNameObj+"저장완료....."+(i+1)+"/"+idxIdyList.length);
			nn=0;
			nnP=0;
		}
		

	}
	
	


	
	private static void xdosToObj(String fileName, String fileNameObj, String fileNameMtl ,String nodeIDX, String nodeIDY) throws IOException {
		
		//쓰기준비
		FileWriter fw = new FileWriter(targetFolder+"xdo_obj\\"+fileNameObj);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write("# Rhino");
		bw.newLine();
		bw.newLine();
		bw.write("mtllib "+fileNameMtl);
		bw.newLine();
		
		FileWriter fw1 = new FileWriter(targetFolder+"xdo_obj_UTMK\\"+fileNameObj);
		BufferedWriter bw1 = new BufferedWriter(fw1);
		
		bw1.write("# Rhino");
		bw1.newLine();
		bw1.newLine();
		bw1.write("mtllib "+fileNameMtl);
		bw1.newLine();
		
		FileWriter fwm = new FileWriter(targetFolder+"xdo_obj\\"+fileNameMtl);
		BufferedWriter bwm = new BufferedWriter(fwm);

		FileWriter fwm1 = new FileWriter(targetFolder+"xdo_obj_UTMK\\"+fileNameMtl);
		BufferedWriter bwm1 = new BufferedWriter(fwm1);		
		
		
		//읽기
		FileReader fr = new FileReader(storageFolder+"xdo_List\\"+fileName);
		BufferedReader br = new BufferedReader(fr);
		
		String line;
		String[] temp;
		
		//네 줄은 파일목록이 아니므로 건너뛴다.
		line=br.readLine();
		line=br.readLine();
		line=br.readLine();
		line=br.readLine();
		
		
		//xdoList에서 xdo 파일이름을 하나하나 읽어들이면서 obj파일을 기록한다.
		//하나의 xdoList에 있는 건물들은 하나의 obj파일에 넣는다.
		while ((line=br.readLine()) != null) {
			
			temp = line.split("\\|");
			String version = temp[0].split("\\.")[3];
			
			String xdofileName = temp[15];
			double lon = Double.parseDouble(temp[4]);
			double lat = Double.parseDouble(temp[5]);
			//float altitude = Float.parseFloat(temp[6]);
			
			//xdo 파일은 3.0.0.1 버젼과 3.0.0.2 버젼이 있다. 각각의 파일에 따라 데이터 저장 방식이 다르므로 구분하여 처리한다.
			if(version.equals("1")) {
				
				//기존에 존재하는 xdo파일이면 다시 요청하지 않는다.
				if (!fileNamesXdo.contains(xdofileName)) {
					sendQueryForBin(getAddressForXdoFile(xdofileName, nodeIDX, nodeIDY), storageFolder+"xdo_Files\\"+xdofileName);
				}
				//System.out.println("version1");
				//둥근 지구 위와 평면 위의 두 가지 형태로 obj를 만든다.
				xdo31Parser(xdofileName, bw, getAddressForJpgFile("", nodeIDX, nodeIDY), bwm);
				xdo31Parser_planar(xdofileName, bw1, lon, lat, bwm1);			
				
			} else if (version.equals("2")){
				
				//기존에 존재하는 xdo파일이면 다시 요청하지 않는다.
				if (!fileNamesXdo.contains(xdofileName)) {
					sendQueryForBin(getAddressForXdoFile(xdofileName, nodeIDX, nodeIDY), storageFolder+"xdo_Files\\"+xdofileName); 
				}
				//System.out.println("version2");
				//둥근 지구 위와 평면 위의 두 가지 형태로 obj를 만든다.
				xdo32Parser(xdofileName, bw, getAddressForJpgFile("", nodeIDX, nodeIDY), bwm);// 다시 xdo 파일을 읽어서 파싱한 후 저장한다.
				xdo32Parser_planar(xdofileName, bw1, lon, lat, bwm1);										
			}
			
		}		
		bw.close();
		bw1.close();
		bwm.close();
		bwm1.close();
		br.close();		
	}
	

	private static void xdo31Parser(String fileName, BufferedWriter bw, String queryAddrForJpg, BufferedWriter bwm) throws IOException {
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(storageFolder+"xdo_Files\\"+fileName)));
		
		int type = pU8(bis);
		int objectId = pU32(bis);
		int keyLen = pU8(bis);
		String key = pChar(bis,keyLen);
		double[] objectBox = {pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis)};
		float altitude = pFloat(bis);
		
		double objX = (objectBox[0]+objectBox[3])/2;
		double objY = (objectBox[1]+objectBox[4])/2;
		double objZ = (objectBox[2]+objectBox[5])/2;
		
		int vertexCount = pU32(bis);		
		
		double[][] vertex = new double[vertexCount][8];
		
		for (int i =0 ; i<vertexCount ; i++) {
			
			float vx = pFloat(bis);
			float vy = pFloat(bis);
			float vz = pFloat(bis);
			float vnx = pFloat(bis);
			float vny = pFloat(bis);
			float vnz = pFloat(bis);
			float vtu = pFloat(bis);
			float vtv = pFloat(bis);			
			
			vertex[i][0] = objX + vx;
			vertex[i][1] = -1 * (objY+ vy);
			vertex[i][2] = objZ + vz;
			vertex[i][3] = vnx;
			vertex[i][4] = vny;
			vertex[i][5] = vnz;
			vertex[i][6] = vtu;
			vertex[i][7] = (1.0f-vtv);
			
		}
		
		int indexedNumber = pU32(bis);
		
		short[] indexed = new short[indexedNumber];
		for (int i=0; i<indexedNumber ; i++) {			
			indexed[i] = (short) (pU16(bis)+1);
		}
		
		int colorA = pU8(bis);
		int colorR = pU8(bis);
		int colorG = pU8(bis);
		int colorB = pU8(bis);		
		
		int imageLevel = pU8(bis);
		int imageNameLen = pU8(bis);
		String imageName = pChar(bis, imageNameLen);		
		
		int nailSize = pU32(bis);		
		//writeNailData(bis, imageName,nailSize);
		
		if (!jpgList.contains(imageName)) sendQueryForBin(queryAddrForJpg+imageName, storageFolder+"jpg\\"+imageName);
		//저장장소에 있는 텍스쳐 파일을 obj와 같은 곳에 복사해준다.
		fileCopy(storageFolder+"jpg\\"+imageName, targetFolder+"xdo_obj\\"+imageName);
		
		bw.write("g "+key);
		bw.newLine();
		
		//material의 기본적 속성은 임의로 아래와 같이 쓴다.
		//mtl 파일의 자세한 스펙은 아래를 참조
		//http://paulbourke.net/dataformats/mtl/
		mtlSubWriter(bwm, key,imageName);	

		for (int i=0 ; i<vertexCount ; i++) {				
			bw.write("v "+vertex[i][0]+" "+vertex[i][1]+" "+vertex[i][2]);	
			bw.newLine();
		}
		for (int i=0 ; i<vertexCount ; i++) {				
			bw.write("vt "+vertex[i][6]+" "+vertex[i][7]);	
			bw.newLine();
		}
		for (int i=0 ; i<vertexCount ; i++) {				
			bw.write("vn "+vertex[i][3]+" "+vertex[i][4]+" "+vertex[i][5]);	
			bw.newLine();
		}
		bw.write("usemtl "+key);
		bw.newLine();
		for (int i=0 ; i<indexedNumber ; i=i+3) {
			bw.write("f ");
			bw.write((indexed[i]+nnP)+"/"+(indexed[i]+nnP)+"/"+(indexed[i]+nnP)+" ");
			bw.write((indexed[i+1]+nnP)+"/"+(indexed[i+1]+nnP)+"/"+(indexed[i+1]+nnP)+" ");	
			bw.write((indexed[i+2]+nnP)+"/"+(indexed[i+2]+nnP)+"/"+(indexed[i+2]+nnP));	
			bw.newLine();
		}
		nn=nn+indexedNumber;
		bis.close();	
	}
	
	private static void xdo31Parser_planar(String fileName, BufferedWriter bw, double lon, double lat, BufferedWriter bwm) throws IOException {
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(storageFolder+"xdo_Files\\"+fileName)));		
		
		int type = pU8(bis);
		int objectId = pU32(bis);
		int keyLen = pU8(bis);
		String key = pChar(bis,keyLen);
		double[] objectBox = {pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis)};
		float altitude = pFloat(bis);
		
		double objX = (objectBox[0]+objectBox[3])/2;
		double objY = (objectBox[1]+objectBox[4])/2;
		double objZ = (objectBox[2]+objectBox[5])/2;
		
		float[] objxyz = rotate3d((float)objX, (float)objY, (float)objZ, lon, lat);
		
		p1.x = lon;
	    p1.y = lat;	
	    trans.transform(p1, p2);		
		
		int vertexCount = pU32(bis);	
		
		double[][] vertex = new double[vertexCount][8];
		
		for (int i =0 ; i<vertexCount ; i++) {
			
			float vx = pFloat(bis);
			float vy = pFloat(bis);
			float vz = pFloat(bis);
			float vnx = pFloat(bis);
			float vny = pFloat(bis);
			float vnz = pFloat(bis);
			float vtu = pFloat(bis);
			float vtv = pFloat(bis);
			
			float[] xyz = rotate3d(vx, vy, vz, lon, lat);			
			
			vertex[i][0] = p2.x + xyz[0];
			vertex[i][1] = p2.y -1 * (xyz[1]);			
			vertex[i][2] = xyz[2] +objxyz[2] -6378137; //vworld이 참조하고 있는 world wind는 타원체가 아니라 6,378,137m의 반지름을 가지는 구면체다.
			vertex[i][3] = vnx;
			vertex[i][4] = vny;
			vertex[i][5] = vnz;
			vertex[i][6] = vtu;
			vertex[i][7] = (1.0f-vtv);
			
		}
		
		int indexedNumber = pU32(bis);		
		
		short[] indexed = new short[indexedNumber];
		for (int i=0; i<indexedNumber ; i++) {
			indexed[i] = (short) (pU16(bis)+1);		
		}		

		int colorA = pU8(bis);
		int colorR = pU8(bis);
		int colorG = pU8(bis);
		int colorB = pU8(bis);		
		
		int imageLevel = pU8(bis);
		int imageNameLen = pU8(bis);
		String imageName = pChar(bis, imageNameLen);		
		
		int nailSize = pU32(bis);		
		//writeNailData(bis, imageName,nailSize);
		//저장장소에 있는 텍스쳐 파일을 obj와 같은 곳에 복사해준다.
		fileCopy(storageFolder+"jpg\\"+imageName, targetFolder+"xdo_obj_UTMK\\"+imageName);
		
		bw.write("g "+key);
		bw.newLine();
		
		mtlSubWriter(bwm, key,imageName);
		
		for (int i=0 ; i<vertexCount ; i++) {				
			bw.write("v "+vertex[i][0]+" "+vertex[i][1]+" "+vertex[i][2]);	
			bw.newLine();
		}
		for (int i=0 ; i<vertexCount ; i++) {				
			bw.write("vt "+vertex[i][6]+" "+vertex[i][7]);	
			bw.newLine();
		}
		for (int i=0 ; i<vertexCount ; i++) {				
			bw.write("vn "+vertex[i][3]+" "+vertex[i][4]+" "+vertex[i][5]);	
			bw.newLine();
		}
		
		bw.write("usemtl "+key);
		bw.newLine();
		
		for (int i=0 ; i<indexedNumber ; i=i+3) {
			bw.write("f ");
			bw.write((indexed[i]+nnP)+"/"+(indexed[i]+nnP)+"/"+(indexed[i]+nnP)+" ");
			bw.write((indexed[i+1]+nnP)+"/"+(indexed[i+1]+nnP)+"/"+(indexed[i+1]+nnP)+" ");	
			bw.write((indexed[i+2]+nnP)+"/"+(indexed[i+2]+nnP)+"/"+(indexed[i+2]+nnP));	
			bw.newLine();
		}
		nnP=nnP+indexedNumber;
		bis.close();	
		
	}

	private static void xdo32Parser(String fileName, BufferedWriter bw, String queryAddrForJpg, BufferedWriter bwm) throws IOException {
		
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(storageFolder+"xdo_Files\\"+fileName)));
		

		int type = pU8(bis);
		int objectId = pU32(bis);
		int keyLen = pU8(bis);
		String key = pChar(bis,keyLen);
		double[] objectBox = {pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis)};
		float altitude = pFloat(bis);		
		
		double objX = (objectBox[0]+objectBox[3])/2;
		double objY = (objectBox[1]+objectBox[4])/2;
		double objZ = (objectBox[2]+objectBox[5])/2;
		
		int faceNum = pU8(bis);
		
		for (int j=0 ; j<faceNum ; j++) {
			
			int vertexCount = pU32(bis);			
			
			double[][] vertex = new double[vertexCount][8];
			
			for (int i =0 ; i<vertexCount ; i++) {
				
				float vx = pFloat(bis);
				float vy = pFloat(bis);
				float vz = pFloat(bis);
				float vnx = pFloat(bis);
				float vny = pFloat(bis);
				float vnz = pFloat(bis);
				float vtu = pFloat(bis);
				float vtv = pFloat(bis);
				
				vertex[i][0] = objX + vx;
				vertex[i][1] = -1 * (objY+ vy);				
				vertex[i][2] = objZ + vz;
				vertex[i][3] = vnx;
				vertex[i][4] = vny;
				vertex[i][5] = vnz;
				vertex[i][6] = vtu;
				vertex[i][7] = (1.0f-vtv);				
				
			}
			
			int indexedNumber = pU32(bis);			

			short[] indexed = new short[indexedNumber];
			for (int i=0; i<indexedNumber ; i++) {				
				indexed[i] = (short) (pU16(bis)+1);				
			}
			
			int colorA = pU8(bis);
			int colorR = pU8(bis);
			int colorG = pU8(bis);
			int colorB = pU8(bis);			
			
			int imageLevel = pU8(bis);
			int imageNameLen = pU8(bis);
			String imageName = pChar(bis, imageNameLen);			
			
			int nailSize = pU32(bis);
			
			//writeNailData(bis, imageName,nailSize);
			if (!jpgList.contains(imageName)) sendQueryForBin(queryAddrForJpg+imageName, storageFolder+"jpg\\"+imageName);
			//저장장소에 있는 텍스쳐 파일을 obj와 같은 곳에 복사해준다.
			fileCopy(storageFolder+"jpg\\"+imageName, targetFolder+"xdo_obj\\"+imageName);
			
			bw.write("g "+key);
			bw.newLine();
			
			//material의 기본적 속성은 임의로 아래와 같이 쓴다.
			//mtl 파일의 자세한 스펙은 아래를 참조
			//http://paulbourke.net/dataformats/mtl/
			mtlSubWriter(bwm, key,imageName);
			
			for (int i=0 ; i<vertexCount ; i++) {				
				bw.write("v "+vertex[i][0]+" "+vertex[i][1]+" "+vertex[i][2]);	
				bw.newLine();
			}
			for (int i=0 ; i<vertexCount ; i++) {				
				bw.write("vt "+vertex[i][6]+" "+vertex[i][7]);	
				bw.newLine();
			}
			for (int i=0 ; i<vertexCount ; i++) {				
				bw.write("vn "+vertex[i][3]+" "+vertex[i][4]+" "+vertex[i][5]);	
				bw.newLine();
			}
			
			bw.write("usemtl "+key);
			bw.newLine();
			
			for (int i=0 ; i<indexedNumber ; i=i+3) {
				bw.write("f ");
				bw.write((indexed[i]+nnP)+"/"+(indexed[i]+nnP)+"/"+(indexed[i]+nnP)+" ");
				bw.write((indexed[i+1]+nnP)+"/"+(indexed[i+1]+nnP)+"/"+(indexed[i+1]+nnP)+" ");	
				bw.write((indexed[i+2]+nnP)+"/"+(indexed[i+2]+nnP)+"/"+(indexed[i+2]+nnP));	
				bw.newLine();
			}
				
			nn=nn+indexedNumber;	
			
		}
		bis.close();		
	
		
	}

	private static void xdo32Parser_planar(String fileName, BufferedWriter bw, double lon, double lat, BufferedWriter bwm) throws IOException {
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(storageFolder+"xdo_Files\\"+fileName)));		
		
		int type = pU8(bis);
		int objectId = pU32(bis);
		int keyLen = pU8(bis);
		String key = pChar(bis,keyLen);
		double[] objectBox = {pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis)};
		float altitude = pFloat(bis);	
		
		double objX = (objectBox[0]+objectBox[3])/2;
		double objY = (objectBox[1]+objectBox[4])/2;
		double objZ = (objectBox[2]+objectBox[5])/2;
		
		float[] objxyz = rotate3d((float)objX, (float)objY, (float)objZ, lon, lat);
		
		p1.x = lon;
	    p1.y = lat;	
	    trans.transform(p1, p2);
		
		int faceNum = pU8(bis);		
		
		for (int j=0 ; j<faceNum ; j++) {

			int vertexCount = pU32(bis);			
			
			double[][] vertex = new double[vertexCount][8];
			
			for (int i =0 ; i<vertexCount ; i++) {
				
				float vx = pFloat(bis);
				float vy = pFloat(bis);
				float vz = pFloat(bis);
				float vnx = pFloat(bis);
				float vny = pFloat(bis);
				float vnz = pFloat(bis);
				float vtu = pFloat(bis);
				float vtv = pFloat(bis);
				
				float[] xyz = rotate3d(vx, vy, vz, lon, lat);
				
				vertex[i][0] = p2.x + xyz[0];
				vertex[i][1] = p2.y -1 * (xyz[1]);
				vertex[i][2] = xyz[2] +objxyz[2] -6378137;
				vertex[i][3] = vnx;
				vertex[i][4] = vny;
				vertex[i][5] = vnz;
				vertex[i][6] = vtu;
				vertex[i][7] = (1.0f-vtv);				
				
			}
			
			int indexedNumber = pU32(bis);

			short[] indexed = new short[indexedNumber];
			for (int i=0; i<indexedNumber ; i++) {				
				indexed[i] = (short) (pU16(bis)+1);							
			}
			
			int colorA = pU8(bis);
			int colorR = pU8(bis);
			int colorG = pU8(bis);
			int colorB = pU8(bis);
			
			int imageLevel = pU8(bis);
			int imageNameLen = pU8(bis);
			String imageName = pChar(bis, imageNameLen);
			
			int nailSize = pU32(bis);
			
			//writeNailData(bis, imageName,nailSize);
			//저장장소에 있는 텍스쳐 파일을 obj와 같은 곳에 복사해준다.
			fileCopy(storageFolder+"jpg\\"+imageName, targetFolder+"xdo_obj_UTMK\\"+imageName);
			
			bw.write("g "+key);
			bw.newLine();
			
			mtlSubWriter(bwm, key,imageName);
			
			for (int i=0 ; i<vertexCount ; i++) {				
				bw.write("v "+vertex[i][0]+" "+vertex[i][1]+" "+vertex[i][2]);	
				bw.newLine();
			}
			
			for (int i=0 ; i<vertexCount ; i++) {				
				bw.write("vt "+vertex[i][6]+" "+vertex[i][7]);	
				bw.newLine();
			}
			for (int i=0 ; i<vertexCount ; i++) {				
				bw.write("vn "+vertex[i][3]+" "+vertex[i][4]+" "+vertex[i][5]);	
				bw.newLine();
			}
			
			bw.write("usemtl "+key);
			bw.newLine();
			
			for (int i=0 ; i<indexedNumber ; i=i+3) {
				bw.write("f ");
				bw.write((indexed[i]+nnP)+"/"+(indexed[i]+nnP)+"/"+(indexed[i]+nnP)+" ");
				bw.write((indexed[i+1]+nnP)+"/"+(indexed[i+1]+nnP)+"/"+(indexed[i+1]+nnP)+" ");	
				bw.write((indexed[i+2]+nnP)+"/"+(indexed[i+2]+nnP)+"/"+(indexed[i+2]+nnP));	
				bw.newLine();
			}
				
			nnP=nnP+indexedNumber;				
			
		}
		bis.close();	
		
	}
	
	private static void mtlSubWriter(BufferedWriter bw, String key, String imageName) throws IOException {
		
		bw.write("newmtl "+key);			
		bw.newLine();
		bw.write("Ka 0.000000 0.000000 0.000000");	
		bw.newLine();
		bw.write("Kd 1.000000 1.000000 1.000000");	
		bw.newLine();
		bw.write("Ks 1.000000 1.000000 1.000000");	
		bw.newLine();
		bw.write("Tf 0.0000 0.0000 0.0000");
		bw.newLine();
		bw.write("d 1.0000");
		bw.newLine();
		bw.write("Ns 0");
		bw.newLine();
		bw.write("map_Kd "+imageName);
		bw.newLine();
		bw.newLine();
		
	}


	//xdo 에 기본적으로 포함된 최하위 해상도 텍스쳐 파일을 꺼낸다.
	private static void writeNailData(BufferedInputStream bis, String fileName, int nailSize) throws IOException {
		
		byte[] b = new byte[nailSize];
		int readByteNo = bis.read(b);
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(storageFolder+"xdo_Files\\"+fileName)));
		
		bos.write(b);
		bos.close();
		return;
	}
	

	private static boolean datChecker(String fileNameXdo) throws IOException {
		
		FileReader fr = new FileReader(fileNameXdo);
		BufferedReader br = new BufferedReader(fr);
		
		//첫줄에 해당 내용이 있다.
		String line = br.readLine();		
		br.close();		
		int check = line.indexOf("ERROR_SERVICE_FILE_NOTTHING");
		
		if (check==-1) return true;
		else return false;	
		
	}



	/**
	 * xdo 리스트가 있는 dat를 읽고 txt에 파싱하여 저장한다.
	 * @param fileName
	 * @param fileNameW
	 * @throws IOException
	 */
	private static void datParser(String fileName, String fileNameW) throws IOException {
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)));		
		
		FileWriter fw = new FileWriter(fileNameW);
		BufferedWriter bw = new BufferedWriter(fw);
		
		int[] datHeader = new int[4];
		String[] datHeaderName = {"level","IDX","IDY","ObjectCount"};
		
		//Header 읽기
		for (int i=0 ; i<4 ; i++) {
			datHeader[i] = pU32(bis);
			bw.write(datHeaderName[i]+"="+datHeader[i]);
			bw.newLine();
		}
		
		//Real3D Model Object 읽기
		for (int i=0 ; i<datHeader[3] ; i++) {
			
			String r_version = pU8(bis)+"."+pU8(bis)+"."+pU8(bis)+"."+pU8(bis);
			int r_type = pU8(bis);
			int r_keylen = pU8(bis);
			
			String r_key = pChar(bis,r_keylen);
			
			double[] r_CenterPos = {pDouble(bis),pDouble(bis)};	
			
			float r_altitude = pFloat(bis);
			
			double[] r_box = {pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis),pDouble(bis)};			
			
			int r_imgLevel = pU8(bis);
			int r_dataFileLen = pU8(bis);
			String r_dataFile = pChar(bis,r_dataFileLen);
			
			int r_imgFileNameLen = pU8(bis);
			String r_imgFileName = pChar(bis,r_imgFileNameLen);			
			
			bw.write(r_version+"|"+r_type+"|"+r_keylen+"|"+r_key+"|"+r_CenterPos[0]+"|"+r_CenterPos[1]
					+"|"+r_altitude+"|"+r_box[0]+"|"+r_box[1]+"|"+r_box[2]+"|"+r_box[3]+"|"+r_box[4]+"|"+r_box[5]+"|"
					+r_imgLevel+"|"+r_dataFileLen+"|"+r_dataFile+"|"+r_imgFileNameLen+"|"+r_imgFileName);
			bw.newLine();
		}
		bis.close();
		bw.close();
		
	}


	//바이너리 파일 파싱
	private static String pVersion(BufferedInputStream bis) throws IOException {
		
		byte[] b = new byte[1];
		int readByteNo = bis.read(b);
		
		return null;
	}

	//바이너리 파일 파싱
	private static float pFloat(BufferedInputStream bis) throws IOException {
		
		byte[] b = new byte[4];
		int readByteNo = bis.read(b);	
		ArrayUtils.reverse(b);
		return ByteBuffer.wrap(b).getFloat();
		
	}

	//바이너리 파일 파싱
	private static double pDouble(BufferedInputStream bis) throws IOException {
		
		byte[] b = new byte[8];
		int readByteNo = bis.read(b);	
		ArrayUtils.reverse(b);
		return ByteBuffer.wrap(b).getDouble();
		
	}

	//바이너리 파일 파싱
	private static String pChar(BufferedInputStream bis, int r_keylen) throws IOException {
		
		StringBuffer string = new StringBuffer();
		
		for (int i = 0 ; i<r_keylen ; i++) {
			
			byte[] b = new byte[1];
			int readByteNo = bis.read(b);				
			char cha = (char)b[0];
			string.append(cha);
		}
		
		return string.toString();
		
	}

	//바이너리 파일 파싱
	private static int pU8(BufferedInputStream bis) throws IOException {
		
		byte[] b = new byte[1];
		int readByteNo = bis.read(b);		
		int number = b[0];		
		return number;
		
	}
	
	//바이너리 파일 파싱
	private static short pU16(BufferedInputStream bis) throws IOException {
		
		byte[] b = new byte[2];
		int readByteNo = bis.read(b);
		ArrayUtils.reverse(b);
		return ByteBuffer.wrap(b).getShort();
		
	}

	//바이너리 파일 파싱
	private static int pU32(BufferedInputStream bis) throws IOException {
		
		
		byte[] b = new byte[4];
		int readByteNo = bis.read(b);
		ArrayUtils.reverse(b);
		return ByteBuffer.wrap(b).getInt();		
	}

	
	private static float[] rotate3d(float vx, float vy, float vz, double lon, double lat) {
		
		float x,y,z;
		
		double p = (lon)/180 * Math.PI;
		double t = (90-lat)/180 * Math.PI;
		
		//원래 회전공식대로 하니까 90도 회전된 결과가 나와 z축을 중심으로 다시 -90도 회전을 했다.
		y = (float) (Math.cos(t)*Math.cos(p)*vx - Math.cos(t)* Math.sin(p) * vy - Math.sin(t)*vz);
		x = -1 *(float) (Math.sin(p)*vx + Math.cos(p)*vy);
		z = (float) (Math.sin(t)*Math.cos(p)*vx -Math.sin(t)*Math.sin(p)*vy + Math.cos(t)*vz);		
		
		return new float[]{x,y,z};
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


	
	/**
	 * httpRequest를 보내고 바이너리 파일을 받아 저장한다.
	 * @param address
	 * @param xdofileName
	 */
	private static void sendQueryForBin(String address, String fileName) {

		try {			
			//이 부분을 쓰려면 아파치에서 httpClient를 다운받아 설치하여야 한다.
			CloseableHttpClient httpClient = HttpClients.createDefault();
			
			HttpGet httpGet = new HttpGet(address);	
			httpGet.addHeader("Referer",referer); //api key 요청시 등록한 주소를 써준다.
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();	
			BufferedInputStream bis = null;
			
			if (entity != null) {
			    long len = entity.getContentLength();
			    bis = new BufferedInputStream(entity.getContent());			    
			}
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
			int inByte;
			while((inByte = bis.read()) != -1) bos.write(inByte);
			
			bis.close();
			bos.close();			
			httpResponse.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}		
		return;		
	}
	

	private static HashSet<String> getFileNames(String fileLocation, String extension) {
		
		HashSet<String> fileNames = new HashSet<String>(); 
		
		File[] files = (new File(fileLocation)).listFiles(); 
		
		// 디렉토리가 비어 있지 않다면 
		if(!(files.length <= 0)){ 
			
			for (int i = 0; i < files.length; i++) { 
				
				// 디렉토리가 아닌 파일인 경우에만 txt 파일인지 검사한다. 
				if(files[i].isFile() && files[i].getName().endsWith(extension)){ 

				fileNames.add(files[i].getName());
				//System.out.println(files[i].getName()+extension);

            } 

         } 

       }		
		
		return fileNames;
	}


	private static String getAddressForXdoFile(String dataFile, String nodeIDX, String nodeIDY) {		
		
		String address= url4+ apiKey + "&Layer=" + layerName+"&Level="+ level +"&IDX=" +nodeIDX+"&IDY="+ nodeIDY
				+ "&DataFile="+dataFile;		
		return address;
	}
	
	private static String getAddressForJpgFile(String jpgFile, String nodeIDX, String nodeIDY) {		
		String address= url4+ apiKey + "&Layer=" + layerName+"&Level="+ level +"&IDX=" +nodeIDX+"&IDY="+ nodeIDY
				+ "&DataFile="+jpgFile;		
		return address;
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


