package com.example.module.excel;

import com.example.common.Code;
import com.example.common.Result;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * 功能: 处理excel请求
 * 作者: PadoPhyllis
 * 日期: 2023.10.11
 */
@RestController
@RequestMapping("/api/excel")
public class ExcelController {
    //日志输出
    private static Logger logger = LoggerFactory.getLogger(ExcelController.class);

    @Autowired
    private ExcelService excelService;

    /**
     * 导入应收商户excel表
     * @param file 上传文件
     * @return Result
     */
    @PostMapping("setReceivableExcel")
    public Result receivedImport(@RequestParam("files[]") MultipartFile file){

        List<Map<Integer, Object>> list = ExcelUtils.readExcel(file,0,-1,false);

        for (Map<Integer, Object> map : list) {
            map.forEach((key,value)->{
                System.out.println(key);
                System.out.println(value);
            });
        }

        return new Result(Code.SUCCESS,null,"ok");
    }

    /**
     * 导出应收商户excel表
     * @param response
     * @return Result
     */
    @PostMapping("getReceivableExcel")
    public Result receivableOut(HttpServletResponse response){

        HSSFWorkbook wb = excelService.getReceivableExcel("sheet1");

        try {
            response.setContentType("application/vnd.ms-excel;charset=utf-8");

            OutputStream os = response.getOutputStream();
            wb.write(os);

            os.flush();
            os.close();

        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage() + "(excel处理异常)");
            return new Result(Code.EXCELERROR,null,"应收商户导出失败！");
        } catch (IOException e) {
            logger.error(e.getMessage() + "(io处理异常)");
            return new Result(Code.EXCELERROR,null,"应收商户导出失败！");
        }

        return null;
    }
}
