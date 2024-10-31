package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;

import ar.edu.utn.dds.k3003.model.Clases.Donacion;
import ar.edu.utn.dds.k3003.model.Clases.SuscripcionHeladera;
import ar.edu.utn.dds.k3003.model.DTOs.ColaboradorDTOActualizado;
import ar.edu.utn.dds.k3003.model.Request.*;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborarActualizadoEnum;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.step.StepMeterRegistry;

import java.time.LocalDateTime;
import java.util.*;

public class ColaboradorController {

    private final Fachada fachada;
    private final StepMeterRegistry stepMeterRegistry;
    private Counter contadorColaboradores;
    private Counter formasModificadas;

    public ColaboradorController(Fachada fachada, StepMeterRegistry stepMeterRegistry) {
        this.fachada = fachada;
        this.stepMeterRegistry = stepMeterRegistry;
        this.contadorColaboradores = stepMeterRegistry.counter("ddsColaboradores.colaboradoresAgregados");
        this.formasModificadas = stepMeterRegistry.counter("ddsColaboradores.colaboradoresModificados");
        var gauge = stepMeterRegistry.gauge("ddsColaboradores.CantColaboradoresEnBD", fachada, f -> f.cantColaboradores());

    }

    public void agregar(Context context) {
        try {
            var colaboradorDTOActualizado = context.bodyAsClass(ColaboradorDTOActualizado.class);
            var colaboradorDTORta = this.fachada.agregar(colaboradorDTOActualizado);
            contadorColaboradores.increment();
            context.json(colaboradorDTORta);
            context.status(HttpStatus.CREATED);
        } catch (NoSuchElementException ex) {
            context.result(ex.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
        }
    }

    public void modificar(Context context) {
        var id = context.pathParamAsClass("colaboradorId", Long.class).get();
        List<FormaDeColaborarActualizadoEnum> formas = context.bodyAsClass(UpdateFormasColaborarRequest.class).getFormas();
        try{
            var colaboradorDTO = this.fachada.modificar(id, formas);
            context.json(colaboradorDTO);
        } catch (NoSuchElementException ex) {
            context.result(ex.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
        }
    }

    public void obtener(Context context) {
        var id = context.pathParamAsClass("colaboradorId", Long.class).get();
        try {
            var colaboradorDTO = this.fachada.buscarXId(id);
            context.json(colaboradorDTO);
        } catch (NoSuchElementException ex) {
            context.result(ex.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
        }
    }

    public void puntos(Context context) {
        var id = context.pathParamAsClass("colaboradorId", Long.class).get();
        try {
            var puntosColaborador = fachada.puntos(id);
            context.json(puntosColaborador);
        } catch (NoSuchElementException ex) {
            context.result(ex.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
        }
    }

    public void actualizarPesosPuntos(Context context) {
        try {
            // Obtener los parámetros del cuerpo de la solicitud
            double pesosDonados = context.bodyAsClass(UpdatePesosPuntosRequest.class).getPesosDonados();
            double viandasDistribuidas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getViandasDistribuidas();
            double viandasDonadas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getViandasDonadas();
            double tarjetasRepartidas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getTarjetasRepartidas();
            double heladerasActivas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getHeladerasActivas();

            // Actualizar los coeficientes de puntos
            this.fachada.actualizarPesosPuntos(
                    pesosDonados,
                    viandasDistribuidas,
                    viandasDonadas,
                    tarjetasRepartidas,
                    heladerasActivas
            );
            context.result("Puntos correctamente actualizados");
            context.status(HttpStatus.OK);

        } catch (Exception e) {
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
        }
    }

    // Para ello expondrá un endpoint a ser utilizado por este sistema de terceros. El mismo recibirá el monto, la fecha de acreditación
    // y el colaboradorId. Esto influirá en el cálculo de puntos más adelante.
    public void recibirDonacion(Context context) {
        try {
            var donacion = context.bodyAsClass(Donacion.class);
            Donacion donacionFix = new Donacion(donacion.getMonto(), LocalDateTime.now(), donacion.getColaboradorId());
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Donación registrada correctamente");
            response.put("Donación", donacionFix);
            this.fachada.registrarDonacion(donacionFix);
            context.status(200).json(response);

        } catch (IllegalArgumentException e1){
            throw new BadRequestResponse("El ID del Colaborador no existe.");
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }

    }

    public void arreglarHeladera(Context context) {
        var incidente_id = context.pathParamAsClass("id_incidente", Long.class).get();
        int heladeraId = context.bodyAsClass(ArreglarHeladeraRequest.class).getHeladera_id();
        Long colaboradorId = context.bodyAsClass(ArreglarHeladeraRequest.class).getColaborador_id();
        try {
            this.fachada.registrarArreglo(incidente_id,colaboradorId, heladeraId);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Heladera arreglada correctametne");
            context.status(200).json(response);
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }
    }
    public void suscripcionAPocasViandas(Context context){
        var id = context.pathParamAsClass("colaboradorId", Long.class).get();

        //try {
            int heladera_id = context.bodyAsClass(SuscripcionCantViandasRequest.class).getHeladera_id();
            int cantViandas = context.bodyAsClass(SuscripcionCantViandasRequest.class).getCantidadViandas();
            SuscripcionHeladera suscripcion = this.fachada.suscribirseAPocasViandas(id,heladera_id,cantViandas);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Suscripcion registrada correctamente");
            response.put("Suscripcion ID", suscripcion.getId());
            context.status(200).json(response);
        //} catch (Exception e){
        //    throw new BadRequestResponse("Error de solicitud.");
        //}
    }

    public void suscripcionAFaltanViandas(Context context){
        var id = context.pathParamAsClass("colaboradorId", Long.class).get();

        try {
            int heladera_id = context.bodyAsClass(SuscripcionCantViandasRequest.class).getHeladera_id();
            int cantViandas = context.bodyAsClass(SuscripcionCantViandasRequest.class).getCantidadViandas();
            this.fachada.suscribirseAFaltanViandas(id,heladera_id,cantViandas);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Suscripcion registrada correctamente");
            context.status(200).json(response);
        }catch (Exception e){
            throw new BadRequestResponse("Error de solicitud.");
        }

    }

    public void suscripcionADesperfecto(Context context){
        var id = context.pathParamAsClass("colaboradorId", Long.class).get();
        try{
            int heladera_id = context.bodyAsClass(SuscripcionADesperfectoRequest.class).getHeladera_id();
            this.fachada.suscribirseADesperfecto(id,heladera_id);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Suscripcion registrada correctamente");
            context.status(200).json(response);
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }
    }

    public void desuscribirse(Context context){
        var id = context.pathParamAsClass("id",Long.class).get();
        try {
            this.fachada.desuscribirse(id);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Desuscrito correctamente");
            context.status(200).json(response);
        }catch (Exception e){
            throw new BadRequestResponse("Error de solicitud.");
        }
    }

    public void getSuscripcion(Context context){
        var id = context.pathParamAsClass("id",Long.class).get();
        try{
            SuscripcionHeladera suscripcionHeladera= this.fachada.getSuscripcion(id);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Donación registrada correctamente");
            response.put("Suscripcion", suscripcionHeladera);
            context.status(200).json(response);
        }catch (Exception e){
            throw new BadRequestResponse("Error de solicitud.");
        }
    }
    public void reportarFalla(Context context){
        var id = context.pathParamAsClass("id",Integer.class).get();
        try {
            this.fachada.reportarFalla(id);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Falla reportada correctamente");
            context.status(200).json(response);
        }catch (Exception e){
            throw new BadRequestResponse("Error de solicitud.");
        }
    }


}
