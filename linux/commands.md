# Task 4 - Linux/Unix basics

Sample input files in this folder:
- `sample.log` — a mock HyperExecute-style log with INFO/PASS/FAIL/ERROR lines.
- `data.txt` — a space-delimited file: `<test> <browser> <platform> <result>`.

---

### 1. `grep` — find all FAIL/ERROR lines

```bash
grep -E 'FAIL|ERROR' sample.log
```
**What it does:** prints every line containing `FAIL` or `ERROR` (`-E` enables the `|` alternation).

**Output:**
```
2026-07-10 10:00:09 FAIL  Test_2 assertion mismatch expected 4 got 5
2026-07-10 10:00:10 ERROR Test_2 could not read element on staging
2026-07-10 10:00:16 FAIL  Test_5 intentional assertion failure on staging
2026-07-10 10:00:20 FAIL  Test_5 intentional assertion failure on staging
```

---

### 2. `awk` — print the second column

```bash
awk '{print $2}' data.txt
```
**What it does:** for each line, prints field 2 (whitespace-delimited) — here the browser column.

**Output:**
```
Chrome
Firefox
Edge
Chrome
```

---

### 3. `sed` — find-and-replace `staging` -> `production`

```bash
sed 's/staging/production/g' sample.log
```
**What it does:** substitutes every (`g` = global) occurrence of `staging` with `production` on each line, printing the result to stdout. Add `-i` to edit the file in place.

**Output (first 3 lines):**
```
2026-07-10 10:00:01 INFO  Test_1 started on production
2026-07-10 10:00:05 PASS  Test_1 element_addition completed
2026-07-10 10:00:06 INFO  Test_2 started on production
```

---

### 4. Pipe — chain `grep` then `awk`

```bash
grep 'FAIL' sample.log | awk '{print $4}'
```
**What it does:** `grep` keeps only the failed lines, then `awk` extracts field 4 (the test name) from each — i.e. "which tests hard-failed?"

**Output:**
```
Test_2
Test_5
Test_5
```

Bonus (unique failing test names): `grep 'FAIL' sample.log | awk '{print $4}' | sort -u`
