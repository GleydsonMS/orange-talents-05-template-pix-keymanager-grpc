package br.com.zup.edu.integracoes.itau

import br.com.zup.edu.pix.entidades.ContaAssociada

data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstiuicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel() : ContaAssociada {
        return ContaAssociada(
            instituicao = this.instituicao.nome,
            nomeDoTitular = this.titular.nome,
            cpfDoTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroDaConta = this.numero
        )
    }
}

data class InstiuicaoResponse(
    val nome: String,
    val ispb: String
)

data class TitularResponse(
    val id : String,
    val nome: String,
    val cpf: String
)