import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Run with: javac -d build *.java tests/RegressionTests.java
//           java -cp build RegressionTests
public class RegressionTests {
    private static int passed;

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }

    private static Path source(String text) throws Exception {
        Path path = Files.createTempFile("jay-test-", ".jay");
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    private static void scan(String text, String... expected) throws Exception {
        Path path = source(text);
        try {
            TokenStream scanner = new TokenStream(path.toString());
            List<String> actual = new ArrayList<String>();
            // The bound catches accidental failure to advance the scanner.
            for (int i = 0; i <= text.length() + 1; i++) {
                Token token = scanner.nextToken();
                if (scanner.isEndofFile() && token.getValue().isEmpty()) {
                    check(actual.equals(Arrays.asList(expected)),
                            "Tokens for " + text + ": " + actual);
                    Token eof = scanner.nextToken();
                    check(eof.getValue().isEmpty(), "EOF should be stable");
                    passed++;
                    return;
                }
                actual.add(token.getType() + ":" + token.getValue());
            }
            throw new AssertionError("Scanner did not finish: " + text);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    private static Program parse(String text) throws Exception {
        Path path = source(text);
        try {
            return new ConcreteSyntax(new TokenStream(path.toString())).program();
        } finally {
            Files.deleteIfExists(path);
        }
    }

    private static void accept(String text) throws Exception {
        check(parse(text).display().contains("Abstract syntax of the JAY Program:"),
                "AST display failed for " + text);
        passed++;
    }

    private static void reject(String text) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream quiet = new PrintStream(output)) {
            System.setOut(quiet);
            try {
                parse(text);
                throw new AssertionError("Parser accepted invalid input: " + text);
            } catch (RuntimeException expected) {
                check(expected.getMessage().startsWith("Syntax error - Expecting:"),
                        "Wrong error for " + text + ": " + expected);
            }
        } finally {
            System.setOut(original);
        }
        passed++;
    }

    // Prefix form makes the actual AST's grouping unambiguous.
    private static String tree(Expression expression) {
        if (expression instanceof Binary) {
            Binary binary = (Binary) expression;
            return "(" + binary.op.val + " " + tree(binary.term1)
                    + " " + tree(binary.term2) + ")";
        }
        if (expression instanceof Unary) {
            Unary unary = (Unary) expression;
            return "(" + unary.op.val + " " + tree(unary.term) + ")";
        }
        if (expression instanceof Variable)
            return ((Variable) expression).id;
        Value value = (Value) expression;
        return value.type.isBoolean() ? String.valueOf(value.boolValue)
                : String.valueOf(value.intValue);
    }

    private static void expression(String text, String expected) throws Exception {
        Program program = parse("void main() { int x; x = " + text + "; }");
        Assignment assignment = (Assignment) program.body.blockmembers.elementAt(0);
        String actual = tree(assignment.source);
        check(actual.equals(expected), text + " should produce " + expected + " saw " + actual);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        scan("");
        scan(" \t\r\n\f");
        scan("// comment without a newline");
        scan("// first\r\n // second\n// third\f31", "Literal:31");
        scan("boolean else if int main void while",
                "Keyword:boolean", "Keyword:else", "Keyword:if", "Keyword:int",
                "Keyword:main", "Keyword:void", "Keyword:while");
        scan("true false True false1 bool integer", "Literal:true", "Literal:false",
                "Identifier:True", "Identifier:false1", "Identifier:bool", "Identifier:integer");
        scan("a Z abc123 A0", "Identifier:a", "Identifier:Z", "Identifier:abc123", "Identifier:A0");
        scan("0 31 0007 2147483647", "Literal:0", "Literal:31", "Literal:0007", "Literal:2147483647");
        scan("(){};,", "Separator:(", "Separator:)", "Separator:{", "Separator:}",
                "Separator:;", "Separator:,");
        scan("= + - * / < <= > >= == != && || !", "Operator:=", "Operator:+",
                "Operator:-", "Operator:*", "Operator:/", "Operator:<", "Operator:<=",
                "Operator:>", "Operator:>=", "Operator:==", "Operator:!=", "Operator:&&",
                "Operator:||", "Operator:!");
        scan("a<=2&&b!=3||!false", "Identifier:a", "Operator:<=", "Literal:2",
                "Operator:&&", "Identifier:b", "Operator:!=", "Literal:3",
                "Operator:||", "Operator:!", "Literal:false");
        scan("31 3a", "Literal:31", "Other:3a");
        scan("abc_def _x 13.5 @ $", "Other:abc_def", "Other:_x", "Other:13.5", "Other:@", "Other:$");
        scan("| & || &&", "Other:|", "Other:&", "Operator:||", "Operator:&&");
        scan("/", "Operator:/");
        scan("x/2//comment", "Identifier:x", "Operator:/", "Literal:2");
        scan("//comment\nx/", "Identifier:x", "Operator:/");
        scan("a;", "Identifier:a", "Separator:;");
        scan("a", "Identifier:a");
        scan("//one\n//two\n");

        accept("void main(){}");
        accept("void main(){ int a,b; boolean done; }");
        accept("void main(){ ; {} {{;}} }");
        accept("void main(){ int x; x=7; } // trailing comment");
        accept("void main(){ boolean b; b = true; if (b) ; }");
        accept("void main(){ if(true) {} else {} }");
        accept("void main(){ while(false) ; }");
        accept("void main(){ int i; i=0; while(i<=10) {i=i+1;} }");
        // Semantic errors are deliberately accepted by a syntax-only parser.
        accept("void main(){ int i; i = true; i = !3; }");
        accept("void main(){ undeclared = 1; }");
        accept("void main(){ int x; x=1<2<3; }");

        expression("x+2*3", "(+ x (* 2 3))");
        expression("(x+2)*3", "(* (+ x 2) 3)");
        expression("8-3-1", "(- (- 8 3) 1)");
        expression("8/4/2", "(/ (/ 8 4) 2)");
        expression("1+2-3*4/2", "(- (+ 1 2) (/ (* 3 4) 2))");
        expression("a||b&&c", "(|| a (&& b c))");
        expression("a&&b&&c", "(&& (&& a b) c)");
        expression("a||b||c", "(|| (|| a b) c)");
        expression("!a==b", "(== (! a) b)");
        expression("!(a||b)", "(! (|| a b))");
        expression("true", "true");
        expression("false", "false");
        for (String op : new String[]{"<", "<=", ">", ">=", "==", "!="})
            expression("a" + op + "b", "(" + op + " a b)");

        Program nested = parse("void main(){ if(true) if(false) ; else ; }");
        Conditional outer = (Conditional) nested.body.blockmembers.elementAt(0);
        check(outer.elsebranch == null, "Else must belong to the nearest if");
        check(((Conditional) outer.thenbranch).elsebranch instanceof Skip, "Missing inner else");
        passed++;

        Program declarations = parse("void main(){ int a,b; boolean c; c=true; }");
        check(declarations.decpart.size() == 3, "Comma declarations lost");
        check(((Declaration) declarations.decpart.elementAt(2)).t.isBoolean(), "Boolean type mismatch");
        check(new Operator("!=").RelationalOp(), "AST must recognize JAY !=");
        passed++;

        for (String invalid : new String[]{
                "", "main{}", "void main{}", "void main(){", "void main(){ int x }",
                "void main(){ int ; }", "void main(){ int a,; }", "void main(){ boolean ; }",
                "void main(){ bool b; }", "void main(){ int x; x:=1; }",
                "void main(){ int x; x=1 }", "void main(){ int x; x=; }",
                "void main(){ int x; x=1+; }", "void main(){ int x; x=(1+2; }",
                "void main(){ int x; x=!!true; }", "void main(){ int x; x=-1; }",
                "void main(){ int x; x=1<>2; }", "void main(){ int x; x=3a; }",
                "void main(){ int x; x=13.5; }", "void main(){ int name_list; }",
                "void main(){ while(true) { int x; } }", "void main(){ ; int x; }",
                "void main(){ if true ; }", "void main(){ if(true) }",
                "void main(){ else ; }", "void main(){ while(true) }",
                "void main(){ int x; x=true & false; }", "void main(){} extra",
                "void main(){} @", "void main(){} }", "void main(){ // unfinished block"
        })
            reject(invalid);

        System.out.println("Passed " + passed + " scanner/parser regression cases.");
    }
}
