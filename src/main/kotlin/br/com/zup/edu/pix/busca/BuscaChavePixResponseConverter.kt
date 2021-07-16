package br.com.zup.edu.pix.busca

import br.com.zup.edu.BuscaChavePixResponse
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class BuscaChavePixResponseConverter {

    fun converter(chavePixInfo: ChavePixInfo): BuscaChavePixResponse {
        return BuscaChavePixResponse.newBuilder()
            .setClienteId(chavePixInfo.clienteId?.toString() ?: "")
            .setPixId(chavePixInfo.pixId?.toString() ?: "")
            .setChavePix(BuscaChavePixResponse.ChavePix
                .newBuilder()
                .setTipoChave(TipoChave.valueOf(chavePixInfo.tipoChave.name))
                .setChave(chavePixInfo.chavePix)
                .setConta(BuscaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipoConta(TipoConta.valueOf(chavePixInfo.tipoConta.name))
                    .setInstituicao(chavePixInfo.conta.instituicao)
                    .setNomeTitular(chavePixInfo.conta.nomeDoTitular)
                    .setCpfTitular(chavePixInfo.conta.cpfDoTitular)
                    .setAgencia(chavePixInfo.conta.agencia)
                    .setNumeroConta(chavePixInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chavePixInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            ).build()
    }
}