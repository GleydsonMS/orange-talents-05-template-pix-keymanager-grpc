package br.com.zup.edu.pix.busca

import br.com.zup.edu.pix.entidades.ChavePix
import br.com.zup.edu.pix.entidades.ContaAssociada
import br.com.zup.edu.pix.enums.TipoChave
import br.com.zup.edu.pix.enums.TipoConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: String? = null,
    val clienteId: UUID? = null,
    val tipoChave: TipoChave,
    val chavePix: String,
    val tipoConta: TipoConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun of(chavePix: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chavePix.id,
                clienteId = chavePix.clienteId,
                tipoChave = chavePix.tipoChave,
                chavePix = chavePix.chavePix,
                tipoConta = chavePix.tipoConta,
                conta = chavePix.conta,
                registradaEm = chavePix.criadaEm
            )
        }
    }
}