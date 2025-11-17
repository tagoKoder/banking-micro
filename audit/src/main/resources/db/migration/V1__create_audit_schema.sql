CREATE TABLE IF NOT EXISTS audit_events (
    id              UUID PRIMARY KEY,
    topic           VARCHAR(200) NOT NULL,
    partition       INT          NOT NULL,
    "offset"        BIGINT       NOT NULL,
    key             VARCHAR(500),
    event_type      VARCHAR(200),
    aggregate_type  VARCHAR(200),
    aggregate_id    VARCHAR(200),
    headers         JSONB        NOT NULL,
    payload         JSONB        NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- evita duplicados si el listener reintenta
ALTER TABLE audit_events
  ADD CONSTRAINT uq_topic_partition_offset UNIQUE (topic, partition, "offset");

CREATE INDEX IF NOT EXISTS idx_audit_events_agg
  ON audit_events(aggregate_type, aggregate_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_events_topic_time
  ON audit_events(topic, created_at DESC);
