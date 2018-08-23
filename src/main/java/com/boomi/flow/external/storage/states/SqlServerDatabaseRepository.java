package com.boomi.flow.external.storage.states;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.time.format.DateTimeFormatter;

public class SqlServerDatabaseRepository extends StateDatabaseRepository {

    public SqlServerDatabaseRepository(Jdbi jdbi)
    {
        super(jdbi);
    }

    @Override
    protected void addStateToBatch(PreparedBatch batch, State state) {
        batch.bind("id", state.getId())
                .bind("tenant", state.getTenantId())
                .bind("parent", state.getParentId())
                .bind("flow", state.getFlowId())
                .bind("flowVersion", state.getFlowVersionId())
                .bind("isDone", state.isDone())
                .bind("currentMapElement", state.getCurrentMapElementId())
                .bind("currentUser", state.getCurrentUserId())
                .bind("content", state.getContent())
                .bind("createdAt", state.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .bind("updatedAt", state.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .bind("expiresAt", state.getExpiresAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .add();
    }

    @Override
    protected String upsertQuery() {
        return ("MERGE states WITH (HOLDLOCK) AS oldState " +
                "  USING (SELECT :id id, :tenant tenant_id, :parent parent_id, :flow flow_id, :flowVersion flow_version_id, :isDone is_done, :currentMapElement current_map_element_id, :currentUser current_user_id, :createdAt created_at, :updatedAt updated_at, :expiresAt expires_at, :content content) AS newState " +
                "    ON newState.id = oldState.id  and newState.id = :id " +
                "WHEN MATCHED and oldState.updated_at <= newState.updated_at THEN UPDATE " +
                "    SET flow_id = newState.flow_id, flow_version_id = newState.flow_version_id, is_done = newState.is_done, current_map_element_id = newState.current_map_element_id, current_user_id = newState.current_user_id, updated_at = newState.updated_at, expires_at=newState.expires_at, content = newState.content " +
                "WHEN NOT MATCHED THEN " +
                "    INSERT (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, expires_at, content) " +
                "    VALUES (newState.id, newState.tenant_id, newState.parent_id, newState.flow_id, newState.flow_version_id, newState.is_done, newState.current_map_element_id, newState.current_user_id, newState.created_at, newState.updated_at, newState.expires_at, newState.content); ");

    }
}
