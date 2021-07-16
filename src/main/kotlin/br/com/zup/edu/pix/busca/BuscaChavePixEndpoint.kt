package br.com.zup.edu.pix.busca

import br.com.zup.edu.BuscaChavePixRequest
import br.com.zup.edu.BuscaChavePixResponse
import br.com.zup.edu.KeyManagerBuscaServiceGrpc
import br.com.zup.edu.compartilhado.excessoes.ErrorHandler
import br.com.zup.edu.integracoes.bcb.BancoCentralClient
import br.com.zup.edu.pix.repositorios.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class BuscaChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcb: BancoCentralClient,
    @Inject private val validator: Validator
): KeyManagerBuscaServiceGrpc.KeyManagerBuscaServiceImplBase() {

    override fun buscar(
        request: BuscaChavePixRequest,
        responseObserver: StreamObserver<BuscaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chavePixInfo = filtro.filtra(repository, bcb)

        responseObserver.onNext(BuscaChavePixResponseConverter().converter(chavePixInfo))
        responseObserver.onCompleted()
    }
}