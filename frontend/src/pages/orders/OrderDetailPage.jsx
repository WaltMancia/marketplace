import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
    ArrowLeft, Package, MapPin,
    CheckCircle, Clock, XCircle, Truck,
} from 'lucide-react';
import { getOrderByIdService } from '../../services/order.service.js';
import Spinner from '../../components/ui/Spinner.jsx';
import Badge from '../../components/ui/Badge.jsx';
import Card, { CardHeader, CardContent } from '../../components/ui/Card.jsx';
import Button from '../../components/ui/Button.jsx';

const statusConfig = {
    PENDING: {
        variant: 'warning',
        label: 'Pendiente de confirmación',
        icon: Clock,
        description: 'Tu orden fue recibida y estamos verificando el stock.',
    },
    CONFIRMED: {
        variant: 'info',
        label: 'Confirmada',
        icon: CheckCircle,
        description: 'Stock reservado. Tu orden está lista para pago.',
    },
    PAID: {
        variant: 'success',
        label: 'Pagada',
        icon: CheckCircle,
        description: 'Pago confirmado. Preparando tu pedido.',
    },
    SHIPPED: {
        variant: 'info',
        label: 'En camino',
        icon: Truck,
        description: 'Tu pedido está en camino.',
    },
    DELIVERED: {
        variant: 'success',
        label: 'Entregada',
        icon: CheckCircle,
        description: '¡Tu pedido fue entregado exitosamente!',
    },
    CANCELLED: {
        variant: 'danger',
        label: 'Cancelada',
        icon: XCircle,
        description: 'Esta orden fue cancelada.',
    },
};

const OrderDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        getOrderByIdService(id)
            .then(setOrder)
            .catch(() => navigate('/mis-ordenes'))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) return (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
    );
    if (!order) return null;

    const status = statusConfig[order.status] || statusConfig.PENDING;
    const StatusIcon = status.icon;

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <button
                onClick={() => navigate('/mis-ordenes')}
                className="flex items-center gap-2 text-sm text-gray-500
          hover:text-gray-900 transition-colors">
                <ArrowLeft size={16} />
                Volver a mis órdenes
            </button>

            {/* Header */}
            <div className="flex items-start justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        Orden #{order.id}
                    </h1>
                    <p className="text-sm text-gray-400 mt-1">
                        {new Date(order.createdAt).toLocaleDateString('es-ES', {
                            weekday: 'long',
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric',
                        })}
                    </p>
                </div>
                <Badge variant={status.variant} className="text-sm px-3 py-1">
                    {status.label}
                </Badge>
            </div>

            {/* Status Banner */}
            <div className={`flex items-start gap-3 p-4 rounded-2xl ${order.status === 'CANCELLED'
                    ? 'bg-red-50 border border-red-100'
                    : order.status === 'DELIVERED' || order.status === 'PAID'
                        ? 'bg-emerald-50 border border-emerald-100'
                        : 'bg-blue-50 border border-blue-100'
                }`}>
                <StatusIcon size={22} className={
                    order.status === 'CANCELLED' ? 'text-red-500 flex-shrink-0 mt-0.5'
                        : order.status === 'DELIVERED' || order.status === 'PAID'
                            ? 'text-emerald-500 flex-shrink-0 mt-0.5'
                            : 'text-blue-500 flex-shrink-0 mt-0.5'
                } />
                <p className={`text-sm font-medium ${order.status === 'CANCELLED' ? 'text-red-800'
                        : order.status === 'DELIVERED' || order.status === 'PAID'
                            ? 'text-emerald-800'
                            : 'text-blue-800'
                    }`}>
                    {status.description}
                </p>
            </div>

            {/* Items */}
            <Card>
                <CardHeader>
                    <h2 className="font-semibold text-gray-900">
                        Productos ({order.items?.length})
                    </h2>
                </CardHeader>
                <CardContent>
                    <div className="space-y-4">
                        {order.items?.map((item) => (
                            <div
                                key={item.id}
                                className="flex items-center justify-between
                  pb-4 border-b border-gray-50 last:border-0 last:pb-0">
                                <div className="flex items-center gap-3">
                                    <div className="w-12 h-12 bg-gray-100 rounded-xl flex
                    items-center justify-center flex-shrink-0">
                                        <Package size={18} className="text-gray-400" />
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900">
                                            {item.productName}
                                        </p>
                                        <p className="text-xs text-gray-400 mt-0.5">
                                            x{item.quantity} ·{' '}
                                            ${Number(item.unitPrice).toFixed(2)} c/u
                                        </p>
                                    </div>
                                </div>
                                <span className="font-bold text-gray-900">
                                    ${Number(item.subtotal).toFixed(2)}
                                </span>
                            </div>
                        ))}

                        <div className="flex justify-between items-center pt-2
              border-t border-gray-100 font-bold text-gray-900 text-lg">
                            <span>Total</span>
                            <span>${Number(order.total).toFixed(2)}</span>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* Dirección */}
            {order.shippingAddress && (
                <Card>
                    <CardContent className="pt-5">
                        <div className="flex items-start gap-3">
                            <MapPin size={18} className="text-gray-400 mt-0.5 flex-shrink-0" />
                            <div>
                                <p className="font-medium text-gray-900 mb-0.5">
                                    Dirección de envío
                                </p>
                                <p className="text-sm text-gray-500">
                                    {order.shippingAddress}
                                </p>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            )}

            <Link to="/productos">
                <Button variant="secondary" className="w-full">
                    <Package size={16} />
                    Seguir comprando
                </Button>
            </Link>
        </div>
    );
};

export default OrderDetailPage;