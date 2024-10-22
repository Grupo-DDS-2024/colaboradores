package ar.edu.utn.dds.k3003.model.formaDeColaborar;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Implementacion implements FormaDeColaborar{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
