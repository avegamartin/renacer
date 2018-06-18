package org.neocities.renacer.docbook.translate.view;

import java.io.File;
import java.util.ListIterator;

import org.neocities.renacer.docbook.translate.TraducciónDocBookGUI;
import org.neocities.renacer.util.NumberedSAXReader;
import org.neocities.renacer.util.NumberedSAXReader.LocationAwareElement;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

/**
 * Controlador del lienzo raíz del GUI del artefacto de soporte PO a traducción.
 * 
 * @author avega
 */
public class LienzoRaízControlador {

	private TraducciónDocBookGUI traducciónGUI;
	private ListIterator<NumberedSAXReader.LocationAwareElement> iteListaErroresTraducción;
	private TreeView<LocationAwareElement> libroOrigenÁrbol;
	private NumberedSAXReader.LocationAwareElement últimoErrorVisitado = null;

	@FXML
	private MenuItem ítemMenúSalvarPO;
	@FXML
	private Button botónSalvarPO;
	@FXML
	private Button botónErrorAnterior;
	@FXML
	private Button botónErrorSiguiente;

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
			traducciónGUI.getTraducciónPO().estableceFicheroPO(ficheroPO);
			traducciónGUI.getTraducciónPO().generaFicheroPO();
			botónSalvarPO.setDisable(true);
			ítemMenúSalvarPO.setDisable(true);
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
	 * Localización del anterior error de traducción.
	 */
	@FXML
	private void manejaLocalizaErrorAnterior() {
		if (iteListaErroresTraducción == null)
			return;

		if (iteListaErroresTraducción.hasPrevious()) {
			LocationAwareElement errorPrevio = iteListaErroresTraducción.previous();

			if (últimoErrorVisitado != null && errorPrevio == últimoErrorVisitado) {
				errorPrevio = iteListaErroresTraducción.previous();
			}

			últimoErrorVisitado = errorPrevio;
			visualizaÚltimoErrorVisitado();
		}
	}

	/**
	 * Localización del siguiente error de traducción.
	 */
	@FXML
	private void manejaLocalizaErrorSiguiente() {
		if (iteListaErroresTraducción == null)
			return;

		if (iteListaErroresTraducción.hasNext()) {
			LocationAwareElement errorPosterior = iteListaErroresTraducción.next();

			if (últimoErrorVisitado != null && errorPosterior == últimoErrorVisitado) {
				errorPosterior = iteListaErroresTraducción.next();
			}

			últimoErrorVisitado = errorPosterior;
			visualizaÚltimoErrorVisitado();
		}
	}

	/**
	 * Visualización, en el árbol del libro de idioma de origen, del último error
	 * visitado.
	 */
	private void visualizaÚltimoErrorVisitado() {
		// Si el nodo con error de traducción no tiene aún especificada su posición,
		// calcularla
		if (últimoErrorVisitado.getIndexInTree() == -1) {
			libroOrigenÁrbol.getSelectionModel().select(últimoErrorVisitado.getVisualNode());
			últimoErrorVisitado.setIndexInTree(libroOrigenÁrbol.getSelectionModel().getSelectedIndex());
		}

		libroOrigenÁrbol.getSelectionModel().select(últimoErrorVisitado.getIndexInTree());
		libroOrigenÁrbol.scrollTo(últimoErrorVisitado.getIndexInTree());
		libroOrigenÁrbol.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true,
				true, true, true, true, true, true, true, true, true, null));
		adecúaBotonesAListaErrores();
	}

	/**
	 * Adecuación del estado de habilitación de los botones de recorrido de la lista
	 * de errores, a la posición actual en la misma.
	 */
	private void adecúaBotonesAListaErrores() {
		if (iteListaErroresTraducción.hasNext())
			botónErrorSiguiente.setDisable(false);
		else
			botónErrorSiguiente.setDisable(true);

		if (iteListaErroresTraducción.hasPrevious())
			botónErrorAnterior.setDisable(false);
		else
			botónErrorAnterior.setDisable(true);
	}

	/**
	 * Inicialización de la información necesaria sobre la traducción.
	 */
	void inicializaInfoTraducción() {
		iteListaErroresTraducción = traducciónGUI.getTraducciónPO().getListaErroresTraducción().listIterator();
		libroOrigenÁrbol = traducciónGUI.getLienzoGeneralControlador().getLibroOrigenÁrbol();
		botónSalvarPO.setDisable(false);
		ítemMenúSalvarPO.setDisable(false);
		if (!traducciónGUI.getTraducciónPO().getListaErroresTraducción().isEmpty())
			botónErrorSiguiente.setDisable(false);
	}

	/**
	 * Establecimiento del objeto de interfaz gráfica al artefacto de soporte PO a
	 * traducción.
	 * 
	 * @param traducciónGUI
	 *            Objeto de interfaz gráfica al artefacto de soporte PO a
	 *            traducción.
	 */
	public void setTraducciónGUI(TraducciónDocBookGUI traducciónGUI) {
		this.traducciónGUI = traducciónGUI;
	}
}
