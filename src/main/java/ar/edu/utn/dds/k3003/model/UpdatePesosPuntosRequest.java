package ar.edu.utn.dds.k3003.model;

import lombok.Getter;

@Getter
public class UpdatePesosPuntosRequest {
    private double pesosDonados;
    private double viandasDistribuidas;
    private double viandasDonadas;
    private double tarjetasRepartidas;
    private double heladerasActivas;

    public UpdatePesosPuntosRequest() {
    }
}
