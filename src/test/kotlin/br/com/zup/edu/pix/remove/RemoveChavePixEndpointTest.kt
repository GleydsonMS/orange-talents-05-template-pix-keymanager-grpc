package br.com.zup.edu.pix.remove

import br.com.zup.edu.KeyManagerRemoveServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.integracoes.bcb.BancoCentralClient
import br.com.zup.edu.integracoes.bcb.DeletePixKeyRequest
import br.com.zup.edu.integracoes.bcb.DeletePixKeyResponse
import br.com.zup.edu.pix.entidades.ChavePix
import br.com.zup.edu.pix.entidades.ContaAssociada
import br.com.zup.edu.pix.enums.TipoConta
import br.com.zup.edu.pix.repositorios.ChavePixRepository
import br.com.zup.edu.util.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub,
) {

    @Inject
    lateinit var bcb: BancoCentralClient

    lateinit var chavePixExistente: ChavePix

    @BeforeEach
    fun setup() {
        chavePixExistente = chavePix()
        repository.save(chavePixExistente)
    }

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover uma chave pix existente no banco de dados`() {

        `when`(bcb.deletaChavePix("rafael@ponte.com", DeletePixKeyRequest("rafael@ponte.com")))
            .thenReturn(HttpResponse.ok(
                DeletePixKeyResponse(
                    key = "rafael@ponte.com",
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    deletedAt = LocalDateTime.now())))

        val response = grpcClient.remover(RemoveChavePixRequest.newBuilder()
                                                                .setPixId(chavePixExistente.id)
                                                                .setClienteId(chavePixExistente.clienteId.toString())
                                                                .build())
        with(response) {
            assertEquals(chavePixExistente.id, pixId)
            assertEquals(chavePixExistente.clienteId.toString(), clienteId)
            assertFalse(repository.existsByChavePix(chavePix = chavePixExistente.chavePix))
        }
    }

    @Test
    fun `nao deve remover chave pix inexistente no banco de dados`() {
        val pixIdInexistente = UUID.randomUUID().toString()

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.remover(RemoveChavePixRequest.newBuilder()
                                                    .setPixId(pixIdInexistente)
                                                    .setClienteId(chavePixExistente.clienteId.toString())
                                                    .build())
        }

        with(errors) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover quando chave pix informada pertencer a outro cliente`() {
        val pixIdTerceiro = UUID.randomUUID().toString()

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.remover(RemoveChavePixRequest.newBuilder()
                .setPixId(pixIdTerceiro)
                .setClienteId(chavePixExistente.clienteId.toString())
                .build())
        }

        with(errors) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando parametros informados forem invalidos`() {
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.remover(RemoveChavePixRequest.newBuilder().build())
        }

        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("pixId", "não deve estar em branco"),
                Pair("clienteId", "não deve estar em branco"),
                Pair("pixId", "Não é um formato válido de UUID"),
                Pair("clienteId", "Não é um formato válido de UUID"),
            ))
        }
    }

    @Test
    fun `nao deve remover chave pix quando ocorrer algum erro no banco central`() {
        `when`(bcb.deletaChavePix("rafael@ponte.com", DeletePixKeyRequest("rafael@ponte.com")))
            .thenReturn(HttpResponse.unprocessableEntity())

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.remover(RemoveChavePixRequest.newBuilder()
                .setPixId(chavePixExistente.id)
                .setClienteId(chavePixExistente.clienteId.toString())
                .build())
        }

        with(errors) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave Pix no Banco Central (BDB)", status.description)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcb(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Client {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub? {
            return KeyManagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chavePix(): ChavePix {
        return ChavePix(
            clienteId = UUID.randomUUID(),
            tipoChave = br.com.zup.edu.pix.enums.TipoChave.EMAIL,
            chavePix = "rafael@ponte.com",
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