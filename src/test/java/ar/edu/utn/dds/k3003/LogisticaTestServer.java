package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.facades.dtos.EstadoTrasladoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;

import java.time.LocalDateTime;
import java.util.List;

import static ar.edu.utn.dds.k3003.app.WebApp.configureObjectMapper;

public class LogisticaTestServer {

    public static void main(String[] args) throws Exception {

        var env = System.getenv();

        var port = Integer.parseInt(env.getOrDefault("PORT", "8082"));

        var app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                configureObjectMapper(mapper);
            }));
        }).start(port);

        app.get("/colaboradores/{colaboradorId}/traslados", LogisticaTestServer::obtenerTrasladosColaborador);
    }

    private static void obtenerTrasladosColaborador(Context context) {
        try {
            // Obtener el ID del colaborador de los parámetros de consulta
            var colaboradorId = Long.parseLong(context.pathParam("colaboradorId"));

            // Verificar si el colaboradorId es 2
            if (colaboradorId == 1L) {
                // Crear algunos datos de traslado de ejemplo
                var trasladoDTO1 = new TrasladoDTO("QR001", EstadoTrasladoEnum.ENTREGADO, LocalDateTime.now(), 1, 2);
                var trasladoDTO2 = new TrasladoDTO("QR003", EstadoTrasladoEnum.ENTREGADO, LocalDateTime.now(), 1, 2);

                // Agregar los traslados a una lista
                List<TrasladoDTO> traslados = List.of(trasladoDTO1, trasladoDTO2);

                // Devolver los traslados al cliente
                context.json(traslados);
            } else {
                // Si el colaboradorId no es 2, devolver un mensaje de error al cliente
                context.result("No se encontraron traslados para el colaborador con ID: " + colaboradorId);
                context.status(HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            // Manejar el caso en que el ID del colaborador no sea un número válido
            context.result("El ID del colaborador proporcionado no es válido.");
            context.status(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Manejar otros errores inesperados
            context.result("Error al procesar la solicitud.");
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
