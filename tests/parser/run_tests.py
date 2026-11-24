#!/usr/bin/env python3
"""Compile fresh classes in a temporary folder; run 38 fixtures and reflection test 39."""
from pathlib import Path
import json
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
CASES = json.loads((ROOT / 'tests/parser/manifest.json').read_text())

def run(args):
    return subprocess.run(args, cwd=ROOT, capture_output=True, text=True, timeout=15)

def main():
    ids = [case['id'] for case in CASES]
    assert sorted(ids) == list(range(1, 39)), 'Expected exactly tests 1–38'
    failures = 0
    with tempfile.TemporaryDirectory(prefix='jay-parser-tests-') as build:
        sources = sorted(ROOT.glob('*.java')) + [ROOT / 'tests/Test39IsInteger.java', ROOT / 'tests/RegressionTests.java']
        compiled = run(['javac', '-d', build, *map(str, sources)])
        if compiled.returncode:
            raise SystemExit(compiled.stdout + compiled.stderr)
        for case in CASES:
            try:
                result = run(['java', '-cp', build, 'ParserDemo', str(ROOT / case['file'])])
                output = result.stdout + result.stderr
                accepted = result.returncode == 0 and 'Abstract syntax of the JAY Program:' in output and 'Syntax error' not in output
                rejected = result.returncode != 0 and 'Syntax error - Expecting:' in output
                passed = accepted if case['expected'] == 'ACCEPT' else rejected
                detail = output
            except subprocess.TimeoutExpired:
                passed, detail = False, 'TIMEOUT'
            print(f"{'PASS' if passed else 'FAIL'} T{case['id']:02} {case['purpose']} ({case['expected']})")
            if not passed:
                failures += 1
                print(detail)
        for name in ['Test39IsInteger', 'RegressionTests']:
            result = run(['java', '-cp', build, name])
            print(result.stdout + result.stderr, end='')
            if result.returncode:
                failures += 1
        print(f"Parser fixtures: {38 - min(failures,38)}/38 (see individual results above)." if failures else 'Passed 38/38 parser fixtures, Test 39, and existing regression suite.')
    return 1 if failures else 0

if __name__ == '__main__':
    raise SystemExit(main())
