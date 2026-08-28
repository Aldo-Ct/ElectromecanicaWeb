import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { RolUsuario } from '../modelos/dominio.model';
import { AutenticacionService } from '../servicios/autenticacion.service';

@Injectable({ providedIn: 'root' })
export class GuardiaRol implements CanActivate {
  constructor(private autenticacion: AutenticacionService, private enrutador: Router) {}

  canActivate(ruta: ActivatedRouteSnapshot): boolean | UrlTree {
    const roles = (ruta.data['roles'] || []) as RolUsuario[];
    return !roles.length || this.autenticacion.tieneRol(roles)
      ? true
      : this.enrutador.createUrlTree(['/panel']);
  }
}
