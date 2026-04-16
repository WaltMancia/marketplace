import { useState, useEffect, useCallback } from 'react';
import { Search, SlidersHorizontal, X } from 'lucide-react';
import {
    getProductsService,
    getCategoriesService,
} from '../../services/product.service.js';
import ProductCard from '../../components/ProductCard.jsx';
import Spinner from '../../components/ui/Spinner.jsx';
import EmptyState from '../../components/ui/EmptyState.jsx';
import Button from '../../components/ui/Button.jsx';

const ProductsPage = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [pagination, setPagination] = useState(null);
    const [loading, setLoading] = useState(true);

    const [keyword, setKeyword] = useState('');
    const [keywordInput, setKeywordInput] = useState('');
    const [categoryId, setCategoryId] = useState('');
    const [page, setPage] = useState(0);

    const fetchProducts = useCallback(async () => {
        setLoading(true);
        try {
            const params = { page, size: 12 };
            if (keyword) params.keyword = keyword;
            if (categoryId) params.categoryId = categoryId;

            const data = await getProductsService(params);
            setProducts(data.content);
            setPagination(data);
        } catch {
            setProducts([]);
        } finally {
            setLoading(false);
        }
    }, [page, keyword, categoryId]);

    useEffect(() => { fetchProducts(); }, [fetchProducts]);

    useEffect(() => {
        getCategoriesService().then(setCategories).catch(() => { });
    }, []);

    const handleSearch = (e) => {
        e.preventDefault();
        setKeyword(keywordInput);
        setPage(0);
    };

    const clearFilters = () => {
        setKeyword('');
        setKeywordInput('');
        setCategoryId('');
        setPage(0);
    };

    const hasFilters = keyword || categoryId;

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center
        justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Productos</h1>
                    {pagination && (
                        <p className="text-sm text-gray-500 mt-0.5">
                            {pagination.totalElements} productos disponibles
                        </p>
                    )}
                </div>

                {/* Búsqueda */}
                <form onSubmit={handleSearch} className="flex gap-2 w-full sm:w-auto">
                    <div className="relative flex-1 sm:w-72">
                        <Search size={15} className="absolute left-3.5 top-1/2
              -translate-y-1/2 text-gray-400" />
                        <input
                            value={keywordInput}
                            onChange={(e) => setKeywordInput(e.target.value)}
                            placeholder="Buscar productos..."
                            className="w-full pl-10 pr-4 py-2 border border-gray-200
                rounded-xl text-sm focus:outline-none focus:ring-2
                focus:ring-gray-900"
                        />
                    </div>
                    <Button type="submit" size="sm" variant="secondary">
                        <SlidersHorizontal size={14} />
                    </Button>
                    {hasFilters && (
                        <Button type="button" variant="ghost" size="sm"
                            onClick={clearFilters}>
                            <X size={14} />
                        </Button>
                    )}
                </form>
            </div>

            {/* Filtro de categorías */}
            <div className="flex gap-2 flex-wrap">
                <button
                    onClick={() => { setCategoryId(''); setPage(0); }}
                    className={`px-4 py-1.5 rounded-full text-sm font-medium
            transition-colors ${!categoryId
                            ? 'bg-gray-900 text-white'
                            : 'bg-white border border-gray-200 text-gray-600 hover:border-gray-300'
                        }`}>
                    Todos
                </button>
                {categories.map((cat) => (
                    <button
                        key={cat.id}
                        onClick={() => { setCategoryId(cat.id); setPage(0); }}
                        className={`px-4 py-1.5 rounded-full text-sm font-medium
              transition-colors ${categoryId === cat.id
                                ? 'bg-gray-900 text-white'
                                : 'bg-white border border-gray-200 text-gray-600 hover:border-gray-300'
                            }`}>
                        {cat.name}
                    </button>
                ))}
            </div>

            {/* Grid de productos */}
            {loading ? (
                <div className="flex justify-center py-20">
                    <Spinner size="lg" />
                </div>
            ) : products.length === 0 ? (
                <EmptyState
                    icon="🔍"
                    title="Sin productos"
                    description={hasFilters
                        ? 'No hay resultados para tu búsqueda'
                        : 'No hay productos disponibles'}
                    action={hasFilters && (
                        <Button variant="secondary" onClick={clearFilters}>
                            Limpiar filtros
                        </Button>
                    )}
                />
            ) : (
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-5">
                    {products.map((product) => (
                        <ProductCard key={product.id} product={product} />
                    ))}
                </div>
            )}

            {/* Paginación */}
            {pagination && pagination.totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 pt-4">
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

export default ProductsPage;