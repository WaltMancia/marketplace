import api from './api.js';

export const getProductsService = async (params = {}) => {
    const { data } = await api.get('/products', { params });
    return data;
};

export const getProductByIdService = async (id) => {
    const { data } = await api.get(`/products/${id}`);
    return data;
};

export const createProductService = async (productData) => {
    const { data } = await api.post('/products', productData);
    return data;
};

export const updateProductService = async (id, productData) => {
    const { data } = await api.put(`/products/${id}`, productData);
    return data;
};

export const deleteProductService = async (id) => {
    await api.delete(`/products/${id}`);
};

export const getCategoriesService = async () => {
    const { data } = await api.get('/categories');
    return data;
};

export const getSellerProductsService = async (sellerId, params = {}) => {
    const { data } = await api.get('/products', {
        params: { sellerId, ...params },
    });
    return data;
};