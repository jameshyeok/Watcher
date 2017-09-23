package kr.co.codedraw.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import kr.co.codedraw.dao.StatisticsMapDao;
import kr.co.codedraw.dto.CrimeInfo;
import kr.co.codedraw.dto.CrimeViewDto;
import kr.co.codedraw.dto.StatisticsMapLog;
import kr.co.codedraw.dto.WeatherDto;
import kr.co.codedraw.service.StatisticsMapService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class StatisticsMapServiceImpl implements StatisticsMapService{

	
	@Autowired
	private StatisticsMapDao statisticsMapDao; 
	

	@Override
	public void makeStatisticsMap(String cDate) {
		//KML File Load
		String origKmlFilePath = "D:"+File.separator+"watcher"+File.separator+"kml"; //local
		//String origKmlFilePath =File.separator+"root"+File.separator+"watcher"+File.separator+"apache-tomcat-7.0.72"+File.separator+"webapps"+File.separator+"kml"; //server
		List<CrimeViewDto> crimeInfoList = statisticsMapDao.getCrimeRateofArea(cDate);
		File file = new File(origKmlFilePath+File.separator+"KOR_adm.kml");
		Kml kml = Kml.unmarshal(file);
		
		//Placemark ��ť��Ʈ ����
		Document document = (Document)kml.getFeature();
		List<Feature> featureList = document.getFeature();
		int idx= 0;
		for(Feature documentFeature : featureList) {
			if(documentFeature instanceof Placemark) {
				//�������� ���� StyleURL ����(��� ���� �� ����)
				Placemark placemark = (Placemark) documentFeature;
				int value = crimeInfoList.get(idx).getCrimeRate().intValue();
				if(0 <= value && 3 >= value) {
					placemark.withStyleUrl("#"+1);
				} else if (4 <= value && 7 >= value){
					placemark.withStyleUrl("#"+2);
				} else if (8 <= value && 11 >= value){
					placemark.withStyleUrl("#"+3);
				} else if (12 <= value && 15 >= value){
					placemark.withStyleUrl("#"+4);
				} else if (16 <= value && 19 >= value){
					placemark.withStyleUrl("#"+5);
				} else if (20 <= value && 23 >= value){
					placemark.withStyleUrl("#"+6);
				} else if (24 <= value && 27 >= value){
					placemark.withStyleUrl("#"+7);
				} else if (28 <= value && 31 >= value){
					placemark.withStyleUrl("#"+8);
				} else if (32 <= value && 35 >= value){
					placemark.withStyleUrl("#"+9);
				} else {
					placemark.withStyleUrl("#"+10);
				}
				
				//������ Description�� ������� ����
				JSONObject json = new JSONObject();
				json.put("cDate", crimeInfoList.get(idx).getcDate());
				json.put("crimeCodeNum", crimeInfoList.get(idx).getCrimeCodeNum());
				json.put("regionCode", crimeInfoList.get(idx).getRegionCode());
				json.put("regionName", crimeInfoList.get(idx).getRegionName());
				json.put("temperature", crimeInfoList.get(idx).getTemperature());
				json.put("humidity", crimeInfoList.get(idx).getHumidity());
				json.put("value", crimeInfoList.get(idx).getCrimeRate());
				placemark.withDescription(json.toString());
				idx++;
			}
		}
	 	 
	 	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	 	String kmlOutputName = "main_statistics_map_c"+cDate;
	 	File kmlOutput = new File(origKmlFilePath+File.separator+kmlOutputName);
		
		try {// KML File save and DB insert
			kmlOutput.createNewFile();
			kml.marshal(kmlOutput);
			StatisticsMapLog sml = new StatisticsMapLog();
			sml.setFileName(kmlOutputName);
			sml.setFilePath(origKmlFilePath+kmlOutputName);
			statisticsMapDao.insertStatisticsMapLog(sml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<List<CrimeViewDto>>  load5CrimeRateofArea(String selMonth) {
		String cDate = "2015"+selMonth+"00";
		List<CrimeViewDto> crimeInfoList = statisticsMapDao.load5CrimeRateofArea(cDate);
		List<List<CrimeViewDto>> list = new ArrayList<List<CrimeViewDto>>();
		for(CrimeViewDto dto : crimeInfoList) {
			if(dto.getCrimeRate() < 0) {
				dto.setCrimeRate(0d);
			}
		}
		list.add(crimeInfoList.subList(0, 16)); // 001
		list.add(crimeInfoList.subList(16, 32)); // 002
		list.add(crimeInfoList.subList(32, 48)); // 003
		list.add(crimeInfoList.subList(48, 64)); // 004
		list.add(crimeInfoList.subList(64, 80)); // 005
		return list;
	}

	@Override
	public ModelMap loadSimulKml(Double temperature,  Double humidity){
	
		List<CrimeViewDto> crimeInfoList = statisticsMapDao.loadAvgCrimeRateofAreaAllMonth2015();
		//2015�� �����͸� �� �����ͼ�
		// 1. ������ �ű⿡ ���� ����� temperature�� ���� ���˿������� �����´�.
		// 2. ������ �ű⿡ ���� ����� humidity�� ���� ���˿������� �����´�.
		// ������ 1�� 2�� ��� �� ���� ����Ʈ�� �����.
		
		//16�������� 12���� �����ͷ� �迭ȭ
		List<List<CrimeViewDto>> list = new ArrayList<List<CrimeViewDto>>();
		for(int i=0; i< crimeInfoList.size(); i= i+12){
			list.add(crimeInfoList.subList(i, i+12));
		}
		List<CrimeViewDto> resultTemperatureList = new ArrayList<CrimeViewDto>();
		List<CrimeViewDto> resultHumidityList = new ArrayList<CrimeViewDto>();
		//���� ����� ����� ���� ������ ����
		for(int i=0; i< list.size(); i++) {
			List<CrimeViewDto> tmpList = list.get(i);
			CrimeViewDto tmp = tmpList.get(0);
			for(int j =1; j < tmpList.size(); j++ ) {
				if(Math.abs(tmp.getTemperature() - temperature) > Math.abs(tmpList.get(j).getTemperature() - temperature)){
					tmp = tmpList.get(j);
				}
			}
			resultTemperatureList.add(tmp);
		}
		
		//���� ����� ������ ���� ������ ����
		for(int i=0; i< list.size(); i++) {
			List<CrimeViewDto> tmpList = list.get(i);
			CrimeViewDto tmp = tmpList.get(0);
			for(int j =1; j < tmpList.size(); j++ ) {
				if(Math.abs(tmp.getHumidity() - humidity) > Math.abs(tmpList.get(j).getHumidity() - humidity)){
					tmp = tmpList.get(j);
				}
			}
			resultHumidityList.add(tmp);
		}
		 //((��¹����� + ���� ���˷�)/2) �� ��� �������� set ���ش�.
		//������ �μ����� �κе� set ���ش�.
		List<CrimeViewDto> resultList = new ArrayList<CrimeViewDto>();
		for(int i =0; i< resultTemperatureList.size(); i++){
			CrimeViewDto tmp = new CrimeViewDto();
			tmp.setCrimeRate((Math.round(((resultTemperatureList.get(i).getCrimeRate() + resultHumidityList.get(i).getCrimeRate()) / 2)*100)) /100d);
			tmp.setcDate(resultTemperatureList.get(i).getcDate()); 
			tmp.setTemperature(resultTemperatureList.get(i).getTemperature());
			tmp.setHumidity(resultHumidityList.get(i).getHumidity());
			tmp.setRegionName(resultTemperatureList.get(i).getRegionName());
			tmp.setRegionCode(resultTemperatureList.get(i).getRegionCode());
			resultList.add(tmp);
		}
		
		//resultList �̰ɷ� KML ���� �����
		
		//KML File Load
		//String origKmlFilePath = "D:"+File.separator+"watcher"+File.separator+"kml"; //local
		String origKmlFilePath =File.separator+"root"+File.separator+"watcher"+File.separator+"apache-tomcat-7.0.72"+File.separator+"webapps"+File.separator+"kml"; //server
		File file = new File(origKmlFilePath+File.separator+"KOR_adm.kml");
		Kml kml = Kml.unmarshal(file);
		
		//Placemark ��ť��Ʈ ����
		Document document = (Document)kml.getFeature();
		List<Feature> featureList = document.getFeature();
		int idx= 0;
		for(Feature documentFeature : featureList) {
			if(documentFeature instanceof Placemark) {
				//�������� ���� StyleURL ����(��� ���� �� ����)
				Placemark placemark = (Placemark) documentFeature;
				int value = resultList.get(idx).getCrimeRate().intValue();
				if(0 <= value && 3 >= value) {
					placemark.withStyleUrl("#"+1);
				} else if (4 <= value && 7 >= value){
					placemark.withStyleUrl("#"+2);
				} else if (8 <= value && 11 >= value){
					placemark.withStyleUrl("#"+3);
				} else if (12 <= value && 15 >= value){
					placemark.withStyleUrl("#"+4);
				} else if (16 <= value && 19 >= value){
					placemark.withStyleUrl("#"+5);
				} else if (20 <= value && 23 >= value){
					placemark.withStyleUrl("#"+6);
				} else if (24 <= value && 27 >= value){
					placemark.withStyleUrl("#"+7);
				} else if (28 <= value && 31 >= value){
					placemark.withStyleUrl("#"+8);
				} else if (32 <= value && 35 >= value){
					placemark.withStyleUrl("#"+9);
				} else {
					placemark.withStyleUrl("#"+10);
				}
				
				//������ Description�� ������� ����
				JSONObject json = new JSONObject();
				json.put("regionCode", resultList.get(idx).getRegionCode());
				json.put("regionName", resultList.get(idx).getRegionName());
				json.put("value", resultList.get(idx).getCrimeRate());
				placemark.withDescription(json.toString());
				idx++;
			}
		}
		
	 	String kmlOutputName = "simul_statistics_map"+getRandomLotto();
	 	File kmlOutput = new File(origKmlFilePath+File.separator+kmlOutputName);
		
		try {// KML File save and DB insert
			kmlOutput.createNewFile();
			kml.marshal(kmlOutput);
			StatisticsMapLog sml = new StatisticsMapLog();
			sml.setFileName(kmlOutputName);
			sml.setFilePath(origKmlFilePath+kmlOutputName);
			statisticsMapDao.insertStatisticsMapLog(sml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// resultTemperatureList, resultHumidityList�� cDate�� regionCodeNum�� �̿��Ͽ� ������ �ش� ���� 5����� ������ �µ��� �̾ƿ´�.
		List<CrimeViewDto> resultInfoListTmp = new ArrayList<CrimeViewDto>();
		for(CrimeViewDto temp : resultTemperatureList){
			resultInfoListTmp.addAll(statisticsMapDao.load5CrimeRateofAreaForRegionCode(temp));	
		}
		
		List<CrimeViewDto> resultInfoListHum = new ArrayList<CrimeViewDto>();
		for(CrimeViewDto temp : resultHumidityList){
			resultInfoListHum.addAll(statisticsMapDao.load5CrimeRateofAreaForRegionCode(temp));	
		}
		
		//�� ���� ������ ����� ���ϰ� List�� ��´�.
		List<CrimeViewDto> resultInfoList = new ArrayList<CrimeViewDto>();
		for(int i=0; i <resultInfoListTmp.size(); i++ ){
			CrimeViewDto temp = new CrimeViewDto();
			temp.setCrimeCodeNum(resultInfoListTmp.get(i).getCrimeCodeNum());
			temp.setCrimeRate((Math.round(((resultInfoListTmp.get(i).getCrimeRate() + resultInfoListHum.get(i).getCrimeRate())/2) *100))/100d);
			temp.setRegionCode(resultInfoListTmp.get(i).getRegionCode());
			temp.setRegionName(resultInfoListTmp.get(i).getRegionName());
			resultInfoList.add(temp);
		}
		
		//���
		/*for(CrimeViewDto dto : resultInfoList){
			System.out.println("���� :" +dto.getRegionName() + "�����ڵ� : " +dto.getRegionCode()  + " ������ : " + dto.getCrimeRate());
		}
		*/
		List<List<CrimeViewDto>> listResult = new ArrayList<List<CrimeViewDto>>();
		listResult.add(resultInfoList.subList(0, 16)); // 001
		listResult.add(resultInfoList.subList(16, 32)); // 002
		listResult.add(resultInfoList.subList(32, 48)); // 003
		listResult.add(resultInfoList.subList(48, 64)); // 004
		listResult.add(resultInfoList.subList(64, 80)); // 005
		ModelMap  map = new ModelMap();
		map.put("kmlFileName", kmlOutputName);
		map.put("data", listResult);
		return map;	
	}
	
	public String getRandomLotto() {
		String result = "";
		for(int i=0; i <6; i++){
			result += ( (int)(Math.random()*45) +1);
		}
		return result;
	}
 
	@Override
	public List<CrimeViewDto> getCrimeRateofArea(String cDate) {
		List<CrimeViewDto> crimeInfoList = statisticsMapDao.getCrimeRateofArea(cDate);
		return crimeInfoList;
	}

	@Override
	public ModelMap loadCrimeRate5AndAvg(String cDate, String regionCode) {
		CrimeViewDto dto = new CrimeViewDto();
		dto.setcDate(cDate);
		dto.setRegionCode(regionCode);
		
		List<CrimeViewDto> list5Crime = statisticsMapDao.load5CrimeRateofAreaForRegionCode(dto);
		List<CrimeViewDto> listAvgCrime =  statisticsMapDao.loadAvgCrimeRateofAreaForRegionCode(dto);
		ModelMap map = new ModelMap();
		map.put("list5Crime", list5Crime);
		map.put("listAvgCrime", listAvgCrime);
		return map;
	}
}
