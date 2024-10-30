package ar.edu.utn.dds.k3003;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersistenceTest {
  static EntityManagerFactory entityManagerFactory ;
  EntityManager entityManager ;



  @BeforeAll
  public static void setUpClass() throws Exception {
    entityManagerFactory = Persistence.createEntityManagerFactory("bd_colaboradores");
  }
  @BeforeEach
  public void setup() throws Exception {
    entityManager = entityManagerFactory.createEntityManager();
  }
  @Test
  public void testConectar() {
    // vacío, para ver que levante el ORM
  }

}
