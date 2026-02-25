import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography, Select, Row, Col } from 'antd';
import { UserOutlined, LockOutlined, PhoneOutlined, IdcardOutlined, HomeOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import request from '../../api/request';
import './index.css';

const { Title, Text } = Typography;
const { Option } = Select;

const Register = () => {
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const onFinish = async (values) => {
        setLoading(true);
        try {
            await request.post('/user/register', values);
            message.success('注册成功，请登录');
            navigate('/login');
        } catch (error) {
            console.error('注册失败', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="register-container">
            <Card className="register-card" variant="borderless">
                <div className="register-header">
                    <Title level={2}>加入我们</Title>
                    <Text type="secondary">创建您的校园跳蚤市场账号</Text>
                </div>
                <Form
                    name="register"
                    onFinish={onFinish}
                    size="large"
                    layout="vertical"
                >
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                name="username"
                                label="用户名"
                                rules={[{ required: true, message: '请输入用户名' }]}
                            >
                                <Input prefix={<UserOutlined />} placeholder="用于登录" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="nickname"
                                label="昵称"
                                rules={[{ required: true, message: '请输入昵称' }]}
                            >
                                <Input prefix={<UserOutlined />} placeholder="展示名称" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        name="password"
                        label="密码"
                        rules={[{ required: true, message: '请输入密码' }]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="至少6位字符" />
                    </Form.Item>

                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                name="phone"
                                label="联系电话"
                                rules={[{ required: true, message: '请输入联系电话' }]}
                            >
                                <Input prefix={<PhoneOutlined />} placeholder="手机号" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="studentId"
                                label="学号"
                                rules={[{ required: true, message: '请输入学号' }]}
                            >
                                <Input prefix={<IdcardOutlined />} placeholder="学生证号" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                name="campus"
                                label="所属校区"
                                rules={[{ required: true, message: '请选择校区' }]}
                            >
                                <Select placeholder="选择校区">
                                    <Option value="主校区">主校区</Option>
                                    <Option value="南校区">南校区</Option>
                                    <Option value="北校区">北校区</Option>
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="dormitory"
                                label="宿舍楼"
                                rules={[{ required: true, message: '请输入宿舍楼' }]}
                            >
                                <Input prefix={<HomeOutlined />} placeholder="如：12号楼" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" block loading={loading}>
                            立即注册
                        </Button>
                    </Form.Item>
                    <div className="register-footer">
                        已有账号？ <Link to="/login">立即登录</Link>
                    </div>
                </Form>
            </Card>
        </div>
    );
};

export default Register;
