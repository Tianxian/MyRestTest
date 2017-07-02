package cn.demo.test.methods;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.jayway.restassured.response.Response;

import cn.demo.test.utils.DBConnnection;
import cn.demo.test.utils.DataReader;
import cn.demo.test.utils.DataWriter;
import cn.demo.test.utils.HTTPReqGen;
import cn.demo.test.utils.RecordHandler;

public class NewTest {
	protected static final Logger logger = LoggerFactory.getLogger(NewTest.class);
	private Response response;
	private DataReader myInputData;
	private String template;
	static int TC_count = 0;
	Date startTime = null;

	public String getTestName() {
		return "API Test";
	}

	String filePath = "";

	XSSFWorkbook wb = null;
	XSSFSheet inputSheet = null;
	XSSFSheet baselineSheet = null;
	XSSFSheet outputSheet = null;
	XSSFSheet comparsionSheet = null;
	XSSFSheet resultSheet = null;
	XSSFRow myRow = null;
	
	private List<String> headers = new ArrayList<String>();
	
	@BeforeTest
	@Parameters("workBook")
	public void setup(String path) {
		filePath = path;
		// debug System.out.println("filePath: " + filePath);
		try {
			//wb = new XSSFWorkbook(new FileInputStream(filePath));
			wb = new XSSFWorkbook(NewTest.class.getClassLoader().getResourceAsStream(filePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		inputSheet = wb.getSheet("input");
		
		try {
			 InputStream is =
			 NewTest.class.getClassLoader().getResourceAsStream("Config/http_request_template.txt");
			template = IOUtils.toString(is, Charset.defaultCharset());
		} catch (Exception e) {
			Assert.fail("Problem fetching data from input file:" + e.getMessage());

		}
		startTime = new Date();
		
		DBConnnection.execDelete("delete from BookingImp.dbo.CreditCard where CardNumber in ('11111111','11111112','11111113','11111114')");
	}

	@DataProvider(name = "WorkBookData")
	protected Iterator<Object[]>  testProvider(ITestContext context) {
		List<Object[]> test_IDs = new ArrayList<Object[]>();
		myInputData = new DataReader(inputSheet, true, true, 0);
		Map<String, RecordHandler> myInput = myInputData.get_map();

		// sort map in order so that test cases ran in a fixed order
		// Map<String, RecordHandler> sortmap = Utils.sortmap(myInput);

		for(int i=1;i<myInput.size();i++){
			RecordHandler rh = myInput.get(""+i);
			//System.out.println(i+": "+rh.get("TC_Description"));
			String test_ID = ""+i;
			String test_case = rh.get("TC_Description");
			test_IDs.add(new Object[] { test_ID, test_case });	
		}
		
		/*
		for (Map.Entry<String, RecordHandler> entry : myInput.entrySet()) {
			String test_ID = entry.getKey();
			String test_case = entry.getValue().get("TC_Description");
			if (!test_ID.equals("") && !test_case.equals("")) {
				System.out.println("test_ID: "+test_ID+" ,test_case: "+test_case);
				test_IDs.add(new Object[] { test_ID, test_case });
			}
		}
		Iterator<Object[]> it = test_IDs.iterator();
		while(it.hasNext()){
			Object[] temp = it.next();
			System.out.println(Arrays.toString(temp));
		}*/
		return test_IDs.iterator();
	}
	
	@Test
	public void db_test(){
		
		System.out.println(DBConnnection.columnResult("select CardNumber from BookingImp.dbo.CreditCard"));
	}
	
	@Test
	public void jsonReader_test(){
		
		System.out.println(HTTPReqGen.getJsonString("1.txt"));
	}
	
	@Test
	public void write_cardID_to_Excel(){
		String valueToWrite = DBConnnection.columnResult("select cardID from BookingImp.dbo.CreditCard");
		System.out.println("valueToWrite: "+valueToWrite);
		DataWriter.writeCardIDToCell(inputSheet, "2", "post a single NEW creditcard to DB", valueToWrite);
	}
	
	@Test
	public void appendURL_test(){
		DataWriter.appendURL(inputSheet, "8", "update a credit card,partially,by cardID, only varchar fields");
		DataWriter.appendURL(inputSheet, "9", "update a credit card,partially,by cardID, only varchar fields");
		DataWriter.appendURL(inputSheet, "10", "update a credit card,partially,by cardID, only varchar fields");
		DataWriter.appendURL(inputSheet, "11", "update a credit card,partially,by cardID, only varchar fields");
	}
	
	@Test(dataProvider = "WorkBookData", description = "ReqGenTest")
	public void api_test(String ID, String test_case) {
		
		TC_count++;//count total test cases run
		logger.info("**************************TC_"+TC_count+" is running*************************************");
		String URL_To_Append = myInputData.get_record(ID).get("URL_To_Append");
		logger.info("curent URL_To_Append is: "+URL_To_Append);
		if(URL_To_Append.contains("cardID=")&&!URL_To_Append.endsWith("cardID=0")){
			DataWriter.appendURL(inputSheet,ID, test_case);
		}
		
		HTTPReqGen myReqGen = new HTTPReqGen();	
		try {
			myReqGen.generate_request(template, myInputData.get_record(ID));//generate a request by processing Excel sheet and template file
			response = myReqGen.perform_request();//perform the generated request and get response
			logger.info("response.asString(): "+response.asString());
		} catch (Exception e) {
			Assert.fail("Problem using HTTPRequestGenerator to generate response: " + e.getMessage());
		}

		  String expectedMessage_Excel = myInputData.get_record(ID).get("Expected_Response");//expected response message in Excel sheet
		  Assert.assertTrue(response.asString().contains(expectedMessage_Excel));//see if response has the message we expected
		  //Assert.assertTrue(response.statusCode() == 201);	  
		  String querySQL = myInputData.get_record(ID).get("SQL");//get the SQL query from Excel sheet "SQL" column
		  logger.info("querySQL: "+querySQL);
		  if(querySQL!=null){
			  String expectedValue_DB = myInputData.get_record(ID).get("Expected_Value_DB");//get Expected_Value_DB from Excel sheet "Expected_Value_DB" column
			  logger.info("expectedValue_DB: "+expectedValue_DB);
			  String actualValue_DB = DBConnnection.columnResult(querySQL).replace("\n", "");
			  logger.info("actualValue_DB: "+actualValue_DB);
			  Assert.assertTrue(actualValue_DB.equals(expectedValue_DB));
		  }
	      
	      if(TC_count<3){
			String 	writeSQLForCardIDs =   myInputData.get_record(ID).get("GetCardID_SQL");
			logger.info("writeSQLForCardIDs: "+writeSQLForCardIDs);
			String  CardIDValueToWrite = DBConnnection.columnResult(writeSQLForCardIDs);
			logger.info("CardIDValueToWrite: "+CardIDValueToWrite);
			DataWriter.writeCardIDToCell(inputSheet, ID, test_case, CardIDValueToWrite);
		 }
	      
	      logger.info("**************************TC_"+TC_count+" completed*************************************");
	}

		  @AfterTest
		  @Parameters("workBook")
		  public void teardown(String path) {
			  String relativelyPath=System.getProperty("user.dir");
			  String absolutePath = relativelyPath+"\\src\\main\\resources\\"+path.replace('/', '\\');
			  
			  try {
				 // System.out.println(this.getClass().getResource("path").getPath());
				  //FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
		            FileOutputStream fileOutputStream = new FileOutputStream(new File(absolutePath));
		            wb.write(fileOutputStream);
		            fileOutputStream.close();
		        } catch (FileNotFoundException e) {
		            e.printStackTrace();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		  }
	

}
