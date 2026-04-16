import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Store, Mail, Lock, ArrowRight } from 'lucide-react';
import useAuth from '../../hooks/useAuth.js';
import Button from '../../components/ui/Button.jsx';

const LoginPage = () => {
    const [form, setForm] = useState({ email: '', password: '' });
    const { login, loading } = useAuth();

    const handleSubmit = (e) => {
        e.preventDefault();
        login(form);
    };

    return (
        <div className="min-h-screen flex">
            {/* Panel izquierdo decorativo */}
            <div className="hidden lg:flex lg:w-1/2 bg-gray-900 flex-col
        justify-between p-16 relative overflow-hidden">
                <div className="absolute inset-0 bg-gradient-to-br
          from-gray-900 via-gray-800 to-gray-900" />
                <div className="absolute top-0 right-0 w-96 h-96
          bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
                <div className="absolute bottom-0 left-0 w-64 h-64
          bg-white/5 rounded-full translate-y-1/2 -translate-x-1/2" />

                <div className="relative z-10 flex items-center gap-3">
                    <div className="w-10 h-10 bg-white/10 rounded-xl flex
            items-center justify-center">
                        <Store size={20} className="text-white" />
                    </div>
                    <span className="text-white font-bold text-xl">Marketplace</span>
                </div>

                <div className="relative z-10">
                    <h1 className="text-4xl font-bold text-white leading-tight mb-4">
                        Tu tienda,<br />tu negocio,<br />tu éxito.
                    </h1>
                    <p className="text-gray-400 text-lg leading-relaxed">
                        Compra y vende productos con la plataforma más completa
                        del mercado.
                    </p>

                    <div className="mt-10 grid grid-cols-2 gap-4">
                        {[
                            { label: 'Vendedores', value: '2,400+' },
                            { label: 'Productos', value: '48,000+' },
                            { label: 'Pedidos', value: '120K+' },
                            { label: 'Satisfacción', value: '98%' },
                        ].map(({ label, value }) => (
                            <div key={label} className="bg-white/5 rounded-2xl p-4">
                                <p className="text-2xl font-bold text-white">{value}</p>
                                <p className="text-gray-400 text-sm mt-0.5">{label}</p>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="relative z-10 text-gray-600 text-sm">
                    © 2024 Marketplace. Todos los derechos reservados.
                </div>
            </div>

            {/* Panel derecho — formulario */}
            <div className="flex-1 flex items-center justify-center px-8">
                <div className="w-full max-w-md">
                    <div className="mb-8">
                        <h2 className="text-2xl font-bold text-gray-900">
                            Iniciar sesión
                        </h2>
                        <p className="text-gray-500 mt-1">
                            Ingresa tus credenciales para continuar
                        </p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                Correo electrónico
                            </label>
                            <div className="relative">
                                <Mail size={16} className="absolute left-3.5 top-1/2
                  -translate-y-1/2 text-gray-400" />
                                <input
                                    type="email"
                                    value={form.email}
                                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                                    required
                                    placeholder="tu@email.com"
                                    className="w-full pl-10 pr-4 py-2.5 border border-gray-200
                    rounded-xl text-sm focus:outline-none focus:ring-2
                    focus:ring-gray-900"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                Contraseña
                            </label>
                            <div className="relative">
                                <Lock size={16} className="absolute left-3.5 top-1/2
                  -translate-y-1/2 text-gray-400" />
                                <input
                                    type="password"
                                    value={form.password}
                                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                                    required
                                    placeholder="••••••••"
                                    className="w-full pl-10 pr-4 py-2.5 border border-gray-200
                    rounded-xl text-sm focus:outline-none focus:ring-2
                    focus:ring-gray-900"
                                />
                            </div>
                        </div>

                        <Button
                            type="submit"
                            loading={loading}
                            className="w-full mt-2"
                            size="lg">
                            Iniciar sesión
                            <ArrowRight size={16} />
                        </Button>
                    </form>

                    <p className="text-center text-sm text-gray-500 mt-6">
                        ¿No tienes cuenta?{' '}
                        <Link to="/registro"
                            className="font-medium text-gray-900 hover:underline">
                            Regístrate gratis
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;