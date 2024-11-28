package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Clases.Colaborador;
import ar.edu.utn.dds.k3003.model.Clases.Incidentes;
import ar.edu.utn.dds.k3003.model.Enums.EstadoIncidenteEnum;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
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

    public List<Incidentes> todos(){
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Incidentes> cq = cb.createQuery(Incidentes.class);
        Root<Incidentes> ruta = cq.from(Incidentes.class);
        cq.select(ruta);
        List<Incidentes> incidentes = entityManager.createQuery(cq).getResultList();
        entityManager.getTransaction().commit();
        entityManager.close();
        return incidentes;
    }

    public boolean existeIncidente(Integer heladeraId, EstadoIncidenteEnum estado){
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        Long count = (Long) entityManager.createQuery("SELECT COUNT(i) FROM Incidentes i WHERE i.heladeraId = :heladeraId " +
                "AND i.estado = :estado").setParameter("heladeraId",heladeraId).setParameter("estado",estado).getSingleResult();

        entityManager.getTransaction().commit();
        entityManager.close();
        return count > 0;
    }
}
