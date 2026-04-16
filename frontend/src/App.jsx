import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import LoginPage from './pages/auth/LoginPage.jsx';
import RegisterPage from './pages/auth/RegisterPage.jsx';
import HomePage from './pages/HomePage.jsx';
import ProductsPage from './pages/products/ProductsPage.jsx';
import ProductDetailPage from './pages/products/ProductDetailPage.jsx';
import CartPage from './pages/cart/CartPage.jsx';

// Placeholders para el Paso 9
const OrdersPage = () => (
  <div className="text-center py-20 text-gray-400">
    Mis Órdenes — Paso 9
  </div>
);
const OrderDetailPage = () => (
  <div className="text-center py-20 text-gray-400">
    Detalle de Orden — Paso 9
  </div>
);

const App = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/registro" element={<RegisterPage />} />

    <Route element={<MainLayout />}>
      <Route path="/" element={<HomePage />} />
      <Route path="/productos" element={<ProductsPage />} />
      <Route path="/productos/:id" element={<ProductDetailPage />} />

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
    </Route>

    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
);

export default App;