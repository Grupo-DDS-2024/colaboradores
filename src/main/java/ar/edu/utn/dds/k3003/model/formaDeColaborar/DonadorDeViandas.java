package ar.edu.utn.dds.k3003.model.formaDeColaborar;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "formas_colaborar")
public class DonadorDeViandas implements  FormaDeColaborar{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    @Transient
    FachadaViandas fachadaViandas;
    @Setter
    @Transient
    Double coeficiente = 2.0;
    @Transient
    private static DonadorDeViandas instancia;

    public DonadorDeViandas(FachadaViandas fachadaViandas) {
        this.fachadaViandas = fachadaViandas;
    }

    @Override
    public Double calcularPuntos(Long colaboradorId){
        Integer mesActual = LocalDateTime.now().getMonthValue();
        Integer anioActual = LocalDateTime.now().getYear();

        Integer viandasDonadas = fachadaViandas.viandasDeColaborador(colaboradorId, mesActual, anioActual).size();

        return  coeficiente * viandasDonadas;

    }
    public DonadorDeViandas() {

    }


    public static DonadorDeViandas getInstance(FachadaViandas fachadaViandas){
        if(instancia == null){
            instancia = new DonadorDeViandas(fachadaViandas);
            System.out.println("se creo la instancia de donador");
        }
        if (instancia.fachadaViandas == null) {
            System.out.println("no hayy");
            instancia.fachadaViandas = fachadaViandas;
        }
        return instancia;
    }


}





