import { Pipe, PipeTransform } from '@angular/core';

const FORMATEADOR_MONEDA = new Intl.NumberFormat('es-PE', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});

export function formatearMoneda(valor: number | string | null | undefined): string {
  const monto = Number(valor ?? 0);
  return `S/ ${FORMATEADOR_MONEDA.format(Number.isFinite(monto) ? monto : 0)}`;
}

@Pipe({
    name: 'moneda',
    standalone: false
})
export class MonedaPipe implements PipeTransform {
  transform(valor: number | string | null | undefined): string {
    return formatearMoneda(valor);
  }
}
