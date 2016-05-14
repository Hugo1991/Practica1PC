package practica1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
	private Thread muestraProgreso = new Thread(() -> mostrarProgreso(), "hilo mostrar progreso");
	private Thread detectaTecla = new Thread(() -> detectaTecla(), "TerminaHilo");

	public WebProcessor(String path, int nDown, int maxDown) {
		ruta = path;
		this.nDown = nDown;
		this.maxDown = maxDown;
		progreso = 0;
		//muestraProgreso.setDaemon(true);
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
			linea = b.readLine();
			while (!(linea == null)) {
				if (progreso < nDown) {
					String lineaCopia = linea;
					semMaxDescargas.acquire();
					Thread th = new Thread(() -> descargaFichero(lineaCopia), "Hilo " + progreso);
					th.start();
					hilos.add(th);
					progreso++;
					linea = b.readLine();
				}
				else
					terminarHilos();
			}
			// terminarHilos();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			terminarHilos();

		}
	}

	private void descargaFichero(String url) {

		Connection conn = Jsoup.connect(url);
		try {
			Response resp = conn.execute();
			if (resp.statusCode() == 200) {
				String html = conn.get().html();
				url=url.replace(".", " ");
				escribirFichero(url.split(" ")[1] , html, false);
			} else
				escribirFichero("error_log.txt", url, true);
		} catch (IllegalArgumentException iae) {
			escribirFichero("error_log.txt", url, true);
		} catch (IOException e) {
			escribirFichero("error_log.txt", url, true);
		}
		semMaxDescargas.release();
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
				bw = new BufferedWriter(new FileWriter(fichero+".html", error));
				bw.write(texto);
			}
			bw.close();
		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (IOException e) {

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
	}

	private void terminarHilos() {
		for (Thread hilo : hilos) {
			hilo.interrupt();
		}
		try {
			semMaxDescargas.acquire(nDown);
			semMaxEscritura.acquire();
			
		} catch (InterruptedException e) {
			System.out.println("weeeo");
			detectaTecla.interrupt();
			muestraProgreso.interrupt();
		}

	}

	private void detectaTecla() {
		try {
			while (System.in.available() == 0) {
				Thread.sleep(500);
				if (new Scanner(System.in).hasNextLine()) {
					terminarHilos();
				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
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
				terminarHilos();
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Introduce el fichero con las páginas web que quieres descargar");
		String ficheroEntrada = new Scanner(System.in).nextLine();
		System.out.println("Introduce el directorio donde quieres guardar las descargas");
		String ruta = new Scanner(System.in).nextLine();
		WebProcessor wp = new WebProcessor(ruta, 501, 20);
		wp.process(ficheroEntrada);

	}

}
