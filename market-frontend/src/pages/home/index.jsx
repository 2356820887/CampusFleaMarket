import React, { useEffect, useState } from 'react';
import { Layout, Input, Card, Col, Row, Tag, Select, InputNumber, Button, Space, Badge, Avatar, Dropdown, Empty } from 'antd';
import { FireOutlined, ShoppingCartOutlined, UserOutlined, SearchOutlined, LogoutOutlined, PlusOutlined, EnvironmentOutlined, DashboardOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import request from '../../api/request';
import './index.css';

const { Header, Content, Footer } = Layout;
const { Search } = Input;

const Home = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);
    const [page, setPage] = useState(1);
    const [hasMore, setHasMore] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [activities, setActivities] = useState([]);
    const [filter, setFilter] = useState({
        categoryId: null,
        activityId: null,
        minPrice: null,
        maxPrice: null,
        keyword: ''
    });
    const navigate = useNavigate();

    // 筛选状态


    useEffect(() => {
        // 检查登录状态
        const token = localStorage.getItem('token');
        const userInfo = localStorage.getItem('userInfo');
        if (token && userInfo) {
            try {
                setUser(JSON.parse(userInfo));
            } catch (error) {
                console.error("解析用户信息失败", error);
                localStorage.removeItem('userInfo');
                localStorage.removeItem('token');
            }
        }

        const params = new URLSearchParams(window.location.search);
        const actId = params.get('activityId');
        let initialFilter = { ...filter };

        if (actId) {
            initialFilter.activityId = parseInt(actId);
            setFilter(initialFilter);
        }

        fetchCategories();
        fetchActivities();
        fetchProducts(initialFilter);
    }, []);

    const fetchCategories = async () => {
        try {
            const data = await request.get('/category/list');
            setCategories(data || []);
        } catch (error) {
            console.error("加载分类失败", error);
        }
    };

    const fetchProducts = async (currentFilter = filter, currentPage = 1, isLoadMore = false) => {
        if (isLoadMore) setLoadingMore(true);
        else setLoading(true);

        try {
            const data = await request.get('/product/list', {
                params: { ...currentFilter, page: currentPage, size: 8 }
            });
            const newRecords = data?.records || [];

            if (isLoadMore) {
                setProducts(prev => [...prev, ...newRecords]);
            } else {
                setProducts(newRecords);
            }

            setHasMore(newRecords.length === 8);
            setPage(currentPage);
        } catch (error) {
            console.error("加载失败", error);
        } finally {
            setLoading(false);
            setLoadingMore(false);
        }
    };

    const handleScroll = () => {
        if (loading || loadingMore || !hasMore) return;

        const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        const clientHeight = document.documentElement.clientHeight;
        const scrollHeight = document.documentElement.scrollHeight;

        if (scrollTop + clientHeight >= scrollHeight - 100) {
            fetchProducts(filter, page + 1, true);
        }
    };

    useEffect(() => {
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, [loading, loadingMore, hasMore, page, filter]);

    const handleSearch = (value) => {
        const newFilter = { ...filter, keyword: value };
        setFilter(newFilter);
        fetchProducts(newFilter);
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        setUser(null);
        navigate('/login');
    };

    const userMenuItems = [
        ...(user?.role === 'ADMIN' ? [{
            key: 'admin',
            icon: <DashboardOutlined />,
            label: '返回后台',
        }] : []),
        {
            key: 'profile',
            icon: <UserOutlined />,
            label: '个人中心',
        },
        {
            key: 'logout',
            icon: <LogoutOutlined />,
            label: '退出登录',
        },
    ];


    const fetchActivities = async () => {
        try {
            const data = await request.get('/activityTopic/list/active');
            setActivities(data || []);
        } catch (error) {
            console.error(error);
        }
    };

    // ... fetchProducts ...

    return (
        <Layout className="home-layout">
            <Header className="home-header">
                {/* ... header content ... */}
                <div className="header-content">
                    <div className="logo" onClick={() => navigate('/')}>
                        <span className="logo-icon">🎓</span>
                        <span className="logo-text">校园跳蚤市场</span>
                    </div>

                    <div className="header-search">
                        <Search
                            placeholder="搜索你想要的宝贝..."
                            onSearch={handleSearch}
                            enterButton={<SearchOutlined />}
                            size="large"
                        />
                    </div>

                    <div className="header-actions">
                        <Button type="primary" icon={<PlusOutlined />} className="publish-btn" onClick={() => navigate('/publish')}>发布闲置</Button>


                        <Badge count={0} showZero={false} offset={[-5, 5]}>
                            <Button
                                type="text"
                                icon={<ShoppingCartOutlined style={{ fontSize: '24px' }} />}
                                onClick={() => navigate('/cart')}
                                style={{ marginRight: 16 }}
                            />
                        </Badge>

                        {user ? (
                            <Dropdown
                                menu={{
                                    items: userMenuItems,
                                    onClick: ({ key }) => {
                                        if (key === 'admin') navigate('/admin');
                                        if (key === 'profile') navigate('/user');
                                        if (key === 'logout') handleLogout();
                                    }
                                }}
                                placement="bottomRight"
                            >
                                <div className="user-profile">
                                    <Avatar src={user?.avatar} icon={<UserOutlined />} />
                                    <span className="username">{user?.nickname || user?.username}</span>
                                </div>
                            </Dropdown>
                        ) : (
                            <Space>
                                <Button type="primary" onClick={() => navigate('/login')}>登录</Button>
                                <Button onClick={() => navigate('/register')}>注册</Button>
                            </Space>
                        )}
                    </div>
                </div>
            </Header>

            <Content className="home-content">
                {/* Hero Section */}
                <div className="hero-section">
                    <div className="hero-background"></div>
                    <div className="hero-content">
                        <h1 className="hero-title">让校园闲置流动起来</h1>
                        <p className="hero-subtitle">更安全、更便捷、更懂你的校园二手交易平台</p>
                        <Space>
                            <Button type="primary" size="large" shape="round" icon={<FireOutlined />} onClick={() => {
                                const element = document.getElementById('product-section');
                                if (element) element.scrollIntoView({ behavior: 'smooth' });
                            }}>立即淘宝</Button>
                            <Button size="large" shape="round" icon={<PlusOutlined />} onClick={() => navigate('/publish')}>发布闲置</Button>
                        </Space>
                    </div>
                </div>

                {/* Activity Banner */}
                {activities.length > 0 && (
                    <div className="activity-banner">
                        <div className="section-header">
                            <div className="section-title"><h2>🔥 热门活动专区</h2></div>
                        </div>
                        <Row gutter={[24, 24]}>
                            {activities.map(act => (
                                <Col xs={24} md={8} key={act.id}>
                                    <Card
                                        hoverable
                                        variant="borderless"
                                        cover={act.bannerUrl ? <img alt={act.title} src={act.bannerUrl} style={{ height: 180, objectFit: 'cover' }} /> : null}
                                        onClick={() => {
                                            const nf = { ...filter, activityId: act.id, categoryId: null };
                                            setFilter(nf);
                                            fetchProducts(nf);
                                            message.success(`已切换至【${act.title}】专区`);
                                        }}
                                        className={`activity-card ${filter.activityId === act.id ? 'active-activity-card' : ''}`}
                                    >
                                        <Card.Meta
                                            title={<span style={{ fontSize: 18, fontWeight: 'bold' }}>{act.title}</span>}
                                            description={<span style={{ color: '#666' }}>{act.description}</span>}
                                        />
                                    </Card>
                                </Col>
                            ))}
                        </Row>
                    </div>
                )}

                {/* Filter Bar */}
                <div className="filter-container">
                    <Card variant="borderless" className="filter-card">
                        <Row gutter={24} align="middle">
                            <Col xs={24} md={6}>
                                <div className="filter-item" style={{ display: 'flex', alignItems: 'center' }}>
                                    <span className="filter-label">分类</span>
                                    <Select
                                        placeholder="全部分类"
                                        style={{ width: '100%', background: '#f5f5f5', borderRadius: 8, padding: 4 }}
                                        value={filter.categoryId}
                                        onChange={v => {
                                            const nf = { ...filter, categoryId: v };
                                            setFilter(nf);
                                            fetchProducts(nf);
                                        }}
                                        allowClear
                                        variant="borderless"
                                    >
                                        {categories.map(cat => (
                                            <Select.Option key={cat.id} value={cat.id}>{cat.name}</Select.Option>
                                        ))}
                                    </Select>
                                </div>
                            </Col>
                            <Col xs={24} md={12}>
                                <div className="filter-item" style={{ display: 'flex', alignItems: 'center' }}>
                                    <span className="filter-label">价格</span>
                                    <Space>
                                        <InputNumber
                                            placeholder="最低"
                                            value={filter.minPrice}
                                            onChange={v => setFilter({ ...filter, minPrice: v })}
                                            style={{ borderRadius: 8, background: '#f5f5f5', border: 'none' }}
                                        />
                                        <span className="price-split" style={{ color: '#999' }}>-</span>
                                        <InputNumber
                                            placeholder="最高"
                                            value={filter.maxPrice}
                                            onChange={v => setFilter({ ...filter, maxPrice: v })}
                                            style={{ borderRadius: 8, background: '#f5f5f5', border: 'none' }}
                                        />
                                        <Button type="primary" shape="round" onClick={() => fetchProducts()}>筛选</Button>
                                    </Space>
                                </div>
                            </Col>
                            <Col xs={24} md={6} style={{ textAlign: 'right' }}>
                                <Button type="text" onClick={() => {
                                    const reset = { categoryId: null, activityId: null, minPrice: null, maxPrice: null, keyword: '' };
                                    setFilter(reset);
                                    fetchProducts(reset);
                                }} style={{ color: '#666' }}>重置筛选</Button>
                                {filter.activityId && <Tag color="#FF6B6B" closable onClose={() => {
                                    const nf = { ...filter, activityId: null };
                                    setFilter(nf);
                                    fetchProducts(nf);
                                }} style={{ marginLeft: 8, borderRadius: 20, border: 'none', padding: '2px 10px' }}>活动中</Tag>}
                            </Col>
                        </Row>
                    </Card>
                </div>

                {/* Product Grid */}
                <div className="product-section" id="product-section">
                    <div className="section-header">
                        <div className="section-title">
                            <h2>{filter.activityId ? '专区商品' : '今日推荐'}</h2>
                        </div>
                    </div>

                    {products.length > 0 ? (
                        <>
                            <Row gutter={[24, 32]}>
                                {products.map(item => (
                                    <Col xs={24} sm={12} md={8} lg={6} key={item.id}>
                                        <Card
                                            hoverable
                                            bordered={false}
                                            onClick={() => {
                                                if (!user) {
                                                    navigate('/login');
                                                } else {
                                                    navigate(`/goods/${item.id}`);
                                                }
                                            }}
                                            cover={
                                                <div className="product-image-wrapper">
                                                    <img
                                                        alt={item.title}
                                                        src={item.imageUrls ? item.imageUrls.split(',')[0] : 'https://images.unsplash.com/photo-1581235720704-06d3acfcb36f?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'}
                                                    />
                                                    {item.isHot === 1 && <div className="hot-tag"><FireOutlined /> 热门</div>}
                                                </div>
                                            }
                                            bodyStyle={{ padding: 0 }}
                                        >
                                            <div className="product-info">
                                                <h3 className="product-title">{item.title}</h3>
                                                <div className="product-meta">
                                                    <span className="product-price">￥{item.price}</span>
                                                    <Tag className="condition-tag">{item.conditionLevel}成新</Tag>
                                                </div>
                                                <div className="product-footer">
                                                    <span className="campus-info"><EnvironmentOutlined /> {item.tradeLocation || '校内'}</span>
                                                    <Button type="primary" size="small" shape="round" className="view-btn" onClick={(e) => {
                                                        e.stopPropagation();
                                                        if (!user) {
                                                            navigate('/login');
                                                        } else {
                                                            navigate(`/goods/${item.id}`);
                                                        }
                                                    }}>购买</Button>
                                                </div>
                                            </div>
                                        </Card>
                                    </Col>
                                ))}
                            </Row>
                            <div style={{ textAlign: 'center', marginTop: 40, paddingBottom: 20 }}>
                                {loadingMore ? (
                                    <div className="loading-more">
                                        <FireOutlined spin style={{ color: '#FF6B6B', fontSize: 24 }} />
                                        <span style={{ marginLeft: 8, color: '#666' }}>更多好物加载中...</span>
                                    </div>
                                ) : !hasMore && products.length > 0 ? (
                                    <div className="no-more-data">
                                        <span style={{ color: '#999' }}>— 已经到底啦，去发布一件闲置吧 —</span>
                                    </div>
                                ) : null}
                            </div>
                        </>
                    ) : (
                        <Empty description="暂无相关商品" style={{ padding: '100px 0' }} />
                    )}
                </div>
            </Content>

            <Footer className="home-footer">
                <div className="footer-content">
                    <p>©2025 校园跳蚤市场 - 毕业设计项目</p>
                    <p>让每一件闲置物品都能找到它的新主人</p>
                </div>
            </Footer>
        </Layout >
    );
};

export default Home;
