package ar.edu.utn.dds.k3003.model.Clases;


import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborarActualizadoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colaboradores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Colaborador  {

    @Id
    private Long id;

    @Column(name = "nombre")
    private String nombre;



    @ElementCollection(targetClass = FormaDeColaborarActualizadoEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "colaborador_formas", joinColumns = @JoinColumn(name = "colaborador_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "forma",nullable = false)
    private List<FormaDeColaborarActualizadoEnum> formas = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.ALL},fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    @Getter
    private List<NotificacionesHeladeras> notificaciones = new ArrayList<>();


    @Transient
    private List<SuscripcionHeladera> suscripciones = new ArrayList<>();


    @Column(name= "cantHeladeras")
    private Integer cantHeladerasReparadas=0;




    public Colaborador(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;

    }

    public Colaborador(String nombre, List<FormaDeColaborarActualizadoEnum> formas) {
        this.nombre=nombre;
        this.formas=formas;
    }

    public Colaborador(String nombre, List<FormaDeColaborarActualizadoEnum> formas,String chatId) {
        this.id=Long.parseLong(chatId);
        this.nombre=nombre;
        this.formas=formas;
    }

    public void suscribirseAHeladera(SuscripcionHeladera suscripcion){
        suscripciones.add(suscripcion);
    }

    @PreRemove
    private void preRemove() {
        this.formas.clear();
    }

    public void arreglarHeladera() {
        this.cantHeladerasReparadas += 1;
    }

}