package br.com.zup.edu.pix.registra

import br.com.zup.edu.compartilhado.validacoes.ValidUUID
import br.com.zup.edu.pix.entidades.ChavePix
import br.com.zup.edu.pix.entidades.ContaAssociada
import br.com.zup.edu.pix.enums.TipoChave
import br.com.zup.edu.pix.enums.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @ValidUUID
    @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:Size(max = 77)
    val chavePix: String?,
    @field:NotNull
    val tipoConta: TipoConta?
) {
    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoChave = TipoChave.valueOf(this.tipoChave!!.name),
            chavePix = if (this.tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chavePix!!,
            tipoConta = TipoConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }
}
