package br.com.zup.edu.pix.enums

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    CPF {
        override fun valida(chavePix: String?): Boolean {
            if (chavePix.isNullOrBlank()) return false

            if (!chavePix.matches("[0-9]+".toRegex())) return false

            return CPFValidator().run {
                initialize(null)
                isValid(chavePix, null)
            }
        }
    },
    CELULAR {
        override fun valida(chavePix: String?): Boolean {
            if (chavePix.isNullOrBlank()) return false

            return chavePix.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun valida(chavePix: String?): Boolean {
            if (chavePix.isNullOrBlank()) return false

            return EmailValidator().run {
                initialize(null)
                isValid(chavePix, null)
            }
        }
    },
    ALEATORIA {
        override fun valida(chavePix: String?) = chavePix.isNullOrBlank()
    };

    abstract fun valida(chavePix: String?): Boolean
}