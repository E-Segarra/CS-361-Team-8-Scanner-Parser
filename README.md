# CS 361 Team 8 Scanner and Parser

We completed the JAY scanner using the provided starter code. It reads a source file and prints each token’s number, type, and value in the required format. This part of our project handles lexical analysis. We kept the supplied `Program.java` unchanged.

## Compile and run

From the repository folder, run:

```sh
javac Token.java TokenStream.java ScannerDemo.java
java ScannerDemo
```

The scanner reads `src/prog1.jay`. To use a different input file, change `file1` in `ScannerDemo.java` and compile again.

## Testing

We tested keywords, identifiers, literals, separators, and operators. We also included invalid input to check how the scanner handles lexical errors.

Our test file produces **95 tokens**. We compared their types and values with `tests/expected-output.txt`, and all entries matched.

To check the results, run:

```sh
java ScannerDemo > tests/actual-output.txt
diff -u tests/expected-output.txt tests/actual-output.txt
```

If `diff` prints nothing, the two files match.

The test file contains individual token examples, intentional errors, and a short JAY program.

## Examples

JAY is case-sensitive, so capitalization affects how we classify a word:

- `while` is a keyword, but `While` is an identifier.
- `boolean` is a keyword, but `bool` is an identifier.
- `true` and `false` are Boolean literals, but `True` and `False` are identifiers.
- Invalid token text, such as `3a` or a single `&`, is reported as `Other`.
