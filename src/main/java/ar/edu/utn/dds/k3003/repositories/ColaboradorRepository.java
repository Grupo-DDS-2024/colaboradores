package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.Colaborador;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@NoArgsConstructor
public class ColaboradorRepository {
  @Getter
  @Setter
  private EntityManager entityManager;
  @Setter
  private EntityManagerFactory entityManagerFactory;

  public ColaboradorRepository(EntityManager entityManager, EntityManagerFactory entityManagerFactory) {
    super();
    this.entityManager = entityManager;
    this.entityManagerFactory = entityManagerFactory;
  }

  public Colaborador save(Colaborador colaborador) {
    if (Objects.isNull(colaborador.getId())) {
      entityManager.getTransaction().begin();
      entityManager.persist(colaborador);
      entityManager.getTransaction().commit();
    }
    /*else {
      this.colaboradores.add(colaborador);
    }*/
    return colaborador;
  }

  public Colaborador findById(Long id) {
    Colaborador colaborador = entityManager.find(Colaborador.class, id);
    if (Objects.isNull(colaborador)){
      throw new NoSuchElementException(String.format("No hay un colaborador de id: %s", id));
    }
    return colaborador;

  }

  public void update(Colaborador colaborador,  List<FormaDeColaborarEnum> formas) {
    entityManager.getTransaction().begin();
    colaborador.setFormas(formas);
    entityManager.getTransaction().commit();
  }
}
