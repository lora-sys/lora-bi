import { listChartByPageUsingPost, deleteChartUsingPost } from '@/services/lora-bi/chartController';
import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Col, Form, Input, message, Row, Select, Space, Table, Tag, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import { history } from '@umijs/max';

const { Search } = Input;
const { Title } = Typography;

/**
 * 图表管理页面
 * @constructor
 */
const ChartManagePage: React.FC = () => {
  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({ current: 1, pageSize: 10 });
  const [chartList, setChartList] = useState<API.Chart[]>();
  const [total, setTotal] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await listChartByPageUsingPost(searchParams);
      if (res.data) {
        setChartList(res.data.records ?? []);
        setTotal(res.data.total ?? 0);
      } else {
        message.error('获取图表列表失败');
      }
    } catch (e: any) {
      message.error('获取图表列表失败，' + e.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [searchParams]);

  /**
   * 删除图表
   * @param id
   */
  const deleteChart = async (id: number) => {
    try {
      const res = await deleteChartUsingPost({
        id,
      });
      if (res.data) {
        message.success('删除图表成功');
        loadData();
      } else {
        message.error('删除图表失败');
      }
    } catch (e: any) {
      message.error('删除图表失败，' + e.message);
    }
  };

  // 定义表格列
  const columns : any = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      sorter: true,
    },
    {
      title: '图表名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string) => (
        <Typography.Text ellipsis={{ tooltip: text }}>
          {text || '-'}
        </Typography.Text>
      ),
      filters: chartList
        ?.filter((item) => item.name)
        .map((item) => ({ text: item.name!, value: item.name! }))
        .filter((value, index, self) => self.findIndex((v) => v.value === value.value) === index),
      onFilter: (value: string, record: API.Chart) => (record.name ? record.name.includes(value) : false),
      ellipsis: true,
    },
    {
      title: '分析目标',
      dataIndex: 'goal',
      key: 'goal',
      render: (text: string) => (
        <Typography.Text ellipsis={{ tooltip: text }}>
          {text || '-'}
        </Typography.Text>
      ),
      ellipsis: true,
    },
    {
      title: '图表类型',
      dataIndex: 'chartType',
      key: 'chartType',
      render: (text: string) => (
        <Tag color="blue">{text || '-'}</Tag>
      ),
      filters: chartList
        ?.filter((item) => item.chartType)
        .map((item) => ({ text: item.chartType!, value: item.chartType! }))
        .filter((value, index, self) => self.findIndex((v) => v.value === value.value) === index),
      onFilter: (value: string, record: API.Chart) => (record.chartType ? record.chartType.includes(value) : false),
    },
    {

      title: '图表状态',

      dataIndex: 'status',

      key: 'status',

      render: (text: string) => (

        <Tag
          color={

            text === 'succeed' ? 'green' :
            text === 'failed' ? 'red' :
            text === 'running' ? 'blue' :
            'default'
          }
        >
          {text === 'succeed' ? '成功' :
           text === 'failed' ? '失败' :
           text === 'running' ? '进行中' :
           text || '-'}
        </Tag>
      ),
      filters: [
        { text: '成功', value: 'succeed' },
        { text: '失败', value: 'failed' },
        { text: '进行中', value: 'running' },
      ],
      onFilter: (value: string, record: API.Chart) => record.status === value,
    },
    {

      title: '创建时间',

      dataIndex: 'createTime',

      key: 'createTime',

      sorter: true,

    },
    {
      title: '操作',
      key: 'action',
      render: (_ : any, record: API.Chart) => (
        <Space size="middle">
          <a onClick={() => {
            history.push(`/edit-chart/${record.id}`);
          }}>
            编辑
          </a>

          <a onClick={() => deleteChart(record?.id || 0)}>删除</a>

        </Space>

      ),

    },

  ];



  return (
    <div className="chart-manage-page">
      <Card>
        <Title level={4} style={{ marginBottom: 16 }}>
          图表管理
        </Title>
        <Row gutter={24} style={{ marginBottom: 16 }}>
          <Col span={8}>
            <Form layout="inline">
              <Form.Item label="图表名称">
                <Search
                  placeholder="请输入图表名称"
                  onSearch={(value) => {
                    setSearchParams({
                      ...searchParams,
                      name: value,
                    });
                  }}
                />
              </Form.Item>
            </Form>
          </Col>
          <Col span={8}>
            <Form layout="inline">
              <Form.Item label="图表类型">
                <Select
                  placeholder="请选择图表类型"
                  allowClear
                  style={{ width: 200 }}
                  onChange={(value) => {
                    setSearchParams({
                      ...searchParams,
                      chartType: value,
                    });
                  }}
                >
                  <Select.Option value="折线图">折线图</Select.Option>
                  <Select.Option value="柱状图">柱状图</Select.Option>
                  <Select.Option value="堆叠图">堆叠图</Select.Option>
                  <Select.Option value="饼图">饼图</Select.Option>
                  <Select.Option value="雷达图">雷达图</Select.Option>
                  <Select.Option value="K 线图">K 线图</Select.Option>
                  <Select.Option value="仪表盘">仪表盘</Select.Option>
                  <Select.Option value="Funnel 图">Funnel 图</Select.Option>
                  <Select.Option value="旭日图">旭日图</Select.Option>
                  <Select.Option value="箱型图">箱型图</Select.Option>
                  <Select.Option value="力导图">力导图</Select.Option>
                  <Select.Option value="关系图">关系图</Select.Option>
                  <Select.Option value="树图">树图</Select.Option>
                  <Select.Option value="地图">地图</Select.Option>
                  <Select.Option value="地理图">地理图</Select.Option>
                  <Select.Option value="热力图">热力图</Select.Option>
                </Select>
              </Form.Item>
            </Form>
          </Col>
          <Col span={8} style={{ textAlign: 'right' }}>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => history.push('/add')}
            >
              创建图表
            </Button>
          </Col>
        </Row>
        <Table
          loading={loading}
          dataSource={chartList}
          columns={columns}
          rowKey="id"
          pagination={{
            current: searchParams.current,
            pageSize: searchParams.pageSize,
            total: total,
            onChange: (page, pageSize) => {
              setSearchParams({
                ...searchParams,
                current: page,
                pageSize,
              });
            },
          }}
        />
      </Card>
    </div>
  );
};
export default ChartManagePage;
