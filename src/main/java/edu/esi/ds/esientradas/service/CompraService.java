package edu.esi.ds.esientradas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CompraService {

    @Value("${api_key_Stripe}")
    private String _key;

}
