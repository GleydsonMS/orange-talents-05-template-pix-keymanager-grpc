package br.com.zup.edu.pix.registra

import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "Chave pix inválida (\${validatedValue.tipoChave})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class ValidPixKeyValidator: javax.validation.ConstraintValidator<ValidPixKey, NovaChavePix> {

    override fun isValid(value: NovaChavePix?, context: ConstraintValidatorContext): Boolean {

        if (value?.tipoChave == null) {
            return true
        }

        val valid = value.tipoChave.valida(value.chavePix)
        if (!valid) {
            context.disableDefaultConstraintViolation()
            context
                .buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                .addPropertyNode("chavePix").addConstraintViolation()
        }

        return valid
    }
}