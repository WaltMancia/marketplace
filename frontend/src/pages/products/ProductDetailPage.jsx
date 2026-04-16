import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    ArrowLeft, Package, ShoppingCart,
    CheckCircle, XCircle, Minus, Plus, Store,
} from 'lucide-react';
import { getProductByIdService } from '../../services/product.service.js';
import useCart from '../../hooks/useCart.js';
import useAuthStore from '../../store/authStore.js';
import Button from '../../components/ui/Button.jsx';
import Badge from '../../components/ui/Badge.jsx';
import Spinner from '../../components/ui/Spinner.jsx';
import toast from 'react-hot-toast';

const ProductDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuthStore();
    const { addToCart } = useCart();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [quantity, setQuantity] = useState(1);

    useEffect(() => {
        getProductByIdService(id)
            .then(setProduct)
            .catch(() => navigate('/productos'))
            .finally(() => setLoading(false));
    }, [id]);

    const handleAddToCart = async () => {
        if (!user) {
            toast.error('Inicia sesión para añadir al carrito');
            navigate('/login');
            return;
        }
        await addToCart(product.id, quantity);
    };

    if (loading) return (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
    );
    if (!product) return null;

    const inStock = product.stock > 0;

    return (
        <div className="max-w-5xl mx-auto space-y-8">
            <button
                onClick={() => navigate('/productos')}
                className="flex items-center gap-2 text-sm text-gray-500
          hover:text-gray-900 transition-colors">
                <ArrowLeft size={16} />
                Volver a productos
            </button>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
                {/* Imagen */}
                <div className="aspect-square bg-gradient-to-br from-gray-100
          to-gray-200 rounded-3xl flex items-center justify-center
          overflow-hidden">
                    {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name}
                            className="w-full h-full object-cover" />
                    ) : (
                        <Package size={80} className="text-gray-300" />
                    )}
                </div>

                {/* Info */}
                <div className="space-y-5">
                    <div className="flex items-start justify-between">
                        <div>
                            {product.categoryName && (
                                <Badge variant="default" className="mb-2">
                                    {product.categoryName}
                                </Badge>
                            )}
                            <h1 className="text-3xl font-bold text-gray-900">
                                {product.name}
                            </h1>
                        </div>
                    </div>

                    {/* Vendedor */}
                    {product.sellerName && (
                        <div className="flex items-center gap-2 text-sm text-gray-500">
                            <Store size={15} />
                            <span>Vendido por <strong>{product.sellerName}</strong></span>
                        </div>
                    )}

                    {/* Stock */}
                    <div className="flex items-center gap-2">
                        {inStock ? (
                            <>
                                <CheckCircle size={17} className="text-emerald-500" />
                                <span className="text-emerald-600 text-sm font-medium">
                                    {product.stock <= 5
                                        ? `¡Solo quedan ${product.stock} unidades!`
                                        : 'En stock'}
                                </span>
                            </>
                        ) : (
                            <>
                                <XCircle size={17} className="text-red-500" />
                                <span className="text-red-600 text-sm font-medium">
                                    Agotado
                                </span>
                            </>
                        )}
                    </div>

                    {/* Precio */}
                    <div className="text-4xl font-bold text-gray-900">
                        ${Number(product.price).toLocaleString('es', {
                            minimumFractionDigits: 2,
                        })}
                    </div>

                    {/* Descripción */}
                    {product.description && (
                        <p className="text-gray-600 leading-relaxed">
                            {product.description}
                        </p>
                    )}

                    {/* Selector de cantidad */}
                    {inStock && (
                        <div className="space-y-2">
                            <label className="text-sm font-medium text-gray-700">
                                Cantidad
                            </label>
                            <div className="flex items-center gap-3">
                                <div className="flex items-center border border-gray-200
                  rounded-xl overflow-hidden">
                                    <button
                                        onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                                        className="px-3 py-2 hover:bg-gray-50 transition-colors">
                                        <Minus size={15} />
                                    </button>
                                    <span className="px-4 py-2 font-semibold text-gray-900
                    min-w-[3rem] text-center">
                                        {quantity}
                                    </span>
                                    <button
                                        onClick={() => setQuantity(
                                            (q) => Math.min(product.stock, q + 1)
                                        )}
                                        className="px-3 py-2 hover:bg-gray-50 transition-colors">
                                        <Plus size={15} />
                                    </button>
                                </div>
                                <span className="text-sm text-gray-400">
                                    {product.stock} disponibles
                                </span>
                            </div>
                        </div>
                    )}

                    <Button
                        size="lg"
                        onClick={handleAddToCart}
                        disabled={!inStock}
                        className="w-full">
                        <ShoppingCart size={18} />
                        {inStock ? 'Añadir al carrito' : 'Sin stock'}
                    </Button>

                    {inStock && quantity > 1 && (
                        <p className="text-sm text-gray-500 text-center">
                            Total:{' '}
                            <span className="font-bold text-gray-900">
                                ${(Number(product.price) * quantity).toLocaleString('es', {
                                    minimumFractionDigits: 2,
                                })}
                            </span>
                        </p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProductDetailPage;