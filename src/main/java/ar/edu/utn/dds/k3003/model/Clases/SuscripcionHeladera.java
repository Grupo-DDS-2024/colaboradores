package ar.edu.utn.dds.k3003.model.Clases;

import ar.edu.utn.dds.k3003.model.Clases.Colaborador;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

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
    private int heladera_id;
    @Column
    private int cantMinimaViandas = -1;
    @Column
    private int viandasDisponible = -1;
    @Column
    private boolean notificarDesperfecto = false;
    @Column
    private String nombre;


    public SuscripcionHeladera(Colaborador colaborador, @NotNull int heladera_id, int cantMinimaViandas, int viandasDisponible, boolean notificarDesperfecto,String nombre) {
        this.colaborador = colaborador;
        this.heladera_id = heladera_id;
        this.cantMinimaViandas = cantMinimaViandas;
        this.viandasDisponible = viandasDisponible;
        this.notificarDesperfecto = notificarDesperfecto;
        this.nombre = nombre;
    }

    public SuscripcionHeladera() {

    }

    @Override
    public String toString(){
        if(nombre.equalsIgnoreCase("QuedanViandas")){
            return "Suscripcion: " +
                    "ID de la suscripción: "+id+"\n" +
                    "Suscripción: "+nombre+"\n"+
                    "ID de la heladera: "+heladera_id+"\n"+
                    "Valor de notificación: "+cantMinimaViandas+"\n";

        }
        if(nombre.equalsIgnoreCase("FaltanViandas")){
            return "Suscripcion: " +
                    "ID de la suscripción: "+id+"\n" +
                    "Suscripción: "+nombre+"\n"+
                    "ID de la heladera: "+heladera_id+"\n"+
                    "Valor de notificación: "+cantMinimaViandas+"\n";
        }else {
            return "Suscripcion: " +
                    "ID de la suscripción: "+id+"\n" +
                    "Suscripción: "+nombre+"\n"+
                    "ID de la heladera: "+heladera_id+"\n";
        }
    }
}
