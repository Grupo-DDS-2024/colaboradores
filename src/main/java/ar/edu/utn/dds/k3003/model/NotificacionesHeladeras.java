package ar.edu.utn.dds.k3003.model;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "Notificaciones")
public class NotificacionesHeladeras {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "colaborador_id")
    private  Long colaborador_id;

    @Column
    private int heladera_id;

    @Enumerated(EnumType.STRING)
    private TipoNotificacionEnum tipo;

    public NotificacionesHeladeras(Long colaborador_id, int heladera_id, TipoNotificacionEnum tipo){
        this.colaborador_id=colaborador_id;
        this.heladera_id=heladera_id;
        this.tipo=tipo;
    }

    public NotificacionesHeladeras() {

    }
}
