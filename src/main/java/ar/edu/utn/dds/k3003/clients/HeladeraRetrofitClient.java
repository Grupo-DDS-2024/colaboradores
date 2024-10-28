package ar.edu.utn.dds.k3003.clients;
import ar.edu.utn.dds.k3003.model.ColaboradoresSuscritosDTO;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;


public interface HeladeraRetrofitClient {
    @POST("heladeras/suscripciones")
    Call<Void> agregar_suscripcion(@Body RequestBody requestBody);
}
