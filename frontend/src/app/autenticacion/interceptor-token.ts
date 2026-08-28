import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AutenticacionService } from '../servicios/autenticacion.service';

@Injectable()
export class InterceptorToken implements HttpInterceptor {
  constructor(private autenticacion: AutenticacionService) {}

  intercept(solicitud: HttpRequest<unknown>, siguiente: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.autenticacion.token();
    return siguiente.handle(token
      ? solicitud.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : solicitud);
  }
}
