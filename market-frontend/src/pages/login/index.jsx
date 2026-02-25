import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography } from 'antd';
import { UserOutlined, LockOutlined, HomeOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import request from '../../api/request';
import './index.css';

const { Title, Text } = Typography;

const Login = () => {
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const onFinish = async (values) => {
        setLoading(true);
        try {
            const data = await request.post('/user/login', values);
            message.success('登录成功');
            localStorage.setItem('token', data.token); // 假设后端返回 token
            localStorage.setItem('userInfo', JSON.stringify(data.user));

            if (data.user.role === 'ADMIN') {
                navigate('/admin');
            } else {
                navigate('/');
            }
        } catch (error) {
            console.error('登录失败', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <Card className="login-card" variant="borderless">
                <div className="login-header">
                    <Title level={2}>欢迎回来</Title>
                    <Text type="secondary">登录您的校园跳蚤市场账号</Text>
                </div>
                <Form
                    name="login"
                    onFinish={onFinish}
                    size="large"
                    layout="vertical"
                >
                    <Form.Item
                        name="username"
                        rules={[{ required: true, message: '请输入用户名' }]}
                    >
                        <Input prefix={<UserOutlined />} placeholder="用户名" />
                    </Form.Item>
                    <Form.Item
                        name="password"
                        rules={[{ required: true, message: '请输入密码' }]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" block loading={loading}>
                            立即登录
                        </Button>
                    </Form.Item>
                    <div className="login-footer">
                        还没有账号？ <Link to="/register">立即注册</Link>
                    </div>
                    <div className="login-back-home">
                        <Button type="link" icon={<HomeOutlined />} onClick={() => navigate('/')}>
                            返回首页
                        </Button>
                    </div>
                </Form>
            </Card>
        </div>
    );
};

export default Login;
