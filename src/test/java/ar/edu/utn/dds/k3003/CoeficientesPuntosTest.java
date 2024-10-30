package ar.edu.utn.k3003;

import ar.edu.utn.dds.k3003.model.Clases.CoeficientesPuntos;
import ar.edu.utn.dds.k3003.model.Enums.TipoCoeficienteEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

@ExtendWith({MockitoExtension.class})
public class CoeficientesPuntosTest {
  CoeficientesPuntos coeficientes;

  @BeforeEach
  void setUp() {
    this.coeficientes = new CoeficientesPuntos(0.5,1,1.5,2,5);
    Objects.requireNonNull(this);
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Verificar coeficientes iniciales")
  void verificarCoeficientesIniciales() {
    Assertions.assertEquals(0.5, coeficientes.getValor(TipoCoeficienteEnum.PESOS_DONADOS));
    Assertions.assertEquals(1.0, coeficientes.getValor(TipoCoeficienteEnum.VIANDAS_DISTRIBUIDAS));
    Assertions.assertEquals(1.5, coeficientes.getValor(TipoCoeficienteEnum.VIANDAS_DONADAS));
    Assertions.assertEquals(2.0, coeficientes.getValor(TipoCoeficienteEnum.TARJETAS_REPARTIDAS));
    Assertions.assertEquals(5.0, coeficientes.getValor(TipoCoeficienteEnum.HELADERAS_ACTIVAS));
  }

  @Test
  @DisplayName("Modificar coeficientes")
  void modificarCoeficientes() {
    this.coeficientes.setValor(TipoCoeficienteEnum.PESOS_DONADOS, 0.8);
    this.coeficientes.setValor(TipoCoeficienteEnum.VIANDAS_DISTRIBUIDAS, 1.2);
    this.coeficientes.setValor(TipoCoeficienteEnum.VIANDAS_DONADAS, 1.7);
    this.coeficientes.setValor(TipoCoeficienteEnum.TARJETAS_REPARTIDAS, 2.5);
    this.coeficientes.setValor(TipoCoeficienteEnum.HELADERAS_ACTIVAS, 6.0);

    Assertions.assertEquals(0.8, coeficientes.getValor(TipoCoeficienteEnum.PESOS_DONADOS));
    Assertions.assertEquals(1.2, coeficientes.getValor(TipoCoeficienteEnum.VIANDAS_DISTRIBUIDAS));
    Assertions.assertEquals(1.7, coeficientes.getValor(TipoCoeficienteEnum.VIANDAS_DONADAS));
    Assertions.assertEquals(2.5, coeficientes.getValor(TipoCoeficienteEnum.TARJETAS_REPARTIDAS));
    Assertions.assertEquals(6.0, coeficientes.getValor(TipoCoeficienteEnum.HELADERAS_ACTIVAS));
  }

}
