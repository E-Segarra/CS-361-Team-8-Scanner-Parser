# CS 361 Team 8 Scanner and Parser

We implemented the JAY scanner and recursive descent parser using the provided starter code. The scanner prints each token’s number, type, and value. The parser checks program syntax and prints an abstract syntax tree; it does not execute the program or check variable types.

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

## Compile and run the parser

From the repository folder, with a JDK installed:

```sh
javac -d build *.java
java -cp build ParserDemo tests/parser/valid/T03_assignments.jay
```

Pass the path to any complete JAY program as the argument. The scanner input `src/prog1.jay` contains individual tokens and intentional lexical errors, so it is not a valid parser input.

`ConcreteSyntax.java` implements the grammar rules. `ParserDemo.java` reads the filename and displays the tree. `Program.java` defines the tree classes and their display methods; keep it beside the other Java source files.

## Run the parser tests

With Python 3 and a JDK installed:

```sh
python3 tests/parser/run_tests.py
```

The suite includes 38 separate `.jay` programs: 26 that should be accepted and 12 that should be rejected. We chose these expected results from the grammar before running the parser. An invalid program passes its test when the parser correctly reports a syntax error.

Test 39, in `tests/Test39IsInteger.java`, uses Java reflection to verify that the private `isInteger("")` method returns `false`. An ordinary `.jay` program cannot directly call that helper.

The latest local compatibility check used the existing GitHub scanner with the parser files and tests. All 38 parser cases, Test 39, and the existing 82 scanner/parser regression cases passed. The scanner also still matched all 95 expected token lines. 

The test list and explanations are in [tests/parser/README.md](tests/parser/README.md). Cases worth discussing include operator precedence and left associativity (24 to 26), the nearest-if rule for `else` (29), nested control flow (30), and declaration placement (34–35). `tests/RegressionTests.java` also checks tree structure rather than acceptance alone.

## Grammar and output notes

We follow the assignment's formal JAY grammar: `void main()`, `int` and `boolean`, `=` for assignment, and `!=` for not equal. Declarations come before statements in `main`; nested blocks contain statements only.

Some starter comments refer to KAY, while the assignment's prose example uses `main{}`. That header conflicts with the formal grammar. The supplied tree display also prints `int`, while the prose output example shows `integer`. Our current output retains `int` and the supplied display's surrounding blank lines, so it is not a character-for-character match to that example.

During the scanner phase, `Program.java` was left unchanged. The parser version retains its tree structure with the boolean and not-equal constants aligned to `boolean` and `!=`. No scanner source changes are needed for this parser addition.

Keep generated `.class` files, `build/`, and `.DS_Store` out of version control.

