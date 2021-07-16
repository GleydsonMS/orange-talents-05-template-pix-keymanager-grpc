package br.com.zup.edu.pix.busca

import br.com.zup.edu.compartilhado.validacoes.ValidUUID
import br.com.zup.edu.integracoes.bcb.BancoCentralClient
import br.com.zup.edu.pix.ChavePixNaoEncontradaException
import br.com.zup.edu.pix.repositorios.ChavePixRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcb: BancoCentralClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String,
    ): Filtro() {

        fun clienteIdAsUUID() = UUID.fromString(clienteId)

        override fun filtra(repository: ChavePixRepository, bcb: BancoCentralClient): ChavePixInfo {
            return repository.findById(pixId)
                .filter { it.pertenceAo(clienteIdAsUUID())}
                .map(ChavePixInfo::of)
                .orElseThrow { ChavePixNaoEncontradaException("Chave pix não encontrada") }
        }
    }

    @Introspected
    data class PorChave(
        @field:NotBlank @Size(max = 77) val chavePix: String
    ) : Filtro() {

        override fun filtra(repository: ChavePixRepository, bcb: BancoCentralClient): ChavePixInfo {
            return repository.findByChavePix(chavePix)
                .map(ChavePixInfo::of)
                .orElseGet {
                    val response = bcb.buscaPorChavePix(chavePix)
                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw ChavePixNaoEncontradaException("Chave pix não encontrada")
                    }
                }
        }
    }

    @Introspected
    class Invalido(): Filtro() {
        override fun filtra(repository: ChavePixRepository, bcb: BancoCentralClient): ChavePixInfo {
            throw IllegalArgumentException("Chave pix inválida ou não informada")
        }
    }
}