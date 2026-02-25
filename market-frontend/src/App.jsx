import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Home from './pages/home/index.jsx'
import Login from './pages/login/index.jsx'
import Register from './pages/register/index.jsx'
import Publish from './pages/publish/index.jsx'
import GoodsDetail from './pages/goods/detail.jsx'
import PaymentResult from './pages/payment/result.jsx'
import UserCenter from './pages/user/index.jsx'
import Cart from './pages/cart/index.jsx'
import AdminLayout from './pages/admin/layout.jsx'
import UserManagement from './pages/admin/users.jsx'
import ProductManagement from './pages/admin/products.jsx'
import OrderManagement from './pages/admin/orders.jsx'
import CategoryManagement from './pages/admin/categories.jsx'
import ReportManagement from './pages/admin/reports.jsx'
import ActivityManagement from './pages/admin/activities.jsx'
import MessagePage from './pages/message/index.jsx'
import AdminDashboard from './pages/admin/dashboard.jsx'
import './App.css'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/publish" element={<Publish />} />
        <Route path="/publish/:id" element={<Publish />} />
        <Route path="/goods/:id" element={<GoodsDetail />} />
        <Route path="/payment/result" element={<PaymentResult />} />
        <Route path="/user" element={<UserCenter />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/message" element={<MessagePage />} />

        {/* 管理后台路由 */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="users" element={<UserManagement />} />
          <Route path="products" element={<ProductManagement />} />
          <Route path="categories" element={<CategoryManagement />} />
          <Route path="orders" element={<OrderManagement />} />
          <Route path="reports" element={<ReportManagement />} />
          <Route path="activities" element={<ActivityManagement />} />
        </Route>
      </Routes>
    </Router>
  )
}

export default App
