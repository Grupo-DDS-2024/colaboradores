package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.model.TipoCoeficiente;
import ar.edu.utn.dds.k3003.model.UpdateFormasColaborarRequest;
import ar.edu.utn.dds.k3003.model.UpdatePesosPuntosRequest;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.DonadorDeViandas;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.Transportador;
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
            var colaboradorDTO = context.bodyAsClass(ColaboradorDTO.class);
            var colaboradorDTORta = this.fachada.agregar(colaboradorDTO);
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

        List<FormaDeColaborarEnum> formas = context.bodyAsClass(UpdateFormasColaborarRequest.class).getFormas();
        // {
        //  "formas": [
        //    "DONADOR",
        //    "TRANSPORTADOR"
        //  ]
        //}

        fachada.agregarFormasDeColaborar(formas, id);


        try {
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
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }

    }

    public void arreglarHeladera(Context context) {
        try {
            ColaboradorDTO colaboradorDTO = context.bodyAsClass(ColaboradorDTO.class);
            Long colaboradorId = colaboradorDTO.getId();
            this.fachada.registrarArreglo(colaboradorId);
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }
    }
}
