package org.neocities.renacer.docbook.translate.view;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.neocities.renacer.docbook.translate.TraducciónDocBook;
import org.neocities.renacer.docbook.translate.TraducciónDocBookGUI;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * Controlador del lienzo general del GUI del artefacto de Soporte a traducción.
 * 
 * @author avega
 */
public class LienzoGeneralControlador {

	@FXML
	TreeView<String> libroOrigenÁrbol;
	@FXML
	TreeView<String> libroDestinoÁrbol;

	private TraducciónDocBook traducciónPO;
	private final static Logger BITÁCORA = Logger.getLogger(LienzoGeneralControlador.class.getName());

	/**
	 * Inicialización de la clase controladora del Lienzo General.
	 */
	@FXML
	private void initialize() {
		/*
		 * Configuración de los gestos de arrastrar y soltar en los controles.
		 */
		libroOrigenÁrbol.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent evento) {
				if (evento.getGestureSource() != libroOrigenÁrbol && evento.getDragboard().hasString()
						&& evento.getDragboard().getString().startsWith("file://")
						&& evento.getDragboard().getString().replaceAll("\n|\r", "").endsWith(".xml")) {
					evento.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}
				evento.consume();
			}
		});

		libroOrigenÁrbol.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent evento) {
				Dragboard db = evento.getDragboard();
				boolean esExitoso = false;

				if (db.hasString() && db.getString().startsWith("file://")
						&& evento.getDragboard().getString().replaceAll("\n|\r", "").endsWith(".xml")) {
					try {
						traducciónPO.estableceLibroOrigen(
								db.getString().replaceFirst("file://", "").replaceAll("\n|\r", ""));

						libroOrigenÁrbol.setRoot(
								construyeÁrbolDesdeElementoDOM(traducciónPO.getLibroOrigenDoc().getRootElement()));
						esExitoso = true;
					} catch (FileNotFoundException | DocumentException e) {
						BITÁCORA.log(Level.SEVERE, "Imposible cargar el libro origen: ", e);
						e.printStackTrace();
					}
				}

				evento.setDropCompleted(esExitoso);
				evento.consume();
			}
		});

		libroDestinoÁrbol.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent evento) {
				if (evento.getGestureSource() != libroDestinoÁrbol && evento.getDragboard().hasString()
						&& evento.getDragboard().getString().startsWith("file://")
						&& evento.getDragboard().getString().replaceAll("\n|\r", "").endsWith(".xml")) {
					evento.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}
				evento.consume();
			}
		});

		libroDestinoÁrbol.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent evento) {
				Dragboard db = evento.getDragboard();
				boolean esExitoso = false;

				if (db.hasString() && db.getString().startsWith("file://")
						&& evento.getDragboard().getString().replaceAll("\n|\r", "").endsWith(".xml")) {
					try {
						traducciónPO.estableceLibroDestino(
								db.getString().replaceFirst("file://", "").replaceAll("\n|\r", ""));

						libroDestinoÁrbol.setRoot(
								construyeÁrbolDesdeElementoDOM(traducciónPO.getLibroDestinoDoc().getRootElement()));
						esExitoso = true;
					} catch (FileNotFoundException | DocumentException e) {
						BITÁCORA.log(Level.SEVERE, "Imposible cargar el libro destino: ", e);
						e.printStackTrace();
					}
				}

				evento.setDropCompleted(esExitoso);
				evento.consume();
			}
		});
	}

	/**
	 * Construcción recursiva de un árbol de nodos, JavaFX, con el contenido del
	 * documento DOM cuya raíz se especifica.
	 */
	private TreeItem<String> construyeÁrbolDesdeElementoDOM(Element elementoRaízSubárbol) {
		TreeItem<String> elementoÁrbolVisual = new TreeItem<String>(
				"<" + elementoRaízSubárbol.getName() + "> " + elementoRaízSubárbol.getTextTrim());
		for (Element nodoHijo : elementoRaízSubárbol.elements()) {
			elementoÁrbolVisual.getChildren().add(construyeÁrbolDesdeElementoDOM(nodoHijo));
		}
		return elementoÁrbolVisual;
	}

	/**
	 * Establecimiento de la clase de negocio de traducción de libros DocBook.
	 * 
	 * @param traducciónPO
	 *            Objeto de Traducción de Libro PO a establecer.
	 */
	public void setTraducciónPO(TraducciónDocBook traducciónPO) {
		this.traducciónPO = traducciónPO;
	}
}
