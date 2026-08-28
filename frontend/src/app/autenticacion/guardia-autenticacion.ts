import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AutenticacionService } from '../servicios/autenticacion.service';

@Injectable({ providedIn: 'root' })
export class GuardiaAutenticacion implements CanActivate {
  constructor(private autenticacion: AutenticacionService, private enrutador: Router) {}

  canActivate(_ruta: ActivatedRouteSnapshot, estado: RouterStateSnapshot): boolean | UrlTree {
    return this.autenticacion.autenticado()
      ? true
      : this.enrutador.createUrlTree(['/acceso'], { queryParams: { retorno: estado.url } });
  }
}
