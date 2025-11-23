import React, { useState, useEffect } from 'react';
import { Card, Typography, Space, Button, message, Statistic, Row, Col, Divider } from 'antd';
import { useModel } from '@@/exports';
import { getUserScoreUsingGet, rechargeScoreUsingPost } from '@/services/lora-bi/userController';
import { history } from '@umijs/max';

const { Title, Text } = Typography;

/**
 * 用户积分页面
 * @constructor
 */
const UserScorePage: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const [score, setScore] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [recharging, setRecharging] = useState<boolean>(false);
  const isAdmin = currentUser && currentUser.userRole === 'admin';

  // 获取用户积分
  const fetchScore = async () => {
    try {
      setLoading(true);
      const res = await getUserScoreUsingGet();
      if (res.data !== undefined) {
        setScore(res.data);
      } else {
        message.error('获取积分失败');
      }
    } catch (error: any) {
      message.error('获取积分失败: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  // 充值积分
  const handleRecharge = async (amount: number) => {
    if (recharging) return;
    setRecharging(true);
    
    try {
      const res = await rechargeScoreUsingPost({ amount });
      if (res.data) {
        message.success('充值成功');
        // 重新获取积分
        await fetchScore();
      } else {
        message.error('充值失败');
      }
    } catch (error: any) {
      message.error('充值失败: ' + error.message);
    } finally {
      setRecharging(false);
    }
  };

  useEffect(() => {
    fetchScore();
  }, []);

  return (
    <div style={{ padding: '24px' }}>
      <Card>
        <Title level={3} style={{ textAlign: 'center', color: '#1890ff' }}>用户积分管理</Title>
        <Divider />
        
        <Row justify="center" style={{ marginBottom: 24 }}>
          <Col>
            <Statistic 
              title="当前积分" 
              value={score} 
              precision={0}
              valueStyle={{ color: '#3f8600' }}
              suffix="分"
            />
          </Col>
        </Row>

        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Text type="secondary">每次图表生成消耗 10 积分</Text>
        </div>

        {!isAdmin && (
          <>
            <Divider>积分充值</Divider>
            <div style={{ textAlign: 'center' }}>
              <Space wrap>
                <Button 
                  type="primary" 
                  size="large"
                  loading={recharging}
                  onClick={() => handleRecharge(50)}
                >
                  充值 50 积分
                </Button>
                <Button 
                  type="primary" 
                  size="large"
                  loading={recharging}
                  onClick={() => handleRecharge(100)}
                >
                  充值 100 积分
                </Button>
                <Button 
                  type="primary" 
                  size="large"
                  loading={recharging}
                  onClick={() => handleRecharge(200)}
                >
                  充值 200 积分
                </Button>
              </Space>
            </div>
          </>
        )}

        {isAdmin && (
          <div style={{ textAlign: 'center', marginTop: 20 }}>
            <Text strong type="success">管理员账户，使用无限制</Text>
          </div>
        )}

        <Divider>积分说明</Divider>
        <div style={{ padding: '0 20px' }}>
          <ul style={{ textAlign: 'left' }}>
            <li>每次使用智能分析功能消耗 10 积分</li>
            <li>积分不足时将无法使用图表生成功能</li>
            <li>管理员账户使用无限制</li>
            <li>积分永久有效，不会过期</li>
          </ul>
        </div>
      </Card>
    </div>
  );
};

export default UserScorePage;