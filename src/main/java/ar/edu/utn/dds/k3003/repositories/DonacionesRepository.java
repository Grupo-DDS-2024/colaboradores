package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Clases.Donacion;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Objects;

public class DonacionesRepository {

    private EntityManagerFactory entityManagerFactory;

    public DonacionesRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void save(Donacion donacion){
        EntityManager entityManager= entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        if (Objects.isNull(donacion.getIdDonacion())) {
            entityManager.persist(donacion);
        } else {
            donacion = entityManager.merge(donacion);
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public List<Donacion> all(){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Donacion> cq = cb.createQuery(Donacion.class);
        Root<Donacion> donacion = cq.from(Donacion.class);
        cq.select(donacion);
        entityManager.getTransaction().commit();
        entityManager.close();
        return entityManager.createQuery(cq).getResultList();
    }
    public List<Donacion> donacionesDelMes(Integer mesActual, Integer anioActual,Long colaboradorId){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<Donacion> donacionesDelMes = this.all().stream().filter(t->t.getFechaDonacion().getMonthValue() == mesActual
            && t.getFechaDonacion().getYear() == anioActual && t.getColaboradorId() == colaboradorId).toList();
        entityManager.getTransaction().commit();
        entityManager.close();
        return donacionesDelMes;
    }
}
