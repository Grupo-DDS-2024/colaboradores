package ar.edu.utn.dds.k3003.repositories;


import ar.edu.utn.dds.k3003.model.Clases.Colaborador;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborarActualizadoEnum;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@NoArgsConstructor
public class ColaboradorRepository {
    @Setter
    private EntityManagerFactory entityManagerFactory;

    public ColaboradorRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public Colaborador save(Colaborador colaborador) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        if (Objects.isNull(colaborador.getId())) {
            entityManager.persist(colaborador);
        } else {
            colaborador = entityManager.merge(colaborador);
        }

        entityManager.getTransaction().commit();

        entityManager.close();
        return colaborador;
    }


    public Colaborador findById(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Colaborador colaborador = entityManager.find(Colaborador.class, id);
        if (Objects.isNull(colaborador)) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new NoSuchElementException(String.format("No hay un colaborador de id: %s", id));
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return colaborador;

    }

    public void update(Colaborador colaborador, List<FormaDeColaborarActualizadoEnum> formas) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        colaborador.setFormas(formas);
        entityManager.merge(colaborador);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public long cantColaboradores() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        Long count = 0L;
        entityManager.getTransaction().begin();
        try {
            count = (Long) entityManager.createQuery("SELECT COUNT(id) FROM Colaborador").getSingleResult();
            entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            throw e;
        }
        return count;
    }

    public boolean existeChat(String chatId) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Long count = (Long) entityManager.createQuery("SELECT COUNT(c) FROM Colaborador c WHERE c.id = :chatId",Long.class)
                .setParameter("chatId",Long.parseLong(chatId)).getSingleResult();
        entityManager.getTransaction().commit();
        entityManager.close();
        return count >0;
    }

    public Colaborador findByChatId(String chatId){
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Colaborador colaborador = entityManager.createQuery("SELECT c FROM Colaborador c WHERE c.idTelegram = :chatId",Colaborador.class)
                .setParameter("chatId",chatId).getSingleResult();
        entityManager.getTransaction().commit();
        entityManager.close();
        return colaborador;
    }



}