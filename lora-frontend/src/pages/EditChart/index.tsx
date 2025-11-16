import { getChartByIdUsingGet, updateChartUsingPost } from '@/services/lora-bi/chartController';
import { useModel } from '@@/exports';
import { Button, Card, Col, Form, Input, message, Row, Select, Space, Upload } from 'antd';
import TextArea from 'antd/es/input/TextArea';
import React, { useEffect, useState } from 'react';
import { history, useParams } from '@umijs/max';
import ReactECharts from 'echarts-for-react';
import { UploadOutlined } from '@ant-design/icons';

/**
 * 编辑图表页面
 * @constructor
 */
const EditChartPage: React.FC = () => {
  const [form] = Form.useForm();
  const { id } = useParams<{ id: string }>();
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const [chart, setChart] = useState<API.Chart>();
  const [options, setOptions] = useState<any>({});
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [previewOptions, setPreviewOptions] = useState<any>({});
  const [previewData, setPreviewData] = useState<string>('');

  const loadData = async () => {
    if (!id) {
      message.error('参数错误');
      history.push('/my-chart');
      return;
    }
    
    setLoading(true);
    try {
      const res = await getChartByIdUsingGet({ id: parseInt(id) });
      if (res.data) {
        setChart(res.data);
        form.setFieldsValue({
          name: res.data.name,
          goal: res.data.goal,
          chartType: res.data.chartType,
          chartData: res.data.chartData,
        });
        setPreviewData(res.data.chartData || '');
        
        // 解析图表选项
        if (res.data.genChart) {
          try {
            // 如果 chartOption 是包含 JS 代码的字符串，需要提取其中的配置对象
            let chartOptionStr = res.data.genChart || "{}";

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
            setPreviewOptions(parsedOptions);
          } catch (e) {
            message.error('图表配置解析失败');
            console.error("图表配置解析失败:", e);
            setOptions({});
            setPreviewOptions({});
          }
        }
      } else {
        message.error('获取图表失败');
      }
    } catch (e: any) {
      message.error('获取图表失败，' + e.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [id]);

  /**
   * 提交
   * @param values
   */
  const onFinish = async (values: any) => {
    if (!id) {
      message.error('参数错误');
      return;
    }
      
    if (submitting) return;
    setSubmitting(true);
    
    try {
      const res = await updateChartUsingPost({
        id: parseInt(id),
        name: values.name,
        goal: values.goal,
        chartType: values.chartType,
        chartData: values.chartData,
      });
      
      if (res.data) {
        message.success('更新成功');
        history.push('/my-chart');
      } else {
        message.error('更新失败');
      }
    } catch (e: any) {
      message.error('更新失败: ' + e.message);
    }
    setSubmitting(false);
  };

  /**
   * 预览图表
   * @param values
   */
  const onPreview = async (values: any) => {
    // 这里可以调用后端接口重新生成图表
    // 为了简化，我们只是更新预览数据
    setPreviewData(values.chartData);
    message.success('预览数据已更新');
  };

  return (
    <div className="edit-chart-page" style={{ padding: '24px' }}>
      <div style={{ background: '#fff', padding: '24px', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
        <Row gutter={24}>
          <Col span={12}>
            <Card 
              title={
                <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#1890ff' }}>
                  编辑图表
                </div>
              }
              style={{ height: '100%' }}
            >
              <Form 
                form={form}
                name="editChart" 
                onFinish={onFinish} 
                initialValues={{}} 
                labelAlign="left" 
                labelCol={{ span: 6 }}
                disabled={loading}
              >
                <Form.Item
                  name="name"
                  label={
                    <span style={{ fontWeight: '500' }}>图表名称</span>
                  }
                  rules={[{ required: true, message: '请输入图表名称' }]}
                >
                  <Input placeholder="请输入图表名称" style={{ borderRadius: '6px' }} />
                </Form.Item>
                
                <Form.Item
                  name="goal"
                  label={
                    <span style={{ fontWeight: '500' }}>分析目标</span>
                  }
                  rules={[{ required: true, message: '请输入分析目标' }]}
                >
                  <TextArea 
                    placeholder="请输入你的分析需求" 
                    autoSize={{ minRows: 3, maxRows: 6 }} 
                    style={{ borderRadius: '6px' }}
                  />
                </Form.Item>
                
                <Form.Item 
                  name="chartType" 
                  label={
                    <span style={{ fontWeight: '500' }}>图表类型</span>
                  }
                >
                  <Select
                    placeholder="请选择图表类型"
                    style={{ borderRadius: '6px' }}
                    options={[
                      { value: '折线图', label: '折线图' },
                      { value: '柱状图', label: '柱状图' },
                      { value: '堆叠图', label: '堆叠图' },
                      { value: '饼图', label: '饼图' },
                      { value: '雷达图', label: '雷达图' },
                      { value: 'K 线图', label: 'K 线图' },
                      { value: '仪表盘', label: '仪表盘' },
                      { value: 'Funnel 图', label: 'Funnel 图' },
                      { value: '旭日图', label: '旭日图' },
                      { value: '箱型图', label: '箱型图' },
                      { value: '力导图', label: '力导图' },
                      { value: '关系图', label: '关系图' },
                      { value: '树图', label: '树图' },
                      { value: '地图', label: '地图' },
                      { value: '地理图', label: '地理图' },
                      { value: '热力图', label: '热力图' },
                    ]}
                  />
                </Form.Item>
                
                <Form.Item
                  name="chartData"
                  label={
                    <span style={{ fontWeight: '500' }}>原始数据</span>
                  }
                  rules={[{ required: true, message: '请输入原始数据' }]}
                >
                  <TextArea 
                    placeholder="请输入原始数据" 
                    autoSize={{ minRows: 4, maxRows: 10 }} 
                    style={{ borderRadius: '6px', fontFamily: 'monospace' }}
                  />
                </Form.Item>

                <Form.Item wrapperCol={{ span: 24 }}>
                  <Space size="middle">
                    <Button 
                      type="primary" 
                      htmlType="submit" 
                      loading={submitting} 
                      disabled={submitting}
                      style={{ borderRadius: '6px', minWidth: '80px' }}
                    >
                      更新
                    </Button>
                    <Button 
                      onClick={() => form.validateFields().then(onPreview).catch(() => {})}
                      style={{ borderRadius: '6px', minWidth: '80px' }}
                    >
                      预览
                    </Button>
                    <Button 
                      onClick={() => history.push('/my-chart')}
                      style={{ borderRadius: '6px', minWidth: '80px' }}
                    >
                      返回
                    </Button>
                  </Space>
                </Form.Item>
              </Form>
            </Card>
          </Col>
          
          <Col span={12}>
            <Card 
              title={
                <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#52c41a' }}>
                  图表预览
                </div>
              }
              style={{ height: '100%' }}
            >
              <div style={{ height: '400px', marginBottom: '20px' }}>
                {previewOptions && Object.keys(previewOptions).length > 0 ? (
                  <ReactECharts option={previewOptions} style={{ height: '100%', width: '100%' }} />
                ) : (
                  <div 
                    style={{ 
                      display: 'flex', 
                      justifyContent: 'center', 
                      alignItems: 'center', 
                      height: '100%', 
                      backgroundColor: '#f9f9f9', 
                      borderRadius: '4px',
                      color: '#bfbfbf'
                    }}
                  >
                    暂无图表数据
                  </div>
                )}
              </div>
              <div style={{ marginTop: 16 }}>
                <h4 style={{ 
                  color: '#535353', 
                  marginBottom: '8px', 
                  fontWeight: '600',
                  display: 'flex',
                  alignItems: 'center'
                }}>
                  <span style={{ 
                    display: 'inline-block', 
                    width: '4px', 
                    height: '16px', 
                    backgroundColor: '#1890ff', 
                    borderRadius: '2px',
                    marginRight: '8px'
                  }}></span>
                  原始数据预览
                </h4>
                <div style={{ 
                  backgroundColor: '#f9f9f9', 
                  padding: '12px', 
                  borderRadius: '6px',
                  maxHeight: '200px',
                  overflow: 'auto',
                  border: '1px solid #e8e8e8',
                  fontFamily: 'monospace',
                  fontSize: '12px',
                  lineHeight: '1.5'
                }}>
                  <pre style={{ 
                    margin: 0, 
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-all',
                    color: '#666'
                  }}>
                    {previewData || '暂无数据'}
                  </pre>
                </div>
              </div>
            </Card>
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default EditChartPage;