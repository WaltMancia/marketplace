import api from './api.js';

export const getCartService = async () => {
    const { data } = await api.get('/cart');
    return data;
};

export const addToCartService = async (productId, quantity = 1) => {
    const { data } = await api.post('/cart/items', { productId, quantity });
    return data;
};

export const updateCartItemService = async (productId, quantity) => {
    const { data } = await api.put(`/cart/items/${productId}`, null, {
        params: { quantity },
    });
    return data;
};

export const removeFromCartService = async (productId) => {
    const { data } = await api.delete(`/cart/items/${productId}`);
    return data;
};