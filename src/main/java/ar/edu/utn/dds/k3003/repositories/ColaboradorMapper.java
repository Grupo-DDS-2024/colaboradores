package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.DonadorDeViandas;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.Transportador;

import java.util.ArrayList;
import java.util.List;

public class ColaboradorMapper {
    public ColaboradorDTO map(Colaborador colaborador) {
        ColaboradorDTO colaboradorDTO = new ColaboradorDTO(colaborador.getNombre(), colaborador.getFormas().stream().toList());
        colaboradorDTO.setId(colaborador.getId());
        return colaboradorDTO;
    }
}
