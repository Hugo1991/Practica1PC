package practica1;

import java.io.IOException;

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
	public WebProcessor(String path, int nDown, int maxDown) {
		ruta = path;
		this.nDown = nDown;
		this.maxDown = maxDown;
		progreso=0;
	}

	public void process(String fileName) {
		//while lectura de fichero
			for(int i=0;i<)
			
		//end while
	}

	private void descargaFichero() {
		String url = "http://www.google.es";
		// Creates a connection to a given url
		Connection conn = Jsoup.connect(url);
		try {
			// Performs the connection and retrieves the response
			Response resp = conn.execute();
			// If the response is different from 200 OK,
			// the website is not reachable
			if (resp.statusCode() != 200) {
				System.out.println("Error: " + resp.statusCode());
			} else {
				String html = conn.get().html();
				escribirFichero(url, html, false);
			}
		} catch (IOException e) {
			System.out.println("No se puede conectar");
		}
	}
	private void escribirFichero(String fichero, String texto, boolean error){
		
	}

	private void mostrarProgreso(){
		while(true){
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
		String fichero=null;
		String ruta=null;
		WebProcessor wp=new WebProcessor(ruta, 400, 10);
		wp.process(fichero);
	}

}
