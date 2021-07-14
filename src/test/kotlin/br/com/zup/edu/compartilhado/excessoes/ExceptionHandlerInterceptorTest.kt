package br.com.zup.edu.compartilhado.excessoes

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInvocationContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ExceptionHandlerInterceptorTest {

    @Mock
    lateinit var context: MethodInvocationContext<BindableService, Any?>

    val interceptor = ExceptionHandlerInterceptor(resolver = ExceptionHandlerResolver(handlers = emptyList()))

    @Test
    fun `deve capturar a excecao lancada pelo metodo e gerrar um erro na resposta grpc`(@Mock streamObserver: StreamObserver<*>) {
        with(context) {
            `when`(proceed()).thenThrow(RuntimeException("argh!"))
            `when`(parameterValues).thenReturn(arrayOf(null, streamObserver))
        }

        interceptor.intercept(context)

        verify(streamObserver).onError(notNull())
    }

    @Test
    fun `deve retornar a mesma resposta caso o metodo nao gere nenhuma excecao`() {
        val expected = "whatever"

        `when`(context.proceed()).thenReturn(expected)

        Assertions.assertEquals(expected, interceptor.intercept(context))
    }
}