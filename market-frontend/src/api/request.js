import axios from 'axios';
import { message } from 'antd'; // 引入 Ant Design 的全局提示组件

/**
 * 封装 Axios 实例
 */
const request = axios.create({
    // 这里的 /api 对应后端 application.yml 中的 context-path
    baseURL: 'http://localhost:8080/api',
    timeout: 8000, // 请求超时时间
});

/**
 * 请求拦截器：在发送请求前做些什么
 */
request.interceptors.request.use(
    (config) => {
        // 1. 从本地存储中获取登录时保存的 Token
        const token = localStorage.getItem('token');

        // 2. 如果 Token 存在，则将其放入请求头的 Authorization 中
        // 格式采用标准的 Bearer 模式
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

/**
 * 响应拦截器：在获取结果后做些什么
 */
request.interceptors.response.use(
    (response) => {
        // response.data 就是后端返回的 Result<T> 对象
        const res = response.data;

        // 1. 如果后端 code 是 200，说明业务成功，直接返回内部的 data
        if (res.code === 200) {
            return res.data;
        }

        // 2. 如果 code 是 401，通常代表 Token 过期或未登录
        if (res.code === 401) {
            localStorage.removeItem('token'); // 清理旧 Token
            localStorage.removeItem('userInfo');
            window.location.href = '/login';   // 跳转到登录页
            return Promise.reject(new Error(res.message || 'Unauthorized'));
        }

        // 3. 其他错误（如 500），弹出后端传来的错误信息
        message.error(res.message || '系统繁忙，请稍后再试');
        return Promise.reject(new Error(res.message || 'Error'));
    },
    (error) => {
        // 处理 HTTP 状态码错误
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            window.location.href = '/login';
            return Promise.reject(error);
        }
        message.error('网络请求失败，请检查后端服务是否开启');
        return Promise.reject(error);
    }
);

export default request;