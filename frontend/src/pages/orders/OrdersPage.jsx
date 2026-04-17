import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Package, ChevronRight, ShoppingBag } from 'lucide-react';
import { getMyOrdersService } from '../../services/order.service.js';
import Spinner from '../../components/ui/Spinner.jsx';
import Badge from '../../components/ui/Badge.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import Button from '../../components/ui/Button.jsx';
import Card from '../../components/ui/Card.jsx';

const statusConfig = {
    PENDING: { variant: 'warning', label: 'Pendiente' },
    CONFIRMED: { variant: 'info', label: 'Confirmado' },
    PAID: { variant: 'success', label: 'Pagado' },
    SHIPPED: { variant: 'info', label: 'En camino' },
    DELIVERED: { variant: 'success', label: 'Entregado' },
    CANCELLED: { variant: 'danger', label: 'Cancelado' },
};

const OrdersPage = () => {
    const [orders, setOrders] = useState([]);
    const [pagination, setPagination] = useState(null);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            try {
                const data = await getMyOrdersService(page, 10);
                setOrders(data.content);
                setPagination(data);
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, [page]);

    if (loading) return (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
    );

    if (!orders.length) return (
        <EmptyState
            icon="📦"
            title="Aún no tienes órdenes"
            description="Cuando realices una compra, aparecerá aquí"
            action={
                <Link to="/productos">
                    <Button>
                        <ShoppingBag size={16} />
                        Ir a comprar
                    </Button>
                </Link>
            }
        />
    );

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">Mis órdenes</h1>

            <div className="space-y-3">
                {orders.map((order) => {
                    const status = statusConfig[order.status] || statusConfig.PENDING;
                    return (
                        <Link
                            key={order.id}
                            to={`/mis-ordenes/${order.id}`}
                            className="block">
                            <Card className="p-5 hover:shadow-md transition-shadow">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-4">
                                        <div className="p-2.5 bg-gray-100 rounded-xl">
                                            <Package size={20} className="text-gray-600" />
                                        </div>
                                        <div>
                                            <p className="font-semibold text-gray-900">
                                                Orden #{order.id}
                                            </p>
                                            <p className="text-sm text-gray-400 mt-0.5">
                                                {new Date(order.createdAt).toLocaleDateString('es-ES', {
                                                    year: 'numeric',
                                                    month: 'long',
                                                    day: 'numeric',
                                                })}
                                            </p>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-4">
                                        <div className="text-right">
                                            <p className="font-bold text-gray-900">
                                                ${Number(order.total).toFixed(2)}
                                            </p>
                                            <Badge variant={status.variant} className="mt-1">
                                                {status.label}
                                            </Badge>
                                        </div>
                                        <ChevronRight size={18} className="text-gray-400" />
                                    </div>
                                </div>

                                {order.items?.length > 0 && (
                                    <p className="text-sm text-gray-400 mt-3 pl-14 truncate">
                                        {order.items
                                            .slice(0, 2)
                                            .map((i) => i.productName)
                                            .join(', ')}
                                        {order.items.length > 2 &&
                                            ` y ${order.items.length - 2} más`}
                                    </p>
                                )}
                            </Card>
                        </Link>
                    );
                })}
            </div>

            {/* Paginación */}
            {pagination && pagination.totalPages > 1 && (
                <div className="flex items-center justify-center gap-2">
                    <Button
                        variant="secondary" size="sm"
                        onClick={() => setPage((p) => p - 1)}
                        disabled={page === 0}>
                        ← Anterior
                    </Button>
                    <span className="text-sm text-gray-500">
                        {page + 1} / {pagination.totalPages}
                    </span>
                    <Button
                        variant="secondary" size="sm"
                        onClick={() => setPage((p) => p + 1)}
                        disabled={pagination.last}>
                        Siguiente →
                    </Button>
                </div>
            )}
        </div>
    );
};

export default OrdersPage;