package ar.edu.utn.dds.k3003.model;

import lombok.SneakyThrows;

public enum TipoNotificacionEnum {
    POCAS_VIANDAS(0),
    FALTAN_VIANDAS(1),
    DESPERFECTO(2);

    private final int codigo;

    TipoNotificacionEnum(int codigo){
        this.codigo=codigo;
    }

    @SneakyThrows
    public static TipoNotificacionEnum buscarEnum(int codigo){
        for(TipoNotificacionEnum tipo: TipoNotificacionEnum.values()){
            if(tipo.codigo == codigo){
                return tipo;
            }
        }
        throw new IllegalAccessException("Código de Enum invlaido: "+ codigo);
    }

    public int getCodigo(){
        return codigo;
    }


}
