package com.boomi.flow.external.storage.states;

import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StateRepository {
    private final static Logger LOGGER = LoggerFactory.getLogger(StateRepository.class);

    private final Jdbi jdbi;

    @Inject
    public StateRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void save(UUID tenant, List<State> states) {
        jdbi.withHandle(handle -> {
            // Insert a new state, or update an existing one if one exists and the one we have is newer
            var batch = handle.prepareBatch("INSERT INTO states (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content, token) VALUES (:id, :tenant, :parent, :flow, :flowVersion, :isDone, :currentMapElement, :currentUser, :createdAt, :updatedAt, :content::jsonb, :token) ON CONFLICT (id) DO UPDATE SET (flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, updated_at, content, token) = (:flow, :flowVersion, :isDone, :currentMapElement, :currentUser, :updatedAt, :content::jsonb, :token) WHERE states.id = :id AND states.updated_at <= :updatedAt");

            for (State state : states) {
                LOGGER.info("Saving a state with the ID {} in the tenant {}", state.getId(), tenant);

                batch.bind("id", state.getId())
                        .bind("tenant", state.getTenantId())
                        .bind("parent", state.getParentId())
                        .bind("flow", state.getFlowId())
                        .bind("flowVersion", state.getFlowVersionId())
                        .bind("isDone", state.isDone())
                        .bind("currentMapElement", state.getCurrentMapElementId())
                        .bind("currentUser", state.getCurrentUserId())
                        .bind("content", state.getContent())
                        .bind("token", state.getToken());

                // we save every date in utc zone if we are using mysql
                if ("mysql".equals(System.getenv("DATABASE_TYPE").toLowerCase())) {
                    batch.bind("createdAt", state.getCreatedAt().atZoneSameInstant(ZoneOffset.UTC))
                            .bind("updatedAt", state.getUpdatedAt().atZoneSameInstant(ZoneOffset.UTC));
                } else {
                    batch.bind("createdAt", state.getCreatedAt())
                            .bind("updatedAt", state.getUpdatedAt());
                }

                batch.add();
            }

            return batch.execute();
        });
    }

    public Optional<String> find(UUID tenant, UUID id) {
        LOGGER.info("Loading state with the ID {} from the tenant {}", id, tenant);

        return jdbi.withHandle(handle -> handle.createQuery("SELECT content FROM states WHERE id = :id AND tenant_id = :tenant")
                .bind("id", id)
                .bind("tenant", tenant)
                .mapTo(String.class)
                .findFirst());
    }
}
