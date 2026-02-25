import { create } from 'zustand';

const useUserStore = create((set) => ({
    userInfo: JSON.parse(localStorage.getItem('user')) || null,
    token: localStorage.getItem('token') || '',

    // 登录成功后调用
    setLoginInfo: (user, token) => {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        set({ userInfo: user, token: token });
    },

    // 退出登录
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        set({ userInfo: null, token: '' });
    }
}));

export default useUserStore;