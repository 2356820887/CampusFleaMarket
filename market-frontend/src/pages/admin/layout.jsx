import React, { useState, useEffect } from 'react';
import { Layout, Menu, Button, Avatar, Dropdown, message } from 'antd';
import {
    UserOutlined,
    ShoppingOutlined,
    OrderedListOutlined,
    LogoutOutlined,
    DashboardOutlined,
    HomeOutlined,
    AppstoreOutlined,
    ExceptionOutlined,
    NotificationOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import './layout.css';

const { Header, Sider, Content } = Layout;

const AdminLayout = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [collapsed, setCollapsed] = useState(false);
    const [user, setUser] = useState(null);

    useEffect(() => {
        const userInfo = localStorage.getItem('userInfo');
        if (!userInfo) {
            message.error('请先登录');
            navigate('/login');
            return;
        }
        const parsedUser = JSON.parse(userInfo);
        if (parsedUser.role !== 'ADMIN') {
            message.error('无权访问管理后台');
            navigate('/');
            return;
        }
        setUser(parsedUser);
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        message.success('已退出登录');
        navigate('/login');
    };

    const menuItems = [
        {
            key: '/admin',
            icon: <DashboardOutlined />,
            label: '控制台',
            onClick: () => navigate('/admin')
        },
        {
            key: '/admin/users',
            icon: <UserOutlined />,
            label: '用户管理',
            onClick: () => navigate('/admin/users')
        },
        {
            key: '/admin/products',
            icon: <ShoppingOutlined />,
            label: '商品管理',
            onClick: () => navigate('/admin/products')
        },
        {
            key: '/admin/categories',
            icon: <AppstoreOutlined />,
            label: '分类管理',
            onClick: () => navigate('/admin/categories')
        },
        {
            key: '/admin/orders',
            icon: <OrderedListOutlined />,
            label: '订单管理',
            onClick: () => navigate('/admin/orders')
        },
        {
            key: '/admin/reports',
            icon: <ExceptionOutlined />,
            label: '维权管理',
            onClick: () => navigate('/admin/reports')
        },
        {
            key: '/admin/activities',
            icon: <NotificationOutlined />,
            label: '活动专区',
            onClick: () => navigate('/admin/activities')
        }
    ];

    return (
        <Layout className="admin-layout">
            <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed} className="admin-sider" width={260}>
                <div className="admin-logo">
                    <span className="logo-icon">🛡️</span>
                    {!collapsed && <span className="logo-text">管理后台</span>}
                </div>
                <Menu
                    theme="dark"
                    mode="inline"
                    selectedKeys={[location.pathname]}
                    items={menuItems}
                />
            </Sider>
            <Layout className="site-layout">
                <Header className="admin-header">
                    <div className="header-left">
                        {/* 可以在这里放面包屑或其他 */}
                    </div>
                    <div className="header-right">
                        <Button type="text" icon={<HomeOutlined />} onClick={() => navigate('/')}>
                            返回前台
                        </Button>
                        <Dropdown
                            menu={{
                                items: [
                                    {
                                        key: 'logout',
                                        icon: <LogoutOutlined />,
                                        label: '退出登录',
                                        onClick: handleLogout
                                    }
                                ]
                            }}
                            placement="bottomRight"
                        >
                            <div className="admin-profile">
                                <Avatar size="small" icon={<UserOutlined />} src={user?.avatar} />
                                <span>{user?.nickname || user?.username}</span>
                            </div>
                        </Dropdown>
                    </div>
                </Header>
                <Content className="admin-content-wrapper">
                    <div className="admin-content-card">
                        <Outlet />
                    </div>
                </Content>
            </Layout>
        </Layout>
    );
};

export default AdminLayout;
