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
mvn -Pbenchmark jmh:benchmark
```

Artifacts are written to `target/jmh-results.json` and `target/jmh-results.txt` for further analysis or historical tracking.

## Benchmark Results (Sample)
Sample output captured from `target/jmh-results.txt` (with "+/-" used instead of "±" to keep ASCII-only text):

```text
# JMH version: 1.37
# VM version: JDK 21.0.6, Java HotSpot(TM) 64-Bit Server VM, 21.0.6+8-LTS-188
# VM invoker: C:\Program Files\Java\jdk-21\bin\java.exe
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 5 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.zuomagai.bubble.benchmark.SegmentIdServiceBenchmark.nextId

# Run progress: 0.00% complete, ETA 00:08:20
# Fork: 1 of 5
# Warmup Iteration   1: 71091.016 ops/ms
# Warmup Iteration   2: 71027.458 ops/ms
# Warmup Iteration   3: 77379.887 ops/ms
# Warmup Iteration   4: 77396.569 ops/ms
# Warmup Iteration   5: 77519.774 ops/ms
Iteration   1: 77957.527 ops/ms
Iteration   2: 77816.141 ops/ms
Iteration   3: 77132.316 ops/ms
Iteration   4: 77623.962 ops/ms
Iteration   5: 77701.084 ops/ms

# Run progress: 20.00% complete, ETA 00:06:48
# Fork: 2 of 5
# Warmup Iteration   1: 70381.471 ops/ms
# Warmup Iteration   2: 70120.156 ops/ms
# Warmup Iteration   3: 77548.797 ops/ms
# Warmup Iteration   4: 77679.360 ops/ms
# Warmup Iteration   5: 77408.537 ops/ms
Iteration   1: 77506.213 ops/ms
Iteration   2: 78621.067 ops/ms
Iteration   3: 77952.675 ops/ms
Iteration   4: 77459.238 ops/ms
Iteration   5: 77969.102 ops/ms

# Run progress: 40.00% complete, ETA 00:05:05
# Fork: 3 of 5
# Warmup Iteration   1: 71951.879 ops/ms
# Warmup Iteration   2: 70463.396 ops/ms
# Warmup Iteration   3: 78449.798 ops/ms
# Warmup Iteration   4: 78158.014 ops/ms
# Warmup Iteration   5: 77622.470 ops/ms
Iteration   1: 78359.900 ops/ms
Iteration   2: 78363.807 ops/ms
Iteration   3: 77654.298 ops/ms
Iteration   4: 78811.235 ops/ms
Iteration   5: 78799.358 ops/ms

# Run progress: 60.00% complete, ETA 00:03:23
# Fork: 4 of 5
# Warmup Iteration   1: 71461.598 ops/ms
# Warmup Iteration   2: 70538.707 ops/ms
# Warmup Iteration   3: 77716.674 ops/ms
# Warmup Iteration   4: 77349.796 ops/ms
# Warmup Iteration   5: 78309.379 ops/ms
Iteration   1: 77684.644 ops/ms
Iteration   2: 77747.008 ops/ms
Iteration   3: 78404.550 ops/ms
Iteration   4: 78289.100 ops/ms
Iteration   5: 74579.169 ops/ms

# Run progress: 80.00% complete, ETA 00:01:41
# Fork: 5 of 5
# Warmup Iteration   1: 65667.953 ops/ms
# Warmup Iteration   2: 63212.349 ops/ms
# Warmup Iteration   3: 74969.063 ops/ms
# Warmup Iteration   4: 76126.605 ops/ms
# Warmup Iteration   5: 74992.638 ops/ms
Iteration   1: 75528.279 ops/ms
Iteration   2: 76644.698 ops/ms
Iteration   3: 74287.387 ops/ms
Iteration   4: 74443.476 ops/ms
Iteration   5: 71500.179 ops/ms


Result "com.zuomagai.bubble.benchmark.SegmentIdServiceBenchmark.nextId":
  77153.456 +/- (99.9%) 1319.299 ops/ms [Average]
  (min, avg, max) = (71500.179, 77153.456, 78811.235), stdev = 1761.226
  CI (99.9%): [75834.158, 78472.755] (assumes normal distribution)


# Run complete. Total time: 00:08:30

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

NOTE: Current JVM experimentally supports Compiler Blackholes, and they are in use. Please exercise
extra caution when trusting the results, look into the generated code to check the benchmark still
works, and factor in a small probability of new VM bugs. Additionally, while comparisons between
different JVMs are already problematic, the performance difference caused by different Blackhole
modes can be very significant. Please make sure you use the consistent Blackhole mode for comparisons.

Benchmark                          Mode  Cnt      Score      Error   Units
SegmentIdServiceBenchmark.nextId  thrpt   25  77153.456 +/- 1319.299  ops/ms

Benchmark result is saved to target/jmh-results.json
```
