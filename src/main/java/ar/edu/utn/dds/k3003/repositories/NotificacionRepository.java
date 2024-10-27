package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.NotificacionesHeladeras;
import ar.edu.utn.dds.k3003.model.SuscripcionHeladera;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Objects;

public class NotificacionRepository {

    private EntityManagerFactory entityManagerFactory;

    public NotificacionRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void save(NotificacionesHeladeras notificacionHeladera){
        EntityManager entityManager= entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        if (Objects.isNull(notificacionHeladera.getId())) {
            entityManager.persist(notificacionHeladera);
        } else {
            notificacionHeladera = entityManager.merge(notificacionHeladera);
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }



}
