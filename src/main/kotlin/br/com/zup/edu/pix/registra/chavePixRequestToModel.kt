package br.com.zup.edu.pix.registra

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.TipoChave.TIPO_CHAVE_DESCONHECIDA
import br.com.zup.edu.TipoConta.TIPO_CONTA_DESCONHECIDA
import br.com.zup.edu.pix.enums.TipoChave
import br.com.zup.edu.pix.enums.TipoConta


fun ChavePixRequest.toModel() : NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipoChave = when (tipoChave) {
            TIPO_CHAVE_DESCONHECIDA -> null
            else -> TipoChave.valueOf(tipoChave.name)
        },
        chavePix = chavePix,
        tipoConta = when (tipoConta) {
            TIPO_CONTA_DESCONHECIDA -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }
    )
}