package br.com.zup.edu.pix.repositorios

import br.com.zup.edu.pix.entidades.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {
    fun existsByChavePix(chavePix: String?) : Boolean
    fun findByIdAndClienteId(id: String?, clienteId: UUID?): Optional<ChavePix>
    fun findByChavePix(chavePix: String): Optional<ChavePix>
    fun findAllByClienteId(clienteId: UUID): List<ChavePix>
}