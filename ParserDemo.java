public class ParserDemo {
    public static void main(String[] args) {
        String filename = "src/prog1.jay";

        if (args.length > 0) {
            filename = args[0];
        }

        TokenStream tStream = new TokenStream(filename);
        ConcreteSyntax cSyntax = new ConcreteSyntax(tStream);
        Program p = cSyntax.program();

        System.out.println(p.display());
    }
}
