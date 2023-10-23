package com.example.module.excel;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：Excel处理工具类
 * 作者：PadoPhyllis
 * 日期：2023.10.9
 */
public class ExcelUtils {
    //日志输出
    private static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * 根据行数和列数读取Excel
     * @param file 前端发送的文件
     * @param StatrRow 读取的开始行数（默认填0）
     * @param EndRow 读取的结束行数（填-1为全部）
     * @param ExistTop 是否存在头部（如存在则读取数据时会把头部拼接到对应数据，若无则为当前列数）
     * @return 返回一个List<Map<Integer,Object>>
     */
    public static List<Map<Integer, Object>> readExcel(MultipartFile file, int StatrRow, int EndRow, boolean ExistTop){
        //判断输入的开始值是否少于等于结束值
        if (StatrRow > EndRow && EndRow != -1) {
            logger.warn("输入的开始行值比结束行值大，请重新输入正确的行数");
            return null;
        }
        //声明返回的结果集
        List<Map<Integer, Object>> result = new ArrayList<>();
        //声明一个工作薄
        Workbook workbook = null;
        //声明一个文件输入流
        InputStream is = null;

        String fileName = null;

        try {
            fileName = file.getOriginalFilename();

            // 获取Excel后缀名，判断文件类型
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);

            // 获取Excel工作簿
            workbook = getWorkbook(file.getInputStream(), fileType);
            //处理Excel内容
            result = getListData(workbook, StatrRow, EndRow, ExistTop);
        } catch (Exception e) {
            logger.warn("解析Excel失败，文件名：" + fileName + " 错误信息：" + e.getMessage());
        } finally {
            try {
                if (null != workbook) {
                    workbook.close();
                }
                if (null != is) {
                    is.close();
                }
            } catch (Exception e) {
                logger.warn("关闭数据流出错！错误信息：" + e.getMessage());
                return null;
            }
        }
        return result;
    }

    /**
     * 导出excel表格
     * @param sheetName sheet名字
     * @param title 标题
     * @param values 数据
     * @param wb HSSFWorkbook对象
     * @return 返回一个HssFWorkbook
     */
    public static HSSFWorkbook getHSSFWorkbook(String sheetName, String []title, String [][]values, HSSFWorkbook wb){
        //创建一个HSSFWorkbook，对应一个Excel文件
        if(wb == null){
            wb = new HSSFWorkbook();
        }

        //在workbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet(sheetName);

        //在sheet中添加表头第0行
        HSSFRow row = sheet.createRow(0);

        //设置样式
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER); //创建一个居中格式
        style.setWrapText(true);//自动化换行

        //声明列对象
        HSSFCell cell = null;

        //存储最大列宽
        Map<Integer,Integer> maxWidth = new HashMap<>();

        //创建标题
        for(int i=0;i<title.length;i++){
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
            maxWidth.put(i,cell.getStringCellValue().getBytes().length  * 256 + 200);
        }

        //创建内容
        for(int i=0;i<values.length;i++){
            row = sheet.createRow(i + 1);
            for(int j=0;j<values[i].length;j++) {
                //将内容按顺序赋给对应的列对象
                cell = row.createCell(j);
                cell.setCellValue(values[i][j]);
                cell.setCellStyle(style);
                if (maxWidth.get(j)<cell.getStringCellValue().getBytes().length * 256 + 200) {
                    maxWidth.put(j,cell.getStringCellValue().getBytes().length  * 256 + 200);
                }
            }
        }

        //列宽自适应
        for (int i = 0; i < title.length; i++) {
            sheet.setColumnWidth(i, maxWidth.get(i));
        }

        return wb;
    }


    //Workbook初始化
    private static Workbook getWorkbook(InputStream inputStream, String fileType) throws IOException {
        //后缀判断有版本转换问题
        Workbook workbook = null;
        if (fileType.equalsIgnoreCase("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (fileType.equalsIgnoreCase("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        }
        return workbook;
    }

    //根据读取的单元格类型，对数据预处理
    private static String cellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
            case NUMERIC:   //数字
                Double doubleValue = cell.getNumericCellValue();
                // 格式化科学计数法，取一位整数，如取小数，值如0.0,取小数点后几位就写几个0
                DecimalFormat df = new DecimalFormat("0");
                returnValue = df.format(doubleValue);
                break;
            case STRING:    //字符串
                returnValue = cell.getStringCellValue();
                break;
            case BOOLEAN:   //布尔
                Boolean booleanValue = cell.getBooleanCellValue();
                returnValue = booleanValue.toString();
                break;
            case BLANK:     // 空值
                break;
            case FORMULA:   // 公式
                returnValue = cell.getCellFormula();
                break;
            case ERROR:     // 故障
                break;
            default:
                break;
        }
        return returnValue;
    }

    //处理Excel内容转为List<Map<Integer,Object>>输出
    private static List<Map<Integer, Object>> getListData(Workbook workbook, int StatrRow, int EndRow, boolean ExistTop){
        //声明返回结果集result
        List<Map<Integer, Object>> result = new ArrayList<>();
        //声明一个Excel头部函数
        ArrayList<String> top = new ArrayList<>();

        //解析sheet
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            Sheet sheet = workbook.getSheetAt(sheetNum);
            // 校验sheet是否合法
            if (sheet == null) {
                continue;
            }
            //如存在头部，处理头部数据
            if (ExistTop) {
                int firstRowNum = sheet.getFirstRowNum();
                Row firstRow = sheet.getRow(firstRowNum);
                if (null == firstRow) {
                    logger.warn("解析Excel失败，在第一行没有读取到任何数据！");
                }
                for (int i = 0; i < firstRow.getLastCellNum(); i++) {
                    top.add(cellValueToString(firstRow.getCell(i)));
                }
            }
            //处理Excel数据内容
            int endRowNum;
            //获取结束行数
            if (EndRow == -1) {
                endRowNum = sheet.getPhysicalNumberOfRows();
            } else {
                endRowNum = EndRow <= sheet.getPhysicalNumberOfRows() ? EndRow : sheet.getPhysicalNumberOfRows();
            }
            //遍历行数
            for (int i = StatrRow - 1; i < endRowNum; i++) {
                Row row = sheet.getRow(i);
                if (null == row) {
                    continue;
                }
                Map<Integer, Object> map = new HashMap<>();
                //获取所有列数据
                for (int y = 0; y < row.getLastCellNum(); y++) {
                    if (top.size() > 0) {
                        if (top.size() >= y) {
                            map.put(Integer.parseInt(top.get(y)), cellValueToString(row.getCell(y)));
                        } else {
                            map.put(y+1, cellValueToString(row.getCell(y)));
                        }
                    } else {
                        map.put(y + 1, cellValueToString(row.getCell(y)));
                    }
                }
                result.add(map);
            }
        }
        return result;
    }
}
