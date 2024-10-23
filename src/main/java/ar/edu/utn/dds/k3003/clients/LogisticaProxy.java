package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaLogistica;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.RutaDTO;
import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import ar.edu.utn.dds.k3003.facades.exceptions.TrasladoNoAsignableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.NoSuchElementException;

public class LogisticaProxy implements FachadaLogistica {
    private final String endpoint;
    private final LogisticaRetrofitClient service;

    public LogisticaProxy(ObjectMapper objectMapper) {
        var env = System.getenv();
        this.endpoint = env.getOrDefault("URL_LOGISTICA", "http://localhost:8082/");

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(this.endpoint)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(LogisticaRetrofitClient.class);
    }


    @Override
    public RutaDTO agregar(RutaDTO rutaDTO) {
        return null;
    }

    @Override
    public TrasladoDTO buscarXId(Long aLong) throws NoSuchElementException {
        return null;
    }

    @Override
    public TrasladoDTO asignarTraslado(TrasladoDTO trasladoDTO) throws TrasladoNoAsignableException {
        return null;
    }

    @SneakyThrows
    @Override
    public List<TrasladoDTO> trasladosDeColaborador(Long colaboradorId, Integer mes, Integer anio) {
        Response<List<TrasladoDTO>> execute = service.get(colaboradorId, mes, anio).execute();

        if (execute.isSuccessful()) {
            return execute.body();
        }
        if (execute.code() == 500) {
            throw new NoSuchElementException("No se encontraron traslados del colaborador " + colaboradorId);
        }
        throw new RuntimeException("Error al conectarse con el componente Logistica");
    }

    @Override
    public void setHeladerasProxy(FachadaHeladeras fachadaHeladeras) {

    }

    @Override
    public void setViandasProxy(FachadaViandas fachadaViandas) {

    }

    @Override
    public void trasladoRetirado(Long aLong) {

    }

    @Override
    public void trasladoDepositado(Long aLong) {

    }
}
