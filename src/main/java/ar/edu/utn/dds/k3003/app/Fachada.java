package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaLogistica;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;

import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import ar.edu.utn.dds.k3003.model.CoeficientesPuntos;
import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.model.TipoCoeficiente;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.*;
import ar.edu.utn.dds.k3003.repositories.ColaboradorMapper;
import ar.edu.utn.dds.k3003.repositories.ColaboradorRepository;
import ar.edu.utn.dds.k3003.repositories.DonacionesRepository;
import lombok.Getter;
import org.eclipse.jetty.util.Uptime;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

public class Fachada implements ar.edu.utn.dds.k3003.facades.FachadaColaboradores {
    @Getter
    private ColaboradorRepository colaboradorRepository;
    private ColaboradorMapper colaboradorMapper = new ColaboradorMapper();
    private CoeficientesPuntos coeficientesPuntos = new CoeficientesPuntos(0.5, 1, 1.5, 2.5, 5);
    @Getter
    private FachadaViandas fachadaViandas;
    @Getter
    private FachadaLogistica fachadaLogistica;
    private EntityManagerFactory entityManagerFactory;

    private DonacionesRepository donacionesRepository;

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
    }

    public Fachada() {

    }


    @Override
    public ColaboradorDTO agregar(ColaboradorDTO colaboradorDTO) {
        Colaborador colaborador = new Colaborador(colaboradorDTO.getId(), colaboradorDTO.getNombre());
        colaborador = this.colaboradorRepository.save(colaborador);
        this.agregarFormasDeColaborar(colaboradorDTO.getFormas(),colaborador.getId());
        return colaboradorMapper.map(colaborador);
    }

    @Override
    public ColaboradorDTO buscarXId(Long colaboradorId) throws NoSuchElementException {
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        return colaboradorMapper.map(colaborador);
    }


    @Override
    public Double puntos(Long colaboradorId) throws NoSuchElementException {
//        Integer mesActual = LocalDateTime.now().getMonthValue();
//        Integer anioActual = LocalDateTime.now().getYear();
//
//        List<ViandaDTO> viandasDTO = fachadaViandas.viandasDeColaborador(colaboradorId, mesActual, anioActual);
//        Integer viandasDonadas = viandasDTO.size();
//        List<TrasladoDTO> trasladosDTO = fachadaLogistica.trasladosDeColaborador(colaboradorId, mesActual, anioActual);
//        Integer traslados = trasladosDTO.size();
//
//        return
//                coeficientesPuntos.getValor(TipoCoeficiente.VIANDAS_DONADAS) * viandasDonadas +
//                        coeficientesPuntos.getValor(TipoCoeficiente.VIANDAS_DISTRIBUIDAS) * traslados;
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId);
        return colaborador.getFormas().stream().mapToDouble(forma -> forma.calcularPuntos(colaboradorId)).sum();
    }



    @Override
    public ColaboradorDTO modificar(Long colaboradorId, List<FormaDeColaborarEnum> formas) throws NoSuchElementException {
        // Buscar el colaborador por ID
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        colaboradorRepository.update(colaborador, castearFormaDeColaborar(formas));
        // Mapear y devolver el colaborador actualizado como DTO
        return colaboradorMapper.map(colaborador);
}


//    public ColaboradorDTO modificar2(Long colaboradorId, List<FormaDeColaborarEnum> formas) throws NoSuchElementException {
//        // Buscar el colaborador por ID
//        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
//        colaboradorRepository.update(colaborador, formas);
//        // Mapear y devolver el colaborador actualizado como DTO
//        return colaboradorMapper.map(colaborador);
//    }

    public void agregarForma(Long colaboradorId, List<Implementacion> formas){
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        colaborador.getFormas().addAll(formas);
        this.colaboradorRepository.save(colaborador);
    }

    @Override
    public void actualizarPesosPuntos(Double pesosDonados, Double viandasDistribuidas, Double viandasDonadas, Double tarjetasRepartidas, Double heladerasActivas) {
        coeficientesPuntos.setValor(TipoCoeficiente.PESOS_DONADOS, pesosDonados);
        coeficientesPuntos.setValor(TipoCoeficiente.VIANDAS_DISTRIBUIDAS, viandasDistribuidas);
        coeficientesPuntos.setValor(TipoCoeficiente.VIANDAS_DONADAS, viandasDonadas);
        coeficientesPuntos.setValor(TipoCoeficiente.TARJETAS_REPARTIDAS, tarjetasRepartidas);
        coeficientesPuntos.setValor(TipoCoeficiente.HELADERAS_ACTIVAS, heladerasActivas);
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

    public void registrarDonacion(Donacion donacion) {
        this.donacionesRepository.save(donacion);
    }
    public List<Donacion> donacionesDelMes(int mesActual, int anioActual, Long colaboradorId){
        return this.donacionesRepository.donacionesDelMes(mesActual,anioActual,colaboradorId);
    }

    public void registrarArreglo(Long colaboradorId) {
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        colaborador.arreglarHeladera();
        colaboradorRepository.save(colaborador);
    }

    public int cantHeladerasReparadas(Long colaboradorId){
        return this.colaboradorRepository.findById(colaboradorId).getCantHeladerasReparadas();
    }

//    public List<Implementacion> agregarFormasDeColaborar(List<FormaDeColaborarEnum> formas, Long id){
//        List<Implementacion> formasAgregar = new ArrayList<>();
//        for (int i = 0; i < formas.size(); i++) {
//            if (formas.get(i).equals(FormaDeColaborarEnum.DONADOR)){
//                DonadorDeViandas donadorDeViandas = DonadorDeViandas.getInstance(this.fachadaViandas);
//                formasAgregar.add(donadorDeViandas);
//            } else if (formas.get(i).equals(FormaDeColaborarEnum.TRANSPORTADOR)){
//                Transportador transportador = Transportador.getInstance(this.getFachadaLogistica());
//            }
//
//
//
//        }
//        this.agregarForma(id, formasAgregar);
//        return formasAgregar;
//    }
    public void agregarFormasDeColaborar(List<FormaDeColaborarEnum> formas, Long id){
        List <Implementacion> formasAgregar =new ArrayList<>();
        for(FormaDeColaborarEnum forma : formas){
            switch (forma){
                case DONADOR:
                    DonadorDeViandas donadorDeViandas = DonadorDeViandas.getInstance(this.fachadaViandas);
                    formasAgregar.add(donadorDeViandas);
                    break;
                case TRANSPORTADOR:
                    Transportador transportador = Transportador.getInstance(this.fachadaLogistica);
                    formasAgregar.add(transportador);
                    break;
                default:
                    throw new IllegalArgumentException("Forma de colaborar desconocida");
            }
        }
        this.agregarForma(id,formasAgregar);
    }


    public List<Implementacion> castearFormaDeColaborar(List<FormaDeColaborarEnum> formas){
        List<Implementacion> formasAgregar = new ArrayList<>();
        for (int i = 0; i < formas.size(); i++) {
            if (formas.get(i).equals(FormaDeColaborarEnum.DONADOR)){

                DonadorDeViandas donadorDeViandas = DonadorDeViandas.getInstance(this.getFachadaViandas());
                donadorDeViandas.setFachadaViandas(this.fachadaViandas);
                formasAgregar.add(donadorDeViandas);
            }
            if (formas.get(i).equals(FormaDeColaborarEnum.TRANSPORTADOR)){
                Transportador transportador = Transportador.getInstance(this.getFachadaLogistica());
                formasAgregar.add(transportador);
            }
        }
        return formasAgregar;
    }
}
