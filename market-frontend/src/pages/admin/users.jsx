import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Space, message, Input, Modal } from 'antd';
import { SearchOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import request from '../../api/request';

const { confirm } = Modal;

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
    const [keyword, setKeyword] = useState('');

    useEffect(() => {
        fetchUsers();
    }, [pagination.current, pagination.pageSize]);

    const fetchUsers = async (page = pagination.current, size = pagination.pageSize, key = keyword) => {
        setLoading(true);
        try {
            const data = await request.get('/user/list', {
                params: { page, size, keyword: key }
            });
            setUsers(data?.records || []);
            setPagination({
                ...pagination,
                current: data?.current || 1,
                total: data?.total || 0
            });
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = () => {
        setPagination({ ...pagination, current: 1 });
        fetchUsers(1, pagination.pageSize, keyword);
    };

    const handleStatusChange = (userId, currentStatus) => {
        // currentStatus: 1-正常, 0-禁言, -1-封号
        // 这里简化逻辑，如果是正常则封号，如果是封号/禁言则解封
        const newStatus = currentStatus === 1 ? -1 : 1;
        const actionText = newStatus === 1 ? '解封' : '封禁';

        confirm({
            title: `确认${actionText}该用户吗？`,
            icon: <ExclamationCircleOutlined />,
            onOk: async () => {
                try {
                    await request.post('/user/status', { userId, status: newStatus });
                    message.success('操作成功');
                    fetchUsers();
                } catch (error) {
                    // error handled by interceptor
                }
            }
        });
    };

    const columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            width: 80,
        },
        {
            title: '用户名',
            dataIndex: 'username',
        },
        {
            title: '昵称',
            dataIndex: 'nickname',
        },
        {
            title: '角色',
            dataIndex: 'role',
            render: role => <Tag color={role === 'ADMIN' ? 'red' : 'blue'}>{role}</Tag>
        },
        {
            title: '状态',
            dataIndex: 'status',
            render: status => {
                // 1-正常, 0-禁言, -1-封号
                let color = 'green';
                let text = '正常';
                if (status === 0) { color = 'orange'; text = '禁言'; }
                if (status === -1) { color = 'red'; text = '封号'; }
                return <Tag color={color}>{text}</Tag>;
            }
        },
        {
            title: '注册时间',
            dataIndex: 'createdAt',
            render: val => val ? new Date(val).toLocaleString() : '-'
        },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space size="middle">
                    {record.role !== 'ADMIN' && (
                        <Button
                            type="link"
                            danger={record.status === 1}
                            onClick={() => handleStatusChange(record.id, record.status)}
                        >
                            {record.status === 1 ? '封禁' : '解封'}
                        </Button>
                    )}
                </Space>
            ),
        },
    ];

    return (
        <div>
            <div className="page-header">
                <div className="page-title">用户管理</div>
                <Space>
                    <Input
                        placeholder="搜索用户名/昵称"
                        value={keyword}
                        onChange={e => setKeyword(e.target.value)}
                        onPressEnter={handleSearch}
                        style={{ width: 200, borderRadius: '8px' }}
                    />
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} style={{ borderRadius: '8px' }}>搜索</Button>
                </Space>
            </div>
            <Table
                columns={columns}
                dataSource={users}
                rowKey="id"
                loading={loading}
                pagination={{
                    ...pagination,
                    onChange: (page, size) => {
                        setPagination({ ...pagination, current: page, pageSize: size });
                    }
                }}
            />
        </div>
    );
};

export default UserManagement;
