package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaLogistica;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.CoeficientesPuntos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.List;
import java.util.Objects;

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
