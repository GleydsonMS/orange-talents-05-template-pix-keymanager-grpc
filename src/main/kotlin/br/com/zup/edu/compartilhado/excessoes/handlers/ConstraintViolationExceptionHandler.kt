package br.com.zup.edu.compartilhado.excessoes.handlers

import br.com.zup.edu.compartilhado.excessoes.ExceptionHandler
import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintViolationExceptionHandler: ExceptionHandler<ConstraintViolationException> {
    override fun handle(e: ConstraintViolationException): ExceptionHandler.StatusWithDetails {
        val details = BadRequest.newBuilder()
            .addAllFieldViolations(e.constraintViolations.map {
                BadRequest.FieldViolation.newBuilder()
                    .setField(it.propertyPath.last().name ?: "?? key ??")
                    .setDescription(it.message)
                    .build()
            })
            .build()

        val statusProto = Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Dados inválidos")
            .addDetails(Any.pack(details))
            .build()

        return ExceptionHandler.StatusWithDetails(statusProto)
    }

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }
}