create table states
(
  id UNIQUEIDENTIFIER not null primary key,
  tenant_id UNIQUEIDENTIFIER not null,
  parent_id UNIQUEIDENTIFIER,
  flow_id UNIQUEIDENTIFIER not null,
  flow_version_id UNIQUEIDENTIFIER not null,
  is_done bit not null,
  current_map_element_id UNIQUEIDENTIFIER not null,
  current_user_id UNIQUEIDENTIFIER,
  created_at datetimeoffset not null,
  updated_at datetimeoffset not null,
  content nvarchar(max) CONSTRAINT [content should be formatted as JSON] CHECK ( ISJSON(content)>0 )
);