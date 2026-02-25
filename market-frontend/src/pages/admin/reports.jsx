import React, { useState, useEffect } from 'react';
import { Table, Card, Button, Input, Select, Tag, Modal, Form, message, Space, Image, Tooltip } from 'antd';
import { SearchOutlined, EyeOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import request from '../../api/request';

const { Option } = Select;

const ReportManagement = () => {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState([]);
    const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
    const [statusFilter, setStatusFilter] = useState(undefined);

    const [isHandleModalVisible, setIsHandleModalVisible] = useState(false);
    const [isDetailModalVisible, setIsDetailModalVisible] = useState(false);
    const [currentReport, setCurrentReport] = useState(null);
    const [form] = Form.useForm();

    const fetchReports = async (page = 1) => {
        setLoading(true);
        try {
            const res = await request.get('/report/admin/list', {
                params: {
                    page: page,
                    size: pagination.pageSize,
                    status: statusFilter
                }
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
        fetchReports();
    }, [statusFilter]);

    const handleTableChange = (page) => {
        fetchReports(page.current);
    };

    const onHandleClick = (record) => {
        setCurrentReport(record);
        form.resetFields();
        setIsHandleModalVisible(true);
    };

    const onViewDetail = (record) => {
        setCurrentReport(record);
        setIsDetailModalVisible(true);
    };

    const submitHandle = async () => {
        try {
            const values = await form.validateFields();
            await request.post('/report/admin/handle', {
                reportId: currentReport.id,
                status: values.status,
                reply: values.reply
            });
            message.success('处理成功');
            setIsHandleModalVisible(false);
            fetchReports(pagination.current);
        } catch (error) {
            console.error(error);
        }
    };

    const columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            width: 80,
        },
        {
            title: '举报人ID',
            dataIndex: 'reporterId',
            width: 100,
        },
        {
            title: '对象类型',
            dataIndex: 'targetType',
            width: 100,
            render: (text) => <Tag color="blue">{text}</Tag>
        },
        {
            title: '对象ID',
            dataIndex: 'targetId',
            width: 100,
            render: (text, record) => (
                <a href={record.targetType === 'PRODUCT' ? `/goods/${text}` : '#'}>{text}</a>
            )
        },
        {
            title: '原因',
            dataIndex: 'reason',
            ellipsis: true,
        },
        {
            title: '证据',
            dataIndex: 'evidenceImages',
            render: (text) => (
                text ? <Image src={text.split(',')[0]} width={50} height={50} /> : '无'
            )
        },
        {
            title: '提交时间',
            dataIndex: 'createdAt',
            render: (text) => new Date(text).toLocaleString(),
        },
        {
            title: '状态',
            dataIndex: 'status',
            render: (status) => {
                if (status === 0) return <Tag color="orange">待处理</Tag>;
                if (status === 1) return <Tag color="green">已通过</Tag>;
                if (status === 2) return <Tag color="red">已驳回</Tag>;
                return <Tag>{status}</Tag>;
            }
        },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space>
                    <Button 
                        icon={<EyeOutlined />} 
                        type="default" 
                        size="small" 
                        onClick={() => onViewDetail(record)}
                    >
                        详情
                    </Button>
                    {record.status === 0 && (
                        <Button 
                            type="primary" 
                            size="small" 
                            onClick={() => onHandleClick(record)}
                        >
                            处理
                        </Button>
                    )}
                    {record.status !== 0 && (
                        <Tag color="default">已处理</Tag>
                    )}
                </Space>
            ),
        },
    ];

    return (
        <Card title="维权管理" extra={
            <Select
                placeholder="筛选状态"
                style={{ width: 150 }}
                allowClear
                onChange={setStatusFilter}
            >
                <Option value="0">待处理</Option>
                <Option value="1">已通过</Option>
                <Option value="2">已驳回</Option>
            </Select>
        }>
            <Table
                rowKey="id"
                columns={columns}
                dataSource={data}
                pagination={pagination}
                loading={loading}
                onChange={handleTableChange}
            />

            <Modal
                title="处理举报"
                open={isHandleModalVisible}
                onOk={submitHandle}
                onCancel={() => setIsHandleModalVisible(false)}
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="status" label="处理结果" rules={[{ required: true }]}>
                        <Select>
                            <Option value={1}>通过 (举报属实)</Option>
                            <Option value={2}>驳回 (无违规/证据不足)</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item name="reply" label="回复/备注" rules={[{ required: true, message: '请输入回复内容' }]}>
                        <Input.TextArea rows={4} placeholder="请输入处理意见，将展示给用户" />
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="举报详情"
                open={isDetailModalVisible}
                onCancel={() => setIsDetailModalVisible(false)}
                footer={[
                    <Button key="close" onClick={() => setIsDetailModalVisible(false)}>
                        关闭
                    </Button>,
                    currentReport?.status === 0 && (
                        <Button 
                            key="handle" 
                            type="primary" 
                            onClick={() => {
                                setIsDetailModalVisible(false);
                                onHandleClick(currentReport);
                            }}
                        >
                            去处理
                        </Button>
                    )
                ]}
                width={700}
            >
                {currentReport && (
                    <div style={{ padding: '10px 0' }}>
                        <div style={{ marginBottom: 24 }}>
                            <div style={{ fontWeight: 'bold', marginBottom: 8 }}>基本信息</div>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                <div><span style={{ color: '#666' }}>举报 ID：</span>{currentReport.id}</div>
                                <div><span style={{ color: '#666' }}>举报人 ID：</span>{currentReport.reporterId}</div>
                                <div>
                                    <span style={{ color: '#666' }}>对象类型：</span>
                                    <Tag color="blue">{currentReport.targetType}</Tag>
                                </div>
                                <div>
                                    <span style={{ color: '#666' }}>对象 ID：</span>
                                    {currentReport.targetType === 'PRODUCT' ? (
                                        <a href={`/goods/${currentReport.targetId}`} target="_blank" rel="noreferrer">
                                            {currentReport.targetId} (点击查看商品)
                                        </a>
                                    ) : currentReport.targetId}
                                </div>
                                <div><span style={{ color: '#666' }}>提交时间：</span>{new Date(currentReport.createdAt).toLocaleString()}</div>
                                <div>
                                    <span style={{ color: '#666' }}>当前状态：</span>
                                    {currentReport.status === 0 && <Tag color="orange">待处理</Tag>}
                                    {currentReport.status === 1 && <Tag color="green">已通过</Tag>}
                                    {currentReport.status === 2 && <Tag color="red">已驳回</Tag>}
                                </div>
                            </div>
                        </div>

                        <div style={{ marginBottom: 24 }}>
                            <div style={{ fontWeight: 'bold', marginBottom: 8 }}>举报原因</div>
                            <Card size="small" style={{ backgroundColor: '#f9f9f9' }}>
                                {currentReport.reason}
                            </Card>
                        </div>

                        <div style={{ marginBottom: 24 }}>
                            <div style={{ fontWeight: 'bold', marginBottom: 8 }}>证据图片</div>
                            <Space wrap size={12}>
                                {currentReport.evidenceImages ? (
                                    currentReport.evidenceImages.split(',').map((url, index) => (
                                        <Image
                                            key={index}
                                            src={url}
                                            width={120}
                                            height={120}
                                            style={{ objectFit: 'cover', borderRadius: '4px' }}
                                        />
                                    ))
                                ) : (
                                    <span style={{ color: '#999' }}>暂无图片证据</span>
                                )}
                            </Space>
                        </div>

                        {currentReport.status !== 0 && (
                            <div style={{ marginBottom: 0 }}>
                                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>管理员处理信息</div>
                                <Card size="small" style={{ backgroundColor: '#f6ffed', borderColor: '#b7eb8f' }}>
                                    <div style={{ marginBottom: 8 }}>
                                        <span style={{ color: '#666' }}>处理结果：</span>
                                        {currentReport.status === 1 ? <Tag color="green">通过</Tag> : <Tag color="red">驳回</Tag>}
                                    </div>
                                    <div>
                                        <span style={{ color: '#666' }}>管理员回复：</span>
                                        <span style={{ whiteSpace: 'pre-wrap' }}>{currentReport.adminReply || '无备注'}</span>
                                    </div>
                                </Card>
                            </div>
                        )}
                    </div>
                )}
            </Modal>
        </Card>
    );
};

export default ReportManagement;
