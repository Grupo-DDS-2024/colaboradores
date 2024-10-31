package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Clases.Colaborador;
import ar.edu.utn.dds.k3003.model.Clases.Incidentes;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.NoSuchElementException;
import java.util.Objects;

public class IncidentesRepository {
    private EntityManagerFactory entityManagerFactory;

    public IncidentesRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void save(Incidentes incidentes){
        EntityManager entityManager= entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        if (Objects.isNull(incidentes.getId())) {
            entityManager.persist(incidentes);
        } else {
            entityManager.merge(incidentes);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }
    public Incidentes findById(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Incidentes incidentes = entityManager.find(Incidentes.class, id);
        if (Objects.isNull(incidentes)) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new NoSuchElementException(String.format("No hay un incidente de id: %s", id));
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return incidentes;

    }
}
