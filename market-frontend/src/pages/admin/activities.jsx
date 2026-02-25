import React, { useState, useEffect } from 'react';
import { Table, Card, Button, Input, Modal, Form, message, Space, DatePicker, Switch, Image } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import request from '../../api/request';
import ImageUpload from '../../components/ImageUpload';
import dayjs from 'dayjs';

const ActivityManagement = () => {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState([]);
    const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });

    const [isModalVisible, setIsModalVisible] = useState(false);
    const [currentId, setCurrentId] = useState(null);
    const [form] = Form.useForm();

    const fetchData = async (page = 1) => {
        setLoading(true);
        try {
            const res = await request.get('/activityTopic/admin/list', {
                params: { page, size: pagination.pageSize }
            });
            if (res) {
                setData(res.records);
                setPagination({ ...pagination, current: page, total: res.total });
            }
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const handleAdd = () => {
        setCurrentId(null);
        form.resetFields();
        // default status
        form.setFieldsValue({ isActive: true });
        setIsModalVisible(true);
    };

    const handleEdit = (record) => {
        setCurrentId(record.id);
        form.setFieldsValue({
            ...record,
            isActive: record.isActive === 1,
            timeRange: (record.startTime && record.endTime) ? [dayjs(record.startTime), dayjs(record.endTime)] : []
        });
        setIsModalVisible(true);
    };

    const handleDelete = async (id) => {
        Modal.confirm({
            title: '确认删除?',
            onOk: async () => {
                await request.post(`/activityTopic/admin/delete/${id}`);
                message.success('已删除');
                fetchData(pagination.current);
            }
        });
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            const payload = {
                id: currentId,
                title: values.title,
                description: values.description,
                bannerUrl: values.bannerUrl,
                isActive: values.isActive ? 1 : 0
            };

            if (values.timeRange && values.timeRange.length === 2) {
                payload.startTime = values.timeRange[0].format('YYYY-MM-DD HH:mm:ss');
                payload.endTime = values.timeRange[1].format('YYYY-MM-DD HH:mm:ss');
            }

            await request.post('/activityTopic/admin/save', payload);
            message.success('保存成功');
            setIsModalVisible(false);
            fetchData(pagination.current);
        } catch (error) {
            console.error(error);
        }
    };

    const columns = [
        { title: 'ID', dataIndex: 'id', width: 60 },
        { title: '活动名称', dataIndex: 'title' },
        { title: '封面', dataIndex: 'bannerUrl', render: url => url ? <Image src={url} width={80} /> : '-' },
        {
            title: '时间范围',
            render: (_, r) => (
                <div style={{ fontSize: 12 }}>
                    <div>起: {r.startTime ? dayjs(r.startTime).format('YYYY-MM-DD') : '不限'}</div>
                    <div>止: {r.endTime ? dayjs(r.endTime).format('YYYY-MM-DD') : '不限'}</div>
                </div>
            )
        },
        {
            title: '状态',
            dataIndex: 'isActive',
            render: (v) => <Switch checked={v === 1} disabled size="small" />
        },
        {
            title: '操作',
            render: (_, record) => (
                <Space>
                    <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
                    <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
                </Space>
            )
        }
    ];

    return (
        <Card title="特色专区活动管理" extra={
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新建活动</Button>
        }>
            <Table
                rowKey="id"
                columns={columns}
                dataSource={data}
                pagination={pagination}
                loading={loading}
                onChange={p => fetchData(p.current)}
            />

            <Modal
                title={currentId ? "编辑活动" : "新建活动"}
                open={isModalVisible}
                onOk={handleSubmit}
                onCancel={() => setIsModalVisible(false)}
            >
                <Form form={form} layout="vertical">
                    <Form.Item label="活动名称" name="title" rules={[{ required: true }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item label="活动简介" name="description">
                        <Input.TextArea rows={2} />
                    </Form.Item>
                    <Form.Item label="封面图片" name="bannerUrl">
                        <ImageUpload />
                    </Form.Item>
                    <Form.Item label="活动时间 (留空代表长期有效)" name="timeRange">
                        <DatePicker.RangePicker showTime />
                    </Form.Item>
                    <Form.Item label="是否启用" name="isActive" valuePropName="checked">
                        <Switch />
                    </Form.Item>
                </Form>
            </Modal>
        </Card>
    );
};

export default ActivityManagement;
