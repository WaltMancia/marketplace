import api from './api.js';

export const checkoutService = async (shippingAddress) => {
    const { data } = await api.post('/orders/checkout', { shippingAddress });
    return data;
};

export const getMyOrdersService = async (page = 0, size = 10) => {
    const { data } = await api.get('/orders', { params: { page, size } });
    return data;
};

export const getOrderByIdService = async (id) => {
    const { data } = await api.get(`/orders/${id}`);
    return data;
};