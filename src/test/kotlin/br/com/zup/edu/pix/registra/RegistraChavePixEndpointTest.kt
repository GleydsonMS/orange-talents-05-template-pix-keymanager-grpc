package br.com.zup.edu.pix.registra

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.TipoChave.EMAIL
import br.com.zup.edu.TipoConta.CONTA_CORRENTE
import br.com.zup.edu.integracoes.bcb.*
import br.com.zup.edu.integracoes.itau.DadosDaContaResponse
import br.com.zup.edu.integracoes.itau.ErpItau
import br.com.zup.edu.integracoes.itau.InstiuicaoResponse
import br.com.zup.edu.integracoes.itau.TitularResponse
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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class RegistraChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {
    @Inject
    lateinit var erpItau: ErpItau

    @Inject
    lateinit var bcb: BancoCentralClient

    companion object {
        val CLIENTE_ID = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar uma nova chave pix`() {
        `when`(erpItau.consultaContaCliente(CLIENTE_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcb.criaChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        val response = grpcClient.registrar(ChavePixRequest.newBuilder()
                                                            .setClienteId(CLIENTE_ID)
                                                            .setTipoChave(EMAIL)
                                                            .setChavePix("rafael@ponte.com")
                                                            .setTipoConta(CONTA_CORRENTE)
                                                            .build())
        with(response) {
            assertEquals(CLIENTE_ID, clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix ja existente`() {
        val response = repository.save(chavePix())

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(ChavePixRequest.newBuilder()
                .setClienteId(response.clienteId.toString())
                .setTipoChave(EMAIL)
                .setChavePix(response.chavePix)
                .setTipoConta(CONTA_CORRENTE)
                .build())
        }

        with(errors) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix 'rafael@ponte.com' existente", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando cliente nao for encontrado no erp do itau`() {
        `when`(erpItau.consultaContaCliente(CLIENTE_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(ChavePixRequest.newBuilder()
                                                .setClienteId(CLIENTE_ID)
                                                .setTipoChave(EMAIL)
                                                .setChavePix("rafael@ponte.com")
                                                .setTipoConta(CONTA_CORRENTE)
                                                .build())
        }

        with(errors) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando os dados informado forem invalidos`(){
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(ChavePixRequest.newBuilder().build())
        }

        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("clienteId", "Não é um formato válido de UUID"),
                Pair("tipoChave", "não deve ser nulo"),
                Pair("clienteId", "não deve estar em branco"),
                Pair("tipoConta", "não deve ser nulo"),
            ))
        }
    }

    @Test
    fun `nao deve registrar chave pix quando o valor da chave pix informado for invalido`() {
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(ChavePixRequest.newBuilder()
                                                    .setClienteId(CLIENTE_ID)
                                                    .setTipoChave(EMAIL)
                                                    .setChavePix("email-inválido")
                                                    .setTipoConta(CONTA_CORRENTE)
                                                    .build())
        }

        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("chavePix", "Chave pix inválida (EMAIL)"),
            ))
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao for possivel registrar chave no banco central`() {
        `when`(erpItau.consultaContaCliente(CLIENTE_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcb.criaChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.badRequest())

        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(ChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoChave(EMAIL)
                .setChavePix("rafael@ponte.com")
                .setTipoConta(CONTA_CORRENTE)
                .build())
        }

        with(errors) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar chave pix no Banco Central (BCB)", status.description)
        }
    }

    @MockBean(ErpItau::class)
    fun erpItau(): ErpItau? {
        return Mockito.mock(ErpItau::class.java)
    }

    @MockBean(BancoCentralClient::class)
    fun bcb(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstiuicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
            agencia =  "0001",
            numero = "291900",
            titular = TitularResponse(
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                "Rafael M C Ponte",
                "02467781054"
            )
        )
    }

    private fun chavePix(): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(CLIENTE_ID),
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

    private fun createPixKeyRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = KeyType.EMAIL,
            key = "rafael@ponte.com",
            bankAccount = bankAccount(),
            owner = owner()
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
            taxIdNumber = "02467781054",
        )
    }

    private fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = KeyType.EMAIL,
            key = "rafael@ponte.com",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now(),
        )
    }
}