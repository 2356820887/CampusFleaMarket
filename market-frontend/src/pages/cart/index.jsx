import React, { useState, useEffect, useMemo } from 'react';
import { List, Card, Button, Image, Typography, Space, message, Empty, Checkbox, Row, Col } from 'antd';
import { DeleteOutlined, ShoppingCartOutlined, DollarOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import request from '../../api/request';
import './index.css';

const { Title, Text } = Typography;

const Cart = () => {
    const [cartItems, setCartItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedRowKeys, setSelectedRowKeys] = useState([]);
    const [settling, setSettling] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchCart();
    }, []);

    const fetchCart = async () => {
        setLoading(true);
        try {
            const data = await request.get('/cart/list');
            // Ensure each item has a unique key for selection, typically item.id
            setCartItems(data || []);
            // Reset selection on refresh to avoid ghost selections
            setSelectedRowKeys([]);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    const handleRemove = async (productId) => {
        try {
            await request.post('/cart/remove', { productId });
            message.success('已移除');
            // Remove from local state immediately for better UX
            setCartItems(prev => prev.filter(item => item.id !== productId));
            setSelectedRowKeys(prev => prev.filter(key => key !== productId));
        } catch (error) {
            // error handled by interceptor
            fetchCart(); // Fallback to refetch
        }
    };

    // Calculate total price
    const totalPrice = useMemo(() => {
        return cartItems
            .filter(item => selectedRowKeys.includes(item.id))
            .reduce((total, item) => total + (item.price || 0), 0)
            .toFixed(2);
    }, [cartItems, selectedRowKeys]);

    const handleSelectAll = (e) => {
        if (e.target.checked) {
            setSelectedRowKeys(cartItems.map(item => item.id));
        } else {
            setSelectedRowKeys([]);
        }
    };

    const isAllSelected = cartItems.length > 0 && selectedRowKeys.length === cartItems.length;
    const indeterminate = selectedRowKeys.length > 0 && selectedRowKeys.length < cartItems.length;

    const handleBatchBuy = async () => {
        if (selectedRowKeys.length === 0) {
            message.warning('请先选择要结算的商品');
            return;
        }

        setSettling(true);
        try {
            // Since backend doesn't support batch order creation yet, we create orders sequentially
            // Ideally, backend should provide a /order/batch-create API
            let successCount = 0;
            const orderIds = [];

            for (const productId of selectedRowKeys) {
                const item = cartItems.find(i => i.id === productId);
                if (!item) continue;

                // Create Order
                const createData = {
                    productId: item.id,
                    tradeTime: new Date().toISOString(), // Default now
                    tradeLocation: item.tradeLocation || "校内协商"
                };

                try {
                    const orderId = await request.post('/order/create', createData);
                    if (orderId) {
                        orderIds.push(orderId);
                        successCount++;
                        // Optionally remove from cart
                        await request.post('/cart/remove', { productId: item.id });
                    }
                } catch (e) {
                    console.error(`Failed to create order for product ${item.id}`, e);
                }
            }

            if (successCount > 0) {
                message.success(`成功创建 ${successCount} 个订单，请前往订单页支付`);
                // Update local cart state
                setCartItems(prev => prev.filter(item => !selectedRowKeys.includes(item.id)));
                setSelectedRowKeys([]);
                // Navigate to My Orders page
                navigate('/user?tab=order'); // Assuming user center has tabs
            } else {
                message.error('结算失败，请重试');
            }

        } catch (error) {
            console.error('Batch settlement error', error);
            message.error('结算过程中发生错误');
        } finally {
            setSettling(false);
        }
    };

    const handleSingleBuy = (productId) => {
        navigate(`/goods/${productId}`);
    };

    return (
        <div className="cart-container">
            <div className="cart-wrapper">
                <div className="cart-header">
                    <Button
                        type="text"
                        icon={<ArrowLeftOutlined />}
                        onClick={() => navigate(-1)}
                        style={{ marginRight: 16, fontSize: '20px' }}
                    />
                    <h1 className="cart-title">
                        <ShoppingCartOutlined style={{ color: '#333' }} />
                        我的购物车
                    </h1>
                </div>

                {cartItems.length === 0 && !loading ? (
                    <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description={<span style={{ fontSize: 16, color: '#999' }}>购物车空空如也，快去逛逛吧</span>}
                        style={{ padding: '100px 0' }}
                    >
                        <Button type="primary" shape="round" size="large" onClick={() => navigate('/')}>去首页逛逛</Button>
                    </Empty>
                ) : (
                    <div className="cart-card-container">
                        <List
                            loading={loading}
                            itemLayout="horizontal"
                            dataSource={cartItems}
                            renderItem={item => (
                                <Card hoverable className="cart-item-card" bodyStyle={{ padding: 20 }}>
                                    <div className="cart-item-content">
                                        {/* Checkbox */}
                                        <Checkbox
                                            checked={selectedRowKeys.includes(item.id)}
                                            onChange={(e) => {
                                                if (e.target.checked) {
                                                    setSelectedRowKeys([...selectedRowKeys, item.id]);
                                                } else {
                                                    setSelectedRowKeys(selectedRowKeys.filter(key => key !== item.id));
                                                }
                                            }}
                                            className="cart-checkbox"
                                            disabled={item.status !== 1} // Disable if sold out
                                        />

                                        {/* Product Info */}
                                        <div className="cart-item-main" onClick={() => navigate(`/goods/${item.id}`)}>
                                            <div className="product-image-container">
                                                <img
                                                    className="product-image"
                                                    src={item.imageUrls ? item.imageUrls.split(',')[0] : 'https://via.placeholder.com/200'}
                                                    alt={item.title}
                                                />
                                            </div>

                                            <div className="item-details">
                                                <div className="item-title-row">
                                                    <h3 className="item-title">{item.title}</h3>
                                                    <span className="item-price">￥{item.price}</span>
                                                </div>
                                                <div className="item-desc">
                                                    {item.description ? (item.description.length > 50 ? item.description.substring(0, 50) + '...' : item.description) : '暂无描述'}
                                                </div>
                                                <div>
                                                    {item.status !== 1 && (
                                                        <span className="item-status-tag">已失效</span>
                                                    )}
                                                </div>
                                            </div>
                                        </div>

                                        {/* Actions */}
                                        <div className="item-actions">
                                            <Button
                                                type="text"
                                                danger
                                                shape="circle"
                                                size="large"
                                                icon={<DeleteOutlined />}
                                                onClick={() => handleRemove(item.id)}
                                                className="remove-btn"
                                            />
                                        </div>
                                    </div>
                                </Card>
                            )}
                        />
                    </div>
                )}
            </div>

            {/* Fixed Footer for Settlement */}
            {cartItems.length > 0 && (
                <div className="cart-footer">
                    <div className="cart-footer-left">
                        <Checkbox
                            checked={isAllSelected}
                            indeterminate={indeterminate}
                            onChange={handleSelectAll}
                            style={{ transform: 'scale(1.1)' }}
                        >
                            <span className="select-all-text">全选</span>
                        </Checkbox>
                        <span className="selected-count">已选 {selectedRowKeys.length} 件</span>
                    </div>
                    <div className="cart-footer-right">
                        <div className="total-wrapper">
                            <span className="total-label">合计 Total</span>
                            <span className="total-price">￥{totalPrice}</span>
                        </div>
                        <Button
                            type="primary"
                            className="checkout-btn"
                            onClick={handleBatchBuy}
                            loading={settling}
                            disabled={selectedRowKeys.length === 0}
                        >
                            立即结算
                        </Button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Cart;
