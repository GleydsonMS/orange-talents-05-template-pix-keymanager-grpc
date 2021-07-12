package br.com.zup.edu.pix.repositorios

import br.com.zup.edu.pix.entidades.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {
    fun existsByChavePix(chavePix: String?) : Boolean
}