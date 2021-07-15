package br.com.zup.edu.pix.registra

import br.com.zup.edu.integracoes.bcb.BancoCentralClient
import br.com.zup.edu.integracoes.bcb.CreatePixKeyRequest
import br.com.zup.edu.integracoes.itau.ErpItau
import br.com.zup.edu.pix.ChavePixExistenteException
import br.com.zup.edu.pix.entidades.ChavePix
import br.com.zup.edu.pix.repositorios.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val erpItau: ErpItau,
    @Inject val bcb: BancoCentralClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix) : ChavePix {

        if (chavePixRepository.existsByChavePix(novaChavePix.chavePix)) {
            throw ChavePixExistenteException("Chave Pix '${novaChavePix.chavePix}' existente")
        }

        val response = erpItau.consultaContaCliente(novaChavePix.clienteId!!, novaChavePix.tipoConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        val chavePix = novaChavePix.toModel(conta)
        chavePixRepository.save(chavePix)

        val bcbRequest = CreatePixKeyRequest.of(chavePix)

        val bcbResponse = bcb.criaChavePix(bcbRequest)
        if (bcbResponse.status != HttpStatus.CREATED) {
            throw IllegalStateException("Erro ao registrar chave pix no Banco Central (BCB)")
        }

        chavePix.atualiza(bcbResponse.body()!!.key)

        return chavePix
    }
}