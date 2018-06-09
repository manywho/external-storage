create table states
(
  id uuid not null
    constraint pk_states
    primary key,
  tenant_id uuid not null,
  parent_id uuid,
  flow_id uuid not null,
  flow_version_id uuid not null,
  is_done boolean not null,
  current_map_element_id uuid not null,
  current_user_id uuid,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  content jsonb not null
);
