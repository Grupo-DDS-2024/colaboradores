package ar.edu.utn.dds.k3003.model.formaDeColaborar;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.model.Clases.Donacion;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "formas_colaborar")
public class DonadorDePesos implements FormaDeColaborar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    @Transient
    double coeficiente = 0.5;
    @Transient
    Fachada colaboradorInstancia;

    @Getter
    @Transient
    private static DonadorDePesos instancia = null;


    public DonadorDePesos(Fachada colaboradorInstancia) {
        this.colaboradorInstancia = colaboradorInstancia;
    }

    public DonadorDePesos() {

    }
    public static DonadorDePesos getInstance(Fachada colaboradorInstancia){
        if(instancia == null){
            instancia = new DonadorDePesos(colaboradorInstancia);
        }
        return instancia;
    }

    @Override
    public Double calcularPuntos(Long colaboradorId) {
        Integer mesActual = LocalDateTime.now().getMonthValue();
        Integer anioActual = LocalDateTime.now().getYear();
        List<Donacion> donaciones = colaboradorInstancia.donacionesDelMes(mesActual,anioActual,colaboradorId);
        return donaciones.stream().mapToDouble(Donacion::getMonto).sum();
    }
}
