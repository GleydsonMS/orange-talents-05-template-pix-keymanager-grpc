package br.com.zup.edu.integracoes.bcb

import br.com.zup.edu.pix.entidades.ChavePix
import br.com.zup.edu.pix.entidades.ContaAssociada
import br.com.zup.edu.pix.enums.TipoChave
import br.com.zup.edu.pix.enums.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb.pix.url}")
interface BancoCentralClient {

    @Post(
        "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun criaChavePix(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun deletaChavePix(@PathVariable key: String, @Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>

}

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
) {
    companion object {
        fun of(chavePix: ChavePix): CreatePixKeyRequest {
            return CreatePixKeyRequest(
                keyType = KeyType.by(chavePix.tipoChave),
                key = chavePix.chavePix,
                bankAccount = BankAccount(
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    branch = chavePix.conta.agencia,
                    accountNumber = chavePix.conta.numeroDaConta,
                    accountType = BankAccount.AccountType.by(chavePix.tipoConta)
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chavePix.conta.nomeDoTitular,
                    taxIdNumber = chavePix.conta.cpfDoTitular
                )
            )
        }
    }
}

enum class KeyType(val type: TipoChave?) {
    CPF(TipoChave.CPF),
    CNPJ(null),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIA);

    companion object {

        private val map = KeyType.values().associateBy(KeyType::type)

        fun by(type: TipoChave) : KeyType {
            return map[type] ?: throw IllegalArgumentException("KeyType inválido ou não encontrado para $type")
        }
    }
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType,
) {
    enum class AccountType() {
        CACC,
        SVGS;

        companion object {
            fun by(type: TipoConta) :AccountType {
                return when (type) {
                    TipoConta.CONTA_CORRENTE -> CACC
                    TipoConta.CONTA_POUPANCA -> SVGS
                }
            }
        }
    }
}

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {
    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

data class DeletePixKeyRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
)

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)