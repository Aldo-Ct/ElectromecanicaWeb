import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core';

interface AplicacionCatalogo {
  nombre: string;
  detalle: string;
}

interface GrupoCatalogo {
  id: string;
  etiqueta: string;
  titulo: string;
  descripcion: string;
  imagen: string;
  textoAlternativo: string;
  aplicaciones: AplicacionCatalogo[];
}

@Component({
    selector: 'app-pagina-publica',
    templateUrl: './pagina-publica.component.html',
    styleUrls: ['./pagina-publica.component.scss'],
    standalone: false
})
export class PaginaPublicaComponent implements AfterViewInit, OnDestroy {
  menuAbierto = false;
  cabeceraCompacta = false;
  mostrarVolverArriba = false;
  progresoScroll = 0;
  movimientoHero = 0;
  categoriaActiva = 'electrico';
  catalogoAnimando = false;
  consultaAbierta = false;
  readonly anioActual = new Date().getFullYear();
  consulta = { nombre: '', empresa: '', correo: '', producto: '', mensaje: '' };
  private observador?: IntersectionObserver;
  private fotogramaScroll?: number;
  private temporizadorCatalogo?: ReturnType<typeof setTimeout>;

  readonly categorias: GrupoCatalogo[] = [
    {
      id: 'electrico',
      etiqueta: 'Energía y control',
      titulo: 'Controla, protege y transforma la energía.',
      descripcion: 'Componentes para maniobra, accionamiento y distribución en entornos industriales.',
      imagen: 'https://images.pexels.com/photos/33706880/pexels-photo-33706880.jpeg?auto=compress&cs=tinysrgb&w=1400',
      textoAlternativo: 'Sala industrial con tableros eléctricos de control',
      aplicaciones: [
        { nombre: 'Motores y accionamiento', detalle: 'Motores trifásicos, variadores y soluciones de potencia.' },
        { nombre: 'Control y maniobra', detalle: 'Contactores y componentes para automatización.' },
        { nombre: 'Protección eléctrica', detalle: 'Interruptores y equipos para distribución segura.' }
      ]
    },
    {
      id: 'mecanico',
      etiqueta: 'Movimiento mecánico',
      titulo: 'Precisión para transmitir fuerza y movimiento.',
      descripcion: 'Elementos mecánicos seleccionados para continuidad, ajuste y desempeño operativo.',
      imagen: 'https://images.pexels.com/photos/28752152/pexels-photo-28752152.jpeg?auto=compress&cs=tinysrgb&w=1400',
      textoAlternativo: 'Detalle de engranajes y componentes mecánicos industriales',
      aplicaciones: [
        { nombre: 'Rodamientos', detalle: 'Alternativas para diferentes cargas y velocidades.' },
        { nombre: 'Reductores y transmisión', detalle: 'Componentes para adaptar torque y movimiento.' },
        { nombre: 'Bombas y válvulas', detalle: 'Equipos para conducción y control de fluidos.' }
      ]
    },
    {
      id: 'abastecimiento',
      etiqueta: 'Mantenimiento y suministro',
      titulo: 'Abastecimiento preparado para el trabajo diario.',
      descripcion: 'Repuestos, herramientas y control de disponibilidad para atender requerimientos operativos.',
      imagen: 'https://images.pexels.com/photos/37340066/pexels-photo-37340066.jpeg?auto=compress&cs=tinysrgb&w=1400',
      textoAlternativo: 'Técnico realizando mantenimiento de maquinaria industrial',
      aplicaciones: [
        { nombre: 'Herramientas industriales', detalle: 'Elementos de apoyo para instalación y mantenimiento.' },
        { nombre: 'Repuestos identificados', detalle: 'Búsqueda por modelo, aplicación o referencia.' },
        { nombre: 'Entrega documentada', detalle: 'Control del pedido desde la consulta hasta el despacho.' }
      ]
    }
  ];

  cerrarMenu(): void {
    this.menuAbierto = false;
  }

  get categoriaSeleccionada(): GrupoCatalogo {
    return this.categorias.find(categoria => categoria.id === this.categoriaActiva) || this.categorias[0];
  }

  seleccionarCategoria(id: string): void {
    if (id === this.categoriaActiva || this.catalogoAnimando) {
      return;
    }

    this.catalogoAnimando = true;
    if (this.temporizadorCatalogo) {
      clearTimeout(this.temporizadorCatalogo);
    }
    this.temporizadorCatalogo = setTimeout(() => {
      this.categoriaActiva = id;
      requestAnimationFrame(() => this.catalogoAnimando = false);
    }, 150);
  }

  enviarConsulta(): void {
    const asunto = `Consulta comercial: ${this.consulta.producto || 'requerimiento industrial'}`;
    const cuerpo = [
      `Nombre: ${this.consulta.nombre}`,
      `Empresa: ${this.consulta.empresa || 'No indicada'}`,
      `Correo: ${this.consulta.correo}`,
      `Producto o aplicación: ${this.consulta.producto}`,
      '',
      this.consulta.mensaje || 'Solicito información y disponibilidad.'
    ].join('\n');
    this.consultaAbierta = true;
    window.location.href = `mailto:ventas@electromecanica.pe?subject=${encodeURIComponent(asunto)}&body=${encodeURIComponent(cuerpo)}`;
  }

  volverArriba(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  @HostListener('window:scroll')
  actualizarNavegacion(): void {
    if (this.fotogramaScroll !== undefined) {
      return;
    }

    this.fotogramaScroll = requestAnimationFrame(() => {
      const desplazamiento = window.scrollY;
      const recorrido = document.documentElement.scrollHeight - window.innerHeight;
      const movimientoReducido = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

      this.cabeceraCompacta = desplazamiento > 32;
      this.mostrarVolverArriba = desplazamiento > 650;
      this.progresoScroll = recorrido > 0 ? Math.min(100, (desplazamiento / recorrido) * 100) : 0;
      this.movimientoHero = movimientoReducido ? 0 : Math.min(72, desplazamiento * .1);
      this.fotogramaScroll = undefined;
    });
  }

  ngAfterViewInit(): void {
    this.actualizarNavegacion();
    const elementos = Array.from(document.querySelectorAll<HTMLElement>('.sitio-publico [data-reveal]'));
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches || !('IntersectionObserver' in window)) {
      elementos.forEach(elemento => elemento.classList.add('visible'));
      return;
    }
    this.observador = new IntersectionObserver(entradas => {
      entradas.forEach(entrada => {
        if (entrada.isIntersecting) {
          entrada.target.classList.add('visible');
          this.observador?.unobserve(entrada.target);
        }
      });
    }, { threshold: .14, rootMargin: '0px 0px -40px' });
    elementos.forEach(elemento => this.observador?.observe(elemento));
  }

  ngOnDestroy(): void {
    this.observador?.disconnect();
    if (this.fotogramaScroll !== undefined) {
      cancelAnimationFrame(this.fotogramaScroll);
    }
    if (this.temporizadorCatalogo) {
      clearTimeout(this.temporizadorCatalogo);
    }
  }
}
