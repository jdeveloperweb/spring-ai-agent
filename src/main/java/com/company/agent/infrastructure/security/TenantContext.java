package com.company.agent.infrastructure.security;

import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public void setCurrentTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    public String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        if (tenant == null) {
            throw new IllegalStateException("Nenhum tenant definido no contexto atual");
        }
        return tenant;
    }

    public void clear() {
        CURRENT_TENANT.remove();
    }

    public boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}
