import { create } from 'zustand';

const useCartStore = create((set) => ({
    cart: null,
    itemCount: 0,

    setCart: (cart) => set({
        cart,
        itemCount: cart?.items?.reduce(
            (sum, item) => sum + item.quantity, 0
        ) || 0,
    }),

    clearCart: () => set({ cart: null, itemCount: 0 }),
}));

export default useCartStore;