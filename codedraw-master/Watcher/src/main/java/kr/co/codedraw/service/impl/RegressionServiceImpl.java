package kr.co.codedraw.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import kr.co.codedraw.dao.CrimeDao;
import kr.co.codedraw.dao.WeatherDao;
import kr.co.codedraw.dto.CrimeDto;
import kr.co.codedraw.dto.WeatherDto;
import kr.co.codedraw.service.RegressionService;
import kr.co.codedraw.util.CrimeXlsx;
import kr.co.codedraw.util.RegionCode;
import kr.co.codedraw.util.RegressionMatrix;
import kr.co.codedraw.util.WeatherXlsx;

@Component
public class RegressionServiceImpl implements RegressionService {
	
	@Autowired
	private CrimeDao crimeDao;
	
	@Autowired
	private WeatherDao weatherDao;
	
	@Override
	public List<CrimeDto> readCrimeRate() {
		List<CrimeDto> crimeList = new ArrayList<CrimeDto>();
		crimeList = crimeDao.readCrimeRate(); 
		return crimeList;
	}

	@Override
	public void writeCrimeRate(CrimeDto crimeDto) {
		crimeDao.writeCrimeRate(crimeDto);
		
	}

	@Override
	public void writeHistoryCrimeRate(CrimeDto crimeDto) {
		crimeDao.writeHistoryCrimeRate(crimeDto);
		
	}

	@Override
	public void writeCrimeCount(CrimeDto crimeDto) {
		crimeDao.writeCrimeCount(crimeDto);
		
	}
	
	@Override
	public List<WeatherDto> readWeather() {
		List<WeatherDto> weatherList = new ArrayList<WeatherDto>();
		weatherList = weatherDao.readWeather();
		return weatherList;
	}

	@Override
	public void writeWeather(WeatherDto weatherDto) {
		weatherDao.writeWeather(weatherDto);
	}
	
	@Override
	public void updateAutoIncrementCrimeRate() {
		crimeDao.updateAutoIncrementCrimeRate();
	}

	@Override
	public void deleteCrimeRate() {
		crimeDao.deleteCrimeRate();
	}

	@Override
	public void updateAutoIncrementCrimeCount() {
		crimeDao.updateAutoIncrementCrimeCount();
	}

	@Override
	public void deleteCrimeCount() {
		crimeDao.deleteCrimeCount();
	}

	@Override
	public void updateAutoIncrementWeather() {
		weatherDao.updateAutoIncrementWeather();
		
	}

	@Override
	public void deleteWeather() {
		weatherDao.deleteWeather();
	}
	
	
	
	
	@Override
	public void writeMonthRate(CrimeDto crimeDto) {
		crimeDao.writeMonthRate(crimeDto);
	}

	//(fixedDelay = 1000 * 15) = 15 �� ����
	//@Scheduled(cron = "0 59 23 * * *")
	// ���� �������ڸ� �� �Լ��� �ٷ� ���۵ǵ��� �ϴ� ������̼� @PostConstruct
	//@PostConstruct	
	public void scheduleRun(){
		
		/*Date date = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
		String presentDate = simpleDateFormat.format(date);*/
		
		CrimeXlsx crimeXlsx = new CrimeXlsx();
		WeatherXlsx weatherXlsx = new WeatherXlsx();
		RegionCode regionCode = new RegionCode();
		
		String[] crimeName = {"����", "����", "����", "����", "����"};
		String[] crimeCodeNum = {"001", "002", "003", "004", "005"};	// theft, murder, robbery, rape, assault
		HashMap <String, String> region = regionCode.getRegionCode();
	
		/*deleteCrimeRate();			// ���� �������� �����ϵ��� ���� ������ ����
		updateAutoIncrementCrimeRate();		// crimeRate_table�� cId���� 1���� �����ϵ��� �ʱ�ȭ
		deleteCrimeCount();
		updateAutoIncrementCrimeCount();
		deleteWeather();
		updateAutoIncrementWeather();*/
		
		int regionCodeNumCount = 0;		// 16�� �������� ���� ����
		while(regionCodeNumCount < 16){
			double[][][] weather = weatherXlsx.readWeatherXlsx(region.get(regionCode.regionName[regionCodeNumCount]));
			double[][][] crime = crimeXlsx.readCrimeXlsx(region.get(regionCode.regionName[regionCodeNumCount]));	// ȸ�ͺм��� ����� �Ǽ��� ����, �������� ���ڷγѰ���	
			double[][] presentWeather = weatherXlsx.getPresentWeather();	// ����(������) ���� ������
			double[][] preYearWeather = weatherXlsx.getPreYearWeather();
			double[][] predictionCrime = new double[5][12];			// ������(15�⵵) 5�� ���˴� 12�� ���˰Ǽ�
			double[][] predictionCrimeRate = new double[5][12];		// ������(15�⵵)5�� ���˴� 12�� ������
			double[][] regressionR2 = new double[5][12];			// 5�� ���˴� 12��, �������R2
			double[] totalCrime = new double[5];					// ������(15�⵵) 1��ġ �� ���˰Ǽ�
			double[] preTotalCrime = new double[5];					// ���⵵(14�⵵) 1��ġ �� ���� �Ǽ�
			double[][] preYearCrime = crimeXlsx.preYearCrime(region.get(regionCode.regionName[regionCodeNumCount]));	// ���⵵(14�⵵) 5����˴� 12�� ���˰Ǽ�
			double[][] preYearCrimeRate = new double[5][12];	// ���⵵(14�⵵)5�� ���˴� 12�� ������
			
			
			WeatherDto weatherDto = new WeatherDto();
			CrimeDto crimeDto = new CrimeDto();
			
			/* ���� ������ DB�� ���� */
			//15�⵵ ���� ������ ����
			/*for(int i = 0; i < 12; i++){	// while���� �������� 16�� ���� ������ for�����δ� 12�޸� ����ϸ��
				if(i>8)
					weatherDto.setDate("2015" + (i + 1) + "00");	
				else
					weatherDto.setDate("20150" + (i + 1) + "00");
				weatherDto.setRegionCodeNum(regionCode.regionNum[regionCodeNumCount]);
				weatherDto.setTemperature(presentWeather[i][0]);
				weatherDto.setHumidity(presentWeather[i][1]);
				writeWeather(weatherDto);
			}*/
			
			//14�⵵ ���� ������ ����
			/*for(int i = 0; i < 12; i++){	// while���� �������� 16�� ���� ������ for�����δ� 12�޸� ����ϸ��
				if(i>8)
					weatherDto.setDate("2014" + (i + 1) + "00");	
				else
					weatherDto.setDate("20140" + (i + 1) + "00");
				weatherDto.setRegionCodeNum(regionCode.regionNum[regionCodeNumCount]);
				weatherDto.setTemperature(preYearWeather[i][0]);
				weatherDto.setHumidity(preYearWeather[i][1]);
				writeWeather(weatherDto);
			}*/
			
			//ȸ�ͺм� - �����⵵(2015��) ���˰Ǽ� ��� 
			/*for (int i = 0; i < crimeCodeNum.length; i++) {				//	crimeName.length = 5
				for (int j = 0; j < presentWeather.length; j++){		//  weather.getWeatherData().length = 12
					RegressionMatrix regression = new RegressionMatrix(weather[i], crime[i][j]);
					// ���� ���� �Ǽ� ��� 
					predictionCrime[i][j] = Math.round(regression.beta(0) + regression.beta(1) * presentWeather[i][0] 
							+ regression.beta(2) * presentWeather[i][1]);	
					regressionR2[i][j] = regression.R2();
					if(predictionCrime[i][j] < 0 )
						predictionCrime[i][j] = 0; //�������˰Ǽ��� ������������ 0���� ����
				}
			}*/
	
			// 14��, 15�� 5�� ���˺� 1��ġ '���� �Ǽ�' �հ�
			/*for(int i = 0; i < crimeCodeNum.length; i++){		// crimeCodeNum.length = 5
				for(int j = 0; j < presentWeather.length; j++){	// presentWeather.length = 12
					totalCrime[i] += predictionCrime[i][j];	 	// 15�� ���˺� ���� �Ǽ� �հ�
					preTotalCrime[i] += preYearCrime[i][j];		// 14�� ���˺� ���� �Ǽ� �հ�
				}
			}*/
			 
			
			/*int total=0;
			int preTotal =0;
			double[] regionCrimeRate1 = new double[12];
			double[] regionCrimeRate2 = new double[12];
			double[] tmp1 = new double[12];
			double[] tmp2 = new double[12];
			
			//preTotalCrime[0] ���� 1��ġ �� ���Ѱ�
			//preYearCrime[0][0] + preYearCrime[1][0] + preYearCrime[2][0] ... => 5����� 1����ġ �� ���Ѱ�
			for(int i= 0; i < 12; i++){
				for(int j=0; j< 5; j++){
					tmp1[i] += predictionCrime[j][i];
					tmp2[i] += preYearCrime[j][i];
				}
			}
	
			// 1�� ���˰Ǽ� ���հ�
			for(int i = 0; i < 5; i++){
				for(int j = 0; j < 12; j++){
					total += predictionCrime[i][j];
					preTotal += preYearCrime[i][j];
				}
			}
			
			// ������ ���� ������ ��� = ���� �ѹ��˰Ǽ� / ��ü ���˰Ǽ�
			for(int i = 0; i < 12; i++){
				regionCrimeRate1[i] = Math.round(tmp1[i] / total * 100 * 10) /10.0;
				regionCrimeRate2[i] = Math.round(tmp2[i] / preTotal * 100 * 10) /10.0;
			}
			
			// ���� ��Ʈ�� �� 2015�� ���� ������ ������ ����
			for(int i = 0; i < 12; i++){ 
				if(i>8)
					crimeDto.setDate("2015" + (i + 1) + "00");	
				else
					crimeDto.setDate("20150" + (i + 1) + "00");	
				crimeDto.setCrimeRate(regionCrimeRate1[i]);
				crimeDto.setRegionCodeNum(regionCode.regionNum[regionCodeNumCount]);
				writeMonthRate(crimeDto);
			
			}
			// ���� ��Ʈ�� �� 2014�� ���� ������ ������ ����
			for(int i = 0; i < 12; i++){ 
				if(i>8)
					crimeDto.setDate("2014" + (i + 1) + "00");	
				else
					crimeDto.setDate("20140" + (i + 1) + "00");	
				crimeDto.setCrimeRate(regionCrimeRate2[i]);
				crimeDto.setRegionCodeNum(regionCode.regionNum[regionCodeNumCount]);
				writeMonthRate(crimeDto);
			
			}*/
			
			
			
			
			// 14��, 15�� 5�� ���˺� ���� '������' ���
			/*for(int i = 0; i < crimeCodeNum.length; i++){
				for(int j = 0; j < presentWeather.length; j++){
					preYearCrimeRate[i][j] = Math.round(preYearCrime[i][j] / preTotalCrime[i] * 100 * 10) / 10.0;
					predictionCrimeRate[i][j] = Math.round(predictionCrime[i][j] / totalCrime[i] * 100 * 10) / 10.0;
					if(predictionCrimeRate[i][j] < 0 )
						predictionCrimeRate[i][j] = 0;	// ���˿������� ������������ 0���� ����
				}
			}*/
			
			
			/* 15�⵵ ���˿����� �� ���˿����Ǽ� DB�� ���� */
			/*for(int i = 0; i < 5; i++){ // i = 5�� ���˴� j = 12��
				if(i == 2)		// 2015�� ������ ���� 
					continue;
				if(regionCode.regionNum[regionCodeNumCount].equals("053") && i == 3)	// 053(�뱸)�̸鼭 004(����)�̸� ����
					continue;
				for(int j = 0; j < presentWeather.length; j++){
					if(j>8)
						crimeDto.setDate("2015" + (j + 1) + "00");	
					else
						crimeDto.setDate("20150" + (j + 1) + "00");	
					crimeDto.setCrimeRate(predictionCrimeRate[i][j]);
					crimeDto.setRegionCodeNum(regionCode.regionNum[regionCodeNumCount]);
					crimeDto.setCrimeCodeNum(crimeCodeNum[i]);
					crimeDto.setCrimeCount(predictionCrime[i][j]);
				//	writeHistoryCrimeRate(crimeDto);	// ���������� �����丮 ����
				//	writeCrimeRate(crimeDto);			// ���������� ����
				//	writeCrimeCount(crimeDto);			// �������� �Ǽ� ����
				}
			}*/
			
			/* �뱸(053)�� ����(004)�� 15�⵵�� 2014�⵵ ������ ���� �� */
			/*if(regionCode.regionNum[regionCodeNumCount].equals("053")){
				for(int i=0; i < 12; i++){
					if(i > 8)
						crimeDto.setDate("2015" + (i + 1) + "00");	
					else
						crimeDto.setDate("20150" + (i + 1) + "00");	
					crimeDto.setCrimeRate(preYearCrimeRate[3][i]);
					crimeDto.setRegionCodeNum(regionCode.regionNum[regionCodeNumCount]);
					crimeDto.setCrimeCodeNum(crimeCodeNum[3]);
					crimeDto.setCrimeCount(preYearCrime[3][i]);
				//	writeHistoryCrimeRate(crimeDto);	// ������ �����丮 ����
				//	writeCrimeRate(crimeDto);			// ������ ����
				//	writeCrimeCount(crimeDto);			// ���� �Ǽ� ����
				}
			}*/
			
			
			
			/* 2015�� ���� �����Ϳ��� 2014�� ���������͸� ���� */
			/*for(int i = 0; i < 12; i++){
				if(i > 8)
					crimeDto.setDate("2015" + (i + 1) + "00");	
				else
					crimeDto.setDate("20150" + (i + 1) + "00");	
				crimeDto.setCrimeRate(preYearCrimeRate[2][i]);
				crimeDto.setRegionCodeNum(regionCode.regionNum[regionCodeNumCount]);
				crimeDto.setCrimeCodeNum(crimeCodeNum[2]);
				crimeDto.setCrimeCount(preYearCrime[2][i]);
			//	writeHistoryCrimeRate(crimeDto);	// ������ �����丮 ����
			//	writeCrimeRate(crimeDto);			// ������ ����
			//	writeCrimeCount(crimeDto);			// ���� �Ǽ� ����
			}*/
			
			/* ���⵵(2014) ������ �� ���˰Ǽ� DB�� ���� */
			/*for(int i=0;i<5;i++){
				for(int j=0; j<12;j++){
					
					if(j>8)
						crimeDto.setDate("2014" + (j + 1) + "00");	
					else
						crimeDto.setDate("20140" + (j + 1) + "00");	
					crimeDto.setCrimeRate(preYearCrimeRate[i][j]);
					crimeDto.setRegionCodeNum(regionCode.regionNum[regionCodeNumCount]);
					crimeDto.setCrimeCodeNum(crimeCodeNum[i]);
					crimeDto.setCrimeCount(preYearCrime[i][j]);
				//	writeCrimeRate(crimeDto);			// 14�⵵ ���������� ����
				//	writeCrimeCount(crimeDto);			// 14�⵵ �������� �Ǽ� ����
				}
			}*/
			
			// �������� '���� ������' �ܼ�â�� ���
						/*for(int i =0 ; i< 5;i++){
							int month = 1;
							System.out.println(" ** ���˸� : " + crimeName[i] + " **");
							for(int j=0; j < presentWeather.length;j++){
								System.out.print("2015�� ");
								System.out.printf("%2d �� ", month++);
								System.out.print(regionCode.regionName[regionCodeNumCount] + " " + crimeName[i] + " ���� ���˰Ǽ� = ");
								System.out.print((int)predictionCrime[i][j]);
								System.out.printf(",  ���� ������ = %.2f %%", predictionCrimeRate[i][j]);
								System.out.printf(",  (������� R^2 = %.2f)\n" ,regressionR2[i][j]);
							}
							System.out.println();
						}*/
			regionCodeNumCount++;	
		}
		
		
	}
}