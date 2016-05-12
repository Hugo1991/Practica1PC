package practica1;

import java.io.BufferedReader;
import java.io.FileReader;
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
		
		try {
			FileReader f = new FileReader(fileName);
			BufferedReader b = new BufferedReader(f);
			String linea;
			linea = b.readLine();
		
		
		while (!(linea == null)||progreso<=nDown) {
			for(int i=0;i<maxDown;i++){
				String lineaCopia=linea;
				new Thread(()->descargaFichero(lineaCopia),"Hilo "+i).start();
				progreso++;
				linea=b.readLine();
			}
			
		}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				System.out.println("Error: " + resp.statusCode());
			} else {
				String html = conn.get().html();
				escribirFichero(url, html, false);
			}
		}catch(IllegalArgumentException iae){
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
		String fichero="3.txt";
		String ruta="descargas";
		WebProcessor wp=new WebProcessor(ruta, 400, 10);
		wp.process(fichero);
	}

}
