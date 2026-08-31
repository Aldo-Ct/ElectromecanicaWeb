import { Component, OnInit } from '@angular/core';
import { finalize, timeout } from 'rxjs';
import { ProductoPublico } from '../../modelos/dominio.model';
import { ProductoService } from '../../servicios/producto.service';

interface OpcionFiltro {
  id: string;
  nombre: string;
  cantidad: number;
}

@Component({
    selector: 'app-catalogo-productos',
    templateUrl: './catalogo-productos.component.html',
    styleUrls: ['./catalogo-productos.component.scss'],
    standalone: false
})
export class CatalogoProductosComponent implements OnInit {
  productos: ProductoPublico[] = [];
  productosFiltrados: ProductoPublico[] = [];
  categorias: OpcionFiltro[] = [];
  categoriasNavegacion: OpcionFiltro[] = [];
  marcas: OpcionFiltro[] = [];
  buscando = true;
  mensajeError = '';
  termino = '';
  categoriaSeleccionada = '';
  marcaSeleccionada = '';
  orden = 'recomendados';
  mostrarFiltros = false;

  constructor(private productoService: ProductoService) {}

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.buscando = true;
    this.mensajeError = '';

    this.productoService.listarPublicos().pipe(
      timeout({ first: 10000 }),
      finalize(() => this.buscando = false)
    ).subscribe({
      next: productos => {
        this.productos = productos;
        this.construirOpcionesFiltro();
        this.aplicarFiltros();
      },
      error: error => {
        this.mensajeError = error.name === 'TimeoutError'
          ? 'El servidor tardó demasiado en responder.'
          : error.message;
      }
    });
  }

  private construirOpcionesFiltro(): void {
    const conteo = new Map<string, OpcionFiltro>();
    this.productos.forEach(producto => {
      const id = String(producto.categoriaId);
      const existente = conteo.get(id);
      conteo.set(id, { id, nombre: producto.categoriaNombre, cantidad: (existente?.cantidad || 0) + 1 });
    });
    this.categorias = Array.from(conteo.values()).sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
    this.categoriasNavegacion = this.categorias.slice(0, 6);

    const conteoMarcas = new Map<string, OpcionFiltro>();
    this.productos.forEach(producto => {
      const existente = conteoMarcas.get(producto.marcaNombre);
      conteoMarcas.set(producto.marcaNombre, {
        id: producto.marcaNombre,
        nombre: producto.marcaNombre,
        cantidad: (existente?.cantidad || 0) + 1
      });
    });
    this.marcas = Array.from(conteoMarcas.values()).sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
  }

  aplicarFiltros(): void {
    const texto = this.normalizar(this.termino);
    const filtrados = this.productos.filter(producto => {
      const coincideTexto = !texto || this.normalizar([
        producto.nombre, producto.sku, producto.modelo, producto.marcaNombre,
        producto.categoriaNombre, producto.descripcion
      ].filter(Boolean).join(' ')).includes(texto);
      const coincideCategoria = !this.categoriaSeleccionada || String(producto.categoriaId) === this.categoriaSeleccionada;
      const coincideMarca = !this.marcaSeleccionada || producto.marcaNombre === this.marcaSeleccionada;
      return coincideTexto && coincideCategoria && coincideMarca;
    });

    this.productosFiltrados = filtrados.sort((a, b) => {
      if (this.orden === 'precio-menor') return a.precioVenta - b.precioVenta;
      if (this.orden === 'precio-mayor') return b.precioVenta - a.precioVenta;
      if (this.orden === 'nombre') return a.nombre.localeCompare(b.nombre, 'es');
      if (a.disponible !== b.disponible) return a.disponible ? -1 : 1;
      return a.nombre.localeCompare(b.nombre, 'es');
    });
  }

  seleccionarCategoria(id: string): void {
    this.categoriaSeleccionada = id && this.categoriaSeleccionada !== id ? id : '';
    this.mostrarFiltros = false;
    this.aplicarFiltros();
  }

  limpiarTermino(): void {
    this.termino = '';
    this.aplicarFiltros();
  }

  limpiarMarca(): void {
    this.marcaSeleccionada = '';
    this.aplicarFiltros();
  }

  limpiarFiltros(): void {
    this.termino = '';
    this.categoriaSeleccionada = '';
    this.marcaSeleccionada = '';
    this.orden = 'recomendados';
    this.aplicarFiltros();
  }

  identificarOpcion(_indice: number, opcion: OpcionFiltro): string {
    return opcion.id;
  }

  identificarProducto(_indice: number, producto: ProductoPublico): number {
    return producto.id;
  }

  enlaceConsulta(producto: ProductoPublico): string {
    const asunto = `Consulta de producto: ${producto.nombre}`;
    const cuerpo = `Hola, deseo consultar disponibilidad y atención para:\n\nProducto: ${producto.nombre}\nSKU: ${producto.sku}\nMarca: ${producto.marcaNombre}\nPrecio referencial: S/ ${producto.precioVenta.toFixed(2)}`;
    return `mailto:ventas@electromecanica.pe?subject=${encodeURIComponent(asunto)}&body=${encodeURIComponent(cuerpo)}`;
  }

  imagenProducto(producto: ProductoPublico): string {
    if (producto.imagenUrl?.trim()) return producto.imagenUrl;
    const categoria = this.normalizar(producto.categoriaNombre);
    if (categoria.includes('rodamiento') || categoria.includes('reductor')) {
      return '/assets/productos/rodamiento.svg';
    }
    if (categoria.includes('herramienta') || categoria.includes('bomba') || categoria.includes('valvula')) {
      return '/assets/productos/hidraulica.svg';
    }
    return '/assets/productos/electrico.svg';
  }

  ocultarImagenRota(evento: Event): void {
    const imagen = evento.target as HTMLImageElement;
    imagen.style.display = 'none';
    imagen.parentElement?.classList.add('sin-imagen');
  }

  private normalizar(valor: string | undefined): string {
    return (valor || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  }
}
