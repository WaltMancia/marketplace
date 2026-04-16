import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
    Trash2, Minus, Plus, ShoppingBag,
    ArrowRight, Package,
} from 'lucide-react';
import useCart from '../../hooks/useCart.js';
import { checkoutService } from '../../services/order.service.js';
import Button from '../../components/ui/Button.jsx';
import Spinner from '../../components/ui/Spinner.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import Card from '../../components/ui/Card.jsx';
import toast from 'react-hot-toast';

const CartPage = () => {
    const { cart, updateItem, removeItem } = useCart();
    const [checkoutLoading, setCheckoutLoading] = useState(false);
    const [showAddressForm, setShowAddressForm] = useState(false);
    const [address, setAddress] = useState('');
    const navigate = useNavigate();

    const handleCheckout = async (e) => {
        e.preventDefault();
        if (!address.trim()) return;

        setCheckoutLoading(true);
        try {
            const order = await checkoutService(address);
            toast.success('¡Orden creada exitosamente! 🎉');
            navigate(`/mis-ordenes/${order.id}`);
        } catch (error) {
            toast.error(
                error.response?.data?.message || 'Error al procesar la orden'
            );
        } finally {
            setCheckoutLoading(false);
        }
    };

    if (!cart) return (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
    );

    if (cart.items.length === 0) return (
        <EmptyState
            icon="🛒"
            title="Tu carrito está vacío"
            description="Explora nuestro catálogo y añade productos"
            action={
                <Link to="/productos">
                    <Button>
                        <ShoppingBag size={16} />
                        Ver productos
                    </Button>
                </Link>
            }
        />
    );

    return (
        <div className="max-w-5xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">
                Mi carrito
                <span className="text-sm font-normal text-gray-400 ml-2">
                    ({cart.items.length}{' '}
                    {cart.items.length === 1 ? 'producto' : 'productos'})
                </span>
            </h1>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Items */}
                <div className="lg:col-span-2 space-y-3">
                    {cart.items.map((item) => (
                        <Card key={item.id} className="p-4">
                            <div className="flex gap-4">
                                {/* Imagen */}
                                <div className="w-20 h-20 bg-gray-100 rounded-xl flex
                  items-center justify-center flex-shrink-0 overflow-hidden">
                                    <Package size={24} className="text-gray-300" />
                                </div>

                                <div className="flex-1 min-w-0">
                                    <p className="font-semibold text-gray-900 truncate">
                                        {item.productName}
                                    </p>
                                    <p className="text-sm text-gray-400 mt-0.5">
                                        ${Number(item.unitPrice).toFixed(2)} por unidad
                                    </p>

                                    <div className="flex items-center justify-between mt-3">
                                        {/* Cantidad */}
                                        <div className="flex items-center border border-gray-200
                      rounded-lg overflow-hidden">
                                            <button
                                                onClick={() => item.quantity > 1
                                                    ? updateItem(item.productId, item.quantity - 1)
                                                    : removeItem(item.productId)
                                                }
                                                className="px-2.5 py-1.5 hover:bg-gray-50
                          transition-colors">
                                                <Minus size={13} />
                                            </button>
                                            <span className="px-3 py-1.5 text-sm font-semibold
                        min-w-[2.5rem] text-center">
                                                {item.quantity}
                                            </span>
                                            <button
                                                onClick={() => updateItem(
                                                    item.productId, item.quantity + 1
                                                )}
                                                className="px-2.5 py-1.5 hover:bg-gray-50
                          transition-colors">
                                                <Plus size={13} />
                                            </button>
                                        </div>

                                        <div className="flex items-center gap-3">
                                            <span className="font-bold text-gray-900">
                                                ${Number(item.subtotal).toFixed(2)}
                                            </span>
                                            <button
                                                onClick={() => removeItem(item.productId)}
                                                className="p-1.5 text-gray-400 hover:text-red-500
                          hover:bg-red-50 rounded-lg transition-colors">
                                                <Trash2 size={15} />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </Card>
                    ))}
                </div>

                {/* Resumen */}
                <div className="lg:col-span-1">
                    <Card className="p-6 sticky top-6">
                        <h2 className="font-bold text-gray-900 text-lg mb-4">
                            Resumen del pedido
                        </h2>

                        <div className="space-y-2 text-sm mb-4">
                            {cart.items.map((item) => (
                                <div key={item.id}
                                    className="flex justify-between text-gray-500">
                                    <span className="truncate max-w-[160px]">
                                        {item.productName} x{item.quantity}
                                    </span>
                                    <span>${Number(item.subtotal).toFixed(2)}</span>
                                </div>
                            ))}
                        </div>

                        <div className="border-t pt-3 mb-5">
                            <div className="flex justify-between font-bold text-gray-900
                text-lg">
                                <span>Total</span>
                                <span>${Number(cart.total).toFixed(2)}</span>
                            </div>
                        </div>

                        {/* Formulario de dirección */}
                        {!showAddressForm ? (
                            <Button
                                className="w-full"
                                size="lg"
                                onClick={() => setShowAddressForm(true)}>
                                Proceder al pago
                                <ArrowRight size={16} />
                            </Button>
                        ) : (
                            <form onSubmit={handleCheckout} className="space-y-3">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700
                    mb-1.5">
                                        Dirección de envío
                                    </label>
                                    <textarea
                                        value={address}
                                        onChange={(e) => setAddress(e.target.value)}
                                        required
                                        rows={3}
                                        placeholder="Calle, número, ciudad, país..."
                                        className="w-full px-3 py-2 border border-gray-200
                      rounded-xl text-sm focus:outline-none focus:ring-2
                      focus:ring-gray-900 resize-none"
                                    />
                                </div>
                                <Button
                                    type="submit"
                                    className="w-full"
                                    loading={checkoutLoading}>
                                    Confirmar pedido
                                </Button>
                                <button
                                    type="button"
                                    onClick={() => setShowAddressForm(false)}
                                    className="w-full text-sm text-gray-500 hover:text-gray-700
                    transition-colors py-1">
                                    Cancelar
                                </button>
                            </form>
                        )}

                        <Link to="/productos"
                            className="block text-center text-sm text-gray-500
                hover:text-gray-700 mt-3 transition-colors">
                            ← Seguir comprando
                        </Link>
                    </Card>
                </div>
            </div>
        </div>
    );
};

export default CartPage;