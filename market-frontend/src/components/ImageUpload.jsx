import React, { useState, useEffect } from 'react';
import { Upload, message } from 'antd';
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import { uploadFile } from '../api/file';

/**
 * ImageUpload Component
 * 
 * @param {string} value - 当前图片的URL (受控属性)
 * @param {function} onChange - 图片上传成功后的回调，参数为新图片的URL
 * @param {number} maxCount - 最大上传数量 (目前仅只支持单图模式的UI，但代码结构预留扩展)
 */
const ImageUpload = ({ value, onChange }) => {
    const [loading, setLoading] = useState(false);
    const [imageUrl, setImageUrl] = useState(value);

    // 监听外部 value 变化
    useEffect(() => {
        setImageUrl(value);
    }, [value]);

    const handleChange = (info) => {
        if (info.file.status === 'uploading') {
            setLoading(true);
            return;
        }
        if (info.file.status === 'done') {
            // Get this url from response in real world.
            setLoading(false);
            // 这里已经在 customRequest 中处理了 onChange
        }
    };

    const customRequest = async ({ file, onSuccess, onError }) => {
        try {
            setLoading(true);
            const url = await uploadFile(file);
            setImageUrl(url);
            setLoading(false);
            onSuccess({ url }, file);
            if (onChange) {
                onChange(url);
            }
            message.success('上传成功');
        } catch (error) {
            setLoading(false);
            onError(error);
            console.error('上传失败', error);
            message.error('上传失败，请重试');
        }
    };

    const beforeUpload = (file) => {
        const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif';
        if (!isJpgOrPng) {
            message.error('只能上传 JPG/PNG/GIF 文件!');
        }
        const isLt2M = file.size / 1024 / 1024 < 5; // 限制 5MB
        if (!isLt2M) {
            message.error('图片必须小于 5MB!');
        }
        return isJpgOrPng && isLt2M;
    };

    const uploadButton = (
        <div>
            {loading ? <LoadingOutlined /> : <PlusOutlined />}
            <div style={{ marginTop: 8 }}>上传图片</div>
        </div>
    );

    return (
        <Upload
            name="file"
            listType="picture-card"
            className="avatar-uploader"
            showUploadList={false}
            customRequest={customRequest}
            beforeUpload={beforeUpload}
            onChange={handleChange}
        >
            {imageUrl ? <img src={imageUrl} alt="avatar" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : uploadButton}
        </Upload>
    );
};

export default ImageUpload;
