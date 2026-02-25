import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, message, Space, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import request from '../../api/request';

const CategoryManagement = () => {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingCategory, setEditingCategory] = useState(null);
    const [form] = Form.useForm();

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        setLoading(true);
        try {
            const data = await request.get('/category/list');
            setCategories(data || []);
        } catch (error) {
            console.error(error);
            message.error('获取分类列表失败');
        } finally {
            setLoading(false);
        }
    };

    const handleAdd = () => {
        setEditingCategory(null);
        form.resetFields();
        setIsModalVisible(true);
    };

    const handleEdit = (record) => {
        setEditingCategory(record);
        form.setFieldsValue(record);
        setIsModalVisible(true);
    };

    const handleDelete = async (id) => {
        try {
            await request.post(`/category/delete/${id}`);
            message.success('删除成功');
            fetchCategories();
        } catch (error) {
            message.error('删除失败');
        }
    };

    const handleOk = async () => {
        try {
            const values = await form.validateFields();
            if (editingCategory) {
                await request.post('/category/update', { ...editingCategory, ...values });
                message.success('更新成功');
            } else {
                await request.post('/category/add', values);
                message.success('添加成功');
            }
            setIsModalVisible(false);
            fetchCategories();
        } catch (error) {
            console.error(error);
            // message.error('操作失败');
        }
    };

    const columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 80,
        },
        {
            title: '分类名称',
            dataIndex: 'name',
            key: 'name',
        },
        // {
        //     title: '父分类ID',
        //     dataIndex: 'parentId',
        //     key: 'parentId',
        // },
        {
            title: '排序',
            dataIndex: 'sort',
            key: 'sort',
        },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space>
                    <Button icon={<EditOutlined />} type="link" onClick={() => handleEdit(record)}>编辑</Button>
                    <Popconfirm title="确定要删除吗？" onConfirm={() => handleDelete(record.id)} okText="确定" cancelText="取消">
                        <Button icon={<DeleteOutlined />} type="link" danger>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div className="admin-page">
            <div className="page-header">
                <div className="page-title">分类管理</div>
                <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>添加分类</Button>
            </div>

            <Table
                columns={columns}
                dataSource={categories}
                rowKey="id"
                loading={loading}
                pagination={false}
            />

            <Modal
                title={editingCategory ? '编辑分类' : '添加分类'}
                open={isModalVisible}
                onOk={handleOk}
                onCancel={() => setIsModalVisible(false)}
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        name="name"
                        label="分类名称"
                        rules={[{ required: true, message: '请输入分类名称' }]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item
                        name="sort"
                        label="排序权重 (越大越靠前)"
                    >
                        <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                    {/* 暂时简化不处理父分类选择 */}
                </Form>
            </Modal>
        </div>
    );
};

export default CategoryManagement;
