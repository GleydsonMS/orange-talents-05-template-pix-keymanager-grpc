package br.com.zup.edu.pix.busca

import br.com.zup.edu.BuscaChavePixRequest
import br.com.zup.edu.KeyManagerBuscaServiceGrpc
import br.com.zup.edu.integracoes.bcb.*
import br.com.zup.edu.pix.entidades.ChavePix
import br.com.zup.edu.pix.entidades.ContaAssociada
import br.com.zup.edu.pix.enums.TipoChave
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.mockito.Mockito.`when`
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class BuscaChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerBuscaServiceGrpc.KeyManagerBuscaServiceBlockingStub
) {
    @Inject
    lateinit var bcb: BancoCentralClient

    @BeforeEach
    fun setup() {
        repository.save(chavePix())
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve buscar chave pix por pixId e clienteId`() {
        val chavePix = repository.findByChavePix("rafael@ponte.com").get()

        val response = grpcClient.buscar(BuscaChavePixRequest.newBuilder()
                                                            .setPixId(BuscaChavePixRequest.FiltroPorPixId.newBuilder()
                                                                .setPixId(chavePix.id)
                                                                .setClienteId(chavePix.clienteId.toString())
                                                                .build())
                                                            .build())

        with(response) {
            assertEquals(chavePix.id, this.pixId)
            assertEquals(chavePix.clienteId.toString(), this.clienteId)
            assertEquals(chavePix.tipoChave.name, this.chavePix.tipoChave.name)
            assertEquals(chavePix.chavePix, this.chavePix.chave)
        }
    }

    @Test
    fun `nao deve buscar chave por pixId e clienteId quando filtro for invalido`(){
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.buscar(BuscaChavePixRequest.newBuilder()
                                                    .setPixId(BuscaChavePixRequest.FiltroPorPixId.newBuilder()
                                                        .setPixId("")
                                                        .setClienteId("")
                                                        .build())
                                                    .build())
        }

        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("pixId", "não deve estar em branco"),
                Pair("clienteId", "não deve estar em branco"),
                Pair("clienteId", "Não é um formato válido de UUID"),
                Pair("pixId", "Não é um formato válido de UUID")
            ))
        }
    }

    @Test
    fun `nao deve buscar chave pix por pixId e clienteId quando registro nao existir no banco de dados`() {
        val pixIdInexistente = UUID.randomUUID().toString()
        val clienteIdInexistente = UUID.randomUUID().toString()

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.buscar(BuscaChavePixRequest.newBuilder()
                .setPixId(BuscaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(pixIdInexistente)
                    .setClienteId(clienteIdInexistente)
                    .build()
                ).build())
        }

        with(errors) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve buscar chave pix pelo valor da chave quando existir no banco de dados`() {
        val chavePix = repository.findByChavePix("rafael@ponte.com").get()

        val response = grpcClient.buscar(BuscaChavePixRequest.newBuilder()
            .setChave("rafael@ponte.com")
            .build())

        with(response) {
            assertEquals(chavePix.id, this.pixId)
            assertEquals(chavePix.clienteId.toString(), this.clienteId)
            assertEquals(chavePix.tipoChave.name, this.chavePix.tipoChave.name)
            assertEquals(chavePix.chavePix, this.chavePix.chave)
        }
    }

    @Test
    fun `deve buscar chave pix pelo valor da chave no Banco Central quando a mesma nao existir localmente`() {
        val bcbResponse = pixKeyDetailsResponse()

        `when`(bcb.buscaPorChavePix(key = "rafael@ponte.com.br"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val response = grpcClient.buscar(BuscaChavePixRequest.newBuilder()
                                                            .setChave("rafael@ponte.com.br")
                                                            .build())

        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.clienteId)
            assertEquals(bcbResponse.keyType.name, this.chavePix.tipoChave.name)
            assertEquals(bcbResponse.key, this.chavePix.chave)
        }
    }

    @Test
    fun `nao deve buscar chave pix pelo valor da chave quando registro nao existir nem localmente e nem no Banco Central`() {
        `when`(bcb.buscaPorChavePix(key = "chave@naoexiste.com"))
            .thenReturn(HttpResponse.notFound())

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.buscar(BuscaChavePixRequest.newBuilder()
                                                    .setChave("chave@naoexiste.com")
                                                    .build())
        }

        with(errors) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave pix por valor da chave quando filtro for invalido`() {
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.buscar(BuscaChavePixRequest
                .newBuilder()
                .setChave("")
                .build())
        }

        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("chavePix", "não deve estar em branco"),
            ))
        }
    }

    @Test
    fun `nao deve carregar chave pix quando filtro for invalido`() {
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.buscar(BuscaChavePixRequest.newBuilder().build())
        }

        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave pix inválida ou não informada", status.description)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcb(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Client {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerBuscaServiceGrpc.KeyManagerBuscaServiceBlockingStub? {
            return KeyManagerBuscaServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chavePix(): ChavePix {
        return ChavePix(
            clienteId = UUID.randomUUID(),
            tipoChave = TipoChave.EMAIL,
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

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = KeyType.EMAIL,
            key = "rafael@ponte.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "0001",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael M C Ponte",
            taxIdNumber = "02467781054"
        )
    }
}