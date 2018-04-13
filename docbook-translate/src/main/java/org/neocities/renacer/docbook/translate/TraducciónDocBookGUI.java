package org.neocities.renacer.docbook.translate;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neocities.renacer.docbook.translate.view.LienzoGeneralControlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Interfaz Gráfica de Usuario principal del artefacto de traducción de libros
 * estructurados según el estándar DocBook v5.x.
 *  
 * @author avega
 */
public class TraducciónDocBookGUI extends Application {
	
	// Interfaz de Usuario
	private Stage escenarioPrimario;
	private BorderPane lienzoRaíz;
	
	// Objetos de Dominio
	private TraducciónDocBook traducciónPO;
	
	private final static Logger BITÁCORA = Logger.getLogger(TraducciónDocBookGUI.class.getName());
	
	@Override
	public void init() {
		traducciónPO = new TraducciónDocBook();
	}

	@Override
	public void start(Stage primaryStage) {
		escenarioPrimario = primaryStage;
		escenarioPrimario.setTitle("Generación de ficheros PO");
		inicializaLienzoRaíz();
		visualizaLienzoGeneral();
	}
	
	/**
	 * Inicialización del lienzo raíz de la aplicación.
	 */
	private void inicializaLienzoRaíz() {
		try {
			// Carga de la definición del lienzo raíz.
			FXMLLoader cargador = new FXMLLoader();
			cargador.setLocation(TraducciónDocBookGUI.class.getResource("view/LienzoRaíz.fxml"));
			lienzoRaíz = (BorderPane) cargador.load();
			
			// Visualización de la escena contenedora del lienzo raíz.
			Scene escena = new Scene(lienzoRaíz);
			escenarioPrimario.setScene(escena);
			escenarioPrimario.show();
			
		} catch (IOException ioe) {
			BITÁCORA.log(Level.SEVERE, "Imposible cargar la definición del lienzo raíz: ", ioe);
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Visualización del lienzo general de la aplicación.
	 */
	private void visualizaLienzoGeneral() {
		try {
			// Carga de la definición del lienzo general.
			FXMLLoader cargador = new FXMLLoader();
			cargador.setLocation(TraducciónDocBookGUI.class.getResource("view/LienzoGeneral.fxml"));
			AnchorPane lienzoGeneral = (AnchorPane) cargador.load();
			
			// Colocación del lienzo general en el centro del lienzo raíz.
			lienzoRaíz.setCenter(lienzoGeneral);
			
			// Cesión de acceso al modelo de dominio, por parte del controlador del lienzo general.
			LienzoGeneralControlador controlador = cargador.getController();
			controlador.setTraducciónPO(traducciónPO);
			
		} catch (IOException ioe) {
			BITÁCORA.log(Level.SEVERE, "Imposible cargar la definición del lienzo general: ", ioe);
			ioe.printStackTrace();
		}
	}

	/**
	 * @return El escenario primario de la aplicación.
	 */
	public Stage getEscenarioPrimario() {
		return escenarioPrimario;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
