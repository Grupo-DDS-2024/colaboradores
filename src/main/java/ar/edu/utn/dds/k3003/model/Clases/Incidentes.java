package ar.edu.utn.dds.k3003.model.Clases;

import ar.edu.utn.dds.k3003.model.Enums.TipoIncidenteEnum;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Incidentes")
@Getter
public class Incidentes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    Integer heladeraId;
    @Column
    LocalDateTime fechaIncidente;
    @Enumerated(EnumType.STRING)
    TipoIncidenteEnum tipoIncidente;

    public Incidentes(Integer heladeraId, LocalDateTime fechaIncidente, TipoIncidenteEnum tipoIncidente) {
        this.heladeraId = heladeraId;
        this.fechaIncidente = fechaIncidente;
        this.tipoIncidente = tipoIncidente;
    }
}
