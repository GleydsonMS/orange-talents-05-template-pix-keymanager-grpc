package br.com.zup.edu.pix.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoChaveTest {

    @Nested
    inner class CPF {

        @Test
        fun `deve ser valido quando CPF informado for valido`() {
            with(TipoChave.CPF) {
                assertTrue(valida("02467781054"))
            }
        }

        @Test
        fun `nao deve ser valido quando CPF informado for invalido`() {
            with(TipoChave.CPF) {
                assertFalse(valida("cpf invalido"))
            }
        }

        @Test
        fun `nao deve ser valido quando CPF nao for informado`() {
            with(TipoChave.CPF) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class CELULAR {

        @Test
        fun `deve ser valido quando o celular informado for valido`() {
            with(TipoChave.CELULAR) {
                assertTrue(valida("+5538999998888"))
            }
        }

        @Test
        fun `nao deve ser valido quando o celular informado for invalido`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida("38999998888"))
                assertFalse(valida("+5538G999988E8"))
            }
        }

        @Test
        fun `nao deve ser valido quando o celular informado for nulo ou vazio`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve ser valido quando o email informado for valido`() {
            with(TipoChave.EMAIL) {
                assertTrue(valida("email@valido.com"))
            }
        }

        @Test
        fun `nao deve ser valido quando email informado for invalido`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida("email-invalido"))
            }
        }

        @Test
        fun `nao deve ser valido quando email nao for informado`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class ALEATORIA {

        @Test
        fun `deve ser valido quando chave aleatoria for nula ou vazia`(){
            with(TipoChave.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
            }
        }

        @Test
        fun `nao deve ser valida quando chave aleatoria possuir um valor informado`() {
            with(TipoChave.ALEATORIA) {
                assertFalse(valida("valor nao pode ser informado"))
            }
        }
    }
}