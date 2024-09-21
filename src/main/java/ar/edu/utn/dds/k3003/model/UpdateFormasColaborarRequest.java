package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateFormasColaborarRequest {
  private List<FormaDeColaborarEnum> formas;

  public UpdateFormasColaborarRequest() {
  }

}
