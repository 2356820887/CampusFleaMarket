import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Spin } from 'antd';
import { UserOutlined, ShoppingOutlined, PayCircleOutlined, DollarOutlined, ArrowUpOutlined } from '@ant-design/icons';
import request from '../../api/request';

const AdminDashboard = () => {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalUsers: 0,
        totalProducts: 0,
        totalOrders: 0,
        totalSales: 0
    });

    useEffect(() => {
        fetchStats();
    }, []);

    const fetchStats = async () => {
        try {
            const data = await request.get('/admin/stats/overview');
            setStats(data || {
                totalUsers: 0,
                totalProducts: 0,
                totalOrders: 0,
                totalSales: 0
            });
        } catch (error) {
            console.error("Fetch stats failed", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="dashboard-container">
            <h2 style={{ marginBottom: 24 }}>平台运营概览</h2>
            <Spin spinning={loading}>
                <Row gutter={24}>
                    <Col span={6}>
                        <Card hoverable variant="borderless" style={{ background: '#e6f7ff', borderRadius: 12 }}>
                            <Statistic
                                title="总用户数"
                                value={stats.totalUsers}
                                prefix={<UserOutlined style={{ color: '#1890ff' }} />}
                                valueStyle={{ color: '#1890ff' }}
                            />
                        </Card>
                    </Col>
                    <Col span={6}>
                        <Card hoverable variant="borderless" style={{ background: '#fff7e6', borderRadius: 12 }}>
                            <Statistic
                                title="在线商品数"
                                value={stats.totalProducts}
                                prefix={<ShoppingOutlined style={{ color: '#fa8c16' }} />}
                                valueStyle={{ color: '#fa8c16' }}
                            />
                        </Card>
                    </Col>
                    <Col span={6}>
                        <Card hoverable variant="borderless" style={{ background: '#f6ffed', borderRadius: 12 }}>
                            <Statistic
                                title="总订单数"
                                value={stats.totalOrders}
                                prefix={<PayCircleOutlined style={{ color: '#52c41a' }} />}
                                valueStyle={{ color: '#52c41a' }}
                            />
                        </Card>
                    </Col>
                    <Col span={6}>
                        <Card hoverable variant="borderless" style={{ background: '#fff1f0', borderRadius: 12 }}>
                            <Statistic
                                title="平台总交易额"
                                value={stats.totalSales}
                                precision={2}
                                prefix={<DollarOutlined style={{ color: '#f5222d' }} />}
                                valueStyle={{ color: '#f5222d' }}
                                suffix="CNY"
                            />
                        </Card>
                    </Col>
                </Row>
            </Spin>

            <div style={{ marginTop: 24, padding: 24, background: '#fff', borderRadius: 12 }}>
                <h3>快捷入口</h3>
                {/*<p>暂无更多图表数据，后续可接入ECharts展示每日趋势。</p>*/}
            </div>
        </div>
    );
};

export default AdminDashboard;
