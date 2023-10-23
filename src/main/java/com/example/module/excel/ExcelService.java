package com.example.module.excel;

import com.example.dao.UserDao;
import com.example.pojo.User;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExcelService {
    @Autowired
    private UserDao userDao;

    /**
     * @param sheetName sheet名字
     * @return HSSFWorkbook
     */
    public HSSFWorkbook getReceivableExcel(String sheetName){
        //获取数据
        List<User> list = userDao.selectAll();

        //excel标题
        String[] title = {"用户ID", "用户名称", "用户密码", "用户性别","用户地址"};

        String [][] content = new String[list.size()][5];

        for (int i = 0; i < list.size(); i++) {
            content[i] = new String[title.length];
            User user = list.get(i);
            content[i][0] = String.valueOf(user.getId());
            content[i][1] = user.getUsername();
            content[i][2] = user.getPassword();
            content[i][3] = user.getGender();
            content[i][4] = user.getAddr();
        }

        //创建HSSFWorkbook
        HSSFWorkbook wb = ExcelUtils.getHSSFWorkbook(sheetName, title, content, null);

        return wb;
    }
}