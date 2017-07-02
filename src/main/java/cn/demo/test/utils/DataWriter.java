package cn.demo.test.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataWriter {
	protected static final Logger logger = LoggerFactory.getLogger(DataWriter.class);
	// static int generated_CardId_ColumnID = 0;
	static int count = 0;
	
	public static void writeCardIDToCell(XSSFSheet sheet, String ID, String test_case, String valueToWrite) {
		int rowID = Integer.parseInt(ID);
		XSSFRow myRow = sheet.getRow(rowID);
		logger.info("rowID: " + rowID+ ", ColumnID: " + getSpecifiedColumn(sheet,"Generated_CardId")+ ", valueToWrite: "+valueToWrite);
		XSSFCell generated_CardId_Cell = myRow.getCell(getSpecifiedColumn(sheet,"Generated_CardId"));
		generated_CardId_Cell.setCellValue(valueToWrite);
	}

	public static int getSpecifiedColumn(XSSFSheet sheet,String columnName) {
		XSSFRow firstRow = sheet.getRow(0);
		int generated_CardId_ColumnID = 0;
		for (int colunmNum = 0; colunmNum < firstRow.getLastCellNum(); colunmNum++) {
			XSSFCell c = firstRow.getCell(colunmNum);
			if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK) {
				// Can't be this cell - it's empty
				continue;
			}
			if (c.getCellType() == Cell.CELL_TYPE_STRING) {
				String text = c.getStringCellValue();
				// System.out.println("c.getStringCellValue(): "+text);
				if (columnName.equals(text)) {
					generated_CardId_ColumnID = colunmNum;
					break;
				}
			}
		}
		if (generated_CardId_ColumnID == 0) {
			try {
				logger.error("None of the cells in the first row were"+columnName);
				throw new Exception("None of the cells in the first row were "+columnName);			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return generated_CardId_ColumnID;
	}

	public static void appendURL(XSSFSheet sheet,String ID, String test_case){
		 XSSFRow row1 = sheet.getRow(1);
		 XSSFRow row2 = sheet.getRow(2);
		 int currentRowID = Integer.parseInt(ID);
		 XSSFRow currentRow = sheet.getRow(currentRowID);
		 int generatedCardIDColumn = getSpecifiedColumn(sheet,"Generated_CardId");
		 String row1CardId_raw = row1.getCell(generatedCardIDColumn).toString();
		 logger.info("row1CardId_raw: "+row1CardId_raw);
		 String row1CardId_final = row1CardId_raw+",\n";
		 logger.info("row1CardId_final: "+row1CardId_final);
		 String row2CardId = row2.getCell(generatedCardIDColumn).toString(); 
		 String cardIDs = row1CardId_final + row2CardId;
		 String[] cardIDArray = cardIDs.split(",\\n");
		 //System.out.println(Arrays.toString(cardIDArray));
		 
		 int requestTypeColumn = getSpecifiedColumn(sheet,"Request_Method");
		 String requestTypeColumnValue = currentRow.getCell(requestTypeColumn).toString();
		 
		 String URLcolumnValue = "/rest.booking/creditcards/cardID?cardID=";
		 
		 if(!requestTypeColumnValue.equals("POST")){
			 URLcolumnValue = URLcolumnValue+cardIDArray[0];
			 logger.info("POST method, appended '"+cardIDArray[0]+"' , now the URL is:  "+URLcolumnValue);
		 }else{
			 URLcolumnValue = URLcolumnValue+cardIDArray[count];
			 logger.info("non POST method, appended '"+cardIDArray[count]+"' , now the URL is:  "+URLcolumnValue);
			 count++;
			 if(count>3){
				 count = 0;
			 }
		 }		 
		 
		 XSSFCell generated_CardId_Cell = currentRow.getCell(getSpecifiedColumn(sheet,"URL_To_Append"));
		 generated_CardId_Cell.setCellValue(URLcolumnValue);
	 }
}
