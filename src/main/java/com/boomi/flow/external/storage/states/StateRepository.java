package com.boomi.flow.external.storage.states;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StateRepository {
    void delete(UUID tenant, List<UUID> ids);
    Optional<String> find(UUID tenant, UUID id);
    void save(UUID tenant, List<State> states);
}
