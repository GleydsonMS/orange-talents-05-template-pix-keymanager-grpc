package br.com.zup.edu.integracoes.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("\${itau.contas.url}")
interface ErpItau {

    @Get("/api/v1/clientes/{id}/contas{?tipo}")
    fun consultaContaCliente(@PathVariable id: String, @QueryValue tipo: String): HttpResponse<DadosDaContaResponseResponse>
}