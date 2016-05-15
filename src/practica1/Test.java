package practica1;

public class Test {
	public static void main(String[] args){
		WebProcessor wp0 = new WebProcessor("ficheroFallo0", 2, 20);
		wp0.process("3.txt");
		WebProcessor wp = new WebProcessor("ficheroFallo", 10, 20);
		wp.process("3.txt");
		
		WebProcessor wp1 = new WebProcessor("ficheroFallo1", 5, 2);
		wp1.process("3.txt");
		
		WebProcessor wp2 = new WebProcessor("ficheroBueno", 10, 20);
		wp2.process("2.txt");
		
		WebProcessor wp3 = new WebProcessor("ficheroBueno1", 50, 20);
		wp3.process("2.txt");
		
		WebProcessor wp4 = new WebProcessor("ficheroBueno2", 505, 2);
		wp4.process("2.txt");
	}
}
