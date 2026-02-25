import React, { useEffect, useState } from 'react';
import { Layout, Card, Avatar, Tabs, List, Tag, Button, message, Space, Empty, Popconfirm, Modal, Form, Input, Row, Col, Rate } from 'antd';
import { UserOutlined, ShoppingOutlined, AppstoreOutlined, LogoutOutlined, ArrowLeftOutlined, MessageOutlined, HeartOutlined } from '@ant-design/icons';
import { useNavigate, useSearchParams } from 'react-router-dom';
import request from '../../api/request';
import ImageUpload from '../../components/ImageUpload';
import './index.css';

const { Header, Content, Footer } = Layout;

const UserCenter = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [user, setUser] = useState(null);
    const [myProducts, setMyProducts] = useState([]);
    const [mySales, setMySales] = useState([]);
    const [myOrders, setMyOrders] = useState([]);
    const [myFavorites, setMyFavorites] = useState([]);
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('1');
    const [isEditModalVisible, setIsEditModalVisible] = useState(false);

    const [isReviewModalVisible, setIsReviewModalVisible] = useState(false);
    const [isViewReviewVisible, setIsViewReviewVisible] = useState(false);
    const [currentReview, setCurrentReview] = useState(null);
    const [currentReviewOrderId, setCurrentReviewOrderId] = useState(null);
    const [form] = Form.useForm();
    const [reviewForm] = Form.useForm();

    useEffect(() => {
        const tab = searchParams.get('tab');
        if (tab === 'order') {
            setActiveTab('2');
        }

        const userInfo = localStorage.getItem('userInfo');
        if (!userInfo) {
            message.error('请先登录');
            navigate('/login');
            return;
        }
        setUser(JSON.parse(userInfo));
        fetchMyData();
    }, []);

    const fetchMyData = async () => {
        setLoading(true);
        try {
            // 获取我的发布
            const productsData = await request.get('/product/my-list', { params: { page: 1, size: 100 } });
            setMyProducts(productsData?.records || []);

            // 获取我的订单 (买家视角)
            try {
                const ordersData = await request.get('/order/my-list');
                setMyOrders(ordersData || []);
            } catch (e) {
                console.warn("获取订单接口可能不存在", e);
            }

            // 获取我的销售 (卖家视角 - 用于统计)
            try {
                const salesData = await request.get('/order/my-sales');
                setMySales(salesData || []);
            } catch (e) {
                console.warn("获取销售数据失败", e);
            }

            // 获取我的收藏
            try {
                const favData = await request.get('/favorite/list', { params: { page: 1, size: 100 } });
                setMyFavorites(favData?.records || []);
            } catch (e) {
                console.warn("获取收藏失败", e);
            }
        } catch (error) {
            console.error("获取数据失败", error);
        } finally {
            setLoading(false);
        }
    };

    const handleOffShelf = async (id) => {
        try {
            await request.post(`/product/off-shelf/${id}`);
            message.success('下架成功');
            fetchMyData();
        } catch (error) {
            console.error('下架失败', error);
            // message.error('下架失败');
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        message.success('已退出登录');
        navigate('/login');
    };

    const handleCancelOrder = async (orderId) => {
        try {
            await request.post(`/order/cancel/${orderId}`);
            message.success('订单已取消，商品已重新上架');
            fetchMyData();
        } catch (error) {
            console.error('取消失败', error);
            // message.error('取消失败');
        }
    };

    const getStatusTag = (status) => {
        const statusMap = {
            0: { text: '待付款', color: 'orange' },
            1: { text: '已付款', color: 'green' },
            2: { text: '已收货', color: 'blue' },
            3: { text: '已完成', color: 'cyan' },
            5: { text: '已关闭', color: 'default' },
            6: { text: '已发货', color: 'purple' },
            9: { text: '支付中', color: 'processing' }
        };
        const s = statusMap[status] || { text: '未知', color: 'default' };
        return <Tag color={s.color}>{s.text}</Tag>;
    };

    const handleEditProfile = () => {
        form.setFieldsValue(user);
        setIsEditModalVisible(true);
    };

    const handleSaveProfile = async () => {
        try {
            const values = await form.validateFields();
            const res = await request.post('/user/update', values);
            message.success('更新成功');
            setUser(res);
            localStorage.setItem('userInfo', JSON.stringify(res));
            setIsEditModalVisible(false);
        } catch (error) {
            console.error('更新失败', error);
        }
    };

    const handleConfirmReceipt = async (orderId) => {
        try {
            await request.post(`/order/confirm/${orderId}`);
            message.success('已确认收货，交易完成！');
            fetchMyData();
        } catch (error) {
            console.error('操作失败', error);
        }
    };

    const handleShipOrder = async (orderId) => {
        try {
            await request.post(`/order/ship/${orderId}`);
            message.success('发货成功！');
            fetchMyData();
        } catch (error) {
            console.error('发货失败', error);
        }
    };

    const handleRemoveFavorite = async (id) => {
        try {
            await request.post(`/favorite/remove/${id}`);
            message.success('已取消收藏');
            fetchMyData();
        } catch (error) {
            console.error('取消收藏失败', error);
        }
    };

    const handleOpenReview = (orderId) => {
        setCurrentReviewOrderId(orderId);
        reviewForm.resetFields();
        setIsReviewModalVisible(true);
    };

    const handleSubmitReview = async () => {
        try {
            const values = await reviewForm.validateFields();
            await request.post('/orderReview/add', {
                orderId: currentReviewOrderId,
                rating: values.rating,
                content: values.content
            });
            message.success('评价成功！');
            setIsReviewModalVisible(false);
            fetchMyData(); // 刷新列表，订单状态应变为已完成
        } catch (error) {
            console.error('评价失败', error);
        }
    };

    const handleViewReview = async (orderId) => {
        try {
            const res = await request.get(`/orderReview/order/${orderId}`);
            setCurrentReview(res);
            setIsViewReviewVisible(true);
        } catch (error) {
            console.error('获取评价失败', error);
        }
    };


    const [isReportModalVisible, setIsReportModalVisible] = useState(false);
    const [currentReportOrderId, setCurrentReportOrderId] = useState(null);
    const [reportForm] = Form.useForm();

    // ... (existing code, insert handlers before return)

    const handleOpenReport = (orderId) => {
        setCurrentReportOrderId(orderId);
        reportForm.resetFields();
        setIsReportModalVisible(true);
    };

    const [payingId, setPayingId] = useState(null);

    const handlePay = async (orderId) => {
        setPayingId(orderId);
        try {
            message.loading({ content: '正在前往支付页面...', key: 'pay' });
            // 获取支付表单
            const payForm = await request.post(`/order/pay/${orderId}`);

            // 提交支付宝表单
            const div = document.createElement('div');
            div.innerHTML = payForm;
            document.body.appendChild(div);
            const form = div.getElementsByTagName('form')[0];

            // 部分情况下可能是URL跳转
            if (!form && payForm.startsWith('http')) {
                window.location.href = payForm;
            } else if (form) {
                form.submit();
            } else {
                message.error({ content: '支付接口返回格式异常', key: 'pay' });
            }
        } catch (error) {
            console.error("支付跳转失败", error);
            message.error({ content: '支付跳转失败', key: 'pay' });
        } finally {
            setPayingId(null);
        }
    };

    const handleSubmitReport = async () => {
        try {
            const values = await reportForm.validateFields();
            await request.post('/report/add', {
                targetType: 'ORDER',
                targetId: currentReportOrderId,
                reason: values.reason,
                evidenceImages: values.evidenceImages
            });
            message.success('维权申请已提交，请耐心等待平台处理');
            setIsReportModalVisible(false);
        } catch (error) {
            console.error('提交失败', error);
        }
    };

    return (
        <Layout className="user-layout">
            <Header className="user-header">
                {/* ... existing header content ... */}
                <div className="header-content">
                    <div className="logo" onClick={() => navigate('/')}>
                        <span className="logo-icon">🎓</span>
                        <span className="logo-text">校园跳蚤市场</span>
                    </div>
                    <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate('/')}>
                        返回首页
                    </Button>
                </div>
            </Header>

            <Content className="user-content">
                <div className="user-container">
                    {/* ... existing user profile ... */}
                    <Card className="profile-card">
                        <div className="profile-info">
                            <Avatar size={80} src={user?.avatar} icon={<UserOutlined />} />
                            <div className="profile-text">
                                <h2>{user?.nickname || user?.username}</h2>
                                <p>账号：{user?.username}</p>
                                <Space>
                                    <Tag color="blue">学生认证</Tag>
                                    <Tag color="gold">信用极好</Tag>
                                </Space>
                                <div style={{ marginTop: 8 }}>
                                    <Button type="primary" size="small" ghost onClick={handleEditProfile}>
                                        编辑个人信息
                                    </Button>
                                    <Button type="primary" size="small" ghost onClick={() => navigate('/message')} icon={<MessageOutlined />}>
                                        我的消息
                                    </Button>
                                </div>
                            </div>
                            <Button danger icon={<LogoutOutlined />} onClick={handleLogout} className="logout-btn">
                                退出登录
                            </Button>
                        </div>
                    </Card>

                    {/* ... existing dashboard ... */}
                    <div style={{ marginBottom: 24 }}>
                        <Card title="📊 卖家中心" bordered={false} className="dashboard-card">
                            <Row gutter={16}>
                                <Col span={8}>
                                    <div className="stat-item">
                                        <div className="stat-value">￥{mySales.reduce((acc, cur) => acc + (cur.status === 1 || cur.status === 2 || cur.status === 6 ? cur.finalAmount : 0), 0) || 0}</div>
                                        <div className="stat-label">累计成交额</div>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <div className="stat-item">
                                        <div className="stat-value">{mySales.filter(o => o.status === 1 || o.status === 2 || o.status === 6).length}</div>
                                        <div className="stat-label">成功售出</div>
                                    </div>
                                </Col>
                                <Col span={8}>
                                    <div className="stat-item">
                                        <div className="stat-value">{mySales.filter(o => o.status === 1).length}</div>
                                        <div className="stat-label" style={{ color: '#fa8c16' }}>待发货</div>
                                    </div>
                                </Col>
                            </Row>
                        </Card>
                    </div>

                    {/* 功能标签页 */}
                    <Card className="tabs-card">
                        <Tabs
                            activeKey={activeTab}
                            onChange={setActiveTab}
                            items={[
                                {
                                    key: '1',
                                    label: <span><AppstoreOutlined />我的发布</span>,
                                    children: (
                                        <List
                                            loading={loading}
                                            dataSource={myProducts}
                                            renderItem={item => (
                                                <List.Item
                                                    actions={[
                                                        <Button type="link" onClick={() => navigate(`/goods/${item.id}`)}>查看</Button>,
                                                        item.status !== 3 && <Button type="link" onClick={() => navigate(`/publish/${item.id}`)}>编辑</Button>,

                                                        // 状态判断
                                                        item.status === 1 ? (
                                                            <Popconfirm title="确定要下架该商品吗？" onConfirm={() => handleOffShelf(item.id)} okText="确定" cancelText="取消">
                                                                <Button type="link" danger>下架</Button>
                                                            </Popconfirm>
                                                        ) : item.status === 2 ? (
                                                            <Button type="link" disabled>已下架</Button>
                                                        ) : item.status === 3 ? (
                                                            // 已售出，检查关联订单状态
                                                            item.orderStatus === 1 ? (
                                                                <Popconfirm title="确定要发货吗？" onConfirm={() => handleShipOrder(item.orderId)} okText="确定" cancelText="取消">
                                                                    <Button type="primary" size="small">发货</Button>
                                                                </Popconfirm>
                                                            ) : item.orderStatus === 6 ? (
                                                                <Tag color="cyan">已发货</Tag>
                                                            ) : item.orderStatus === 2 ? (
                                                                <Tag color="green">已收货</Tag>
                                                            ) : item.orderStatus === 3 ? (
                                                                <Tag color="blue">已完成</Tag>
                                                            ) : item.orderStatus === 0 ? (
                                                                <Tag color="orange">买家待付款</Tag>
                                                            ) : item.orderStatus === 9 ? (
                                                                <Tag color="processing">买家支付中</Tag>
                                                            ) : (
                                                                <Tag color="default">已售出(状态{item.orderStatus})</Tag>
                                                            )
                                                        ) : (
                                                            <Tag>审核中</Tag>
                                                        )
                                                    ]}
                                                >
                                                    <List.Item.Meta
                                                        avatar={<Avatar shape="square" size={64} src={item.imageUrls?.split(',')[0]} />}
                                                        title={item.title}
                                                        description={
                                                            <Space direction="vertical" size={0}>
                                                                <span>价格：￥{item.price} | 状态：{item.status === 1 ? '在售' : item.status === 3 ? '已售出' : item.status === 2 ? '已下架' : '审核中'}</span>
                                                                {(item.tradeType || item.tradeLocation) && (
                                                                    <span style={{ fontSize: 12, color: '#888' }}>
                                                                        {item.tradeType === 'online' ? '快递' : '自提'} · {item.tradeLocation}
                                                                    </span>
                                                                )}
                                                            </Space>
                                                        }
                                                    />
                                                </List.Item>
                                            )}
                                            locale={{ emptyText: <Empty description="暂无发布商品" /> }}
                                        />
                                    )
                                },
                                {
                                    key: '2',
                                    label: <span><ShoppingOutlined />我的订单</span>,
                                    children: (
                                        <List
                                            loading={loading}
                                            dataSource={myOrders}
                                            renderItem={item => (
                                                <List.Item
                                                    actions={[
                                                        <Button type="link" onClick={() => navigate(`/goods/${item.productId}`)}>查看商品</Button>,
                                                        (item.status === 1) && (
                                                            <Tag color="orange">等待发货</Tag>
                                                        ),
                                                        (item.status === 6) && ( // 已发货，买家可以确认收货
                                                            <Popconfirm
                                                                title="确认收到货了吗？"
                                                                onConfirm={() => handleConfirmReceipt(item.id)}
                                                                okText="是"
                                                                cancelText="否"
                                                            >
                                                                <Button type="link">确认收货</Button>
                                                            </Popconfirm>
                                                        ),
                                                        (item.status === 2) && ( // 已确认收货，可以评价
                                                            <Button type="link" onClick={() => handleOpenReview(item.id)}>去评价</Button>
                                                        ),
                                                        (item.status === 3) && ( // 已完成，可以查看评价
                                                            <Button type="link" onClick={() => handleViewReview(item.id)}>查看评价</Button>
                                                        ),
                                                        (item.status === 0 || item.status === 9) && (
                                                            <>
                                                                <Button
                                                                    type="primary"
                                                                    size="small"
                                                                    onClick={() => handlePay(item.id)}
                                                                    loading={payingId === item.id}
                                                                >
                                                                    去支付
                                                                </Button>
                                                                <Popconfirm
                                                                    title="确定要取消订单吗？取消后商品将重新上架"
                                                                    onConfirm={() => handleCancelOrder(item.id)}
                                                                    okText="确定"
                                                                    cancelText="暂不"
                                                                >
                                                                    <Button type="link" danger>取消订单</Button>
                                                                </Popconfirm>
                                                            </>
                                                        ),
                                                        (item.status === 6 || item.status === 2 || item.status === 3) && (
                                                            <Button type="link" danger size="small" onClick={() => handleOpenReport(item.id)}>申请维权</Button>
                                                        )
                                                    ]}
                                                >
                                                    <List.Item.Meta
                                                        title={`订单号：${item.orderSn}`}
                                                        description={
                                                            <Space direction="vertical">
                                                                <span>金额：￥{item.finalAmount}</span>
                                                                <span>交易地点：{item.tradeLocation}</span>
                                                                {getStatusTag(item.status)}
                                                            </Space>
                                                        }
                                                    />
                                                </List.Item>
                                            )}
                                            locale={{ emptyText: <Empty description="暂无订单记录" /> }}
                                        />
                                    )
                                },
                                {
                                    key: '3',
                                    label: <span><HeartOutlined />我的收藏</span>,
                                    children: (
                                        <List
                                            loading={loading}
                                            dataSource={myFavorites}
                                            renderItem={item => (
                                                <List.Item
                                                    actions={[
                                                        <Button type="link" onClick={() => navigate(`/goods/${item.id}`)}>查看详情</Button>,
                                                        <Popconfirm title="确定取消收藏吗？" onConfirm={() => handleRemoveFavorite(item.id)} okText="确定" cancelText="取消">
                                                            <Button type="link" danger>取消收藏</Button>
                                                        </Popconfirm>
                                                    ]}
                                                >
                                                    <List.Item.Meta
                                                        avatar={<Avatar shape="square" size={64} src={item.imageUrls?.split(',')[0]} />}
                                                        title={item.title}
                                                        description={
                                                            <Space direction="vertical" size={0}>
                                                                <span style={{ color: '#f5222d', fontWeight: 'bold' }}>￥{item.price}</span>
                                                                <span style={{ fontSize: 12, color: '#888' }}>
                                                                    {item.tradeType === 'online' ? '快递' : '自提'} · {item.tradeLocation || '校内'}
                                                                </span>
                                                            </Space>
                                                        }
                                                    />
                                                </List.Item>
                                            )}
                                            locale={{ emptyText: <Empty description="暂无收藏商品" /> }}
                                        />
                                    )
                                }
                            ]} />
                    </Card>
                </div>
            </Content>

            <Footer className="user-footer">
                <p>©2025 校园跳蚤市场 - 毕业设计项目</p>
            </Footer>

            <Modal
                title="编辑个人信息"
                open={isEditModalVisible}
                onOk={handleSaveProfile}
                onCancel={() => setIsEditModalVisible(false)}
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="nickname" label="昵称" rules={[{ required: true, message: '请输入昵称' }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item name="avatar" label="头像">
                        <ImageUpload />
                    </Form.Item>
                    <Form.Item name="phone" label="联系电话">
                        <Input />
                    </Form.Item>
                    <Form.Item name="schoolArea" label="校区">
                        <Input />
                    </Form.Item>
                    <Form.Item name="dormitory" label="宿舍">
                        <Input />
                    </Form.Item>
                    <Form.Item name="bio" label="个人简介">
                        <Input.TextArea rows={3} />
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="商品评价"
                open={isReviewModalVisible}
                onOk={handleSubmitReview}
                onCancel={() => setIsReviewModalVisible(false)}
            >
                <Form form={reviewForm} layout="vertical">
                    <Form.Item name="rating" label="评分" rules={[{ required: true, message: '请打分' }]}>
                        <Rate />
                    </Form.Item>
                    <Form.Item name="content" label="详细评价" rules={[{ required: true, message: '请输入评价内容' }]}>
                        <Input.TextArea rows={4} maxLength={200} showCount placeholder="说说这件宝贝的优点和美中不足的地方吧~" />
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="申请维权/举报"
                open={isReportModalVisible}
                onOk={handleSubmitReport}
                onCancel={() => setIsReportModalVisible(false)}
            >
                <Form form={reportForm} layout="vertical">
                    <Form.Item label="维权原因" name="reason" rules={[{ required: true, message: '请填写原因' }]}>
                        <Input.TextArea rows={4} placeholder="请详细描述您遇到的问题..." />
                    </Form.Item>
                    <Form.Item label="证据图片" name="evidenceImages">
                        <ImageUpload />
                    </Form.Item>
                    <p style={{ fontSize: 12, color: '#999' }}>提交后平台管理员将介入处理。</p>
                </Form>
            </Modal>

            <Modal
                title="评价详情"
                open={isViewReviewVisible}
                onCancel={() => setIsViewReviewVisible(false)}
                footer={[
                    <Button key="close" onClick={() => setIsViewReviewVisible(false)}>关闭</Button>
                ]}
            >
                {currentReview ? (
                    <div className="view-review-content">
                        <div style={{ marginBottom: 16 }}>
                            <span style={{ marginRight: 8 }}>评分：</span>
                            <Rate disabled defaultValue={currentReview.rating} />
                        </div>
                        <div>
                            <div style={{ marginBottom: 8 }}>评价内容：</div>
                            <div style={{ padding: '12px', background: '#f5f5f5', borderRadius: '4px' }}>
                                {currentReview.content || '该用户未填写评价内容内容'}
                            </div>
                        </div>
                        <div style={{ marginTop: 16, color: '#999', fontSize: '12px' }}>
                            评价时间：{new Date(currentReview.createdAt).toLocaleString()}
                        </div>
                    </div>
                ) : (
                    <Empty description="暂无评价信息" />
                )}
            </Modal>
        </Layout>
    );
};


export default UserCenter;
