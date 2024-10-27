package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborar;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;



    @ElementCollection(targetClass = FormaDeColaborarEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "colaborador_formas", joinColumns = @JoinColumn(name = "colaborador_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "forma",nullable = false)
    private List<FormaDeColaborarEnum> formas = new ArrayList<>();


    @Transient
    private List<SuscripcionHeladera> suscripciones = new ArrayList<>();


    @Column(name= "cantHeladeras")
    private Integer cantHeladerasReparadas;



    public Colaborador(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;

    }

    public Colaborador(String nombre, List<FormaDeColaborarEnum> formas) {
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