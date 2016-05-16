package practica1;

import java.util.Scanner;

public class Principal {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String ficheroEntrada;
		do {
			System.out.println("Introduce el fichero con las páginas web que quieres descargar");

		} while (!WebProcessor.existeFichero(ficheroEntrada = new Scanner(System.in).nextLine()));
		System.out.println("Introduce el directorio donde quieres guardar las descargas");
		String ruta = new Scanner(System.in).nextLine();
		WebProcessor wp = new WebProcessor(ruta, 200, 20);
		wp.process(ficheroEntrada);

	}
}
