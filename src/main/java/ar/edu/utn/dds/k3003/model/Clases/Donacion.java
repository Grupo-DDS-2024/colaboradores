package ar.edu.utn.dds.k3003.model.Clases;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name ="Donacion")

public class Donacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    Long idDonacion;
    @Getter
    @Column(name = "montoDonado")
    Double monto;
    @Column(name = "fechaDonacion")
    @Getter
    LocalDateTime fechaDonacion;

    @Getter
    @JoinColumn (name = "colaborador_id", nullable = false)
    Long colaboradorId;


    public Donacion(Double monto, LocalDateTime fechaDonacion, Long colaboradorId) {
        this.monto = monto;
        this.fechaDonacion = fechaDonacion;
        this.colaboradorId = colaboradorId;
    }

    public Donacion() {

    }
}
