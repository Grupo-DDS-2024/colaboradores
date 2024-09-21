package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaLogistica;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;

import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import ar.edu.utn.dds.k3003.model.CoeficientesPuntos;
import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.TipoCoeficiente;
import ar.edu.utn.dds.k3003.repositories.ColaboradorMapper;
import ar.edu.utn.dds.k3003.repositories.ColaboradorRepository;
import lombok.Getter;

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
  private ColaboradorMapper colaboradorMapper;
  private CoeficientesPuntos coeficientesPuntos = new CoeficientesPuntos(0.5,1,1.5,2.5,5);
  private FachadaViandas fachadaViandas;
  private FachadaLogistica fachadaLogistica;
  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;

  public Fachada() {
    this.entityManagerFactory = Persistence.createEntityManagerFactory("bd_colaboradores");
    this.entityManager = entityManagerFactory.createEntityManager();
    this.colaboradorRepository = new ColaboradorRepository();
    this.colaboradorRepository.setEntityManager(entityManager);
    this.colaboradorMapper = new ColaboradorMapper();
  }

  public Fachada(ColaboradorRepository _colaboradorRepository, ColaboradorMapper _colaboradorMapper, EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
    this.colaboradorRepository = _colaboradorRepository;
    colaboradorRepository.setEntityManagerFactory(entityManagerFactory);
    colaboradorRepository.setEntityManager(entityManagerFactory.createEntityManager());
    this.colaboradorMapper = _colaboradorMapper;
  }


  @Override
  public ColaboradorDTO agregar(ColaboradorDTO colaboradorDTO) {
    Colaborador colaborador = new Colaborador(colaboradorDTO.getId(),colaboradorDTO.getNombre(),colaboradorDTO.getFormas());
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
    Integer mesActual = 1; //;
    Integer anioActual = 2024;//LocalDate.now().getYear();

    List<ViandaDTO> viandasDTO = fachadaViandas.viandasDeColaborador(colaboradorId,mesActual,anioActual);
    Integer viandasDonadas = viandasDTO.size();
    List<TrasladoDTO> trasladosDTO = fachadaLogistica.trasladosDeColaborador(colaboradorId,mesActual,anioActual);
    Integer traslados = trasladosDTO.size();

    return
        coeficientesPuntos.getValor(TipoCoeficiente.VIANDAS_DONADAS) * viandasDonadas +
            coeficientesPuntos.getValor(TipoCoeficiente.VIANDAS_DISTRIBUIDAS) * traslados;
  }

  @Override
  public ColaboradorDTO modificar(Long colaboradorId, List<FormaDeColaborarEnum> formas) throws NoSuchElementException {
    // Buscar el colaborador por ID
    Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
    colaboradorRepository.update(colaborador,formas);
    // Mapear y devolver el colaborador actualizado como DTO
    return colaboradorMapper.map(colaborador);
  }
  @Override
  public void actualizarPesosPuntos(Double pesosDonados, Double viandasDistribuidas, Double viandasDonadas, Double tarjetasRepartidas, Double heladerasActivas) {
    coeficientesPuntos.setValor(TipoCoeficiente.PESOS_DONADOS, pesosDonados);
    coeficientesPuntos.setValor(TipoCoeficiente.VIANDAS_DISTRIBUIDAS, viandasDistribuidas);
    coeficientesPuntos.setValor(TipoCoeficiente.VIANDAS_DONADAS, viandasDonadas);
    coeficientesPuntos.setValor(TipoCoeficiente.TARJETAS_REPARTIDAS, tarjetasRepartidas);
    coeficientesPuntos.setValor(TipoCoeficiente.HELADERAS_ACTIVAS, heladerasActivas);
  }

  @Override
  public void setLogisticaProxy(FachadaLogistica fachadaLogistica) {
    this.fachadaLogistica = fachadaLogistica;
  }

  @Override
  public void setViandasProxy(FachadaViandas fachadaViandas) {
    this.fachadaViandas = fachadaViandas;
  }
}
