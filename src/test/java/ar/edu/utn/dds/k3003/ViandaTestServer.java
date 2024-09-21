package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;

import java.time.LocalDateTime;
import java.util.List;

import static ar.edu.utn.dds.k3003.app.WebApp.configureObjectMapper;

public class ViandaTestServer {

    public static void main(String[] args) throws Exception {

        var env = System.getenv();

        var port = Integer.parseInt(env.getOrDefault("PORT", "8081"));

        var app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                configureObjectMapper(mapper);
            }));
        }).start(port);

        app.get("/colaboradores/{colaboradorId}/viandas", ViandaTestServer::obtenerViandasColaborador);
    }

    private static void trasladoTest(Context context) {

        var trasladoDTO = context.bodyAsClass(TrasladoDTO.class);
        trasladoDTO.setId(14L);
        context.json(trasladoDTO);
    }

    private static void obtenerViandasColaborador(Context context) {
        try {
            // Parseo de parámetros de ruta
            var colaboradorId = Long.parseLong(context.pathParam("colaboradorId"));

            // Parseo de parámetros de consulta
            var mes = Integer.parseInt(context.queryParam("mes"));
            var anio = Integer.parseInt(context.queryParam("anio"));

            if (colaboradorId == 1L) {
                // Datos de ejemplo mejorados
                var viandaDTO1 = new ViandaDTO("QR001", LocalDateTime.now(), EstadoViandaEnum.PREPARADA, colaboradorId, 1);
                var viandaDTO2 = new ViandaDTO("QR002", LocalDateTime.now(), EstadoViandaEnum.EN_TRASLADO, colaboradorId, 1);
                var viandaDTO3 = new ViandaDTO("QR003", LocalDateTime.now(), EstadoViandaEnum.DEPOSITADA, colaboradorId, 1);

                // Lista de viandas para devolver
                List<ViandaDTO> viandas = List.of(viandaDTO1, viandaDTO2, viandaDTO3);
                context.json(viandas);
            } else {
                // Caso en que el colaborador no es encontrado
                context.result("Viandas no encontradas para el colaborador con ID: " + colaboradorId);
                context.status(HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            // Manejo de excepción para parámetros inválidos
            context.result("Parámetros de consulta inválidos.");
            context.status(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Manejo de otras posibles excepciones
            context.result("Error al procesar la solicitud.");
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
