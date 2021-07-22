package br.com.zup.edu.pix.lista

import br.com.zup.edu.KeyManagerListaServiceGrpc
import br.com.zup.edu.ListaChavePixRequest
import br.com.zup.edu.pix.entidades.ChavePix
import br.com.zup.edu.pix.entidades.ContaAssociada
import br.com.zup.edu.pix.enums.TipoChave
import br.com.zup.edu.pix.enums.TipoConta
import br.com.zup.edu.pix.repositorios.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import java.util.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@MicronautTest(transactional = false)
internal class ListaChavesPixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub
) {

    companion object {
        val cliente_id = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chavePix(clienteId = cliente_id, chavePix = "aleatoria-01"))
        repository.save(chavePix(clienteId = cliente_id, chavePix = "aleatoria-02"))
        repository.save(chavePix(clienteId = UUID.randomUUID(), chavePix = "aleatoria-03"))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves pix do cliente`() {
        val clienteId = cliente_id.toString()

        val response = grpcClient.listar(ListaChavePixRequest.newBuilder()
                                                        .setClienteId(clienteId)
                                                        .build())

        with(response.chavesPixList) {
            assertThat(this, hasSize(2))
            assertThat(
                this.map { Pair(it.tipoChave, it.chavePix) }.toList(),
                containsInAnyOrder(
                    Pair(br.com.zup.edu.TipoChave.ALEATORIA, "aleatoria-01"),
                    Pair(br.com.zup.edu.TipoChave.ALEATORIA, "aleatoria-02"),
                )
            )
        }
    }

    @Test
    fun `nao deve listar as chaves pix quando o cliente nao possuir chaves cadastradas`() {
        val clienteNaoTemChaves = UUID.randomUUID().toString()

        val response = grpcClient.listar(ListaChavePixRequest.newBuilder()
                                                            .setClienteId(clienteNaoTemChaves)
                                                            .build())

        assertEquals(0, response.chavesPixCount)
    }

    @Test
    fun `nao deve listar chaves pix quando o id do cliente for invalido`() {
        val clienteIdInvalido = ""

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.listar(ListaChavePixRequest.newBuilder()
                                                    .setClienteId(clienteIdInvalido)
                                                    .build())
        }

        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O id do cliente não pode ser nulo ou vazio", status.description)
        }
    }

    @Factory
    class Client {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
            : KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub? {
            return KeyManagerListaServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chavePix(
        clienteId: UUID = UUID.randomUUID(),
        chavePix: String = UUID.randomUUID().toString(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = TipoChave.ALEATORIA,
            chavePix = chavePix,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                agencia = "0001",
                numeroDaConta = "291900"
            )
        )
    }
}
