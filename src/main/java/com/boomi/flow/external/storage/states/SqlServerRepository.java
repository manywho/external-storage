package com.boomi.flow.external.storage.states;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

public class SqlServerRepository  extends StateRepository {

    public SqlServerRepository(Jdbi jdbi)
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
                .bind("createdAt", state.getCreatedAt())
                .bind("updatedAt", state.getUpdatedAt());

        batch.add();
    }

    @Override
    protected String upsertQuery() {
        return "MERGE states as Target " +
                "USING states as states2 ON states2.id = :id " +
                "WHEN MATCHED AND states2.updated_at <= :updatedAt THEN " +
                "  UPDATE  SET flow_id =:flow, flow_version_id =:flowVersion, is_done =:isDone, current_map_element_id=:currentMapElement, current_user_id=:currentUser, updated_at =:updatedAt, content=:content " +
                "WHEN NOT MATCHED THEN " +
                "INSERT (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content) VALUES (:id, :tenant, :parent, :flow, :flowVersion, :isDone, :currentMapElement, :currentUser, :createdAt, :updatedAt, :content);";
    }
}
