package edu.esi.ds.esientradas.scheduler;

import edu.esi.ds.esientradas.service.IEntradaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LiberacionEntradasJob {

    private final IEntradaService entradaService;

    public LiberacionEntradasJob(IEntradaService entradaService) {
        this.entradaService = entradaService;
    }

    @Scheduled(fixedDelay = 60000)
    public void ejecutarLiberacion() {
        entradaService.liberarEntradasCaducadas();
    }
}