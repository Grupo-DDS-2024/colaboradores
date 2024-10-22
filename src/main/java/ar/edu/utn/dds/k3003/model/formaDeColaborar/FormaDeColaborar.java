package ar.edu.utn.dds.k3003.model.formaDeColaborar;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

//@Embeddable

public interface FormaDeColaborar {

    public Double calcularPuntos(Long colaboradorId);
}
