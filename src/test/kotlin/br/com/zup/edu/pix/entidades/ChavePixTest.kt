package br.com.zup.edu.pix.entidades

import br.com.zup.edu.pix.enums.TipoChave
import br.com.zup.edu.pix.enums.TipoConta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest {

    companion object {
        val tiposChaveExcetoAleatoria = TipoChave.values().filterNot { it == TipoChave.ALEATORIA }
    }

    @Test
    fun deveSerChaveTipoAleatoria() {
        with (novaChave(TipoChave.ALEATORIA)) {
            assertTrue(this.isAleatoria())
        }
    }

    @Test
    fun naoDeveSerChaveTipoAleatoria() {
        tiposChaveExcetoAleatoria.forEach {
            assertFalse(novaChave(it).isAleatoria())
        }
    }

    @Test
    fun deveAtualizarChaveQuandoTipoForAleatoria() {
        with(novaChave(TipoChave.ALEATORIA)) {
            assertTrue(this.atualiza("nova-chave-pix"))
            assertEquals("nova-chave-pix", this.chavePix)
        }
    }

    @Test
    fun naoDeveAtualizarChaveQuandoTipoForDiferenteDeAleatoria() {
        val chave = "chave-aleatoria-original"
        tiposChaveExcetoAleatoria.forEach {
            with(novaChave(tipoChave = it, chavePix = chave)) {
                assertFalse(this.atualiza("nova-chave-pix"))
                assertEquals(chave, this.chavePix)
            }
        }
    }

    private fun novaChave(
        tipoChave: TipoChave,
        chavePix: String = UUID.randomUUID().toString(),
    ): ChavePix {
        return ChavePix(
            clienteId = UUID.randomUUID(),
            tipoChave = tipoChave,
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

