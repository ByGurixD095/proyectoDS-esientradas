package edu.esi.ds.esientradas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final String baseUrl = "http://localhost:8081";

    @Autowired
    RestTemplate rest;

    public String validarTokenYObtenerCorreo(String tokenUsuario) {
        if (tokenUsuario == null || tokenUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay token");
        }

        String endpoint = baseUrl + "/external/token/";
        try {
            String email = rest.getForObject(endpoint + tokenUsuario, String.class);

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
