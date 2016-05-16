package practica1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class WebProcessor {
	private String ruta;
	private int nDown; // número de procesos de descarga que lanzará la
						// aplicación
	private int progreso;
	private Semaphore semMaxDescargas; // Sincronizacion condicional
	private Semaphore semMaxEscritura; // Exclusion mutua
	private Thread muestraProgreso;
	private boolean terminar;
	private Thread finalizaProceso;

	/**
	 * Inicializamos todas las variables, iniciamos los hilos de muestraProgreso
	 * y finalizaProceso y ponemos que el semaforo de maxDescargas sea igual al
	 * maximo numero de descargas que le pasamos al constructor
	 * 
	 * @param path
	 * @param nDown
	 * @param maxDown
	 */
	public WebProcessor(String path, int nDown, int maxDown) {
		ruta = path;
		this.nDown = nDown;
		progreso = 0;
		semMaxDescargas = new Semaphore(maxDown);
		semMaxEscritura = new Semaphore(1);

		finalizaProceso = new Thread(() -> terminarHilos(), "Termina hilo");
		finalizaProceso.start();
		muestraProgreso = new Thread(() -> mostrarProgreso(), "Hilo mostrar progreso");
		muestraProgreso.setDaemon(true);
		muestraProgreso.start();

		terminar = false;
	}

	/**
	 * metodo encargado de leer el fichero de texto que le pasamos al metodo.
	 * este metodo esta bajo sincronizacion condicional para que no haya mas de
	 * x descargas simultaneas y crea los hilos para descargar cada una de las
	 * paginas
	 * 
	 * @param fileName
	 *            nombre del fichero a leer
	 */
	public void process(String fileName) {
		try {
			FileReader f = new FileReader(fileName);
			BufferedReader b = new BufferedReader(f);
			String linea = b.readLine();
			while (!(linea == null) && !terminar && progreso < nDown) {
				String lineaCopia = linea;
				semMaxDescargas.acquire();
				Thread th = new Thread(() -> descargaFichero(lineaCopia), "Hilo " + progreso);
				th.start();
				progreso++;
				linea = b.readLine();
			}
			b.close();
			finalizaProceso.interrupt();
			System.out.println("Proceso finalizado con " + progreso + " descargas.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {

		}
	}

	/**
	 * Metodo encargado de conectarse a una URL, detectar si existe conexion, y
	 * escribir en un fichero, si existe conexion, llama al metodo escribir en
	 * fichero pasandole el nombre, el codigo html y false. Si no existe
	 * conexion, le paso el nombre del fichero de error, la URL y true
	 * 
	 * @param url
	 *            Ruta de la URL
	 */
	private void descargaFichero(String url) {
		Connection conn = Jsoup.connect(url);
		try {
			Response resp = conn.execute();
			if (resp.statusCode() == 200) {
				String html = conn.get().html();
				// Reemplazo el . por el espacio para poder hacer un split
				url = url.replace(".", " ");
				// Esta condicion esta creada para que no reemplace paginas que
				// ya existen, por lo que inserto la(s) extension(es)
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

	/**
	 * Si error es true escribe en el fichero de error, con el valor de true
	 * para insertar en la siguiente linea, si no, reemplaza los archivos. El
	 * semaforo controla la exclusion mutuas, que solo se pueda escribir una vez
	 * en el fichero de error de manera simultanea y que no se produzca un
	 * fichero corrupto. libera el recurso del maximo de descargas simultaneas.
	 * 
	 * @param nombreFichero
	 *            nombre del fichero para insertar
	 * @param texto
	 *            texto a insertar
	 * @param error
	 *            variable booleana que indica si se ha producido un error o no
	 */
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
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		semMaxDescargas.release();
	}

	/**
	 * Este proceso es llamado desde el hilo finalizaProceso y ponemos la
	 * variable terminar a true para finalizar el bucle
	 */
	private void terminarHilos() {
		try {
			while (System.in.available() == 0)
				Thread.sleep(500);
			terminar = true;
			System.out.println("Finalizando proceso");
		} catch (IOException | InterruptedException e) {
		}

	}

	/**
	 * Comprueba que existe un fichero y que este no es un directorio
	 * 
	 * @param nombreFichero
	 *            nombre del fichero
	 * @return
	 */
	public static boolean existeFichero(String nombreFichero) {
		if (new File(nombreFichero).exists() && !(new File(nombreFichero).isDirectory())) {
			return true;
		} else {
			System.out.println("Error leyendo el fichero");
			return false;
		}
	}

	/**
	 * Mientras terminar sea false, se mostrara el progreso cada 3 segundos.
	 * Este metodo sera insertado en el hilo muestraProgreso
	 */
	private void mostrarProgreso() {
		while (!terminar) {
			try {
				System.out.println(progreso);
				Thread.sleep(3000);
			} catch (InterruptedException e) {

			}
		}
	}
}
