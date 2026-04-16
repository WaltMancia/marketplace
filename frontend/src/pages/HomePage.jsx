import { Link } from 'react-router-dom';
import { ShoppingBag, Truck, Shield, Star } from 'lucide-react';
import Button from '../components/ui/Button.jsx';

const features = [
    { icon: Truck, title: 'Envío rápido', desc: 'Entrega en 24-48 horas' },
    { icon: Shield, title: 'Compra segura', desc: 'Pagos 100% protegidos' },
    { icon: Star, title: 'Calidad garantizada', desc: 'Vendedores verificados' },
];

const HomePage = () => (
    <div className="space-y-20">
        {/* Hero */}
        <section className="relative rounded-3xl overflow-hidden bg-gray-900
      text-white px-10 py-24">
            <div className="relative z-10 max-w-xl">
                <span className="inline-block bg-white/10 text-white text-sm
          px-3 py-1 rounded-full mb-4">
                    🚀 Miles de productos disponibles
                </span>
                <h1 className="text-5xl font-bold leading-tight mb-4">
                    El marketplace<br />para todos
                </h1>
                <p className="text-gray-400 text-lg mb-8 leading-relaxed">
                    Compra y vende productos con total confianza. La plataforma más
                    completa del mercado.
                </p>
                <div className="flex gap-3">
                    <Link to="/productos">
                        <Button size="lg" variant="secondary">
                            <ShoppingBag size={18} />
                            Explorar productos
                        </Button>
                    </Link>
                    <Link to="/registro">
                        <Button size="lg"
                            className="bg-white/10 hover:bg-white/20 border
                border-white/20 text-white">
                            Vender ahora
                        </Button>
                    </Link>
                </div>
            </div>
            <div className="absolute top-0 right-0 w-96 h-96 bg-white/5
        rounded-full -translate-y-1/2 translate-x-1/3" />
            <div className="absolute bottom-0 right-24 w-64 h-64 bg-white/5
        rounded-full translate-y-1/2" />
        </section>

        {/* Features */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {features.map(({ icon: Icon, title, desc }) => (
                <div key={title} className="flex items-start gap-4 p-6 bg-white
          rounded-2xl border border-gray-100 shadow-sm">
                    <div className="p-3 bg-gray-100 rounded-xl flex-shrink-0">
                        <Icon size={20} className="text-gray-700" />
                    </div>
                    <div>
                        <h3 className="font-semibold text-gray-900">{title}</h3>
                        <p className="text-sm text-gray-500 mt-0.5">{desc}</p>
                    </div>
                </div>
            ))}
        </section>
    </div>
);

export default HomePage;