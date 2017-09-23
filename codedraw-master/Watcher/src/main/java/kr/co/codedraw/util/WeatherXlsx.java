package kr.co.codedraw.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class WeatherXlsx {
	private int testTotalRow; // �׼����� �� ����
	private int testTotalCell = 2; // �׼����� �� ����
	private int weatherTotalRow = 4; // ���� ���� �迭�� ��
	private int weatherTotalCol = 3; // ���� ���� �迭�� ��
	private double[][] test;
	private double[][] presentWeather;
	private double[][] preYearWeather;
	//ȭ�� - 14�⵵ {���,����} ������
	/*double[][] presentWeather = 
			{ { 30.92, 50.0 }, { 35.42, 51.9 }, { 46.04, 59.8 }, { 57.02, 59.8 }, 
			{ 66.02, 59.0 }, { 73.40, 72.8 }, { 78.98, 74.2 }, { 77.18, 77.1 }, 
			{ 71.60, 68.5 }, { 60.08, 63.1 }, { 48.02, 60.9 }, { 26.78, 56.2 } };*/
	
	// 14�⵵ ��� - 2 ������
	/*double[][] presentWeather = 
			{ { 28.92, 50.0 }, { 33.42, 51.9 }, { 44.04, 59.8 }, { 55.02, 59.8 }, 
			{ 64.02, 59.0 }, { 71.40, 72.8 }, { 76.98, 74.2 }, { 75.18, 77.1 }, 
			{ 69.60, 68.5 }, { 58.08, 63.1 }, { 46.02, 60.9 }, { 24.78, 56.2 } };*/
	
	//ȭ�� - 15�⵵ {���, ����}������
	/*double[][] presentWeather =
			{ {30.38, 56.4}, {33.8, 58.9}, {43.34, 45.2}, {55.94, 54.4}, 
			{66.02,52.8}, {74.48, 59.8}, {78.44, 71.2}, {79.34, 69.8}, 
			{72.32, 56.2}, {59.9, 60.4}, {48.02, 73.3}, {34.88, 59.9} };*/
	
	// 15�⵵ ���-2
	/*double[][] presentWeather =
		{ {28.38, 56.4}, {30.8, 58.9}, {41.34, 45.2}, {53.94, 54.4}, 
		{64.02,52.8}, {72.48, 59.8}, {72.44, 71.2}, {72.34, 69.8}, 
		{72.32, 56.2}, {57.9, 60.4}, {42.02, 73.3}, {32.88, 59.9} };*/

	//ȭ�� - 16�⵵ 9������ {���, ����}������
	/*double[][] presentWeather =
		{ {26.24, 53.0}, {32.36, 52.1}, {44.6, 51.0}, {57.38, 54.8},
		  {67.28, 56.2}, {74.48, 62.9}, {79.16, 72.9}, {82.4, 63.9}, {75.02, 65.6}};*/
	
	// 16�⵵ ��� - 2
	/*double[][] presentWeather =
		{ {24.24, 53.0}, {30.36, 52.1}, {42.6, 51.0}, {55.38, 54.8},
		  {65.28, 56.2}, {72.48, 62.9}, {77.16, 72.9}, {80.4, 63.9}, {73.02, 65.6}};*/
	public int compareRegionName(String region){
		int sheetPage = -1;
		RegionCode regionCode = new RegionCode();
		HashMap <String, String> sheetCode = new HashMap<String, String>();
		sheetCode = regionCode.getSheetCode();
		sheetPage = Integer.parseInt(sheetCode.get(region));
		
		return sheetPage;
	}
	public double[][] weatherXlsx(String region) {
		
		try {
			File file = new File("C:/Users/Administrator/Desktop/CodeDraw/weather.xlsx");
			
			//File file = new File("~"+ File.separator + "watcher" + File.separator + "apache-tomcat-7.0.72" + File.separator + "webapps" + File.separator + "xlsx" + File.separator +"weather.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
			
			int sheetPage = compareRegionName(region);
			XSSFSheet sheet = wb.getSheetAt(sheetPage);
			testTotalRow = sheet.getPhysicalNumberOfRows();
			//testTotalRow = 60; // 12���� * 5��ġ 
			test = new double[testTotalRow][testTotalCell];
			for (int rowIdx = 0; rowIdx < testTotalRow; rowIdx++) {
				XSSFRow row = sheet.getRow(rowIdx);
				for (int cellIdx = 0; cellIdx < testTotalCell; cellIdx++) {
					XSSFCell cell = row.getCell(cellIdx);
					if (cell == null)
						continue;
					test[rowIdx][cellIdx] =  Double.parseDouble(cell.toString());
				}
			}
		} catch (FileNotFoundException fe) {
			System.out.println("fileNot");
		} catch (IOException ie) {
			System.out.println("IOException");
		}
		
		return test;
	}
	
	public double[][][] readWeatherXlsx(String region) {
		
		// [��][�⵵][��] ( ������ : ���,����)
		double [][][] weather = new double [12][weatherTotalRow][weatherTotalCol];	
		double[][] test1 = weatherXlsx(region);
		presentWeather = new double[12][2];	// ������(15�⵵) ���� ������
		preYearWeather = new double[12][2];	// ���⵵(14�⵵) ���� ������
		// �������� ������ �����͸� ������ �迭�� �ֱ�
		for (int h = 0; h < 12; h++) {
			for (int i = 0; i < weatherTotalRow; i++) {
				weather[h][i][0] = 1;	// ���� ������ ù��° ���� 1�� ����(jama���̺귯������ �׷��Ե�����) (1,���,����)
				for (int j = 1; j < weatherTotalCol; j++) {
					// test���� 16�⵵���� �����Ͱ� ������ weather���� 14�⵵������ �ֵ�����
					weather[h][i][j] = test1[h + i * 12][j-1];
				}
			}
		}
		for(int i=0; i<12; i++){
			for(int j=0;j<2;j++){
				presentWeather[i][j] = test1[i+60][j];	// weather���� ���Ͽ��� 2015�⵵(+60)�� row���� 12���� ������ 
			}
		}
		for(int i=0; i<12; i++){
			for(int j=0;j<2;j++){
				preYearWeather[i][j] = test1[i+48][j];	// weather���� ���Ͽ��� 2014�⵵(+48)�� row���� 12���� ������ 
			}
		}
		//�׼����� ������ ������ ��� 
		//totalRow = 5����� * ��ġ = 5 * 4��ġ = 20
		//totalCell = 12��  
		/*for (int i = 0; i < testTotalRow; i++) {
			System.out.print("{ ");
			for (int j = 0; j < testTotalCell; j++) {
				System.out.print(test1[i][j] + ", ");
			}
			System.out.println("},");
		}
		System.out.println();*/
		
		//�迭�� �����ؼ� ���� ������ ���
		/*for(int i = 0; i < 12; i++){
			for(int j = 0; j < weatherTotalRow; j++){
				System.out.print("{ ");
				for(int k = 0 ; k < weatherTotalCol; k++){
					System.out.print(weather[i][j][k] + ", ");
				}
				System.out.println("},");
			}
		}*/
		
		return weather;
	}
	public double[][] getPreYearWeather() {
		return preYearWeather;
	}
	
	public double[][] getPresentWeather() {
		return presentWeather;
	}

	public void setPresentWeather(double[][] presentWeather) {
		this.presentWeather = presentWeather;
	}
}