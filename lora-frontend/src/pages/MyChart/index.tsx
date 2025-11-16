import { listMyChartByPageUsingPost } from '@/services/lora-bi/chartController';
import { useModel } from '@@/exports';
import { Avatar, Card, List, message, Button, Modal, Typography } from 'antd';
import ReactECharts from 'echarts-for-react';
import React, { useEffect, useState } from 'react';
import Search from "antd/es/input/Search";
import { history } from '@umijs/max';

const { Paragraph } = Typography;

/**
 * 我的图表页面
 * @constructor
 */
const MyChartPage: React.FC = () => {
  const initSearchParams = {
    current: 1,
    pageSize: 4,
  };

  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({ ...initSearchParams });
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const [chartList, setChartList] = useState<API.Chart[]>();
  const [total, setTotal] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [currentChartData, setCurrentChartData] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await listMyChartByPageUsingPost(searchParams);
      if (res.data) {
        setChartList(res.data.records ?? []);
        setTotal(res.data.total ?? 0);
        // 隐藏图表的 title
        if (res.data.records) {
          res.data.records.forEach(data => {
            if (data.genChart) {
              try {
                // 如果 chartOption 是包含 JS 代码的字符串，需要提取其中的配置对象
                let chartOptionStr = data.genChart || "{}";

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

                // 隐藏图表的 title
                parsedOptions.title = undefined;
                data.genChart = JSON.stringify(parsedOptions);
              } catch (e) {
                console.error("图表配置解析失败:", e);
                // 如果解析失败，保留原始数据但确保不会影响页面渲染
                data.genChart = JSON.stringify({});
              }
            }
          })
        }
      } else {
        message.error('获取我的图表失败');
      }
    } catch (e: any) {
      message.error('获取我的图表失败，' + e.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [searchParams]);

  const showModal = (chartData: string) => {
    setCurrentChartData(chartData);
    setIsModalVisible(true);
  };

  const handleOk = () => {
    setIsModalVisible(false);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
  };

  return (
    <div className="my-chart-page">
      <div>
        <Search placeholder="请输入图表名称" enterButton loading={loading} onSearch={(value) => {
          // 设置搜索条件
          setSearchParams({
            ...initSearchParams,
            name: value,
          })
        }}/>
      </div>
      <div className="margin-16" />
      <List
        grid={{
          gutter: 16,
          xs: 1,
          sm: 1,
          md: 1,
          lg: 2,
          xl: 2,
          xxl: 2,
        }}
        pagination={{
          onChange: (page, pageSize) => {
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize,
            })
          },
          current: searchParams.current,
          pageSize: searchParams.pageSize,
          total: total,
        }}
        loading={loading}
        dataSource={chartList}
        renderItem={(item) => (
          <List.Item key={item.id}>
            <Card style={{ width: '100%' }}>
              <List.Item.Meta
                avatar={<Avatar src={currentUser && currentUser.userAvatar} />}
                title={item.name}
                description={item.chartType ? '图表类型：' + item.chartType : undefined}
              />
              <div style={{ marginBottom: 16 }} />
              <p>{'分析目标：' + item.goal}</p>
              <div style={{ marginBottom: 16 }} />
              {item.genChart && <ReactECharts option={JSON.parse(item.genChart)} />}
              <div style={{ marginBottom: 16 }} />
              <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                <Button 
                  type="primary" 
                  onClick={() => {
                    history.push(`/edit-chart/${item.id}`);
                  }}
                >
                  编辑
                </Button>
                <div style={{ marginRight: 8 }} />
                <Button 
                  onClick={() => {
                    if (item.chartData) {
                      showModal(item.chartData);
                    } else {
                      message.info('暂无原始数据');
                    }
                  }}
                >
                  查看原始数据
                </Button>
              </div>
            </Card>
          </List.Item>
        )}
      />
      
      <Modal
        title="原始数据"
        visible={isModalVisible}
        onOk={handleOk}
        onCancel={handleCancel}
        width={800}
        okText="确定"
        cancelText="取消"
      >
        <Paragraph copyable>{currentChartData}</Paragraph>
      </Modal>
    </div>
  );
};
export default MyChartPage;