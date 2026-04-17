import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import LoginPage from './pages/auth/LoginPage.jsx';
import RegisterPage from './pages/auth/RegisterPage.jsx';
import HomePage from './pages/HomePage.jsx';
import ProductsPage from './pages/products/ProductsPage.jsx';
import ProductDetailPage from './pages/products/ProductDetailPage.jsx';
import CartPage from './pages/cart/CartPage.jsx';
import OrdersPage from './pages/orders/OrdersPage.jsx';
import OrderDetailPage from './pages/orders/OrderDetailPage.jsx';
import SellerProductsPage from './pages/seller/SellerProductsPage.jsx';
import ProductFormPage from './pages/seller/ProductFormPage.jsx';

const App = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/registro" element={<RegisterPage />} />

    <Route element={<MainLayout />}>
      <Route path="/" element={<HomePage />} />
      <Route path="/productos" element={<ProductsPage />} />
      <Route path="/productos/:id" element={<ProductDetailPage />} />

      {/* Rutas de comprador */}
      <Route path="/carrito" element={
        <ProtectedRoute>
          <CartPage />
        </ProtectedRoute>
      } />
      <Route path="/mis-ordenes" element={
        <ProtectedRoute>
          <OrdersPage />
        </ProtectedRoute>
      } />
      <Route path="/mis-ordenes/:id" element={
        <ProtectedRoute>
          <OrderDetailPage />
        </ProtectedRoute>
      } />

      {/* Rutas de vendedor */}
      <Route path="/mis-productos" element={
        <ProtectedRoute roles={['SELLER', 'ADMIN']}>
          <SellerProductsPage />
        </ProtectedRoute>
      } />
      <Route path="/mis-productos/nuevo" element={
        <ProtectedRoute roles={['SELLER', 'ADMIN']}>
          <ProductFormPage />
        </ProtectedRoute>
      } />
      <Route path="/mis-productos/editar/:id" element={
        <ProtectedRoute roles={['SELLER', 'ADMIN']}>
          <ProductFormPage />
        </ProtectedRoute>
      } />
    </Route>

    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
);

export default App;