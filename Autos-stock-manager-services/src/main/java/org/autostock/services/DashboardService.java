package org.autostock.services;

import org.autostock.models.Paiement;

public interface DashboardService extends IService<Paiement, Long> {
    void recomputeVenteTotals(Long venteId);
}
