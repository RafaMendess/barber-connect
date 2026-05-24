package com.projeto.barberconnect.validator;

public final class CnpjValidator {

    // Classe utilitária — não deve ser instanciada
    private CnpjValidator() {}

    /**
     * Valida um CNPJ, aceitando com ou sem máscara.
     * Exemplos válidos: "12.345.678/0001-95" ou "12345678000195"
     *
     * @param cnpj o CNPJ a ser validado
     * @return true se o CNPJ for válido, false caso contrário
     */
    public static boolean isValid(String cnpj) {

        if (cnpj == null) {
            return false;
        }

        // Remove qualquer caractere não numérico (pontos, barra, hífen)
        cnpj = cnpj.replaceAll("[^\\d]", "");

        // CNPJ deve ter exatamente 14 dígitos
        if (cnpj.length() != 14) {
            return false;
        }

        // Rejeita sequências de dígitos iguais (ex: 11111111111111)
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            // Pesos para cálculo do primeiro dígito verificador
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            // Pesos para cálculo do segundo dígito verificador
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            // Cálculo do primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso1[i];
            }
            int resto = soma % 11;
            int digito1 = resto < 2 ? 0 : 11 - resto;

            // Cálculo do segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso2[i];
            }
            resto = soma % 11;
            int digito2 = resto < 2 ? 0 : 11 - resto;

            // Verifica se os dígitos calculados batem com os do CNPJ informado
            return digito1 == Character.getNumericValue(cnpj.charAt(12))
                    && digito2 == Character.getNumericValue(cnpj.charAt(13));

        } catch (Exception e) {
            return false;
        }
    }
}
