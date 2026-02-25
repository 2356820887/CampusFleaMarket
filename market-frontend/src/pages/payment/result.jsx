import React, { useEffect, useState } from 'react';
import { Result, Button, Card } from 'antd';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CheckCircleOutlined, HomeOutlined, ShoppingOutlined } from '@ant-design/icons';

const PaymentResult = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [status, setStatus] = useState('success');

    // 支付宝同步回调参数
    const outTradeNo = searchParams.get('out_trade_no');
    const totalAmount = searchParams.get('total_amount');

    useEffect(() => {
        // 这里可以根据 URL 参数做一些简单的校验或展示
        // 实际上同步回调不可信，真正的状态应以异步通知或主动查询为准
        // 但为了用户体验，我们默认展示成功页面
        console.log('Payment Return:', Object.fromEntries(searchParams));
    }, [searchParams]);

    return (
        <div style={{
            padding: '50px 20px',
            display: 'flex',
            justifyContent: 'center',
            minHeight: '100vh',
            background: '#f5f7fa'
        }}>
            <Card style={{ width: '100%', maxWidth: 600, borderRadius: 8 }}>
                <Result
                    status="success"
                    title="支付成功！"
                    subTitle={
                        <div>
                            <p>订单编号: {outTradeNo}</p>
                            <p>支付金额: ￥{totalAmount}</p>
                            <p style={{ color: '#999', fontSize: '12px', marginTop: 10 }}>
                                商家正在确认收款，请耐心等待发货
                            </p>
                        </div>
                    }
                    extra={[
                        <Button type="primary" key="home" icon={<HomeOutlined />} onClick={() => navigate('/')}>
                            返回首页
                        </Button>,
                        <Button key="buy" icon={<ShoppingOutlined />} onClick={() => navigate('/')}>
                            继续购物
                        </Button>,
                    ]}
                />
            </Card>
        </div>
    );
};

export default PaymentResult;
