import { useEffect } from 'react';
import toast from 'react-hot-toast';
import useCartStore from '../store/cartStore.js';
import useAuthStore from '../store/authStore.js';
import {
    getCartService,
    addToCartService,
    updateCartItemService,
    removeFromCartService,
} from '../services/cart.service.js';

const useCart = () => {
    const { cart, setCart, itemCount } = useCartStore();
    const { user } = useAuthStore();

    useEffect(() => {
        if (user) fetchCart();
    }, [user]);

    const fetchCart = async () => {
        try {
            const data = await getCartService();
            setCart(data);
        } catch {
            // fallo silencioso — el carrito se mostrará vacío
        }
    };

    const addToCart = async (productId, quantity = 1) => {
        try {
            const data = await addToCartService(productId, quantity);
            setCart(data);
            toast.success('Producto añadido al carrito 🛒');
        } catch (error) {
            toast.error(
                error.response?.data?.message || 'Error al añadir al carrito'
            );
        }
    };

    const updateItem = async (productId, quantity) => {
        try {
            const data = await updateCartItemService(productId, quantity);
            setCart(data);
        } catch (error) {
            toast.error('Error al actualizar la cantidad');
        }
    };

    const removeItem = async (productId) => {
        try {
            const data = await removeFromCartService(productId);
            setCart(data);
            toast.success('Producto eliminado del carrito');
        } catch {
            toast.error('Error al eliminar el producto');
        }
    };

    return { cart, itemCount, addToCart, updateItem, removeItem, fetchCart };
};

export default useCart;