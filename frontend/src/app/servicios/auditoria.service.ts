import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Auditoria } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  constructor(private api: ApiService) {}
  listar(): Observable<Auditoria[]> { return this.api.obtener<Auditoria[]>('/auditoria'); }
}
