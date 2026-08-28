import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AutenticacionService } from '../../servicios/autenticacion.service';

@Component({
  selector: 'app-acceso',
  templateUrl: './acceso.component.html',
  styleUrls: ['./acceso.component.scss']
})
export class AccesoComponent {
  procesando = false;
  mensajeError = '';
  mostrarContrasena = false;
  readonly formulario: FormGroup;

  constructor(
    private constructorFormulario: FormBuilder,
    private autenticacion: AutenticacionService,
    private enrutador: Router,
    private ruta: ActivatedRoute
  ) {
    this.formulario = this.constructorFormulario.group({
      correo: ['', [Validators.required, Validators.email]],
      contrasena: ['', Validators.required]
    });
    if (this.autenticacion.autenticado()) this.enrutador.navigate(['/panel']);
  }

  acceder(): void {
    if (this.formulario.invalid || this.procesando) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.procesando = true;
    this.mensajeError = '';
    const { correo, contrasena } = this.formulario.getRawValue();
    this.autenticacion.acceder(correo || '', contrasena || '')
      .pipe(finalize(() => this.procesando = false))
      .subscribe({
        next: () => this.enrutador.navigateByUrl(this.ruta.snapshot.queryParamMap.get('retorno') || '/panel'),
        error: error => this.mensajeError = error.message
      });
  }
}
