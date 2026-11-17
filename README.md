# CS 361 Team 8 Scanner and Parser

We implemented the JAY scanner to identify tokens and report each token’s number, type, and value in the required format. This implementation covers lexical analysis. We retained the supplied abstract syntax classes in `Program.java`.

## Compile and run
From the repository folder, run:

```sh
javac Token.java TokenStream.java ScannerDemo.java
java ScannerDemo
```

We use `src/prog1.jay` as the test input. To scan another file, update `file1` in `ScannerDemo.java` and recompile.

## Testing and verification

We tested keywords, identifiers, literals, separators, operators, and lexical errors. We verified that all **95 token entries** match the expected types and values recorded in `tests/expected-output.txt`.

To reproduce the comparison, run:

```sh
java ScannerDemo > tests/actual-output.txt
diff -u tests/expected-output.txt tests/actual-output.txt
```

No output from `diff` confirms an exact match.

Our test file includes standalone tokens and intentional lexical errors to check the scanner’s behavior.

## Token classification

We follow JAY’s case-sensitive lexical rules:

- `while` is a keyword; `While` is an identifier.
- `boolean` is a keyword; `bool` is an identifier.
- `true` and `false` are Boolean literals; `True` and `False` are identifiers.
- Invalid tokens are classified as `Other`.

We document our changes to the starter files in [CHANGES.md](CHANGES.md).
