import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

public class Test39IsInteger {
    public static void main(String[] args) throws Exception {
        Path empty = Files.createTempFile("jay-test39-", ".jay");
        try {
            ConcreteSyntax parser = new ConcreteSyntax(new TokenStream(empty.toString()));
            Method method = ConcreteSyntax.class.getDeclaredMethod("isInteger", String.class);
            method.setAccessible(true);
            Object actual = method.invoke(parser, "");
            if (!Boolean.FALSE.equals(actual)) {
                throw new AssertionError("Test 39: isInteger(\"\") expected false, got " + actual);
            }
            System.out.println("PASS Test 39: isInteger(\"\") == false");
        } finally {
            Files.deleteIfExists(empty);
        }
    }
}
