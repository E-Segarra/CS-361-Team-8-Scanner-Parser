public class ParserDemo {
    public static void main(String[] args) {
        String file = args.length > 0 ? args[0] : "prog1.jay";
        ConcreteSyntax parser = new ConcreteSyntax(new TokenStream(file));
        Program program = parser.program();
        System.out.println(program.display());
    }
}
