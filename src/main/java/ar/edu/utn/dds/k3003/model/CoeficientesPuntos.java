package ar.edu.utn.dds.k3003.model;

public class CoeficientesPuntos {
  private double pesosDonados;
  private double viandasDistribuidas;
  private double viandasDonadas;
  private double tarjetasRepartidas;
  private double heladerasActivas;

  public CoeficientesPuntos(double pesosDonados,
                            double viandasDistribuidas,
                            double viandasDonadas,
                            double tarjetasRepartidas,
                            double heladerasActivas) {
    this.pesosDonados = pesosDonados;
    this.viandasDistribuidas = viandasDistribuidas;
    this.viandasDonadas = viandasDonadas;
    this.tarjetasRepartidas = tarjetasRepartidas;
    this.heladerasActivas = heladerasActivas;

  }

  public double getValor(TipoCoeficiente tipo) {
    switch (tipo) {
      case PESOS_DONADOS:
        return pesosDonados;
      case VIANDAS_DISTRIBUIDAS:
        return viandasDistribuidas;
      case VIANDAS_DONADAS:
        return viandasDonadas;
      case TARJETAS_REPARTIDAS:
        return tarjetasRepartidas;
      case HELADERAS_ACTIVAS:
        return heladerasActivas;
      default:
        throw new IllegalArgumentException("Tipo de coeficiente no válido: " + tipo);
    }
  }

  public void setValor(TipoCoeficiente tipo, double valor) {
    switch (tipo) {
      case PESOS_DONADOS:
        this.pesosDonados = valor;
        break;
      case VIANDAS_DISTRIBUIDAS:
        this.viandasDistribuidas = valor;
        break;
      case VIANDAS_DONADAS:
        this.viandasDonadas = valor;
        break;
      case TARJETAS_REPARTIDAS:
        this.tarjetasRepartidas = valor;
        break;
      case HELADERAS_ACTIVAS:
        this.heladerasActivas = valor;
        break;
      default:
        throw new IllegalArgumentException("Tipo de coeficiente no válido: " + tipo);
    }
  }
}