package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface ViandasRetrofitClient {

  @GET("viandas/search/findByColaboradorIdAndAnioAndMes")
  Call<List<ViandaDTO>> get(@Query("colaboradorId") Long colaboradorId,
                            @Query("mes") Integer mes,
                            @Query("anio") Integer anio);
}
