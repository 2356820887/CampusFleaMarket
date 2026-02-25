import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Space, Input } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import request from '../../api/request';

const OrderManagement = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });

    useEffect(() => {
        fetchOrders();
    }, [pagination.current, pagination.pageSize]);

    const fetchOrders = async (page = pagination.current, size = pagination.pageSize) => {
        setLoading(true);
        try {
            const data = await request.get('/order/admin/list', {
                params: { page, size }
            });
            setOrders(data?.records || []);
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

    const columns = [
        {
            title: '订单号',
            dataIndex: 'orderSn',
            width: 180,
        },
        {
            title: '商品ID',
            dataIndex: 'productId',
            width: 80,
        },
        {
            title: '买家ID',
            dataIndex: 'buyerId',
            width: 80,
        },
        {
            title: '金额',
            dataIndex: 'finalAmount',
            render: val => `￥${val}`
        },
        {
            title: '状态',
            dataIndex: 'status',
            render: status => {
                // 0-待付, 1-已付, 2-已收货, 3-已完成, 5-已关闭, 9-支付中
                const map = {
                    0: { text: '待付款', color: 'orange' },
                    1: { text: '已付款', color: 'green' },
                    2: { text: '已收货', color: 'blue' },
                    3: { text: '已完成', color: 'cyan' },
                    5: { text: '已关闭', color: 'default' },
                    9: { text: '支付中', color: 'processing' }
                };
                const s = map[status] || { text: '未知', color: 'default' };
                return <Tag color={s.color}>{s.text}</Tag>;
            }
        },
        {
            title: '创建时间',
            dataIndex: 'createdAt',
            render: val => val ? new Date(val).toLocaleString() : '-'
        },
    ];

    return (
        <div>
            <div className="page-header">
                <div className="page-title">订单管理</div>
            </div>
            <Table
                columns={columns}
                dataSource={orders}
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

export default OrderManagement;
