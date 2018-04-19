package org.neocities.renacer.docbook.translate.view;

import java.io.File;

import org.neocities.renacer.docbook.translate.TraducciónDocBookGUI;
import org.neocities.renacer.docbook.translate.model.TraducciónDocBook;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;

/**
 * Controlador del lienzo raíz del GUI del artefacto de soporte a traducción.
 * 
 * @author avega
 */
public class LienzoRaízControlador {

	private TraducciónDocBookGUI traducciónGUI;
	private TraducciónDocBook traducciónPO;

	/**
	 * Apertura de un selector de ficheros para salvado de la traducción PO.
	 */
	@FXML
	private void manejaSalvaFicheroPO() {
		FileChooser selectorFicheroPO = new FileChooser();

		selectorFicheroPO.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ficheros PO", "*.po"));
		File ficheroPO = selectorFicheroPO.showSaveDialog(traducciónGUI.getEscenarioPrimario());

		if (ficheroPO != null) {
			// Aseguramiento de la corrección de la extensión del fichero.
			if (!ficheroPO.getPath().endsWith(".po"))
				ficheroPO = new File(ficheroPO.getPath() + ".po");
			traducciónPO.estableceFicheroPO(ficheroPO);
			traducciónPO.generaFicheroPO();			
		}
	}

	/**
	 * Visualización de un diálogo <i>Acerca de</i>, con información del artefacto.
	 */
	@FXML
	private void manejaAcercaDe() {
		Alert alerta = new Alert(AlertType.INFORMATION);

		alerta.setTitle("Generación de ficheros PO");
		alerta.setHeaderText("Acerca de");
		alerta.setContentText("Iglesia Evangélica Renacer\nhttps://iglesia-renacer.neocities.org/");
		alerta.showAndWait();
	}

	/**
	 * Cierre de la aplicación.
	 */
	@FXML
	private void manejaSalir() {
		System.exit(0);
	}

	/**
	 * Establecimiento del objeto de interfaz gráfica a la Traducción de Libro PO a establecer.
	 * 
	 * @param traducciónGUI
	 *            Objeto de interfaz gráfica a la Traducción de Libro PO a establecer.
	 */
	public void setTraducciónGUI(TraducciónDocBookGUI traducciónGUI) {
		this.traducciónGUI = traducciónGUI;
	}

	/**
	 * Establecimiento del objeto de negocio de traducción de libros DocBook.
	 * 
	 * @param traducciónPO
	 *            Objeto de Traducción de Libro PO a establecer.
	 */
	public void setTraducciónPO(TraducciónDocBook traducciónPO) {
		this.traducciónPO = traducciónPO;
	}
}
