package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Clases.Colaborador;
import ar.edu.utn.dds.k3003.model.Clases.SuscripcionHeladera;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

public class SuscripcionRepository {

    private EntityManagerFactory entityManagerFactory;

    public SuscripcionRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void save(SuscripcionHeladera suscripcionHeladera){
        EntityManager entityManager= entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        if (Objects.isNull(suscripcionHeladera.getId())) {
            entityManager.persist(suscripcionHeladera);
        } else {
            suscripcionHeladera = entityManager.merge(suscripcionHeladera);
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public SuscripcionHeladera findById(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        SuscripcionHeladera suscripcion = entityManager.find(SuscripcionHeladera.class, id);
        if (Objects.isNull(suscripcion)) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new NoSuchElementException(String.format("No hay una suscripcion de id: %s", id));
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return suscripcion;
    }

    public List<String> buscarSuscripcionesPorColaborador(Colaborador colaborador){
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<SuscripcionHeladera> suscripciones = entityManager.createQuery("SELECT r FROM SuscripcionHeladera r WHERE r.colaborador = :colaboradorId",SuscripcionHeladera.class)
                .setParameter("colaboradorId",colaborador).getResultList();
        entityManager.getTransaction().commit();
        entityManager.close();
        List<String> suscripcionesString = suscripciones.stream().map(SuscripcionHeladera::toString).collect(Collectors.toList());
        return suscripcionesString;
    }

    public void delete(Long id){
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        SuscripcionHeladera suscripcionHeladera = entityManager.find(SuscripcionHeladera.class,id);
        if (Objects.isNull(suscripcionHeladera)) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new NoSuchElementException(String.format("No hay una suscripcion de id: %s", id));
        }
        entityManager.remove(suscripcionHeladera);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

}
