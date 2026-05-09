package edu.esi.ds.esientradas.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.esi.ds.esientradas.service.IColaService;

@Component
public class ExpirarTurnosJob {

    private final IColaService colaService;

    public ExpirarTurnosJob(IColaService colaService) {
        this.colaService = colaService;
    }

    @Scheduled(fixedDelay = 60000)
    public void ejecutarLiberacion() {
        colaService.expirarTurnosVencidos();
    }
}
