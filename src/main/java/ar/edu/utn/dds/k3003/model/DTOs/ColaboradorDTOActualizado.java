package ar.edu.utn.dds.k3003.model.DTOs;

import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborarActualizadoEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public final class ColaboradorDTOActualizado {
    private Long id;
    private String nombre;
    private List<FormaDeColaborarActualizadoEnum> formas;

    @Setter
    @Getter
    private Double puntos;

    public ColaboradorDTOActualizado(String nombre, List<FormaDeColaborarActualizadoEnum> formas) {
        this.nombre = nombre;
        this.formas = formas;
    }

    public ColaboradorDTOActualizado(String nombre, List<FormaDeColaborarActualizadoEnum> formas, Long id) {
        this.nombre = nombre;
        this.formas = formas;
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public List<FormaDeColaborarActualizadoEnum> getFormas() {
        return this.formas;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setFormas(List<FormaDeColaborarActualizadoEnum> formas) {
        this.formas = formas;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO)) {
            return false;
        } else {
            ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO other;
            label44: {
                other = (ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO)o;
                Object this$id = this.getId();
                Object other$id = other.getId();
                if (this$id == null) {
                    if (other$id == null) {
                        break label44;
                    }
                } else if (this$id.equals(other$id)) {
                    break label44;
                }

                return false;
            }

            Object this$nombre = this.getNombre();
            Object other$nombre = other.getNombre();
            if (this$nombre == null) {
                if (other$nombre != null) {
                    return false;
                }
            } else if (!this$nombre.equals(other$nombre)) {
                return false;
            }

            Object this$formas = this.getFormas();
            Object other$formas = other.getFormas();
            if (this$formas == null) {
                if (other$formas != null) {
                    return false;
                }
            } else if (!this$formas.equals(other$formas)) {
                return false;
            }

            return true;
        }
    }

//    public int hashCode() {
//        int PRIME = true;
//        int result = 1;
//        Object $id = this.getId();
//        result = result * 59 + ($id == null ? 43 : $id.hashCode());
//        Object $nombre = this.getNombre();
//        result = result * 59 + ($nombre == null ? 43 : $nombre.hashCode());
//        Object $formas = this.getFormas();
//        result = result * 59 + ($formas == null ? 43 : $formas.hashCode());
//        return result;
//    }

    public String toString() {
        Long var10000 = this.getId();
        return "ColaboradorDTO(id=" + var10000 + ", nombre=" + this.getNombre() + ", formas=" + this.getFormas() + ")";
    }

    protected ColaboradorDTOActualizado() {
    }
}