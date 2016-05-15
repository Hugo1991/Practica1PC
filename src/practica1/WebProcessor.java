package practica1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import javax.crypto.ExemptionMechanismSpi;

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
	private Thread muestraProgreso = new Thread(() -> mostrarProgreso(), "hilo mostrar progreso");
	private Thread detectaTecla = new Thread(() -> detectaTecla(), "TerminaHilo");
	private boolean terminar=false;
	public WebProcessor(String path, int nDown, int maxDown) {
		ruta = path;
		this.nDown = nDown;
		this.maxDown = maxDown;
		progreso = 1;
		muestraProgreso.setDaemon(true);
		muestraProgreso.start();
		semMaxDescargas = new Semaphore(maxDown);
		hilos = new ArrayList<>();
		detectaTecla.start();
	}

	public void process(String fileName) {
		try {
			FileReader f = new FileReader(fileName);
			BufferedReader b = new BufferedReader(f);
			String linea;

			while (((linea = b.readLine()) != null) && ((progreso <= nDown))&& !terminar) {

				if ((progreso <= nDown) && (linea != null)) {
					String lineaCopia = linea;
					semMaxDescargas.acquire();
					Thread th = new Thread(() -> descargaFichero(lineaCopia), "Hilo " + progreso);
					th.start();
					hilos.add(th);

				} else
					terminarHilos();
				progreso++;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {

		}
	}

	private void descargaFichero(String url) {

		Connection conn = Jsoup.connect(url);
		try {
			Response resp = conn.execute();
			if (resp.statusCode() == 200) {
				String html = conn.get().html();
				url = url.replace(".", " ");
				if (url.split(" ").length > 3)
					escribirFichero(url.split(" ")[1] + "." + url.split(" ")[2] + "." + url.split(" ")[3], html, false);
				else
					escribirFichero(url.split(" ")[1] + "." + url.split(" ")[2], html, false);
			} else
				escribirFichero("error_log.txt", url, true);
		} catch (IllegalArgumentException iae) {
			escribirFichero("error_log.txt", url, true);
		} catch (IOException e) {
			escribirFichero("error_log.txt", url, true);
		}

	}

	private void escribirFichero(String nombreFichero, String texto, boolean error) {
		BufferedWriter bw = null;
		try {
			if (!(new File(ruta).isDirectory()))
				new File(ruta).mkdir();
			File fichero = new File(ruta + "/" + nombreFichero);
			if (error) {
				semMaxEscritura.acquire();
				bw = new BufferedWriter(new FileWriter(fichero, error));
				bw.write(texto + "\n");
				semMaxEscritura.release();
			} else {
				bw = new BufferedWriter(new FileWriter(fichero + ".html", error));
				bw.write(texto);
			}
			bw.close();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		semMaxDescargas.release();
	}

	private void terminarHilos() {
		try {
			muestraProgreso.interrupt();
			while(System.in.available()==0){
				Thread.sleep(500);
			}
			terminar=true;
			semMaxDescargas.acquire(nDown);
			semMaxEscritura.acquire();
			System.out.println("Terminando proceso");
			

		} catch (InterruptedException e) {
			System.out.println("error interrumpiendo descargas");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void detectaTecla() {
		if (new Scanner(System.in).hasNextLine()) {
			
			terminarHilos();
		}

	}

	public static boolean existeFichero(String nombreFichero) {
		if (new File(nombreFichero).exists() && !(new File(nombreFichero).isDirectory())) {
			return true;
		} else {
			System.out.println("error leyendo el fichero");
			return false;
		}
	}

	private void mostrarProgreso() {

		while (true) {
			System.out.println(progreso);

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block

			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ficheroEntrada;
		do {
			System.out.println("Introduce el fichero con las páginas web que quieres descargar");

		} while (!existeFichero(ficheroEntrada = new Scanner(System.in).nextLine()));
		System.out.println("Introduce el directorio donde quieres guardar las descargas");
		String ruta = new Scanner(System.in).nextLine();
		WebProcessor wp = new WebProcessor(ruta, 200, 20);
		wp.process(ficheroEntrada);

	}

}
