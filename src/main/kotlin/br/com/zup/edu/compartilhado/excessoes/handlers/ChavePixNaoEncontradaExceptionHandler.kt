package br.com.zup.edu.compartilhado.excessoes.handlers

import br.com.zup.edu.compartilhado.excessoes.ExceptionHandler
import br.com.zup.edu.compartilhado.excessoes.ExceptionHandler.StatusWithDetails
import br.com.zup.edu.pix.ChavePixNaoEncontradaException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoEncontradaExceptionHandler : ExceptionHandler<ChavePixNaoEncontradaException> {

    override fun handle(e: ChavePixNaoEncontradaException): StatusWithDetails {
       return StatusWithDetails(Status.NOT_FOUND
                                       .withDescription(e.message)
                                       .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoEncontradaException
    }
}