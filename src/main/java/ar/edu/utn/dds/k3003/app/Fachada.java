package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.clients.HeladeraProxy;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import ar.edu.utn.dds.k3003.model.Clases.*;
import ar.edu.utn.dds.k3003.model.Enums.EstadoIncidenteEnum;
import ar.edu.utn.dds.k3003.model.Enums.TipoIncidenteEnum;
import ar.edu.utn.dds.k3003.model.Enums.TipoNotificacionEnum;
import ar.edu.utn.dds.k3003.model.FachadaColaboradores;
import ar.edu.utn.dds.k3003.facades.FachadaLogistica;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.model.DTOs.ColaboradorDTOActualizado;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborarActualizadoEnum;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.*;
import ar.edu.utn.dds.k3003.repositories.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Fachada implements FachadaColaboradores {
    @Getter
    private ColaboradorRepository colaboradorRepository;
    private ColaboradorMapper colaboradorMapper = new ColaboradorMapper();
    private CoeficientesPuntos coeficientesPuntos = new CoeficientesPuntos(0.5, 1, 1.5, 2.5, 5);
    @Getter
    private FachadaViandas fachadaViandas;
    @Getter
    private FachadaLogistica fachadaLogistica;
    @Getter
    private HeladeraProxy fachadaHeladeras;
    private EntityManagerFactory entityManagerFactory;

    private DonacionesRepository donacionesRepository;
    private SuscripcionRepository suscripcionRepository;
    private IncidentesRepository incidentesRepository;
    @Setter
    private TelegramBot bot;

//  public Fachada() {
//    this.entityManagerFactory = Persistence.createEntityManagerFactory("defaultdb");
//    this.entityManager = entityManagerFactory.createEntityManager();
//    this.colaboradorRepository = new ColaboradorRepository();
//    this.colaboradorRepository.setEntityManager(entityManager);
//    this.colaboradorMapper = new ColaboradorMapper();
//  }

    public Fachada(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.colaboradorRepository = new ColaboradorRepository(entityManagerFactory);
        this.donacionesRepository = new DonacionesRepository(entityManagerFactory);
        this.suscripcionRepository = new SuscripcionRepository(entityManagerFactory);
        this.incidentesRepository= new IncidentesRepository(entityManagerFactory);
    }

    public Fachada() {

    }


    @Override
    public ColaboradorDTOActualizado agregar(ColaboradorDTOActualizado colaboradorDTOActualizado) {
        Colaborador colaborador = new Colaborador(colaboradorDTOActualizado.getNombre(), colaboradorDTOActualizado.getFormas());
        colaborador = this.colaboradorRepository.save(colaborador);
        return colaboradorMapper.map(colaborador);
    }

    public ColaboradorDTOActualizado agregarDesdeBot(String chatId){
        List<FormaDeColaborarActualizadoEnum> formas = new ArrayList<>();
        formas.add(FormaDeColaborarActualizadoEnum.TECNICO);
        Colaborador colaborador = new Colaborador("pepe",formas,chatId);
        this.colaboradorRepository.save(colaborador);
        return this.colaboradorMapper.map(colaborador);
    }

    public boolean existeChat(String chatId){
        return this.colaboradorRepository.existeChat(chatId);
    }

    @Override
    public ColaboradorDTOActualizado buscarXId(Long colaboradorId) throws NoSuchElementException {
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        return colaboradorMapper.map(colaborador);
    }


    @Override
    public Double puntos(Long colaboradorId) throws NoSuchElementException {
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        List<FormaDeColaborarActualizadoEnum> formasDeColaborar = colaborador.getFormas();
        List<FormaDeColaborar> colaboraciones = new ArrayList<>();
        for (FormaDeColaborarActualizadoEnum forma:formasDeColaborar){
            switch (forma){
                case DONADOR_VIANDAS:
                    colaboraciones.add(DonadorDeViandas.getInstance(this.fachadaViandas));
                    break;
                case TRANSPORTADOR:
                    colaboraciones.add(Transportador.getInstance(this.fachadaLogistica));
                    break;
                case TECNICO:
                    colaboraciones.add(Tecnico.getInstance(this));
                    break;
                case DONADOR_DINERO:
                    colaboraciones.add(DonadorDePesos.getInstance(this));
                    break;
                default:
                    throw new IllegalArgumentException("Forma de colaborar desconocida");

            }
        }
        return colaboraciones.stream().mapToDouble(forma -> forma.calcularPuntos(colaboradorId)).sum();
    }

    @Override
    public ColaboradorDTOActualizado modificar(Long colaboradorId, List<FormaDeColaborarActualizadoEnum> formas) throws NoSuchElementException {
        // Buscar el colaborador por ID
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        colaboradorRepository.update(colaborador,formas);
        // Mapear y devolver el colaborador actualizado como DTO
        return colaboradorMapper.map(colaborador);
    }
    @Override
    public void actualizarPesosPuntos(Double pesosDonados, Double viandasDistribuidas, Double viandasDonadas, Double tarjetasRepartidas, Double heladerasActivas) {

        DonadorDePesos.getInstance(this).setCoeficiente(pesosDonados);
        Transportador.getInstance(fachadaLogistica).setCoeficiente(viandasDistribuidas);
        DonadorDeViandas.getInstance(fachadaViandas).setCoeficiente(viandasDonadas);
        Tecnico.getInstance(this).setCoeficiente(heladerasActivas);

    }

    public SuscripcionHeladera suscribirseAPocasViandas(Long colaborador_id, int heladera_id, int cantMinimaViandas){
        Colaborador colaborador = colaboradorRepository.findById(colaborador_id);
        SuscripcionHeladera suscripcion = new SuscripcionHeladera(colaborador,heladera_id,cantMinimaViandas,-1,false);
        this.suscripcionRepository.save(suscripcion);
        colaborador.suscribirseAHeladera(suscripcion);

        this.fachadaHeladeras.agregarSuscriptor(colaborador_id,heladera_id,cantMinimaViandas,-1,false);

        return suscripcion;
    }

    public void suscribirseAFaltanViandas(Long colaborador_id, int heladera_id, int viandasDisponibles){
        Colaborador colaborador = colaboradorRepository.findById(colaborador_id);
        SuscripcionHeladera suscripcion = new SuscripcionHeladera(colaborador,heladera_id,-1,viandasDisponibles,false);
        this.suscripcionRepository.save(suscripcion);
        colaborador.suscribirseAHeladera(suscripcion);
        this.fachadaHeladeras.agregarSuscriptor(colaborador_id,heladera_id,-1,viandasDisponibles,false);
    }

    public void suscribirseADesperfecto(Long colaborador_id, int heladera_id){
        Colaborador colaborador = colaboradorRepository.findById(colaborador_id);
        SuscripcionHeladera suscripcion = new SuscripcionHeladera(colaborador,heladera_id,-1,-1,true);
        this.suscripcionRepository.save(suscripcion);
        colaborador.suscribirseAHeladera(suscripcion);
        this.fachadaHeladeras.agregarSuscriptor(colaborador_id,heladera_id,-1,-1,true);
    }

    public long cantColaboradores() {
        return colaboradorRepository.cantColaboradores();
    }

    @Override
    public void setLogisticaProxy(FachadaLogistica fachadaLogistica) {
        this.fachadaLogistica = fachadaLogistica;
    }

    @Override
    public void setViandasProxy(FachadaViandas fachadaViandas) {
        this.fachadaViandas = fachadaViandas;
    }
    public void setHeladeraProxy(HeladeraProxy fachadaHeladeras){
        this.fachadaHeladeras=fachadaHeladeras;
    }

    public void registrarDonacion(Donacion donacion) { // habria q chequear si el colaboradorId no existe
        try {
            this.colaboradorRepository.findById(donacion.getColaboradorId());
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        this.donacionesRepository.save(donacion);
    }
    public List<Donacion> donacionesDelMes(int mesActual, int anioActual, Long colaboradorId){

        return this.donacionesRepository.donacionesDelMes(mesActual,anioActual,colaboradorId);
    }

    public void registrarArreglo(Long incidente_id,Long colaboradorId) {
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        Incidentes incidentes  = incidentesRepository.findById(incidente_id);
        if(incidentes.getEstado() == EstadoIncidenteEnum.ARREGLADO){
            throw new IllegalArgumentException("El incidente ya fue solucionado");
        }
        if(!colaborador.getFormas().contains(FormaDeColaborarActualizadoEnum.TECNICO)){
            throw new IllegalArgumentException("El colaborador no es técnico");
        }
        colaborador.arreglarHeladera();
        colaboradorRepository.save(colaborador);
        incidentes.cambiarEstado(EstadoIncidenteEnum.ARREGLADO);
        incidentesRepository.save(incidentes);
        this.fachadaHeladeras.arreglarHeladera(incidentes.getHeladeraId());
    }

    public int cantHeladerasReparadas(Long colaboradorId) {
        return this.colaboradorRepository.findById(colaboradorId).getCantHeladerasReparadas();
    }

    public void desuscribirse(Long suscripcionId){
        this.suscripcionRepository.delete(suscripcionId);
    }

    public SuscripcionHeladera getSuscripcion(Long id){
        return this.suscripcionRepository.findById(id);
    }

    public void reportarFalla(Integer heladeraId) {
        this.fachadaHeladeras.reportarDesperfecto(heladeraId);
    }
    public void registrarIncidente(int heladera_id, TipoIncidenteEnum tipo, EstadoIncidenteEnum estado){
        Incidentes incidentes = new Incidentes(heladera_id, LocalDateTime.now(),tipo,estado);
        this.incidentesRepository.save(incidentes);
    }

    public Long obtenerIdColaborador(String chatId) {
        return this.colaboradorRepository.findById(Long.parseLong(chatId)).getId();
    }

    public List<Incidentes> incidentes(){

        return  incidentesRepository.todos();
    }

    public void notificar(Long id, TipoNotificacionEnum tipo, Integer heladeraId){
        bot.sendMessage(id.toString(),"Se detecto un evento de tipo: "+ tipo.toString()+ ", de la heladera: "+heladeraId.toString());
    }

    public void notificarTraslado(TrasladoDTO trasladoDTO){
        bot.sendMessage(trasladoDTO.getColaboradorId().toString(), "Se le asignó el siguiente traslado: HeladeraOrigen: "+trasladoDTO.getHeladeraOrigen()+"\n"+
                "HeladeraDestino: "+trasladoDTO.getHeladeraDestino() +"\n"+"Para la vianda con QR: "+trasladoDTO.getQrVianda());
    }

    public List<SuscripcionHeladera> verSuscripciones(Long colaboradorId) {
        return this.suscripcionRepository.buscarSuscripcionesPorColaborador(this.colaboradorRepository.findById(colaboradorId));
    }
}
