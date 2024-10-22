package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.Implementacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.jetty.util.Uptime;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "colaboradores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;



    @Getter
    @Setter
    //@ElementCollection
    //@CollectionTable(name="formas de colaboradores",joinColumns = @JoinColumn(name="colaborador_id"))
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL},orphanRemoval = true)
    @JoinColumn(name = "colaborador_id")
    private List<Implementacion> formas = new ArrayList<>();


    @Column(name= "cantHeladeras")
    private Integer cantHeladerasReparadas;

    public Colaborador(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;

    }

    @PreRemove
    private void preRemove() {
        this.formas.clear();
    }

    public void arreglarHeladera() {
        this.cantHeladerasReparadas += 1;
    }

}