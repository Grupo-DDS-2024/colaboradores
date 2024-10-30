package ar.edu.utn.dds.k3003.model.Request;


import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborarActualizadoEnum;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateFormasColaborarRequest {
    private List<FormaDeColaborarActualizadoEnum> formas;

    public UpdateFormasColaborarRequest() {
    }

}
