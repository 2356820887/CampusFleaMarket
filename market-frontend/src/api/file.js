import request from './request';

/**
 * 上传文件到 OSS
 * @param {File} file - 要上传的文件对象
 * @returns {Promise<string>} - 返回文件的 URL
 */
export const uploadFile = async (file) => {
    // 构建 FormData 对象
    const formData = new FormData();
    formData.append('file', file);

    // 发送 POST 请求到 /upload 接口
    // 注意：request.js 中已经配置了 baseURL，所以这里只需要写相对路径
    // 如果后端 Controller 是 @PostMapping("/upload")，则路径就是 /upload
    // request.js 的响应拦截器已经处理了解包，所以直接返回 data (即 URL)
    return await request.post('/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
};
