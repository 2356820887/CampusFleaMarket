import React, { useEffect, useState } from 'react';
import { Layout, Card, Button, Tag, Divider, Avatar, Space, message, Spin, Image, Breadcrumb, List, Rate } from 'antd';
import { UserOutlined, EnvironmentOutlined, ClockCircleOutlined, SafetyCertificateOutlined, ArrowLeftOutlined, ShoppingCartOutlined, HeartOutlined, HeartFilled } from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import request from '../../api/request';
import './detail.css';

const { Header, Content, Footer } = Layout;

const GoodsDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [buying, setBuying] = useState(false);
    const [reviews, setReviews] = useState([]);
    const [reviewsLoading, setReviewsLoading] = useState(false);
    const [isFavorite, setIsFavorite] = useState(false);

    useEffect(() => {
        fetchProductDetail();
        fetchReviews();
        checkFavoriteStatus();
    }, [id]);

    const checkFavoriteStatus = async () => {
        try {
            const res = await request.get(`/favorite/check/${id}`);
            setIsFavorite(res === true);
        } catch (e) {
            // console.warn("Check favorite failed", e);
        }
    };

    const toggleFavorite = async () => {
        try {
            if (isFavorite) {
                await request.post(`/favorite/remove/${id}`);
                message.success("已取消收藏");
                setIsFavorite(false);
            } else {
                await request.post(`/favorite/add/${id}`);
                message.success("已收藏");
                setIsFavorite(true);
            }
        } catch (e) {
            message.error("操作失败，请先登录");
        }
    };

    const fetchReviews = async () => {
        setReviewsLoading(true);
        try {
            const data = await request.get(`/orderReview/product/${id}`);
            setReviews(data || []);
        } catch (error) {
            console.error("获取评价失败", error);
        } finally {
            setReviewsLoading(false);
        }
    };

    const fetchProductDetail = async () => {
        setLoading(true);
        try {
            const data = await request.get(`/product/${id}`);

            // 获取卖家信息
            if (data.sellerId) {
                try {
                    const sellerData = await request.get(`/user/info/${data.sellerId}`);
                    // 手动合并卖家信息到商品对象中，用于页面展示
                    data.sellerName = sellerData.nickname || sellerData.username;
                    data.sellerAvatar = sellerData.avatar;
                } catch (e) {
                    console.error("获取卖家信息失败", e);
                }
            }

            setProduct(data);
        } catch (error) {
            console.error("获取详情失败", error);
            message.error("获取商品详情失败");
        } finally {
            setLoading(false);
        }
    };

    const handleBuy = async () => {
        setBuying(true);
        try {
            // 1. 创建订单
            const createData = {
                productId: product.id,
                tradeTime: new Date().toISOString(), // 默认当前时间，实际应由用户选择
                tradeLocation: product.tradeLocation || "校内协商"
            };
            const orderId = await request.post('/order/create', createData);

            if (orderId) {
                message.loading({ content: '正在跳转支付...', key: 'pay' });
                // 2. 获取支付表单
                const payForm = await request.post(`/order/pay/${orderId}`);

                // 3. 提交支付宝表单
                const div = document.createElement('div');
                div.innerHTML = payForm;
                document.body.appendChild(div);
                const form = div.getElementsByTagName('form')[0];
                form.submit();
            }
        } catch (error) {
            console.error("购买失败", error);
            // message.error("购买失败: " + (error.message || "未知错误"));
        } finally {
            setBuying(false);
        }
    };

    const handleAddToCart = async () => {
        try {
            await request.post('/cart/add', { productId: product.id });
            message.success('已添加到购物车');
        } catch (error) {
            // error handled by interceptor
        }
    };

    if (loading) {
        return (
            <div className="loading-container">
                <Spin size="large" tip="加载中...">
                    <div style={{ padding: 50 }} />
                </Spin>
            </div>
        );
    }

    if (!product) {
        return (
            <div className="error-container">
                <h2>商品不存在或已下架</h2>
                <Button type="primary" onClick={() => navigate('/')}>返回首页</Button>
            </div>
        );
    }

    const breadcrumbItems = [
        {
            title: '首页',
            href: '/',
            onClick: (e) => { e.preventDefault(); navigate('/'); }
        },
        {
            title: '商品详情'
        },
        {
            title: product.title
        }
    ];

    return (
        <Layout className="detail-layout">
            <Header className="detail-header">
                <div className="header-content">
                    <div className="logo" onClick={() => navigate('/')}>
                        <span className="logo-icon">🎓</span>
                        <span className="logo-text">校园跳蚤市场</span>
                    </div>
                    <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>
                        返回
                    </Button>
                </div>
            </Header>

            <Content className="detail-content">
                <div className="breadcrumb-container">
                    <Breadcrumb items={breadcrumbItems} />
                </div>

                <div className="detail-container">
                    <div className="detail-main">
                        {/* 左侧图片区 */}
                        <div className="image-section">
                            <div className="main-image">
                                <Image
                                    src={product.imageUrls ? product.imageUrls.split(',')[0] : 'https://via.placeholder.com/400'}
                                    alt={product.title}
                                    width="100%"
                                    height={400}
                                    style={{ objectFit: 'cover', borderRadius: '8px' }}
                                />
                            </div>
                            <div className="thumbnail-list">
                                {product.imageUrls && product.imageUrls.split(',').map((url, index) => (
                                    <div key={index} className="thumbnail-item">
                                        <Image
                                            src={url}
                                            width={80}
                                            height={80}
                                            style={{ objectFit: 'cover', borderRadius: '4px' }}
                                        />
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* 右侧信息区 */}
                        <div className="info-section">
                            <h1 className="product-title">{product.title}</h1>

                            <div className="price-box">
                                <span className="currency">￥</span>
                                <span className="price">{product.price}</span>
                                <Tag color="blue" className="condition-tag">{product.conditionLevel}成新</Tag>
                                {product.originalPrice && <span className="original-price">原价 ￥{product.originalPrice}</span>}
                            </div>

                            <div className="meta-info">
                                <div className="meta-item">
                                    <EnvironmentOutlined />
                                    <span>交易地点：{product.tradeLocation || '校内协商'}</span>
                                </div>
                                <div className="meta-item">
                                    <ClockCircleOutlined />
                                    <span>发布时间：{new Date(product.createdAt).toLocaleDateString()}</span>
                                </div>
                            </div>

                            <Divider />

                            <div className="seller-info">
                                <Avatar size={48} icon={<UserOutlined />} src={product.sellerAvatar} />
                                <div className="seller-detail">
                                    <div className="seller-name">{product.sellerName || '匿名同学'}</div>
                                    <div className="seller-credit">
                                        <SafetyCertificateOutlined style={{ color: '#52c41a' }} /> 信用极好
                                    </div>
                                </div>
                                <Button onClick={() => navigate(`/message?to=${product.sellerId}`)}>联系卖家</Button>
                            </div>

                            <Divider />

                            <div className="action-area">
                                <Button
                                    type="primary"
                                    size="large"
                                    className="buy-btn"
                                    onClick={handleBuy}
                                    loading={buying}
                                    disabled={product.status !== 1} // 1: 在售
                                >
                                    {product.status === 1 ? '立即购买' : '商品已售出或下架'}
                                </Button>
                                <Button
                                    size="large"
                                    icon={<ShoppingCartOutlined />}
                                    onClick={handleAddToCart}
                                    disabled={product.status !== 1}
                                >
                                    加入购物车
                                </Button>
                                <Button
                                    size="large"
                                    icon={isFavorite ? <HeartFilled style={{ color: '#ff4d4f' }} /> : <HeartOutlined />}
                                    onClick={toggleFavorite}
                                >
                                    {isFavorite ? '已收藏' : '想要'}
                                </Button>
                            </div>
                        </div>
                    </div>

                    <div className="detail-desc-card">
                        <h3>商品详情</h3>
                        <Divider />
                        <div className="desc-content">
                            {product.description}
                        </div>
                    </div>

                    <div className="detail-desc-card" style={{ marginTop: 24 }}>
                        <h3>买家评价 ({reviews.length})</h3>
                        <Divider />
                        <List
                            loading={reviewsLoading}
                            dataSource={reviews}
                            renderItem={item => (
                                <List.Item>
                                    <List.Item.Meta
                                        title={<Rate disabled defaultValue={item.rating} style={{ fontSize: 14 }} />}
                                        description={
                                            <div style={{ marginTop: 8 }}>
                                                <div style={{ color: '#333', fontSize: 16 }}>{item.content}</div>
                                                <div style={{ color: '#999', fontSize: 12, marginTop: 8 }}>
                                                    {new Date(item.createdAt).toLocaleString()}
                                                </div>
                                            </div>
                                        }
                                    />
                                </List.Item>
                            )}
                            locale={{ emptyText: '暂无评价' }}
                        />
                    </div>
                </div>
            </Content>

            <Footer className="detail-footer">
                <p>©2025 校园跳蚤市场 - 毕业设计项目</p>
            </Footer>
        </Layout >
    );
};

export default GoodsDetail;
