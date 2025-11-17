import {listMyChartByPageUsingPost} from '@/services/lora-bi/chartController';
import {useModel} from '@@/exports';
import {Avatar, Button, Card, List, message, Modal, Result, Typography} from 'antd';
import ReactECharts from 'echarts-for-react';
import React, {useEffect, useState} from 'react';
import Search from "antd/es/input/Search";
import {history} from '@umijs/max';

const {Paragraph} = Typography;

/**
 * 我的图表页面
 * @constructor
 */
const MyChartPage: React.FC = () => {
  const initSearchParams = {
    current: 1,
    pageSize: 4,
    sortField :"createTime",
    sortOrder:"desc"
  };

  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({...initSearchParams});
  const {initialState} = useModel('@@initialState');
  const {currentUser} = initialState ?? {};
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
                // 如果 chartOption 是包含 JS 代码的字符串，需要提取其中的 JSON 对象
                let chartOptionStr = data.genChart || "{}";

                let parsedOptions;

                try {
                  // 更准确地提取 option = {...}; 格式中的对象部分
                  if (chartOptionStr.includes("option =")) {
                    // 处理可能的多种格式：option = {...}, let option = {...}, const option = {...}
                    const match = chartOptionStr.match(/(const|let|var)?\s*option\s*=\s*({[\s\S]*?});?\s*$/i);
                    if (match && match[2]) {
                      chartOptionStr = match[2].trim();
                    }
                  }

                  // 首先尝试直接JSON.parse
                  parsedOptions = JSON.parse(chartOptionStr);
                } catch (jsonError) {
                  console.error("JSON解析失败，尝试Function解析:", jsonError);

                  try {
                    // 如果JSON解析失败，尝试使用Function解析JavaScript对象
                    // 但要先做一些格式预处理
                    let jsCode = chartOptionStr.trim();

                    // 如果字符串不以 { 开头，尝试提取对象部分
                    if (!jsCode.startsWith('{') && jsCode.includes('{')) {
                      const objMatch = jsCode.match(/{[\s\S]*}/);
                      if (objMatch) {
                        jsCode = objMatch[0];
                      }
                    }

                    // 使用Function解析JavaScript对象
                    const func = new Function(`return (${jsCode})`);
                    parsedOptions = func();
                  } catch (functionError) {
                    console.error("Function解析失败:", functionError);
                    // 最后的备选方案：返回空对象
                    parsedOptions = {};
                  }
                }

                // 隐藏图表的 title
                if (parsedOptions && typeof parsedOptions === 'object') {
                  parsedOptions.title = undefined;
                  data.genChart = JSON.stringify(parsedOptions);
                } else {
                  data.genChart = JSON.stringify({});
                }
              } catch (e) {
                console.error("图表配置解析失败:", e);
                // 如果解析失败，保留原始数据但确保不会影响页面渲染
                data.genChart = JSON.stringify({});
              }
            }
          })
        }
      } else {
        // message.error('获取我的图表失败');
      }
    } catch (e: any) {
      // message.error('获取我的图表失败，' + e.message);
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
      <div className="margin-16"/>
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
            <Card style={{width: '100%'}}>

              <List.Item.Meta
                avatar={<Avatar src={currentUser && currentUser.userAvatar}/>}
                title={item.name}
                description={item.chartType ? '图表类型：' + item.chartType : undefined}
              />

              <>
              {
                item.status === 'wait' && <>
                  <Result
                    status='warning'
                    title=" 图表生成中"
                    subTitle={item.execMessage ?? " 图表生成队列繁忙，请耐心等候"}
                  />
                </>
              }


              {
                item.status === 'succeed' && <>

                  <div style={{marginBottom: 16}}/>
                  <p>{'分析目标：' + item.goal}</p>
                  <div style={{marginBottom: 16}}/>
                  {item.genChart && (() => {
                    try {
                      const chartOption = JSON.parse(item.genChart);
                      return <ReactECharts option={chartOption} />;
                    } catch (e) {
                      console.error("渲染图表失败:", e);
                      return <div>图表数据格式错误</div>;
                    }
                  })()}
                  <div style={{marginBottom: 16}}/>
                </>
              }
                {
                  item.status === 'filed' && <>
                    <Result
                      status="error"
                      title="图表生成错误"
                      subTitle={item.execMessage}
                    />
                  </>
                }
                {
                  item.status === 'running' && <>
                    <Result
                      status='info'
                      title=" 图表生成"
                      subTitle={item.execMessage}
                    />
                  </>
                }

              </>

              <div style={{display: 'flex', justifyContent: 'flex-end'}}>
                <Button
                  type="primary"
                  onClick={() => {
                    history.push(`/edit-chart/${item.id}`);
                  }}
                >
                  编辑
                </Button>
                <div style={{marginRight: 8}}/>
                <Button
                  onClick={() => {
                    if (item.chartData) {
                      showModal(item.chartData);
                    } else {
                      // message.info('暂无原始数据');
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
        open={isModalVisible}
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
