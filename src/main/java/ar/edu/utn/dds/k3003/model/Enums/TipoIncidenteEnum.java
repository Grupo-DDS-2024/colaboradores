package ar.edu.utn.dds.k3003.model.Enums;

import lombok.SneakyThrows;

public enum TipoIncidenteEnum {

    FALLA_TECNICA(0),
    ALERTA(1);


    private final int codigo;

    TipoIncidenteEnum(int codigo){
        this.codigo=codigo;
    }


    @SneakyThrows
    public static TipoIncidenteEnum buscarEnum(int codigo){
        for(TipoIncidenteEnum tipo: TipoIncidenteEnum.values()){
            if(tipo.codigo == codigo){
                return tipo;
            }
        }
        throw new IllegalAccessException("Código de Enum invalido: "+ codigo);
    }

    public int getCodigo(){
        return codigo;
    }

}
