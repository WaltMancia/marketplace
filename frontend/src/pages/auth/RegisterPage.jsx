import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Store, User, Mail, Lock, Briefcase } from 'lucide-react';
import useAuth from '../../hooks/useAuth.js';
import Button from '../../components/ui/Button.jsx';

const RegisterPage = () => {
    const [form, setForm] = useState({
        name: '', email: '', password: '', role: 'CUSTOMER',
    });
    const { register, loading } = useAuth();

    const handleSubmit = (e) => {
        e.preventDefault();
        register(form);
    };

    return (
        <div className="min-h-screen flex items-center justify-center
      bg-gray-50 px-4 py-12">
            <div className="w-full max-w-md">
                <div className="text-center mb-8">
                    <div className="inline-flex items-center gap-2 mb-4">
                        <div className="w-10 h-10 bg-gray-900 rounded-xl flex
              items-center justify-center">
                            <Store size={18} className="text-white" />
                        </div>
                        <span className="font-bold text-gray-900 text-xl">Marketplace</span>
                    </div>
                    <h2 className="text-2xl font-bold text-gray-900">Crea tu cuenta</h2>
                    <p className="text-gray-500 mt-1 text-sm">
                        Únete a miles de compradores y vendedores
                    </p>
                </div>

                <div className="bg-white rounded-2xl border border-gray-100
          shadow-sm p-8">
                    <form onSubmit={handleSubmit} className="space-y-4">

                        {/* Tipo de cuenta */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Tipo de cuenta
                            </label>
                            <div className="grid grid-cols-2 gap-2">
                                {[
                                    { value: 'CUSTOMER', label: 'Comprador', icon: User },
                                    { value: 'SELLER', label: 'Vendedor', icon: Briefcase },
                                ].map(({ value, label, icon: Icon }) => (
                                    <button
                                        key={value}
                                        type="button"
                                        onClick={() => setForm({ ...form, role: value })}
                                        className={`flex items-center justify-center gap-2 py-2.5
                      rounded-xl border text-sm font-medium transition-colors
                      ${form.role === value
                                                ? 'border-gray-900 bg-gray-900 text-white'
                                                : 'border-gray-200 text-gray-600 hover:border-gray-300'
                                            }`}>
                                        <Icon size={15} />
                                        {label}
                                    </button>
                                ))}
                            </div>
                        </div>

                        {[
                            {
                                key: 'name', label: 'Nombre completo', type: 'text',
                                icon: User, placeholder: 'Juan Pérez'
                            },
                            {
                                key: 'email', label: 'Correo electrónico', type: 'email',
                                icon: Mail, placeholder: 'tu@email.com'
                            },
                            {
                                key: 'password', label: 'Contraseña', type: 'password',
                                icon: Lock, placeholder: '••••••••', minLength: 6
                            },
                        ].map(({ key, label, icon: Icon, ...inputProps }) => (
                            <div key={key}>
                                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                                    {label}
                                </label>
                                <div className="relative">
                                    <Icon size={16} className="absolute left-3.5 top-1/2
                    -translate-y-1/2 text-gray-400" />
                                    <input
                                        value={form[key]}
                                        onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                                        required
                                        className="w-full pl-10 pr-4 py-2.5 border border-gray-200
                      rounded-xl text-sm focus:outline-none focus:ring-2
                      focus:ring-gray-900"
                                        {...inputProps}
                                    />
                                </div>
                            </div>
                        ))}

                        <Button
                            type="submit"
                            loading={loading}
                            className="w-full mt-2"
                            size="lg">
                            Crear cuenta
                        </Button>
                    </form>
                </div>

                <p className="text-center text-sm text-gray-500 mt-4">
                    ¿Ya tienes cuenta?{' '}
                    <Link to="/login"
                        className="font-medium text-gray-900 hover:underline">
                        Inicia sesión
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default RegisterPage;