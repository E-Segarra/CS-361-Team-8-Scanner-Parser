# Parser tests

These tests check whether our parser follows the JAY grammar. Each `.jay` file contains one small program, so a failure is easy to locate and explain.

We decided the expected result from the grammar before running each test. The `valid/` folder contains 26 programs the parser should accept. The `invalid/` folder contains 12 programs it should reject. An invalid program passes its test when the parser reports the expected syntax error.

## Run the tests

From the repository root, with a JDK and Python 3 installed:

```sh
python3 tests/parser/run_tests.py
```

The runner compiles fresh Java classes in a temporary folder, checks all 38 programs, runs Test 39, and runs the existing regression tests. It reports failures and returns a nonzero exit code if a check fails. A crash or timeout does not count as a correct syntax rejection.

Latest local result: **38/38 parser tests passed, Test 39 passed, and all 82 existing scanner/parser regression cases passed.** The professor's hidden tests have not been run.

## Tests we can explain during the meeting

| Tests | Why we included them |
|---|---|
| 1–7, 9 | Start with the basic grammar: empty programs, declarations, assignments, expressions, and control flow. |
| 5, 12–15, 24–26 | Check parentheses and repeated operators. For example, `20 - 5 - 3 + 2` should group from left to right, and `&&` should bind more tightly than `\|\|`. |
| 16, 22, 27 | Distinguish valid `!(!true)` from invalid `!!true`. The grammar allows only one `!` before a factor; parentheses allow another expression inside. |
| 28 | Accept chained comparisons because the grammar allows repeated relational operators. This is a syntax test, not a type check. |
| 29 | Check that `else` belongs to the nearest unmatched `if`. |
| 17, 30 | Combine loops, conditions, and blocks to exercise recursive parsing. |
| 34, 35 | Reject declarations after statements and inside nested blocks. Declarations belong at the beginning of `main`. |
| 8, 10, 19–23, 36–38 | Reject malformed headers, unsupported types, incorrect assignments, missing parentheses or operands, stray `else`, and extra input after the program. |
| 39 | Call private `isInteger("")` through Java reflection and assert that it returns `false`. A `.jay` file cannot directly test this helper. |

Test 39 is in `tests/Test39IsInteger.java`. Its assertion works without Java's `-ea` option. It checks the existing fix without changing the parser or making the method public.

Accepting a program alone does not prove that its tree is correct. `tests/RegressionTests.java` also checks expression tree structure, precedence, left associativity, and which `if` receives an `else`.

To display a tree for discussion:

```sh
javac -d build *.java tests/Test39IsInteger.java
java -cp build ParserDemo tests/parser/valid/T24_left_associativity.jay
java -cp build ParserDemo tests/parser/valid/T29_dangling_else.jay
java -cp build Test39IsInteger
```

## Grammar and coverage notes

We follow the assignment's formal lexical and EBNF grammar: `void main()`, `int` or `boolean`, `=`, and `!=`. Some starter comments call the language KAY and the prose example uses `main{}`; those conflict with the formal JAY grammar.

All 38 programs contain no comments. Test 33 checks whitespace; comment handling is covered by the existing regression suite. Tests 16 and 27 deliberately repeat the negation rule with different literals. Types and whether variables have been declared are outside this syntax-only parser's checks.

Numbering keeps the original plan: Test 8 covers the extra “1a” header case, Test 10 covers missing while parentheses, Test 37 covers malformed arithmetic, and Test 34 covers declaration ordering.

## Manifest

| Test | Expected | File / purpose |
|---|---|---|
| 01 | ACCEPT | [empty program](valid/T01_empty_program.jay) |
| 02 | ACCEPT | [variable declarations](valid/T02_variable_declarations.jay) |
| 03 | ACCEPT | [assignments](valid/T03_assignments.jay) |
| 04 | ACCEPT | [arithmetic](valid/T04_arithmetic.jay) |
| 05 | ACCEPT | [precedence parentheses](valid/T05_precedence_parentheses.jay) |
| 06 | ACCEPT | [relational logical](valid/T06_relational_logical.jay) |
| 07 | ACCEPT | [if while blocks](valid/T07_if_while_blocks.jay) |
| 08 | REJECT | [incorrect header](invalid/T08_incorrect_header.jay) |
| 09 | ACCEPT | [comma declarations](valid/T09_comma_declarations.jay) |
| 10 | REJECT | [while missing parentheses](invalid/T10_while_missing_parentheses.jay) |
| 11 | ACCEPT | [empty nested blocks](valid/T11_empty_nested_blocks.jay) |
| 12 | ACCEPT | [repeated add subtract](valid/T12_repeated_add_subtract.jay) |
| 13 | ACCEPT | [repeated multiply divide](valid/T13_repeated_multiply_divide.jay) |
| 14 | ACCEPT | [repeated logical](valid/T14_repeated_logical.jay) |
| 15 | ACCEPT | [nested parentheses](valid/T15_nested_parentheses.jay) |
| 16 | ACCEPT | [nested negation](valid/T16_nested_negation.jay) |
| 17 | ACCEPT | [nested if else while](valid/T17_nested_if_else_while.jay) |
| 18 | ACCEPT | [combined expression](valid/T18_combined_expression.jay) |
| 19 | REJECT | [unsupported type](invalid/T19_unsupported_type.jay) |
| 20 | REJECT | [incorrect assignment](invalid/T20_incorrect_assignment.jay) |
| 21 | REJECT | [missing close parenthesis](invalid/T21_missing_close_parenthesis.jay) |
| 22 | REJECT | [consecutive negation](invalid/T22_consecutive_negation.jay) |
| 23 | REJECT | [else without if](invalid/T23_else_without_if.jay) |
| 24 | ACCEPT | [left associativity](valid/T24_left_associativity.jay) |
| 25 | ACCEPT | [deep parentheses](valid/T25_deep_parentheses.jay) |
| 26 | ACCEPT | [mixed logical precedence](valid/T26_mixed_logical_precedence.jay) |
| 27 | ACCEPT | [parenthesized negation](valid/T27_parenthesized_negation.jay) |
| 28 | ACCEPT | [chained relations](valid/T28_chained_relations.jay) |
| 29 | ACCEPT | [dangling else](valid/T29_dangling_else.jay) |
| 30 | ACCEPT | [nested control](valid/T30_nested_control.jay) |
| 31 | ACCEPT | [multiple declaration groups](valid/T31_multiple_declaration_groups.jay) |
| 32 | ACCEPT | [nested blocks skip](valid/T32_nested_blocks_skip.jay) |
| 33 | ACCEPT | [whitespace](valid/T33_whitespace.jay) |
| 34 | REJECT | [declaration after statement](invalid/T34_declaration_after_statement.jay) |
| 35 | REJECT | [declaration inside block](invalid/T35_declaration_inside_block.jay) |
| 36 | REJECT | [incomplete logical](invalid/T36_incomplete_logical.jay) |
| 37 | REJECT | [missing arithmetic operand](invalid/T37_missing_arithmetic_operand.jay) |
| 38 | REJECT | [trailing tokens](invalid/T38_trailing_tokens.jay) |
| 39 | false | [Private empty-string integer check](../Test39IsInteger.java) |
