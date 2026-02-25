import React, { useState } from 'react';
import { Layout, Form, Input, Select, InputNumber, Button, Upload, message, Card, Steps, Divider } from 'antd';
import { UploadOutlined, InboxOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import request from '../../api/request';
import { uploadFile } from '../../api/file';
import './index.css';

const { Header, Content, Footer } = Layout;
const { Option } = Select;
const { Dragger } = Upload;
const { TextArea } = Input;

const PublishGoods = () => {
    const navigate = useNavigate();
    const { id } = useParams();
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [fileList, setFileList] = useState([]);
    const [categories, setCategories] = useState([]);
    const [activities, setActivities] = useState([]);

    React.useEffect(() => {
        fetchCategories();
        fetchActivities();
        if (id) {
            fetchProductDetail();
        }
    }, [id]);

    const fetchProductDetail = async () => {
        try {
            const data = await request.get(`/product/${id}`);
            // 处理图片列表
            if (data.imageUrls) {
                const images = data.imageUrls.split(',').map((url, index) => ({
                    uid: `-${index}`,
                    name: `image-${index}`,
                    status: 'done',
                    url: url,
                    response: { url } // 保持与其他上传逻辑一致
                }));
                setFileList(images);
            }
            // 填充表单
            form.setFieldsValue({
                ...data,
                // 如果分类ID是数字/字符串不匹配，这里不需要特殊处理，antd通常能处理
            });
        } catch (error) {
            console.error("加载商品详情失败", error);
            message.error("加载商品详情失败");
        }
    };

    const fetchCategories = async () => {
        try {
            const data = await request.get('/category/list');
            setCategories(data || []);
        } catch (error) {
            console.error("加载分类失败", error);
        }
    };

    const fetchActivities = async () => {
        try {
            const data = await request.get('/activityTopic/list/active');
            setActivities(data || []);
        } catch (error) {
            console.error("加载活动失败", error);
        }
    };

    const onFinish = async (values) => {
        setLoading(true);
        try {
            // 构建提交数据
            // 注意：实际项目中可能需要先上传图片获取URL，或者使用FormData直接提交
            // 这里假设后端接收JSON，图片字段为逗号分隔的URL字符串
            // 如果后端需要FormData，请根据实际情况修改

            // 模拟图片上传后的处理（因为没有真实后端上传接口，这里假设直接把文件名当URL）
            // 在真实场景中，Dragger的action应该指向上传接口，或者在这里手动上传
            // 过滤出上传成功的文件
            const imageUrls = fileList
                .filter(file => file.status === 'done')
                .map(file => file.response?.url || file.url || file.name)
                .join(',');

            const submitData = {
                ...values,
                imageUrls: imageUrls,
                // 默认状态
                status: 1 // 假设1为上架
            };

            console.log('Success:', submitData);

            // 发送请求
            if (id) {
                await request.post('/product/update', { ...submitData, id });
                message.success('修改成功！');
            } else {
                await request.post('/product/publish', submitData);
                message.success('发布成功！');
            }

            setTimeout(() => {
                navigate(id ? '/user' : '/'); // 编辑完回用户中心，发布完回首页
            }, 1500);
        } catch (error) {
            console.error('Failed:', error);
            message.error('操作失败，请重试');
            setLoading(false);
        } finally {
            setLoading(false);
        }
    };

    const uploadProps = {
        name: 'file',
        multiple: true,
        fileList: fileList,
        customRequest: async ({ file, onSuccess, onError }) => {
            try {
                const url = await uploadFile(file);
                // Ant Design Upload expects a response object in onSuccess
                onSuccess({ url }, file);
            } catch (error) {
                console.error("Upload error:", error);
                onError(error);
            }
        },
        onChange(info) {
            const { status } = info.file;
            if (status !== 'uploading') {
                console.log(info.file, info.fileList);
            }
            if (status === 'done') {
                message.success(`${info.file.name} 上传成功.`);
                setFileList(info.fileList);
            } else if (status === 'error') {
                message.error(`${info.file.name} 上传失败.`);
            }
            // Ensure fileList is updated for all states to show progress/handling
            setFileList(info.fileList);
        },
        onDrop(e) {
            console.log('Dropped files', e.dataTransfer.files);
        },
    };

    return (
        <Layout className="publish-layout">
            <Header className="publish-header">
                <div className="header-content">
                    <div className="logo" onClick={() => navigate('/')}>
                        <span className="logo-icon">🎓</span>
                        <span className="logo-text">校园跳蚤市场</span>
                    </div>
                    <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>
                        返回
                    </Button>
                </div>
            </Header>

            <Content className="publish-content">
                <div className="publish-container">
                    <Card className="publish-card" title={id ? "编辑商品信息" : "发布闲置宝贝"} variant="borderless">
                        {!id && (
                            <>
                                <Steps
                                    current={0}
                                    className="publish-steps"
                                    items={[
                                        { title: '填写信息', description: '完善商品详情' },
                                        { title: '上传图片', description: '展示商品实拍' },
                                        { title: '发布成功', description: '等待买家联系' },
                                    ]}
                                />
                                <Divider />
                            </>
                        )}

                        <Form
                            form={form}
                            name="publish"
                            layout="vertical"
                            onFinish={onFinish}
                            initialValues={{
                                conditionLevel: 9,
                                tradeType: 'offline'
                            }}
                            className="publish-form"
                        >
                            <Form.Item
                                label="商品标题"
                                name="title"
                                rules={[{ required: true, message: '请输入商品标题，例如：99新 iPad Air 5' }]}
                            >
                                <Input placeholder="品牌型号 + 关键特点，让买家一眼看中" size="large" />
                            </Form.Item>

                            <Form.Item
                                label="商品描述"
                                name="description"
                                rules={[{ required: true, message: '请详细描述商品的细节' }]}
                            >
                                <TextArea
                                    rows={4}
                                    placeholder="描述一下商品的入手渠道、转手原因、新旧程度和使用感受吧..."
                                    showCount
                                    maxLength={500}
                                />
                            </Form.Item>

                            <div className="form-row">
                                <Form.Item
                                    label="商品分类"
                                    name="categoryId"
                                    rules={[{ required: true, message: '请选择分类' }]}
                                    style={{ width: '48%' }}
                                >
                                    <Select placeholder="选择合适的分类" size="large">
                                        {categories.map(cat => (
                                            <Option key={cat.id} value={cat.id}>{cat.name}</Option>
                                        ))}
                                    </Select>
                                </Form.Item>

                                <Form.Item
                                    label="参与活动"
                                    name="activityId"
                                    style={{ width: '48%' }}
                                    tooltip="选择参与当前进行的特色活动，增加曝光率"
                                >
                                    <Select placeholder="选择参加的活动（选填）" size="large" allowClear>
                                        {activities.map(act => (
                                            <Option key={act.id} value={act.id}>{act.title}</Option>
                                        ))}
                                    </Select>
                                </Form.Item>
                            </div>

                            <div className="form-row">
                                <Form.Item
                                    label="新旧程度"
                                    name="conditionLevel"
                                    rules={[{ required: true, message: '请选择新旧程度' }]}
                                    style={{ width: '48%' }}
                                >
                                    <Select placeholder="选择新旧程度" size="large">
                                        <Option value={10}>全新</Option>
                                        <Option value={9}>9成新</Option>
                                        <Option value={8}>8成新</Option>
                                        <Option value={7}>7成新</Option>
                                        <Option value={6}>6成新</Option>
                                        <Option value={5}>5成新及以下</Option>
                                    </Select>
                                </Form.Item>
                            </div>

                            <div className="form-row">
                                <Form.Item
                                    label="出售价格 (元)"
                                    name="price"
                                    rules={[{ required: true, message: '请输入价格' }]}
                                    style={{ width: '48%' }}
                                >
                                    <InputNumber
                                        prefix="￥"
                                        style={{ width: '100%' }}
                                        size="large"
                                        min={0}
                                        precision={2}
                                    />
                                </Form.Item>

                                <Form.Item
                                    label="原价 (元)"
                                    name="originalPrice"
                                    style={{ width: '48%' }}
                                >
                                    <InputNumber
                                        prefix="￥"
                                        style={{ width: '100%' }}
                                        size="large"
                                        min={0}
                                        precision={2}
                                        placeholder="选填"
                                    />
                                </Form.Item>
                            </div>

                            <div className="form-row">
                                <Form.Item
                                    label="交易方式"
                                    name="tradeType"
                                    rules={[{ required: true, message: '请选择交易方式' }]}
                                    style={{ width: '48%' }}
                                >
                                    <Select size="large">
                                        <Option value="offline">线下自提</Option>
                                        <Option value="online">快递邮寄</Option>
                                    </Select>
                                </Form.Item>

                                <Form.Item
                                    label="交易地点/发货地"
                                    name="tradeLocation"
                                    rules={[{ required: true, message: '请输入交易地点' }]}
                                    style={{ width: '48%' }}
                                >
                                    <Input placeholder="例如：图书馆门口 / 快递点" size="large" />
                                </Form.Item>
                            </div>

                            <Form.Item label="商品图片">
                                <Dragger {...uploadProps} style={{ background: '#fafafa', border: '1px dashed #d9d9d9' }}>
                                    <p className="ant-upload-drag-icon">
                                        <InboxOutlined style={{ color: '#1890ff' }} />
                                    </p>
                                    <p className="ant-upload-text">点击或拖拽图片到此处上传</p>
                                    <p className="ant-upload-hint">
                                        支持多张图片上传，首张图片将作为商品封面
                                    </p>
                                </Dragger>
                            </Form.Item>

                            <Form.Item style={{ marginTop: 32 }}>
                                <Button type="primary" htmlType="submit" size="large" block loading={loading} className="submit-btn">
                                    {id ? '确认修改' : '立即发布'}
                                </Button>
                            </Form.Item>
                        </Form>
                    </Card>
                </div>
            </Content>

            <Footer className="publish-footer">
                <p>©2025 校园跳蚤市场 - 毕业设计项目</p>
            </Footer>
        </Layout>
    );
};

export default PublishGoods;
