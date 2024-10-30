package ar.edu.utn.dds.k3003.repositories;
import ar.edu.utn.dds.k3003.model.DTOs.ColaboradorDTOActualizado;
import ar.edu.utn.dds.k3003.model.Clases.Colaborador;

public class ColaboradorMapper {
    public ColaboradorDTOActualizado map(Colaborador colaborador) {
        ColaboradorDTOActualizado colaboradorDTO = new ColaboradorDTOActualizado(colaborador.getNombre(), colaborador.getFormas().stream().toList());
        colaboradorDTO.setId(colaborador.getId());
        return colaboradorDTO;
    }
}
