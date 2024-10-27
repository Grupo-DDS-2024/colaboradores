package ar.edu.utn.dds.k3003.model;

import lombok.Getter;

import javax.persistence.*;

public class NotificacionesHeladeras {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "colaborador_id")
    private  Long colaborador_id;

    @Column
    private Long heladera_id;

    @Enumerated
    private TipoNotificacionEnum tipo;


}
