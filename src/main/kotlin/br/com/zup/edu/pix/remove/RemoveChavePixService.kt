package br.com.zup.edu.pix.remove

import br.com.zup.edu.compartilhado.validacoes.ValidUUID
import br.com.zup.edu.pix.ChavePixNaoEncontradaException
import br.com.zup.edu.pix.repositorios.ChavePixRepository
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChavePixService(
    @Inject val repository: ChavePixRepository
    ) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID clienteId: String?,
        @NotBlank @ValidUUID pixId: String?,
    ) {
        val toClienteId = UUID.fromString(clienteId)

        val chavePix = repository.findByIdAndClienteId(pixId, toClienteId)
            .orElseThrow { ChavePixNaoEncontradaException("Chave pix não encontrada ou não pertence ao cliente") }

        repository.delete(chavePix)
    }
}