package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.BarbeariaDTO;
import com.projeto.barberconnect.entity.Barbearia;
import com.projeto.barberconnect.exception.CnpjAlreadyExistsException;
import com.projeto.barberconnect.exception.InvalidCnpjException;
import com.projeto.barberconnect.repository.BarbeariaRepository;
import com.projeto.barberconnect.validator.CnpjValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BarbeariaService {

    @Autowired
    private BarbeariaRepository repository;

    /**
     * Cadastra uma nova barbearia após validar o CNPJ e verificar duplicidade.
     *
     * Fluxo:
     * 1. Remove máscara do CNPJ e normaliza para apenas dígitos
     * 2. Valida o CNPJ (formato + dígitos verificadores)
     * 3. Verifica duplicidade já com o CNPJ normalizado
     * 4. Monta a entidade e salva no banco
     *
     * O CNPJ é sempre persistido sem máscara (14 dígitos).
     * A formatação para exibição é responsabilidade do frontend.
     *
     * @param dto dados recebidos da requisição
     * @return a barbearia salva com ID gerado
     */
    public Barbearia cadastrar(BarbeariaDTO dto) {

        // Normaliza o CNPJ removendo máscara antes de qualquer operação
        String cnpj = dto.getCnpj().replaceAll("[^\\d]", "");

        if (!CnpjValidator.isValid(cnpj)) {
            throw new InvalidCnpjException(cnpj);
        }

        if (repository.existsByCnpj(cnpj)) {
            throw new CnpjAlreadyExistsException(cnpj);
        }

        Barbearia barbearia = new Barbearia();
        barbearia.setNome(dto.getNome());
        barbearia.setCnpj(cnpj); // sempre sem máscara
        barbearia.setTelefone(dto.getTelefone());
        barbearia.setEndereco(dto.getEndereco());
        barbearia.setHorarioFuncionamento(dto.getHorarioFuncionamento());
        barbearia.setFotoUrl(dto.getFotoUrl());
        barbearia.setLatitude(dto.getLatitude());
        barbearia.setLongitude(dto.getLongitude());

        return repository.save(barbearia);
    }
}
