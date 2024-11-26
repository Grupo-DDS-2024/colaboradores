package ar.edu.utn.dds.k3003.model.Request;

import lombok.Getter;

@Getter
public class SuscripcionADesperfectoRequest {
    @Getter
    private  int heladera_id;

    public SuscripcionADesperfectoRequest(int heladera_id) {
        this.heladera_id = heladera_id;
    }

    public SuscripcionADesperfectoRequest() {
    }
}
