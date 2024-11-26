package ar.edu.utn.dds.k3003.model.Request;

import ar.edu.utn.dds.k3003.model.Clases.SuscripcionHeladera;
import lombok.Getter;

@Getter
public class SuscripcionCantViandasRequest {
    @Getter
    private int cantidadViandas;
    @Getter
    private int heladera_id;

    public SuscripcionCantViandasRequest(Integer idHeladera, Integer cant){
        this.cantidadViandas=cant;
        this.heladera_id = idHeladera;
    }

    public SuscripcionCantViandasRequest() {
    }
}
