import {UploadOutlined} from '@ant-design/icons';

import {Button, Card, Col, Divider, Form, Input, message, Row, Select, Space, Spin, Upload} from 'antd';

import TextArea from 'antd/es/input/TextArea';

import React, {useState} from 'react';

import {genChartByAiUsingPost} from "@/services/lora-bi/chartController";

import {useModel} from "@@/exports";

import ReactECharts from 'echarts-for-react';



/**

 * 添加图表页面

 * @constructor

 */

const AddChart: React.FC = () => {
  const {initialState} = useModel('@@initialState');
  const { currentUser, userScore } = initialState ?? {};
  const isAdmin = currentUser && currentUser.userRole === 'admin';
  const [chart, setChart] = useState<API.BiGenVO>();
  const [options, setOptions] = useState<any>({});
  const [submitting, setSubmitting] = useState<boolean>(false);
  /**
   * 提交
   * @param values
   */

  const onFinish = async (values: any) => {
    // 检查是否为管理员，如果不是管理员则检查积分
    if (!isAdmin) {
      if (userScore === undefined || userScore < 10) {
        message.error("积分不足，每次生成需要消耗10积分，请先充值");
        return;
      }
    }
    if (submitting)
      return;
    setSubmitting(true);
    setChart(undefined);
    setOptions(undefined);
    // 对接后端，上传数据
    try {
      // 从表单值中获取文件对象
      const file = values.file?.fileList?.[0]?.originFileObj || values.file?.[0]?.originFileObj;
      const res = await genChartByAiUsingPost({
        goal: values.goal,
        name: values.name,
        chartType: values.chartType,
      }, {}, file);
      if (!res?.data) {
        message.error("生成失败: 服务器返回空数据");
        return;
      } else {
        message.success("生成成功");
        setChart(res?.data);
        // 解析图表选项
        try {
          // 如果 chartOption 是包含 JS 代码的字符串，需要提取其中的配置对象
          let chartOptionStr = res.data?.chartOption || "{}";
          // 如果是包含 option = {...} 的字符串，提取其中的 JSON 对象
          if (chartOptionStr.includes("option = {")) {
            const match = chartOptionStr.match(/option = ({[\s\S]*});/);
            if (match && match[1]) {
            chartOptionStr = match[1];
            }
          }
          // ECharts 配置本身就是 JavaScript 对象格式，直接用 Function 解析
          let parsedOptions;
          try {
            // 移除开头的 "option =" 和结尾的分号
            let cleanStr = chartOptionStr
              .replace(/^option\s*=\s*/, '')
              .replace(/;\s*$/, '');
            // 用 Function 构造器解析 JavaScript 对象
            const func = new Function(`return ${cleanStr}`);
            parsedOptions = func();
          } catch (e) {
            console.error("Function解析失败，尝试JSON解析:", e);
            // 如果 Function 解析失败，回退到 JSON 解析
            parsedOptions = JSON.parse(chartOptionStr);
          }
          setOptions(parsedOptions);
        } catch (e) {
          message.error("图表配置解析失败");
          console.error("图表配置解析失败:", e);
          //即使解析失败，也设置空对象以避免传递字符串给 ECharts
          setOptions({});
        }
      }
    } catch (error: any) {
      message.error("生成失败: " + error.message);
    }
    setSubmitting(false)
  };

  return (
    <div className="add-chart">
      <Row gutter={24}>
        <Col span={12}>
          <Card title="智能分析">
            <Form name="addChart" onFinish={onFinish} initialValues={{}} labelAlign="left" labelCol={{span: 4}}>
              <Form.Item
                name="goal"
                label="分析目标"
                rules={[{required: true, message: '请输入分析目标'}]}
              >
                <TextArea placeholder="请输入你的分析需求，比如：分析网站用户的增长情况"/>
              </Form.Item>
              <Form.Item name="name" label="图表名称">
                <Input placeholder="请输入图表名称"/>
              </Form.Item>
              <Form.Item name="chartType" label="图表类型">
                <Select
                  options={[
                    {value: '折线图', label: '折线图'},
                    {value: '柱状图', label: '柱状图'},
                    {value: '堆叠图', label: '堆叠图'},
                    {value: '饼图', label: '饼图'},
                    {value: '雷达图', label: '雷达图'},
                    {value: 'K 线图', label: 'K 线图'},
                    {value: '仪表盘', label: '仪表盘'},
                    {value: 'Funnel 图', label: 'Funnel 图'},
                    {value: '旭日图', label: '旭日图'},
                    {value: '箱型图', label: '箱型图'},
                    {value: '力导图', label: '力导图'},
                    {value: '关系图', label: '关系图'},
                    {value: '树图', label: '树图'},
                    {value: '地图', label: '地图'},
                    {value: '地理图', label: '地理图'},
                    {value: '热力图', label: '热力图'},
                  ]}
                />
              </Form.Item>
              <Form.Item name="file" label="原始数据" valuePropName="fileList" getValueFromEvent={(e) => e?.fileList}>
                <Upload name="file" maxCount={1} listType="text">
                  <Button icon={<UploadOutlined/>}>上传 CSV 文件</Button>
                </Upload>
              </Form.Item>

              <Form.Item wrapperCol={{span: 16, offset: 4}}>
                <Space>
                  <Button type="primary" htmlType="submit" loading={submitting} disabled={submitting}>
                    提交
                  </Button>
                  <Button htmlType="reset" onClick={() => (setChart(undefined), setOptions({}))}>重置</Button>
                </Space>
              </Form.Item>

            </Form>
          </Card>

        </Col>
        <Col span={12}>
          <Card title="分析结果">

            {chart?.conclusion ?? <div>请现在左边点击生成提交</div>}
            {<Spin spinning={submitting}/>}
          </Card>
          <Divider/>
          <Card title="图表">

            {options && <ReactECharts option={options}/> || <div>请现在左边点击生成提交</div>}
            {<Spin spinning={submitting}/>}
          </Card>

        </Col>
      </Row>

    </div>
  )
    ;
};
export default AddChart;
