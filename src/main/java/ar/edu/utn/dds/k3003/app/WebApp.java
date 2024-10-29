package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.clients.HeladeraProxy;
import ar.edu.utn.dds.k3003.clients.LogisticaProxy;
import ar.edu.utn.dds.k3003.clients.ViandasProxy;
import ar.edu.utn.dds.k3003.controller.ColaboradorController;
import ar.edu.utn.dds.k3003.facades.dtos.Constants;
import ar.edu.utn.dds.k3003.utils.DataDogsUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.micrometer.MicrometerPlugin;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class WebApp {

    public static void main(String[] args) throws IOException, TimeoutException {
        var env = System.getenv();

        EntityManagerFactory entityManagerFactory = startEntityManagerFactory();
        var fachada = new Fachada(entityManagerFactory);
        var objectMapper = createObjectMapper();
        fachada.setViandasProxy(new ViandasProxy(objectMapper));
        fachada.setLogisticaProxy(new LogisticaProxy(objectMapper));
        fachada.setHeladeraProxy(new HeladeraProxy(objectMapper));
        var DDUtils = new DataDogsUtils("Colaboradores");
        var registro = DDUtils.getRegistro();

        // Metricas
        final var gauge = registro.gauge("ddsColaboradores.unGauge", new AtomicInteger(0));

        // Config
        final var micrometerPlugin = new MicrometerPlugin(config -> config.registry = registro);

        Map<String, String> envMQ = System.getenv();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(envMQ.get("NOTIFICACIONES_HOST"));
        factory.setUsername(envMQ.get("NOTIFICACIONES_USERNAME"));
        factory.setPassword(envMQ.get("NOTIFICACIONES_PASSWORD"));
        // En el plan más barato, el VHOST == USER
        factory.setVirtualHost(envMQ.get("NOTIFICACIONES_USERNAME"));
        String queueName = envMQ.get("NOTIFICACIONES_NAME");
        Connection connection = factory.newConnection();
        com.rabbitmq.client.Channel channel = connection.createChannel();

        EntityManagerFactory entityManagerFactory2 = WebApp.startEntityManagerFactory();

        ColaboradoresWorker worker = new ColaboradoresWorker(channel, queueName, entityManagerFactory2);
        worker.init();

        var port = Integer.parseInt(env.getOrDefault("PORT", "8082"));

        var app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                configureObjectMapper(mapper);
            }));

            config.registerPlugin(micrometerPlugin);

        }).start(port);

        var colaboradorController = new ColaboradorController(fachada, registro);

        app.post("/colaboradores", colaboradorController::agregar);
        app.get("/colaboradores/{colaboradorId}", colaboradorController::obtener);
        app.patch("/colaboradores/{colaboradorId}", colaboradorController::modificar);
        app.get("/colaboradores/{colaboradorId}/puntos", colaboradorController::puntos);
        app.put("/formula", colaboradorController::actualizarPesosPuntos);
        app.put("/donar", colaboradorController::recibirDonacion);
        app.put("/arreglar", colaboradorController::arreglarHeladera);
        app.put("/colaboradores/{colaboradorId}/suscripcionAPocasViandas",colaboradorController::suscripcionAPocasViandas);
        app.put("/colaboradores/{colaboradorId}/suscripcionAFaltanViandas",colaboradorController::suscripcionAFaltanViandas);
        app.put("/colaboradores/{colaboradorId}/suscripcionADesperfecto",colaboradorController::suscripcionADesperfecto);
        //app.delete("/colaboradores", colaboradorController::cleanUp);

        app.delete("/suscripcion/{id}",colaboradorController::desuscribirse);
        app.get("/suscripcion/{id}",colaboradorController::getSuscripcion);

    }

    public static ObjectMapper createObjectMapper() {
        var objectMapper = new ObjectMapper();
        configureObjectMapper(objectMapper);
        return objectMapper;
    }

    public static void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var sdf = new SimpleDateFormat(Constants.DEFAULT_SERIALIZATION_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(sdf);
    }

    public static EntityManagerFactory startEntityManagerFactory() {
        Map<String, String> env = System.getenv();
        Map<String, Object> configOverrides = new HashMap<String, Object>();
        String[] keys = new String[]{"javax.persistence.jdbc.url", "javax.persistence.jdbc.user",
                "javax.persistence.jdbc.password", "javax.persistence.jdbc.driver", "hibernate.hbm2ddl.auto",
                "hibernate.connection.pool_size", "hibernate.show_sql"};
        for (String key : keys) {
            if (env.containsKey(key)) {
                String value = env.get(key);
                configOverrides.put(key, value);
            }
        }
        return Persistence.createEntityManagerFactory("defaultdb", configOverrides);
    }
}
