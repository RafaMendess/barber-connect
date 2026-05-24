package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.Barbearia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarbeariaRepository extends JpaRepository<Barbearia, Long> {

    boolean existsByCnpj(String cnpj);
}
