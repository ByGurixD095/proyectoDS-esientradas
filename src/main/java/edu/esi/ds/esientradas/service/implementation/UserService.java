package edu.esi.ds.esientradas.service.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.service.IUsuarioService;

@Component
public class UserService implements IUsuarioService {

    private final String baseUrl;
    private final String apiKeyHeader;
    private final RestTemplate rest;

    public UserService(
            @Value("${esiusuarios.base-url}") String baseUrl,
            @Value("${esiusuarios.api-key}") String apiKeyHeader,
            RestTemplate rest) {
        this.baseUrl = baseUrl;
        this.apiKeyHeader = apiKeyHeader;
        this.rest = rest;
    }

    @Override
    public String validarTokenYObtenerCorreo(String tokenUsuario) {
        if (tokenUsuario == null || tokenUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay token");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", apiKeyHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = rest.exchange(
                    baseUrl + "/users/token/" + tokenUsuario,
                    HttpMethod.GET, entity, String.class);

            String email = response.getBody();
            if (email == null || email.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
            }
            return email;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }
    }
}