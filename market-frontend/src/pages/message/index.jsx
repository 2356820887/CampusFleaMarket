import React, { useEffect, useState, useRef } from 'react';
import { Layout, Avatar, Input, Button, Empty, Dropdown, message, Spin, Tooltip, Tag } from 'antd';
import { SendOutlined, UserOutlined, SmileOutlined, DownOutlined, ArrowLeftOutlined, MoreOutlined, SyncOutlined } from '@ant-design/icons';
import { useSearchParams, useNavigate } from 'react-router-dom';
import request from '../../api/request';
import './index.css';

const { TextArea } = Input;

const MessagePage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [contacts, setContacts] = useState([]);
    const [activeContact, setActiveContact] = useState(null);
    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState('');
    const [loading, setLoading] = useState(false);
    const [ws, setWs] = useState(null);
    const [user, setUser] = useState(null);
    const messagesEndRef = useRef(null);

    // Quick replies
    const quickReplies = [
        "你好，宝贝还在吗？",
        "可以小刀吗？",
        "什么时候可以发货？",
        "成色怎么样，有瑕疵吗？",
        "我想要这个，怎么交易？"
    ];

    useEffect(() => {
        const userInfo = localStorage.getItem('userInfo');
        if (userInfo) {
            setUser(JSON.parse(userInfo));
            initWebSocket();
        }
    }, []);

    useEffect(() => {
        fetchContacts();
    }, []);

    useEffect(() => {
        if (activeContact) {
            fetchHistory(activeContact.id);
        }
    }, [activeContact]);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const initWebSocket = () => {
        const token = localStorage.getItem('token');
        if (!token) return;
        // WebSocket URL
        // Backend context-path is /api, so we need to include it.
        const wsUrl = `ws://localhost:8080/api/ws/chat?token=${token}`;
        const socket = new WebSocket(wsUrl);

        socket.onopen = () => {
            console.log('WS Connected');
        };

        socket.onmessage = (event) => {
            try {
                const msg = JSON.parse(event.data);
                if (msg.type === 'message') {
                    const chatMsg = msg.data;
                    handleReceiveMessage(chatMsg);
                }
            } catch (e) {
                console.error("WS Parse Error", e);
            }
        };

        socket.onclose = () => {
            console.log('WS Disconnected');
        };

        setWs(socket);
        return () => socket.close();
    };

    const handleReceiveMessage = (chatMsg) => {
        setMessages(prev => {
            return [...prev, chatMsg];
        });
    };

    const fetchContacts = async () => {
        try {
            const res = await request.get('/chatMessage/contacts');
            const contactList = res || [];

            // Check 'to' param
            const toId = searchParams.get('to');
            if (toId) {
                const existing = contactList.find(c => c.id === parseInt(toId));
                if (existing) {
                    setActiveContact(existing);
                } else {
                    try {
                        const newUser = await request.get(`/user/info/${toId}`);
                        if (newUser) {
                            contactList.unshift(newUser);
                            setActiveContact(newUser);
                        }
                    } catch (e) {
                        console.error("User not found");
                    }
                }
            } else if (contactList.length > 0) {
                setActiveContact(contactList[0]);
            }
            setContacts(contactList);
        } catch (error) {
            console.error("Load contacts failed", error);
        }
    };

    const fetchHistory = async (otherId) => {
        setLoading(true);
        try {
            const res = await request.get(`/chatMessage/history?otherId=${otherId}`);
            setMessages(res || []);
        } catch (error) {
            console.error("Load history failed", error);
        } finally {
            setLoading(false);
        }
    };

    const handleSend = () => {
        if (!inputValue.trim() || !activeContact || !ws) return;

        const content = inputValue.trim();
        const payload = {
            to: activeContact.id,
            content: content
        };

        try {
            ws.send(JSON.stringify(payload));

            const newMsg = {
                id: Date.now(),
                senderId: user.id,
                receiverId: activeContact.id,
                content: content,
                createdAt: new Date().toISOString()
            };
            setMessages([...messages, newMsg]);
            setInputValue('');
        } catch (error) {
            message.error("发送失败");
        }
    };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    return (
        <div className="message-container">
            <div className="message-glass-wrapper">
                <div className="contact-sidebar">
                    <div className="sidebar-header">
                        <Button type="text" icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)} className="back-btn" />
                        <span className="sidebar-title">消息列表</span>
                        <div style={{ flex: 1 }}></div>
                        <Tooltip title="刷新列表">
                            <Button type="text" icon={<SyncOutlined />} onClick={fetchContacts} size="small" />
                        </Tooltip>
                    </div>
                    <div className="contact-list">
                        {contacts.map(c => (
                            <div
                                key={c.id}
                                className={`contact-item ${activeContact?.id === c.id ? 'active' : ''}`}
                                onClick={() => setActiveContact(c)}
                            >
                                <Avatar size={44} src={c.avatar} icon={<UserOutlined />} className="contact-avatar" />
                                <div className="contact-info">
                                    <div className="contact-name">{c.nickname || c.username}</div>
                                    <div className="contact-last-msg">点击查看消息记录</div>
                                </div>
                            </div>
                        ))}
                        {contacts.length === 0 && <Empty description="暂无联系人" image={Empty.PRESENTED_IMAGE_SIMPLE} className="sidebar-empty" />}
                    </div>
                </div>

                <div className="chat-main">
                    {activeContact ? (
                        <>
                            <div className="chat-header">
                                <div className="chat-user-info">
                                    <Avatar size="small" src={activeContact.avatar} icon={<UserOutlined />} />
                                    <span className="chat-username">{activeContact.nickname || activeContact.username}</span>
                                    <Tag color="green" style={{ marginLeft: 8, fontSize: 10, lineHeight: '18px', border: 'none' }}>在线</Tag>
                                </div>
                                <Button type="text" icon={<MoreOutlined />} />
                            </div>

                            <div className="chat-messages">
                                {loading ? <div className="loading-container"><Spin /></div> : messages.map((msg, index) => {
                                    const isMe = msg.senderId === user?.id;
                                    return (
                                        <div key={msg.id || index} className={`message-bubble ${isMe ? 'me' : 'other'}`}>
                                            <Avatar className="message-avatar" src={isMe ? user?.avatar : activeContact.avatar} icon={<UserOutlined />} size={36} />
                                            <div className="message-content-wrapper">
                                                <div className="message-content">
                                                    {msg.content}
                                                </div>
                                                <div className="message-time">
                                                    {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                                <div ref={messagesEndRef} />
                            </div>

                            <div className="chat-footer">
                                <div className="chat-tools">
                                    <Dropdown
                                        menu={{
                                            items: quickReplies.map((text, idx) => ({
                                                key: idx,
                                                label: text,
                                                onClick: () => setInputValue(text)
                                            }))
                                        }}
                                        trigger={['click']}
                                        placement="topLeft"
                                    >
                                        <Button size="small" type="text" icon={<DownOutlined />} style={{ color: '#666' }}>常用语</Button>
                                    </Dropdown>
                                </div>
                                <div className="chat-input-area">
                                    <TextArea
                                        value={inputValue}
                                        onChange={e => setInputValue(e.target.value)}
                                        onKeyPress={handleKeyPress}
                                        autoSize={{ minRows: 1, maxRows: 4 }}
                                        placeholder="输入消息... (Enter发送)"
                                        className="chat-textarea"
                                        bordered={false}
                                    />
                                    <Tooltip title="发送">
                                        <Button type="primary" shape="circle" icon={<SendOutlined />} onClick={handleSend} size="large" className="send-btn" />
                                    </Tooltip>
                                </div>
                            </div>
                        </>
                    ) : (
                        <div className="empty-chat">
                            <div className="empty-chat-icon-wrapper">
                                <SmileOutlined style={{ fontSize: 64, color: '#1890ff' }} />
                            </div>
                            <h2>开始新的对话</h2>
                            <p>选择左侧联系人，发起聊天</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MessagePage;
