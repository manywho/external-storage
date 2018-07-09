package com.boomi.flow.external.storage.states;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class StateDatabaseRepository implements StateRepository {
    private final static Logger LOGGER = LoggerFactory.getLogger(StateDatabaseRepository.class);

    private final Jdbi jdbi;

    public StateDatabaseRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void save(UUID tenant, List<State> states) {
        jdbi.withHandle(handle -> {
            // Insert a new state, or update an existing one if one exists and the one we have is newer
            var batch = handle.prepareBatch(upsertQuery());

            for (State state : states) {
                LOGGER.info("Saving a state with the ID {} in the tenant {}", state.getId(), tenant);

                addStateToBatch(batch, state);
            }

            return batch.execute();
        });
    }

    protected abstract void addStateToBatch(PreparedBatch batch, State state);

    protected abstract String upsertQuery();

    public Optional<String> find(UUID tenant, UUID id) {
        LOGGER.info("Loading state with the ID {} from the tenant {}", id, tenant);

        return jdbi.withHandle(handle -> handle.createQuery("SELECT content FROM states WHERE id = :id AND tenant_id = :tenant")
                .bind("id", id)
                .bind("tenant", tenant)
                .mapTo(String.class)
                .findFirst());
    }
}
