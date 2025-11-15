package com.lora.bi.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * excel 相关工具类 excel 转换为 csv
 */
@Slf4j
public class ExcelUtils {

    public static String excelToCsv(MultipartFile multipartFile) throws IOException {

//        File file = null;
//            // 读取数据
        List<Map<Integer, String>> list = null;
        try {

            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("excel 转换为 csv 失败", e);
            e.printStackTrace();
        }

        if (CollUtil.isEmpty(list)) {
            return "";
        }

        // 转换为csv
        StringBuilder stringBuilder = new StringBuilder();
        // 读取表头
        LinkedHashMap<Integer, String> integerStringMap = (LinkedHashMap) list.get(0);
        List<String> headerList = integerStringMap.values().stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
        stringBuilder.append(StrUtil.join(",", headerList)).append("\n");
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> map = (LinkedHashMap) list.get(i);
            List<String> valueList = map.values().stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
            stringBuilder.append(StrUtil.join(",", valueList)).append("\n");
        }

        return stringBuilder.toString();
    }
}
