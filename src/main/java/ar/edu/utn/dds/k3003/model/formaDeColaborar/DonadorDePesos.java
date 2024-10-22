package ar.edu.utn.dds.k3003.model.formaDeColaborar;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.model.Donacion;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class DonadorDePesos extends Implementacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    @Transient
    double coeficiente = 0.5;
    @Transient
    Fachada colaboradorInstancia;

    @Getter
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
