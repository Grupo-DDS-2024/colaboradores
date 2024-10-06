package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.model.Colaborador;

public class ColaboradorMapper {
    public ColaboradorDTO map(Colaborador colaborador) {
        ColaboradorDTO colaboradorDTO = new ColaboradorDTO(colaborador.getNombre(), colaborador.getFormas().stream().toList());
        colaboradorDTO.setId(colaborador.getId());
        return colaboradorDTO;
    }
}
