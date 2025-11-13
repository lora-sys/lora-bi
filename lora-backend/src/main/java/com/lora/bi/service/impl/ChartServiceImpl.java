package com.lora.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lora.bi.service.ChartService;
import com.lora.bi.model.entity.Chart;
import com.lora.bi.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author yanBingZhao
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2025-11-13 21:24:21
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




