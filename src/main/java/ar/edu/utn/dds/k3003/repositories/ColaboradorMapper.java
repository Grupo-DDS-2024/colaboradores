package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.DonadorDeViandas;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.Implementacion;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.Transportador;

import java.util.ArrayList;
import java.util.List;

public class ColaboradorMapper {
    public ColaboradorDTO map(Colaborador colaborador) {
        ColaboradorDTO colaboradorDTO = new ColaboradorDTO(colaborador.getNombre(), this.castear(colaborador.getFormas().stream().toList()));
        colaboradorDTO.setId(colaborador.getId());
        return colaboradorDTO;
    }

    public List<FormaDeColaborarEnum> castear(List<Implementacion> lista){
        List<FormaDeColaborarEnum> listaEnum = new ArrayList<>();
        for(FormaDeColaborar forma : lista){
            if(forma instanceof DonadorDeViandas){
                listaEnum.add(FormaDeColaborarEnum.DONADOR);
            } else if (forma instanceof Transportador) {
                listaEnum.add(FormaDeColaborarEnum.TRANSPORTADOR);
            }
        }
        return listaEnum;
    }

}
