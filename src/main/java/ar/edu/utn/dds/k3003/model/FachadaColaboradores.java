package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.clients.HeladeraProxy;
import ar.edu.utn.dds.k3003.facades.FachadaLogistica;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.model.Clases.Donacion;
import ar.edu.utn.dds.k3003.model.Clases.SuscripcionHeladera;
import ar.edu.utn.dds.k3003.model.DTOs.ColaboradorDTOActualizado;
import ar.edu.utn.dds.k3003.model.Enums.TipoIncidenteEnum;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.*;

import java.util.List;
import java.util.NoSuchElementException;

public interface FachadaColaboradores {

    ColaboradorDTOActualizado agregar(ColaboradorDTOActualizado colaboradorDTOActualizado);


    ColaboradorDTOActualizado buscarXId(Long colaboradorId) throws NoSuchElementException ;

    Double puntos(Long colaboradorId) throws NoSuchElementException ;


    ColaboradorDTOActualizado modificar(Long colaboradorId, List<FormaDeColaborarActualizadoEnum> formas);

    void actualizarPesosPuntos(Double pesosDonados, Double viandasDistribuidas, Double viandasDonadas, Double tarjetasRepartidas, Double heladerasActivas) ;

    SuscripcionHeladera suscribirseAPocasViandas(Long colaborador_id, int heladera_id, int cantMinimaViandas);

    void suscribirseAFaltanViandas(Long colaborador_id, int heladera_id, int viandasDisponibles);

    void suscribirseADesperfecto(Long colaborador_id, int heladera_id);

    long cantColaboradores();


    void setLogisticaProxy(FachadaLogistica fachadaLogistica);

    void setViandasProxy(FachadaViandas fachadaViandas);
    void setHeladeraProxy(HeladeraProxy fachadaHeladeras);

    void registrarDonacion(Donacion donacion);
    List<Donacion> donacionesDelMes(int mesActual, int anioActual, Long colaboradorId);

    void registrarArreglo(Long colaboradorId,Integer heladera_id) ;

    int cantHeladerasReparadas(Long colaboradorId) ;

    void desuscribirse(Long suscripcionId);

    SuscripcionHeladera getSuscripcion(Long id);

    void reportarFalla(Integer heladeraId);
    void registrarIncidente(int heladera_id, TipoIncidenteEnum tipo);

}
