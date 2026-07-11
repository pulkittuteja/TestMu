# HyperExecute — Solutions Engineer Technical Assignment

Submission against the sample project
[`LambdaTest/testng-selenium-hyperexecute-sample`](https://github.com/LambdaTest/testng-selenium-hyperexecute-sample).
All four tasks were completed and **run for real on HyperExecute** — the job links and log
excerpts below are from actual runs.

## Jobs run (evidence)

| Job | Purpose | Result | Link (jobId) |
|-----|---------|--------|--------------|
| A | Task 1 + 2 — corrected YAML, full TestNG suite | **4/4 tasks passed** | `af9a1118-dc04-44ed-aec0-817cc63314d6` |
| B | Task 3 — forced failure + retries | failed as expected, **retried 2×** | `4233bbd0-78fb-4e3d-9c32-a0cb0a5f989b` |
| C | Task 3 re-run + corrected pre-step env print | failed as expected, retried 2× | `7a076f79-52fc-43a4-af6a-8119b2ad7efe` |

Dashboard URL pattern: `https://hyperexecute.lambdatest.com/hyperexecute/task?jobId=<jobId>`
Raw log excerpts are in [`evidence/`](evidence/).

## Repo contents
```
yaml/
  broken_original.yaml              # the original broken gist (reference)
  hyperexecute_task1_fixed.yaml     # Task 1: corrected YAML (annotated)
  hyperexecute_final.yaml           # Task 1 + 2 + 3 combined (annotated)
  run_used_task1-2_he_main.yaml     # exact config used for Job A
  run_used_task3_he_retry.yaml      # exact config used for Jobs B/C
src-changes/
  env-read-snippet.java             # Task 2: System.getenv in a test
  Test5.java                        # Task 3: intentional hard-failure test
  testng_win.xml.snippet            # Task 3: register Test_5 for discovery
linux/
  commands.md + sample.log + data.txt   # Task 4
evidence/
  task1_job_success.txt, task2_prestep.txt, task2_during_test.txt,
  task3_retry.txt, full-logs/*          # real log excerpts
```

---

## Task 1 — Fix the broken YAML

**Corrected file:** [`yaml/hyperexecute_task1_fixed.yaml`](yaml/hyperexecute_task1_fixed.yaml)

I diffed the gist against the repo's own working baseline
(`yaml/win/v1/testng_hyperexecute_autosplit_sample.yaml`) rather than guessing. Three real
injected bugs, plus a few dropped blocks:

| # | Original | Why it broke the job | Fix |
|---|----------|----------------------|-----|
| 1 | `env: TOKEN: <val>` (one line) | Invalid YAML — a mapping value written inline on the parent key's line → *"mapping values are not allowed here"*, the whole file fails to parse. | `env:` block with `TOKEN:` indented on the next line. |
| 2 | `&nbsp;retryOnFailure: true` (leading space) | A top-level key indented by one space = inconsistent indentation → parse error / mis-nesting under `testRunnerCommand`. | De-indent to column 0. |
| 3 | `conCurrency: 1` | Wrong key casing; HyperExecute expects `concurrency`. The mis-cased key is silently ignored, so concurrency is never applied. | `concurrency: 4`. |

Dropped blocks I restored to match the working sample (each can make the run fail or lose caching):
- `runtime: {language: java, version: 11}` — makes the Java runtime explicit on the runner.
- `cacheKey` + `cacheDirectories: [.m2]` — dependency caching between runs.
- `pre` step missing `-Dmaven.repo.local=./.m2` — added so pre-resolved deps land in the cached repo.

**Red herring (NOT a bug):** the odd-looking runner line
``mvn test `-Dplatname=win `-Dmaven.repo.local=./.m2 dependency:resolve `-DselectedTests=$test``
is **identical to the official working sample**. The backticks are escapes for the Windows
runner and `$test` is a HyperExecute placeholder (not a shell variable) that the CLI substitutes
per discovered entity — so I left it unchanged. Calling it "broken" would have been wrong.

**Proof it runs:** Job A finished with `Failed Tasks: 0`, `Pass test stage percentage: 100.00%`
across all 4 discovered tests (`evidence/task1_job_success.txt`).

---

## Task 2 — Environment variables

**File:** [`yaml/hyperexecute_final.yaml`](yaml/hyperexecute_final.yaml)

**1. Defined in YAML:**
```yaml
env:
  TOKEN: anvdegtod-asdaasda0asda-asda
  ENVIRONMENT: staging
```

**2. Printed in pre-steps** — with a real gotcha I hit and debugged:
```yaml
pre:
  - echo "===== ENVIRONMENT value in pre-steps below ====="
  - env | grep ENVIRONMENT      # prints: ENVIRONMENT=staging
```
> **Why not `echo "$ENVIRONMENT"`?** HyperExecute runs its own `$var` templating on command
> strings (the same mechanism that substitutes `$test`). A bare `$ENVIRONMENT` gets consumed by
> HyperExecute *before* the shell sees it and prints empty (I confirmed: `$env:ENVIRONMENT`
> printed `:ENVIRONMENT`, and `$ENVIRONMENT` printed nothing). Referencing the var **without a
> `$`** via `env | grep` sidesteps the templating. Verified output: `ENVIRONMENT=staging`
> (`evidence/task2_prestep.txt`).

**3. Read inside a test** — [`src-changes/env-read-snippet.java`](src-changes/env-read-snippet.java),
added to `Test1.java` (same `System.getenv` pattern the sample uses for `LT_USERNAME`):
```java
String environment = System.getenv("ENVIRONMENT");
System.out.println("ENVIRONMENT (during test execution) => " + environment);
```
Verified output from the Test_1 scenario log: `ENVIRONMENT (during test execution) => staging`
(`evidence/task2_during_test.txt`). Note `System.getenv` works fine — the collision above is
purely about `$`-referencing inside pre-step command strings.

---

## Task 3 — Force a failure and configure retries

**Failing test:** [`src-changes/Test5.java`](src-changes/Test5.java) → `src/test/java/Test5.java`.
A deterministic `Assert.fail(...)` (hard failure, not flaky), no Selenium so the failure and its
retries are unambiguous.

**Registered for discovery:** [`src-changes/testng_win.xml.snippet`](src-changes/testng_win.xml.snippet).
For an isolated fast run I put `Test_5` in its own suite (`xml/testng_retry.xml`) and ran it with
`-Dplatname=retry`.

**Retry config:**
```yaml
retryOnFailure: true
maxRetries: 2
```

**How it works & proof:** `retryOnFailure` is HyperExecute's **task-level** retry — when the
`Test_5` task exits non-zero, HyperExecute re-runs the whole entity up to `maxRetries` times.
(Distinct from a TestNG `IRetryAnalyzer`, which retries at the method level inside one JVM.)
The run produced three scenario logs — **1 initial attempt + 2 retries** — each throwing the
intentional `AssertionError` (`evidence/task3_retry.txt`, `evidence/full-logs/jobC_Test_5_*`):
```
-Test_5-          Tests run: 1, Failures: 1  ... Intentional hard assertion failure ...
-Test_5--retry-1  Tests run: 1, Failures: 1  ... Intentional hard assertion failure ...
-Test_5--retry-2  Tests run: 1, Failures: 1  ... Intentional hard assertion failure ...
```

---

## Task 4 — Linux/Unix basics

Commands, one-line explanations, and real sample I/O are in
[`linux/commands.md`](linux/commands.md):

```bash
grep -E 'FAIL|ERROR' sample.log            # 1. show all failure/error lines
awk '{print $2}' data.txt                  # 2. print the 2nd (browser) column
sed 's/staging/production/g' sample.log    # 3. replace staging -> production
grep 'FAIL' sample.log | awk '{print $4}'  # 4. pipe: failed lines -> test-name field
```

---

## How to reproduce

```powershell
$env:LT_USERNAME="<username>"; $env:LT_ACCESS_KEY="<access_key>"
git clone https://github.com/LambdaTest/testng-selenium-hyperexecute-sample; cd testng-selenium-hyperexecute-sample
# apply src-changes/ (Test1.java env print, Test5.java, xml/testng_retry.xml) and drop the yaml/ files in
curl -o hyperexecute.exe -u "$env:LT_USERNAME:$env:LT_ACCESS_KEY" https://downloads.lambdatest.com/hyperexecute/windows/hyperexecute.exe
.\hyperexecute.exe --user $env:LT_USERNAME --key $env:LT_ACCESS_KEY --config run_used_task1-2_he_main.yaml --download-logs
.\hyperexecute.exe --user $env:LT_USERNAME --key $env:LT_ACCESS_KEY --config run_used_task3_he_retry.yaml --download-logs
```
Java/Maven are **not** needed locally — the Maven build runs on the HyperExecute cloud runner.
`--validate` checks a YAML without running it; `--download-logs` pulls per-stage logs into `logs/`.
