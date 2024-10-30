package ar.edu.utn.dds.k3003.clients;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;


public interface HeladeraRetrofitClient {
    @POST("heladeras/suscripciones")
    Call<Void> agregar_suscripcion(@Body RequestBody requestBody);
    @POST("desperfecto/{id}")
    Call<Void> reportarDesperfecto(@Path("id") Integer heladeraId);
    @POST("arreglar/{id}")
    Call <Void> arreglarHeladera(@Path("id")Integer heladeraId);

}
