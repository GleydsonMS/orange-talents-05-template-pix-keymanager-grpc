package br.com.zup.edu.pix.registra

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.ChavePixResponse
import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.compartilhado.excessoes.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChavePixEndpoint(
    @Inject private val service: NovaChavePixService
    ) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registrar(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>
    ) {

        val novaChavePix = request.toModel()
        val chaveCriada = service.registra(novaChavePix)

        responseObserver.onNext(ChavePixResponse
            .newBuilder()
            .setClienteId(chaveCriada.clienteId.toString())
            .setPixId(chaveCriada.id.toString())
            .build())
        responseObserver.onCompleted()
    }
}