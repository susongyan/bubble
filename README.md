# bubble

High performance distributed id generator using the segment algorithm over a relational database.

## Features
- Spring Boot 2 based REST service exposing `/api/ids/{bizTag}` for fetching ids.
- Database-backed `bubble_alloc` table to manage max ids and versioned updates.
- In-memory double-buffer segments with asynchronous prefetch when usage crosses 80%.
- Adaptive step sizing that increases or shrinks the segment size based on how quickly the previous range was consumed.

## Getting started
1. Build and run the service:
   ```bash
   mvn spring-boot:run
   ```
2. Request a new id for a given business tag (e.g. `order`):
   ```bash
   curl http://localhost:8080/api/ids/order
   ```

## Database schema
The service expects a `bubble_alloc` table. On startup, H2 will be initialized with the following schema and sample data:
```sql
CREATE TABLE IF NOT EXISTS bubble_alloc (
    biz_tag VARCHAR(64) PRIMARY KEY,
    max_id BIGINT NOT NULL,
    step INT NOT NULL,
    version INT NOT NULL
);

INSERT INTO bubble_alloc (biz_tag, max_id, step, version) VALUES ('order', 0, 1000, 0);
INSERT INTO bubble_alloc (biz_tag, max_id, step, version) VALUES ('payment', 0, 500, 0);
```
Use a real relational database in production and seed `biz_tag` rows according to your business domains.

## Notes
- Prefetch kicks in when the current segment is 80% consumed; the threshold is configurable via `bubble.segment.prefetch-threshold`.
- Step size can grow when segments are consumed quickly and shrink when they are consumed slowly, bounded by `bubble.segment.min-step` and `bubble.segment.max-step`.

## Benchmarking
Run the micro-benchmarks with the dedicated Maven profile to generate both JSON and human-readable throughput reports:

```bash
mvn -Pbenchmark clean verify
```

Artifacts are written to `target/jmh-results.json` and `target/jmh-results.txt` for further analysis or historical tracking.
