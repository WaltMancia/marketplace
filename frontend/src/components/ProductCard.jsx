import { ShoppingCart, Package, Star } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import useCart from '../hooks/useCart.js';
import useAuthStore from '../store/authStore.js';
import Button from './ui/Button.jsx';
import Badge from './ui/Badge.jsx';
import toast from 'react-hot-toast';

const ProductCard = ({ product }) => {
    const { addToCart } = useCart();
    const { user } = useAuthStore();
    const navigate = useNavigate();

    const handleAddToCart = async (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (!user) {
            toast.error('Inicia sesión para añadir al carrito');
            navigate('/login');
            return;
        }
        await addToCart(product.id);
    };

    const isOutOfStock = product.stock === 0;

    return (
        <div
            className="group bg-white rounded-2xl border border-gray-100
        shadow-sm overflow-hidden hover:shadow-md transition-all
        duration-300 cursor-pointer"
            onClick={() => navigate(`/productos/${product.id}`)}>

            {/* Imagen */}
            <div className="aspect-square bg-gradient-to-br from-gray-100
        to-gray-200 relative overflow-hidden">
                {product.imageUrl ? (
                    <img
                        src={product.imageUrl}
                        alt={product.name}
                        className="w-full h-full object-cover group-hover:scale-105
              transition-transform duration-500"
                    />
                ) : (
                    <div className="absolute inset-0 flex items-center justify-center">
                        <Package size={40} className="text-gray-300" />
                    </div>
                )}

                {isOutOfStock && (
                    <div className="absolute inset-0 bg-black/50 flex items-center
            justify-center">
                        <span className="text-white font-semibold text-sm
              bg-black/60 px-3 py-1 rounded-full">
                            Agotado
                        </span>
                    </div>
                )}

                {!isOutOfStock && product.stock <= 5 && (
                    <div className="absolute top-2 left-2">
                        <Badge variant="warning">¡Solo {product.stock}!</Badge>
                    </div>
                )}
            </div>

            {/* Info */}
            <div className="p-4">
                {product.categoryName && (
                    <p className="text-xs text-gray-400 font-medium uppercase
            tracking-wide mb-1">
                        {product.categoryName}
                    </p>
                )}

                <h3 className="font-semibold text-gray-900 text-sm mb-1
          line-clamp-2 group-hover:text-gray-600 transition-colors">
                    {product.name}
                </h3>

                {product.sellerName && (
                    <p className="text-xs text-gray-400 mb-3">
                        por {product.sellerName}
                    </p>
                )}

                <div className="flex items-center justify-between">
                    <span className="text-lg font-bold text-gray-900">
                        ${Number(product.price).toLocaleString('es', {
                            minimumFractionDigits: 2,
                        })}
                    </span>
                    <Button
                        size="sm"
                        onClick={handleAddToCart}
                        disabled={isOutOfStock}
                        className="rounded-xl">
                        <ShoppingCart size={14} />
                        Añadir
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default ProductCard;