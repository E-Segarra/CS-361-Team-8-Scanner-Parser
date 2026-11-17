
/**
 * @author scharffc
 * 
 */
public class ScannerDemo {

	// This needs to be changed !
	private static String file1 = "src/prog1.jay";
	private static int counter = 1;

	public static void main(String args[]) {
		TokenStream ts = new TokenStream(file1);
		while (!ts.isEndofFile()) {
			Token tk = ts.nextToken(); 
			if (ts.isEndofFile() && tk.getValue().isEmpty()) {
				break;
			}
			System.out.println("Token " + counter++ + " - Type: " + tk.getType() + " - Value: " + tk.getValue());
		}
	}
}
