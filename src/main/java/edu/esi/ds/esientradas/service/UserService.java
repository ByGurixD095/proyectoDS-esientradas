package edu.esi.ds.esientradas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @Value("${esiusuarios.base-url}")
    private String baseUrl;

    @Value("${esiusuarios.api-key}")
    private String _apiKeyHeader;

    @Autowired
    RestTemplate rest;

    public String validarTokenYObtenerCorreo(String tokenUsuario) {
        if (tokenUsuario == null || tokenUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay token");
        }

        String endpoint = baseUrl + "/users/token/";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", _apiKeyHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = rest.exchange(
                    endpoint + tokenUsuario,
                    HttpMethod.GET,
                    entity,
                    String.class);
            String email = response.getBody();

            if (email == null || email.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Token inválido o expirado");
            }

            return email;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token inválido o expirado");
        }
    }
}