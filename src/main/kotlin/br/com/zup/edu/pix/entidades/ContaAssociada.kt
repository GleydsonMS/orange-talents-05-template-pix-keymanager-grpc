package br.com.zup.edu.pix.entidades

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size


@Embeddable
class ContaAssociada(
    @field:NotBlank
    @Column(nullable = false)
    val instituicao: String,

    @field:NotBlank
    @Column(name = "nome_titular", nullable = false)
    val nomeDoTitular: String,

    @field:NotBlank
    @field:Size(max = 11)
    @Column(name = "cpf_titular", length = 11, nullable = false)
    val cpfDoTitular: String,

    @field:NotBlank
    @field:Size(max = 4)
    @Column(length = 4, nullable = false)
    val agencia: String,

    @field:NotBlank
    @field:Size(max = 6)
    @Column(name = "numero_conta", length = 6, nullable = false)
    val numeroDaConta: String
)