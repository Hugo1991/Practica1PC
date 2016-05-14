package practica1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class WebProcessor {
	private String ruta;
	private int nDown; // número de procesos de descarga que lanzará la
						// aplicación
	private int maxDown; // máximo número de procesos que pueden descargar webs
							// a la vez
	private int progreso;
	private Semaphore semMaxDescargas;
	private Semaphore semMaxEscritura = new Semaphore(1);
	private ArrayList<Thread> hilos;
	private Thread muestraProgreso = new Thread(() -> mostrarProgreso(), "hilo mostrar progreso");;
	//private Thread detectaTecla=new Thread(()->detectaTecla(),"TerminaHilo");
	public WebProcessor(String path, int nDown, int maxDown) {
		ruta = path;
		this.nDown = nDown;
		this.maxDown = maxDown;
		progreso = 0;
		muestraProgreso.setDaemon(true);
		muestraProgreso.start();
		semMaxDescargas = new Semaphore(maxDown);
		hilos = new ArrayList<>();
		new Thread(()->detectaTecla(),"TerminaHilo").start();
	}

	public void process(String fileName) {

		try {
			FileReader f = new FileReader(fileName);
			BufferedReader b = new BufferedReader(f);
			String linea;
			linea = b.readLine();

			while (!(linea == null) && (progreso < nDown)) {

				String lineaCopia = linea;
				semMaxDescargas.acquire();
				Thread th = new Thread(() -> descargaFichero(lineaCopia), "Hilo " + progreso);
				th.start();
				hilos.add(th);

				linea = b.readLine();
				progreso++;
			}
			terminarHilos();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("error");

		}
	}

	private void descargaFichero(String url) {

		// Creates a connection to a given url
		Connection conn = Jsoup.connect(url);
		try {

			// Performs the connection and retrieves the response
			Response resp = conn.execute();
			// If the response is different from 200 OK,
			// the website is not reachable
			if (resp.statusCode() != 200) {
				System.out.println(progreso + " " + url + " error");
			} else {
				String html = conn.get().html();
				System.out.println(" " + url);
				escribirFichero(url, html, false);
			}
		} catch (IllegalArgumentException iae) {
			// System.out.println("error error error");
		} catch (IOException e) {
			escribirFichero("error.txt", url, false);
			// System.out.println(progreso+" No se puede conectar");
		}
		semMaxDescargas.release();
	}

	private void escribirFichero(String fichero, String texto, boolean error) {
		// semMaxEscritura.release();

	}

	private void terminarHilos() {
		try {
			semMaxDescargas.acquire(nDown);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Thread hilo : hilos) {
			hilo.interrupt();
		}

	}
	private void detectaTecla(){
		if (new Scanner(System.in).hasNextLine()) {
			terminarHilos();
		}
	}
	private void mostrarProgreso() {
		while (true) {
			System.out.println(progreso);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Introduce el fichero con las páginas web que quieres descargar");
		String ficheroEntrada = new Scanner(System.in).nextLine();
		System.out.println("Introduce el directorio donde quieres guardar las descargas");
		String ruta = new Scanner(System.in).nextLine();
		WebProcessor wp = new WebProcessor(ruta, 50, 10);
		wp.process(ficheroEntrada);

	}

}
