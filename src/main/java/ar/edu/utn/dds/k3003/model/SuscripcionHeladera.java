package ar.edu.utn.dds.k3003.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import retrofit2.http.GET;

import javax.persistence.*;

@Entity
@Table(name = "suscripcion_a_heladera")
@Getter
public class SuscripcionHeladera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @ManyToOne
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    @Column(name = "id_heladera")
    @NotNull
    private Long heladera_id;
    @Column
    private int cantMinimaViandas = -1;
    @Column
    private int viandasDisponible = -1;
    @Column
    private boolean notificarDesperfecto = false;


    public SuscripcionHeladera(Colaborador colaborador, @NotNull Long heladera_id, int cantMinimaViandas, int viandasDisponible, boolean notificarDesperfecto) {
        this.colaborador = colaborador;
        this.heladera_id = heladera_id;
        this.cantMinimaViandas = cantMinimaViandas;
        this.viandasDisponible = viandasDisponible;
        this.notificarDesperfecto = notificarDesperfecto;
    }

    public SuscripcionHeladera() {

    }
}
