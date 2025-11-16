package com.lora.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lora.bi.model.entity.Chart;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

/**
* @author yanBingZhao
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2025-11-13 21:24:21
* @Entity generator.domain.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {


    List<Map<String,Object>> queryChartData(String querySql);
}




