#!/usr/bin/env python3
import re
import statistics
import sys
from pathlib import Path

LINE_PATTERN = re.compile(r"TS=(\d+),TJ=(\d+)")


def parse_samples(log_path: Path):
    ts_samples = []
    tj_samples = []

    with log_path.open("r", encoding="utf-8") as log_file:
        for line_number, raw_line in enumerate(log_file, start=1):
            line = raw_line.strip()
            if not line:
                continue

            match = LINE_PATTERN.fullmatch(line)
            if match is None:
                raise ValueError(f"Unrecognized log format on line {line_number}: {line}")

            ts_samples.append(int(match.group(1)))
            tj_samples.append(int(match.group(2)))

    if not ts_samples:
        raise ValueError(f"No timing samples found in {log_path}")

    return ts_samples, tj_samples


def main():
    if len(sys.argv) != 2:
        print("Usage: python log_processing.py <path-to-search-timing.log>", file=sys.stderr)
        return 1

    log_path = Path(sys.argv[1])
    if not log_path.is_file():
        print(f"Log file not found: {log_path}", file=sys.stderr)
        return 1

    try:
        ts_samples, tj_samples = parse_samples(log_path)
    except ValueError as exc:
        print(str(exc), file=sys.stderr)
        return 1

    print(f"Samples: {len(ts_samples)}")
    print(f"Average TS (ns): {statistics.fmean(ts_samples):.2f}")
    print(f"Average TJ (ns): {statistics.fmean(tj_samples):.2f}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
