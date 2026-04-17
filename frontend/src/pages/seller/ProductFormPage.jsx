import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Upload, Package } from 'lucide-react';
import {
    createProductService,
    updateProductService,
    getProductByIdService,
    getCategoriesService,
} from '../../services/product.service.js';
import Button from '../../components/ui/Button.jsx';
import Card, { CardHeader, CardContent } from '../../components/ui/Card.jsx';
import Spinner from '../../components/ui/Spinner.jsx';
import toast from 'react-hot-toast';

const ProductFormPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditing = !!id;

    const [form, setForm] = useState({
        categoryId: '',
        name: '',
        description: '',
        price: '',
        stock: '',
        imageUrl: '',
    });
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [initialLoading, setInitialLoading] = useState(isEditing);

    useEffect(() => {
        getCategoriesService().then(setCategories).catch(() => { });

        if (isEditing) {
            getProductByIdService(id)
                .then((product) => {
                    setForm({
                        categoryId: product.categoryId || '',
                        name: product.name || '',
                        description: product.description || '',
                        price: product.price || '',
                        stock: product.stock || '',
                        imageUrl: product.imageUrl || '',
                    });
                })
                .catch(() => {
                    toast.error('Producto no encontrado');
                    navigate('/mis-productos');
                })
                .finally(() => setInitialLoading(false));
        }
    }, [id]);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload = {
                ...form,
                categoryId: parseInt(form.categoryId),
                price: parseFloat(form.price),
                stock: parseInt(form.stock),
            };

            if (isEditing) {
                await updateProductService(id, payload);
                toast.success('Producto actualizado');
            } else {
                await createProductService(payload);
                toast.success('Producto creado');
            }
            navigate('/mis-productos');
        } catch (error) {
            toast.error(
                error.response?.data?.message || 'Error al guardar el producto'
            );
        } finally {
            setLoading(false);
        }
    };

    if (initialLoading) return (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
    );

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <button
                onClick={() => navigate('/mis-productos')}
                className="flex items-center gap-2 text-sm text-gray-500
          hover:text-gray-900 transition-colors">
                <ArrowLeft size={16} />
                Volver a mis productos
            </button>

            <div>
                <h1 className="text-2xl font-bold text-gray-900">
                    {isEditing ? 'Editar producto' : 'Nuevo producto'}
                </h1>
                <p className="text-sm text-gray-500 mt-0.5">
                    {isEditing
                        ? 'Actualiza la información de tu producto'
                        : 'Completa la información para publicar tu producto'}
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
                {/* Información básica */}
                <Card>
                    <CardHeader>
                        <h2 className="font-semibold text-gray-900">
                            Información básica
                        </h2>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            {/* Nombre */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700
                  mb-1.5">
                                    Nombre del producto *
                                </label>
                                <input
                                    name="name"
                                    value={form.name}
                                    onChange={handleChange}
                                    required
                                    maxLength={200}
                                    placeholder="Ej: Laptop Pro 15 pulgadas"
                                    className="w-full px-4 py-2.5 border border-gray-200
                    rounded-xl text-sm focus:outline-none focus:ring-2
                    focus:ring-gray-900"
                                />
                            </div>

                            {/* Categoría */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700
                  mb-1.5">
                                    Categoría *
                                </label>
                                <select
                                    name="categoryId"
                                    value={form.categoryId}
                                    onChange={handleChange}
                                    required
                                    className="w-full px-4 py-2.5 border border-gray-200
                    rounded-xl text-sm focus:outline-none focus:ring-2
                    focus:ring-gray-900 bg-white">
                                    <option value="">Selecciona una categoría</option>
                                    {categories.map((cat) => (
                                        <option key={cat.id} value={cat.id}>
                                            {cat.name}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* Descripción */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700
                  mb-1.5">
                                    Descripción
                                </label>
                                <textarea
                                    name="description"
                                    value={form.description}
                                    onChange={handleChange}
                                    rows={4}
                                    placeholder="Describe las características principales
                    de tu producto..."
                                    className="w-full px-4 py-2.5 border border-gray-200
                    rounded-xl text-sm focus:outline-none focus:ring-2
                    focus:ring-gray-900 resize-none"
                                />
                            </div>
                        </div>
                    </CardContent>
                </Card>

                {/* Precio y stock */}
                <Card>
                    <CardHeader>
                        <h2 className="font-semibold text-gray-900">Precio y stock</h2>
                    </CardHeader>
                    <CardContent>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700
                  mb-1.5">
                                    Precio (USD) *
                                </label>
                                <div className="relative">
                                    <span className="absolute left-3.5 top-1/2
                    -translate-y-1/2 text-gray-400 font-medium text-sm">
                                        $
                                    </span>
                                    <input
                                        type="number"
                                        name="price"
                                        value={form.price}
                                        onChange={handleChange}
                                        required
                                        min="0.01"
                                        step="0.01"
                                        placeholder="0.00"
                                        className="w-full pl-8 pr-4 py-2.5 border border-gray-200
                      rounded-xl text-sm focus:outline-none focus:ring-2
                      focus:ring-gray-900"
                                    />
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700
                  mb-1.5">
                                    Stock disponible *
                                </label>
                                <input
                                    type="number"
                                    name="stock"
                                    value={form.stock}
                                    onChange={handleChange}
                                    required
                                    min="0"
                                    placeholder="0"
                                    className="w-full px-4 py-2.5 border border-gray-200
                    rounded-xl text-sm focus:outline-none focus:ring-2
                    focus:ring-gray-900"
                                />
                            </div>
                        </div>
                    </CardContent>
                </Card>

                {/* Imagen */}
                <Card>
                    <CardHeader>
                        <h2 className="font-semibold text-gray-900">Imagen</h2>
                    </CardHeader>
                    <CardContent>
                        {/* Preview */}
                        {form.imageUrl && (
                            <div className="mb-4 w-32 h-32 rounded-2xl overflow-hidden
                border border-gray-200">
                                <img
                                    src={form.imageUrl}
                                    alt="Preview"
                                    className="w-full h-full object-cover"
                                    onError={(e) => {
                                        e.target.style.display = 'none';
                                    }}
                                />
                            </div>
                        )}

                        <div>
                            <label className="block text-sm font-medium text-gray-700
                mb-1.5">
                                URL de la imagen
                            </label>
                            <div className="relative">
                                <Upload size={15} className="absolute left-3.5 top-1/2
                  -translate-y-1/2 text-gray-400" />
                                <input
                                    name="imageUrl"
                                    value={form.imageUrl}
                                    onChange={handleChange}
                                    placeholder="https://ejemplo.com/imagen.jpg"
                                    className="w-full pl-10 pr-4 py-2.5 border border-gray-200
                    rounded-xl text-sm focus:outline-none focus:ring-2
                    focus:ring-gray-900"
                                />
                            </div>
                            <p className="text-xs text-gray-400 mt-1.5">
                                Añade una URL de imagen externa. Formatos: JPG, PNG, WebP.
                            </p>
                        </div>
                    </CardContent>
                </Card>

                {/* Botones */}
                <div className="flex gap-3">
                    <Button
                        type="submit"
                        loading={loading}
                        className="flex-1"
                        size="lg">
                        {isEditing ? 'Guardar cambios' : 'Publicar producto'}
                    </Button>
                    <Button
                        type="button"
                        variant="secondary"
                        size="lg"
                        onClick={() => navigate('/mis-productos')}>
                        Cancelar
                    </Button>
                </div>
            </form>
        </div>
    );
};

export default ProductFormPage;