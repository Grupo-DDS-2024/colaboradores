package ar.edu.utn.dds.k3003.model.Request;

import lombok.Getter;

@Getter
public class ArreglarHeladeraRequest {
    public ArreglarHeladeraRequest(Long colaborador_id) {
        this.colaborador_id = colaborador_id;
    }

    @Getter
    Long colaborador_id;

    public ArreglarHeladeraRequest() {
    }
}
