package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Clases.Incidentes;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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

}
