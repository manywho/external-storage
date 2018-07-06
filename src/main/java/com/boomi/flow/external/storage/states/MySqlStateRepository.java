package com.boomi.flow.external.storage.states;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import java.time.ZoneOffset;
import java.util.UUID;

public class MySqlStateRepository extends StateRepository {

    public MySqlStateRepository(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected void addStateToBatch(PreparedBatch batch, State state) {
        batch.bind("id", convertUuidToString(state.getId()))
                .bind("tenant", convertUuidToString(state.getTenantId()))
                .bind("parent", convertUuidToString(state.getParentId()))
                .bind("flow", convertUuidToString(state.getFlowId()))
                .bind("flowVersion", convertUuidToString(state.getFlowVersionId()))
                .bind("isDone", convertBoolean(state.isDone()))
                .bind("currentMapElement", convertUuidToString(state.getCurrentMapElementId()))
                .bind("currentUser", convertUuidToString(state.getCurrentUserId()))
                .bind("content", state.getContent())
                .bind("createdAt", state.getCreatedAt().atZoneSameInstant(ZoneOffset.UTC))
                .bind("updatedAt", state.getUpdatedAt().atZoneSameInstant(ZoneOffset.UTC))
                .add();
    }

    private Integer convertBoolean(boolean value) {
        return (value == true)? 1: 0;
    }

    private String convertUuidToString(UUID uuid) {
        if (uuid != null) {
            return uuid.toString();
        }

        return null;
    }

    @Override
    protected String upsertQuery() {
        // "on duplicate key update" with a WHERE expression is not supported in MySql
        // the column updated_at needs to be the last updated, in other case some fields can not be updated

        return "INSERT INTO states (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content) " +
                    "VALUES (:id, :tenant, :parent, :flow, :flowVersion, :isDone, :currentMapElement, :currentUser, :createdAt, :updatedAt, :content) " +
                "ON DUPLICATE KEY UPDATE " +
                    "flow_id = IF(updated_at < VALUES(updated_at), VALUES(flow_id), flow_id)," +
                    "flow_version_id = IF(updated_at < VALUES(updated_at), VALUES(flow_version_id), flow_version_id)," +
                    "is_done = IF(updated_at < VALUES(updated_at), VALUES(is_done), is_done)," +
                    "current_map_element_id = IF(updated_at < VALUES(updated_at), VALUES(current_map_element_id), current_map_element_id)," +
                    "current_user_id= IF(updated_at < VALUES(updated_at), VALUES(current_user_id), current_user_id)," +
                    "content= IF(updated_at < VALUES(updated_at), VALUES(content), content)," +
                    "updated_at= IF(updated_at < VALUES(updated_at), VALUES(updated_at), updated_at)";
    }
}
