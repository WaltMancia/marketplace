import { Link, NavLink } from 'react-router-dom';
import {
    ShoppingCart, Store, Package,
    LogOut, User, LayoutDashboard, Plus,
} from 'lucide-react';
import useAuthStore from '../../store/authStore.js';
import useCartStore from '../../store/cartStore.js';
import useAuth from '../../hooks/useAuth.js';

const Navbar = () => {
    const { user } = useAuthStore();
    const { itemCount } = useCartStore();
    const { handleLogout } = useAuth();

    return (
        <nav className="bg-white border-b border-gray-100 sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4 py-3.5 flex items-center
        justify-between">

                {/* Logo */}
                <Link to="/"
                    className="flex items-center gap-2 font-bold text-gray-900 text-lg">
                    <div className="w-8 h-8 bg-gray-900 rounded-xl flex items-center
            justify-center">
                        <Store size={16} className="text-white" />
                    </div>
                    Marketplace
                </Link>

                {/* Links centrales */}
                <div className="hidden md:flex items-center gap-1">
                    <NavLink to="/productos"
                        className={({ isActive }) =>
                            `px-4 py-2 rounded-xl text-sm font-medium transition-colors ${isActive
                                ? 'bg-gray-100 text-gray-900'
                                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                            }`
                        }>
                        Productos
                    </NavLink>
                </div>

                {/* Acciones derecha */}
                <div className="flex items-center gap-2">
                    {user ? (
                        <>
                            {/* Carrito */}
                            <Link to="/carrito"
                                className="relative p-2 text-gray-600 hover:text-gray-900
                  hover:bg-gray-50 rounded-xl transition-colors">
                                <ShoppingCart size={20} />
                                {itemCount > 0 && (
                                    <span className="absolute -top-0.5 -right-0.5 w-4 h-4
                    bg-gray-900 text-white text-xs rounded-full flex
                    items-center justify-center font-bold">
                                        {itemCount > 9 ? '9+' : itemCount}
                                    </span>
                                )}
                            </Link>

                            {/* Mis órdenes */}
                            <NavLink to="/mis-ordenes"
                                className={({ isActive }) =>
                                    `hidden md:flex items-center gap-1.5 px-3 py-2 rounded-xl
                  text-sm transition-colors ${isActive
                                        ? 'bg-gray-100 text-gray-900'
                                        : 'text-gray-600 hover:bg-gray-50'
                                    }`
                                }>
                                <Package size={16} />
                                Mis Órdenes
                            </NavLink>

                            {/* Seller: crear producto */}
                            {(user.role === 'SELLER' || user.role === 'ADMIN') && (
                                <Link to="/mis-productos/nuevo"
                                    className="hidden md:flex items-center gap-1.5 px-3 py-2
                    bg-gray-900 text-white rounded-xl text-sm hover:bg-gray-700
                    transition-colors">
                                    <Plus size={15} />
                                    Nuevo Producto
                                </Link>
                            )}

                            {/* Admin */}
                            {user.role === 'ADMIN' && (
                                <NavLink to="/admin"
                                    className={({ isActive }) =>
                                        `hidden md:flex items-center gap-1.5 px-3 py-2 rounded-xl
                    text-sm transition-colors ${isActive
                                            ? 'bg-blue-100 text-blue-700'
                                            : 'text-blue-600 hover:bg-blue-50'
                                        }`
                                    }>
                                    <LayoutDashboard size={15} />
                                    Admin
                                </NavLink>
                            )}

                            {/* User info + logout */}
                            <div className="flex items-center gap-2 ml-1 pl-2
                border-l border-gray-100">
                                <div className="hidden md:block text-right">
                                    <p className="text-xs font-semibold text-gray-900 leading-none">
                                        {user.name?.split(' ')[0]}
                                    </p>
                                    <p className="text-xs text-gray-400 mt-0.5 capitalize">
                                        {user.role?.toLowerCase()}
                                    </p>
                                </div>
                                <button
                                    onClick={handleLogout}
                                    className="p-2 text-gray-400 hover:text-red-500
                    hover:bg-red-50 rounded-xl transition-colors"
                                    title="Cerrar sesión">
                                    <LogOut size={18} />
                                </button>
                            </div>
                        </>
                    ) : (
                        <>
                            <Link to="/login"
                                className="px-4 py-2 text-sm text-gray-600
                  hover:text-gray-900 transition-colors">
                                Iniciar sesión
                            </Link>
                            <Link to="/registro"
                                className="px-4 py-2 text-sm bg-gray-900 text-white
                  rounded-xl hover:bg-gray-700 transition-colors">
                                Registrarse
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;