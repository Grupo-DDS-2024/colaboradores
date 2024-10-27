package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.model.SuscripcionHeladera;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.NoSuchElementException;
import java.util.Objects;

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
