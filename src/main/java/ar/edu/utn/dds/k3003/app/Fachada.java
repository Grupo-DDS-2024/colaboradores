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
        Colaborador colaborador = new Colaborador(colaboradorDTO.getNombre(), colaboradorDTO.getFormas());
        colaborador = this.colaboradorRepository.save(colaborador);
        return colaboradorMapper.map(colaborador);
    }

    @Override
    public ColaboradorDTO buscarXId(Long colaboradorId) throws NoSuchElementException {
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        return colaboradorMapper.map(colaborador);
    }


    @Override
    public Double puntos(Long colaboradorId) throws NoSuchElementException {
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        List<FormaDeColaborarEnum> formasDeColaborar = colaborador.getFormas();
        List<FormaDeColaborar> colaboraciones = new ArrayList<>();
        for (FormaDeColaborarEnum forma:formasDeColaborar){
            switch (forma){
                case DONADOR:
                    colaboraciones.add(DonadorDeViandas.getInstance(this.fachadaViandas));
                    break;
                case TRANSPORTADOR:
                    colaboraciones.add(Transportador.getInstance(this.fachadaLogistica));
                    break;
                default:
                    throw new IllegalArgumentException("Forma de colaborar desconocida");

            }
        }
        return colaboraciones.stream().mapToDouble(forma -> forma.calcularPuntos(colaboradorId)).sum();
    }



    @Override
    public ColaboradorDTO modificar(Long colaboradorId, List<FormaDeColaborarEnum> formas) throws NoSuchElementException {
        // Buscar el colaborador por ID
        Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
        colaboradorRepository.update(colaborador,formas);
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

    public int cantHeladerasReparadas(Long colaboradorId) {
        return this.colaboradorRepository.findById(colaboradorId).getCantHeladerasReparadas();
    }

}
