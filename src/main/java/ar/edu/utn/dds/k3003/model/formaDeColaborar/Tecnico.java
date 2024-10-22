package ar.edu.utn.dds.k3003.model.formaDeColaborar;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

public class Tecnico extends Implementacion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Transient
    Double coeficiente = 5.0;
    @Transient
    Fachada colaborador;
    private static Tecnico instancia = null;


    public Tecnico(Fachada colaborador) {
        this.colaborador = colaborador;
    }

    public static Tecnico getInstance(Fachada colaborador){
        if(instancia == null){
            instancia = new Tecnico(colaborador);
        }
        return instancia;
    }

    public Tecnico() {

    }

    @Override
    public Double calcularPuntos(Long colaboradorId){
        return colaborador.cantHeladerasReparadas(colaboradorId) * coeficiente;
    }
}
