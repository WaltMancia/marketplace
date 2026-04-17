import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
    Plus, Pencil, Trash2, Package,
    TrendingUp, ShoppingBag, Eye,
} from 'lucide-react';
import {
    getProductsService,
    deleteProductService,
} from '../../services/product.service.js';
import useAuthStore from '../../store/authStore.js';
import Button from '../../components/ui/Button.jsx';
import Badge from '../../components/ui/Badge.jsx';
import Spinner from '../../components/ui/Spinner.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import Card from '../../components/ui/Card.jsx';
import toast from 'react-hot-toast';

const SellerProductsPage = () => {
    const { user } = useAuthStore();
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [deletingId, setDeletingId] = useState(null);
    const [stats, setStats] = useState({
        total: 0, active: 0, outOfStock: 0,
    });

    const fetchProducts = useCallback(async () => {
        setLoading(true);
        try {
            const data = await getProductsService({
                sellerId: user?.id,
                size: 50,
            });
            const productList = data.content || [];
            setProducts(productList);
            setStats({
                total: productList.length,
                active: productList.filter((p) => p.status === 'ACTIVE').length,
                outOfStock: productList.filter((p) => p.stock === 0).length,
            });
        } catch {
            toast.error('Error al cargar tus productos');
        } finally {
            setLoading(false);
        }
    }, [user]);

    useEffect(() => { fetchProducts(); }, [fetchProducts]);

    const handleDelete = async (id, name) => {
        if (!confirm(`¿Eliminar "${name}"?`)) return;
        setDeletingId(id);
        try {
            await deleteProductService(id);
            toast.success('Producto eliminado');
            fetchProducts();
        } catch {
            toast.error('Error al eliminar el producto');
        } finally {
            setDeletingId(null);
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Mis Productos</h1>
                    <p className="text-sm text-gray-500 mt-0.5">
                        Gestiona tu catálogo de productos
                    </p>
                </div>
                <Link to="/mis-productos/nuevo">
                    <Button>
                        <Plus size={16} />
                        Nuevo producto
                    </Button>
                </Link>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-3 gap-4">
                {[
                    {
                        label: 'Total productos',
                        value: stats.total,
                        icon: ShoppingBag,
                        color: 'text-blue-600',
                        bg: 'bg-blue-50',
                    },
                    {
                        label: 'Activos',
                        value: stats.active,
                        icon: TrendingUp,
                        color: 'text-emerald-600',
                        bg: 'bg-emerald-50',
                    },
                    {
                        label: 'Sin stock',
                        value: stats.outOfStock,
                        icon: Package,
                        color: 'text-amber-600',
                        bg: 'bg-amber-50',
                    },
                ].map(({ label, value, icon: Icon, color, bg }) => (
                    <Card key={label} className="p-5">
                        <div className="flex items-center gap-3">
                            <div className={`p-2.5 rounded-xl ${bg}`}>
                                <Icon size={18} className={color} />
                            </div>
                            <div>
                                <p className="text-2xl font-bold text-gray-900">{value}</p>
                                <p className="text-xs text-gray-500">{label}</p>
                            </div>
                        </div>
                    </Card>
                ))}
            </div>

            {/* Tabla de productos */}
            {loading ? (
                <div className="flex justify-center py-20">
                    <Spinner size="lg" />
                </div>
            ) : products.length === 0 ? (
                <EmptyState
                    icon="📦"
                    title="No tienes productos"
                    description="Crea tu primer producto y empieza a vender"
                    action={
                        <Link to="/mis-productos/nuevo">
                            <Button>
                                <Plus size={16} />
                                Crear producto
                            </Button>
                        </Link>
                    }
                />
            ) : (
                <Card className="overflow-hidden">
                    <table className="w-full text-sm">
                        <thead className="bg-gray-50 border-b border-gray-100">
                            <tr>
                                <th className="text-left px-5 py-3.5 text-gray-500
                  font-medium">
                                    Producto
                                </th>
                                <th className="text-left px-5 py-3.5 text-gray-500
                  font-medium">
                                    Precio
                                </th>
                                <th className="text-left px-5 py-3.5 text-gray-500
                  font-medium">
                                    Stock
                                </th>
                                <th className="text-left px-5 py-3.5 text-gray-500
                  font-medium">
                                    Estado
                                </th>
                                <th className="px-5 py-3.5" />
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-50">
                            {products.map((product) => (
                                <tr key={product.id}
                                    className="hover:bg-gray-50 transition-colors">
                                    <td className="px-5 py-3.5">
                                        <div className="flex items-center gap-3">
                                            <div className="w-10 h-10 bg-gray-100 rounded-xl
                        flex items-center justify-center flex-shrink-0
                        overflow-hidden">
                                                {product.imageUrl ? (
                                                    <img
                                                        src={product.imageUrl}
                                                        alt={product.name}
                                                        className="w-full h-full object-cover"
                                                    />
                                                ) : (
                                                    <Package size={16} className="text-gray-400" />
                                                )}
                                            </div>
                                            <div>
                                                <p className="font-medium text-gray-900 truncate
                          max-w-[200px]">
                                                    {product.name}
                                                </p>
                                                <p className="text-xs text-gray-400">
                                                    {product.categoryName}
                                                </p>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-5 py-3.5 font-medium text-gray-900">
                                        ${Number(product.price).toFixed(2)}
                                    </td>
                                    <td className="px-5 py-3.5">
                                        <Badge
                                            variant={
                                                product.stock === 0 ? 'danger'
                                                    : product.stock <= 5 ? 'warning'
                                                        : 'success'
                                            }>
                                            {product.stock} uds
                                        </Badge>
                                    </td>
                                    <td className="px-5 py-3.5">
                                        <Badge
                                            variant={
                                                product.status === 'ACTIVE' ? 'success' : 'default'
                                            }>
                                            {product.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}
                                        </Badge>
                                    </td>
                                    <td className="px-5 py-3.5">
                                        <div className="flex items-center justify-end gap-1">
                                            <Link to={`/productos/${product.id}`}>
                                                <button className="p-1.5 text-gray-400
                          hover:text-blue-600 hover:bg-blue-50
                          rounded-lg transition-colors">
                                                    <Eye size={15} />
                                                </button>
                                            </Link>
                                            <Link to={`/mis-productos/editar/${product.id}`}>
                                                <button className="p-1.5 text-gray-400
                          hover:text-gray-700 hover:bg-gray-100
                          rounded-lg transition-colors">
                                                    <Pencil size={15} />
                                                </button>
                                            </Link>
                                            <button
                                                onClick={() => handleDelete(product.id, product.name)}
                                                disabled={deletingId === product.id}
                                                className="p-1.5 text-gray-400 hover:text-red-600
                          hover:bg-red-50 rounded-lg transition-colors
                          disabled:opacity-40">
                                                <Trash2 size={15} />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </Card>
            )}
        </div>
    );
};

export default SellerProductsPage;