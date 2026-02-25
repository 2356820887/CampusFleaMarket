import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Space, message, Input, Modal, Image } from 'antd';
import { SearchOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import request from '../../api/request';

const { confirm } = Modal;

const ProductManagement = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
    const [keyword, setKeyword] = useState('');

    useEffect(() => {
        fetchProducts();
    }, [pagination.current, pagination.pageSize]);

    const fetchProducts = async (page = pagination.current, size = pagination.pageSize, key = keyword) => {
        setLoading(true);
        try {
            const data = await request.get('/product/list', {
                params: { page, size, keyword: key }
            });
            setProducts(data?.records || []);
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
        fetchProducts(1, pagination.pageSize, keyword);
    };

    const handleOffShelf = (id) => {
        confirm({
            title: '确认强制下架该商品吗？',
            icon: <ExclamationCircleOutlined />,
            content: '下架后商品将不可见，且卖家无法自行恢复（需重新发布）。',
            onOk: async () => {
                try {
                    await request.post(`/product/admin/off-shelf/${id}`);
                    message.success('操作成功');
                    fetchProducts();
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
            title: '主图',
            dataIndex: 'imageUrls',
            render: urls => urls ? <Image src={urls.split(',')[0]} width={50} /> : '-'
        },
        {
            title: '标题',
            dataIndex: 'title',
            width: 200,
            ellipsis: true,
        },
        {
            title: '价格',
            dataIndex: 'price',
            render: val => `￥${val}`
        },
        {
            title: '卖家ID',
            dataIndex: 'sellerId',
        },
        {
            title: '状态',
            dataIndex: 'status',
            render: status => {
                // 0-草稿, 1-在售, 2-已售出, 3-已下架
                const map = {
                    0: { text: '草稿', color: 'default' },
                    1: { text: '在售', color: 'green' },
                    2: { text: '已售出', color: 'blue' },
                    3: { text: '已下架', color: 'red' }
                };
                const s = map[status] || { text: '未知', color: 'default' };
                return <Tag color={s.color}>{s.text}</Tag>;
            }
        },
        {
            title: '发布时间',
            dataIndex: 'createdAt',
            render: val => val ? new Date(val).toLocaleString() : '-'
        },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space size="middle">
                    {record.status === 1 && (
                        <Button
                            type="link"
                            danger
                            onClick={() => handleOffShelf(record.id)}
                        >
                            强制下架
                        </Button>
                    )}
                </Space>
            ),
        },
    ];

    return (
        <div>
            <div className="page-header">
                <div className="page-title">商品管理</div>
                <Space>
                    <Input
                        placeholder="搜索商品标题"
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
                dataSource={products}
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

export default ProductManagement;
