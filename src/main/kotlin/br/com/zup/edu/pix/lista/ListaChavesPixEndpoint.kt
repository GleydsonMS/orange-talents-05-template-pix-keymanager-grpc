package br.com.zup.edu.pix.lista

import br.com.zup.edu.*
import br.com.zup.edu.compartilhado.excessoes.ErrorHandler
import br.com.zup.edu.pix.repositorios.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesPixEndpoint(
    @Inject private val repository: ChavePixRepository
): KeyManagerListaServiceGrpc.KeyManagerListaServiceImplBase() {

    override fun listar(
        request: ListaChavePixRequest,
        responseObserver: StreamObserver<ListaChavePixResponse>,
    ) {

       if (request.clienteId.isNullOrBlank()) {
           throw IllegalArgumentException("O id do cliente não pode ser nulo ou vazio")
       }

        val clienteId = UUID.fromString(request.clienteId)

        val chavesPix = repository.findAllByClienteId(clienteId).map {
            ListaChavePixResponse.ChavePix.newBuilder()
                .setPixId(it.id)
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                .setChavePix(it.chavePix)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                }).build()
        }

        responseObserver.onNext(ListaChavePixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .addAllChavesPix(chavesPix)
            .build())

        responseObserver.onCompleted()
    }
}