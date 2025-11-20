import {Button, Card, Form, Input, message, Select, Space, Upload} from 'antd';
import React, {useState} from 'react';
import {genChartByAiAsyncMqUsingPost, genChartByAiAsyncUsingPost} from "@/services/lora-bi/chartController";
import TextArea from "antd/es/input/TextArea";
import {UploadOutlined} from "@ant-design/icons";
import {useForm} from "antd/es/form/Form";


/**
 * 添加图表页面 (异步)
 * @constructor
 */
const AddChartAsync: React.FC = () => {
  const [form] = useForm();
  const [chart, setChart] = useState<API.BiGenVO>();
  const [options, setOptions] = useState<any>({});
  const [submitting, setSubmitting] = useState<boolean>(false);
  /**
   * 提交
   * @param values
   */
  const onFinish = async (values: any) => {

    if (submitting)
      return;
    setSubmitting(true);
    setChart(undefined);
    setOptions(undefined);

    // 立即显示提交成功消息，提升用户体验
    message.success("分析任务已提交，正在后台处理中...");

    // 对接后端，上传数据
    try {
      // 从表单值中获取文件对象
      const file = values.file?.fileList?.[0]?.originFileObj || values.file?.[0]?.originFileObj;
      const res = await genChartByAiAsyncMqUsingPost({
        goal: values.goal,
        name: values.name,
        chartType: values.chartType,
      }, {}, file);
      if (!res?.data) {
        message.error("生成失败: 服务器返回空数据");
        return;
      } else {
        // 任务成功提交到后端
        message.success("分析任务提交成功,稍后请在我的图表页面查看");
        form.resetFields();
        // 重置图表状态，确保不会显示之前的图表
        setChart(undefined);
        setOptions({});

      }
    } catch (error: any) {
      message.error("生成失败: " + error.message);
    }
    setSubmitting(false)
  };

  return (
    <div className="add-chart-async">
      <Card title="智能分析">
        <Form name="addChart" form={form} onFinish={onFinish} initialValues={{}} labelAlign="left" labelCol={{span: 4}}>
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
          <Form.Item name="file" label="原始数据" rules={[{required: true, message: '请输入分析文件'}]}>
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

    </div>
  )
    ;
};
export default AddChartAsync;
