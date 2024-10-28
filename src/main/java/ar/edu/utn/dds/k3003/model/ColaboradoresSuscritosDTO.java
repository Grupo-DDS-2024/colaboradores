package ar.edu.utn.dds.k3003.model;

import org.jetbrains.annotations.NotNull;

public class ColaboradoresSuscritosDTO {

    private Long colaborador_id;
    private int heladera_id;

    private int cantMinima = -1;

    private int viandasDisponibles = -1;

    private boolean notificarDesperfecto = false;


    public ColaboradoresSuscritosDTO(Long colaborador, @NotNull int heladera_id, int cantMinimaViandas, int viandasDisponible, boolean notificarDesperfecto) {
        this.colaborador_id = colaborador;
        this.heladera_id = heladera_id;
        this.cantMinima = cantMinimaViandas;
        this.viandasDisponibles = viandasDisponible;
        this.notificarDesperfecto = notificarDesperfecto;
    }

    public ColaboradoresSuscritosDTO() {

    }

}
