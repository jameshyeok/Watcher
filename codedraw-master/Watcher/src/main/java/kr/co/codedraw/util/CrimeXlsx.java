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

public class CrimeXlsx {
	
	private int testTotalRow;		// �׼����� �� ����
	private int testTotalCell = 12;		// �׼����� �� ����
	private int crimeTotalRow = 12;		// ���˰Ǽ� ���� �迭�� ��
	private int crimeTotalCol = 4;		// ���˰Ǽ� ���� �迭�� ��
	private int[][] test;
	
	
	public int compareRegionName(String region){
		int sheetPage = -1;
		RegionCode regionCode = new RegionCode();
		HashMap <String, String> sheetCode = new HashMap<String, String>();
		sheetCode = regionCode.getSheetCode();
		sheetPage = Integer.parseInt(sheetCode.get(region));
		
		return sheetPage;
	}
	
	public int[][] CrimeXlsx(String region) {
		try {
			
			File file = new File("C:/Users/Administrator/Desktop/CodeDraw/crime.xlsx");
			//File file = new File("~"+ File.separator + "watcher" + File.separator + "apache-tomcat-7.0.72" + File.separator + "webapps" + File.separator + "xlsx" + File.separator +"crime.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
			
			int sheetPage = compareRegionName(region);	// �������� �ѱ۷ε� ��Ʈ�̸��� ���ڷ� ��ȯ
			XSSFSheet sheet = wb.getSheetAt(sheetPage);	// ���� ������ sheetPage�� ��Ʈ�� ������
			testTotalRow = sheet.getPhysicalNumberOfRows();	// �ش� sheetPage�� ��ü �� ������ ������
			
			test = new int[testTotalRow][testTotalCell];	// �������� [��ü��][��=12]
			for (int rowIdx = 0; rowIdx < testTotalRow; rowIdx++) {
				XSSFRow row = sheet.getRow(rowIdx);		// rowIdx��°�� ���� �о��
				for (int cellIdx = 0; cellIdx < testTotalCell; cellIdx++) {
					XSSFCell cell = row.getCell(cellIdx);	// �о�� ���� cellIdx��° ���о��
					if (cell == null)
						continue;
					test[rowIdx][cellIdx] = (int) Double.parseDouble(cell.toString());
				}
			}
		} catch (FileNotFoundException fe) {
			System.out.println("fileNot");
		} catch (IOException ie) {
			System.out.println("IOException");
		}
		return test;
	}
	

	public double[][][] readCrimeXlsx(String region) {
		
		// [5�����][��][�� (4)��ġ] ( ���˼��� : ����, ����, ����, ����, ����)
		double [][][] crime = new double [5][crimeTotalRow][crimeTotalCol];
		
		CrimeXlsx(region);
		// �������� ������ �����͸� ���˺��� �迭�� �ֱ�
		for (int h = 0; h < 5; h++) {
			for (int i = 0; i < crimeTotalRow; i++) {
				for (int j = 0, k = 0; j < crimeTotalCol; j++) {
					crime[h][i][j] = test[k + h][i];	// �������� ������ ���� 3�����迭����(ȸ�ͺм��� �´� ����)���� ��迭
					k += 5;
				}
			}
		}
		return crime;
	}
	public double[][] preYearCrime(String region){
		// [5�����][��]
		double[][] preYearCrime = new double[5][12];
		CrimeXlsx(region);
		for(int i = 0; i < 5; i++){
			for(int j = 0; j < 12; j++){
				preYearCrime[i][j] = test[i + 20][j];	// �������� ������ ������ ���� 21��°(2014��) ���ͽ����ؼ� 5�� ���� ���� ������ 
			}
		}
		return preYearCrime;
	}
	
}