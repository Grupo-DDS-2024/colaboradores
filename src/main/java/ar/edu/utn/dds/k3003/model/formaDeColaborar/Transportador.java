package ar.edu.utn.dds.k3003.model.formaDeColaborar;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaLogistica;
import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "formas_colaborar")
public class Transportador implements FormaDeColaborar{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Transient
    FachadaLogistica fachadaLogistica;
    @Transient
    Double coeficiente = 1.0;
    @Transient
    private static Transportador instancia = null;

    public Transportador(FachadaLogistica fachadaLogistica) {
        this.fachadaLogistica = fachadaLogistica;
    }
    public Transportador() {

    }
    public static Transportador getInstance(FachadaLogistica fachadaLogistica){
        if(instancia == null){
            instancia = new Transportador(fachadaLogistica);
        }
        return instancia;
    }

    @Override
    public Double calcularPuntos(Long colaboradorId) {
        Integer mesActual = LocalDateTime.now().getMonthValue();
        Integer anioActual = LocalDateTime.now().getYear();
        List<TrasladoDTO> trasladosDTO = fachadaLogistica.trasladosDeColaborador(colaboradorId, mesActual, anioActual);
        int traslados = trasladosDTO.size();

        return coeficiente * traslados;
    }
}