package ar.edu.utn.dds.k3003.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.NoSuchElementException;

public class HeladeraProxy{

    private final String endpoint;
    private final HeladeraRetrofitClient service;

    public HeladeraProxy(ObjectMapper objectMapper) {
        // agregarSuscriptor(Long colaborador_id, Integer heladera_id, int cantMinima, int viandasDisponibles, boolean notificarDesperfecto){
        var env = System.getenv();
        this.endpoint = env.getOrDefault("URL_HELADERAS", "http://localhost:8080/");

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(this.endpoint)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(HeladeraRetrofitClient.class);
    }

    @SneakyThrows
    public void agregarSuscriptor(Long colaboradorid,int heladeraId, int cantMinima, int viandasDisponibles,boolean notificarDesperfecto){
        String json = "{"
                + "\"colaborador_id\": " + colaboradorid + ","
                + "\"heladera_id\": " + heladeraId + ","
                + "\"cantMinima\": " + cantMinima + ","
                + "\"viandasDisponibles\": " + viandasDisponibles + ","
                + "\"notificarDesperfecto\": " + notificarDesperfecto
                + "}";

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),json);

        Response<Void> execute = service.agregar_suscripcion(requestBody).execute();

        if (execute.isSuccessful()) {
            return;
        }
        if (execute.code() == 500) {
            throw new NoSuchElementException("Error 1");
        }
        throw new RuntimeException("Error al conectarse con el componente Logistica");
    }

    @SneakyThrows
    public void reportarDesperfecto(Integer heladeraId) {
        Response<Void> execute = service.reportarDesperfecto(heladeraId).execute();
        if (execute.isSuccessful()) {
            return;
        }
        if (execute.code() == 500) {
            throw new NoSuchElementException("Error 1");
        }
        throw new RuntimeException("Error al conectarse con el componente Logistica");
    }

    @SneakyThrows
    public void arreglarHeladera(Integer heladeraId){
        Response<Void> execute = service.arreglarHeladera(heladeraId).execute();
        if (execute.isSuccessful()) {
            return;
        }
        if (execute.code() == 500) {
            throw new NoSuchElementException("Error 1");
        }
        throw new RuntimeException("Error al conectarse con el componente Logistica");
    }
}


