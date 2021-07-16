package br.com.zup.edu.pix.entidades

import br.com.zup.edu.pix.enums.TipoChave
import br.com.zup.edu.pix.enums.TipoConta
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var chavePix: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {
    @Id
    var id: String? = UUID.randomUUID().toString()

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun atualiza(chavePix: String): Boolean {
        if (isAleatoria()) {
            this.chavePix = chavePix
            return true
        }
        return false
    }

    fun isAleatoria(): Boolean {
        return tipoChave == TipoChave.ALEATORIA
    }

    fun pertenceAo(clienteId: UUID) = this.clienteId.equals(clienteId)
}