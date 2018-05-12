package org.neocities.renacer.docbook.translate.view;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.neocities.renacer.docbook.translate.model.TraducciónDocBook;
import org.neocities.renacer.util.NumberedSAXReader;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Controlador del lienzo general del GUI del artefacto de soporte PO a traducción.
 * 
 * @author avega
 */
public class LienzoGeneralControlador {

	@FXML
	private TreeView<NumberedSAXReader.LocationAwareElement> libroOrigenÁrbol;
	@FXML
	private TreeView<NumberedSAXReader.LocationAwareElement> libroDestinoÁrbol;
	@FXML
	private TextArea áreaTextoPO;

	private TraducciónDocBook traducciónPO;
	private final static Logger BITÁCORA = Logger.getLogger(LienzoGeneralControlador.class.getName());

	/**
	 * Inicialización de la clase controladora del Lienzo General.
	 */
	@FXML
	private void initialize() {
		List<String> estilosNodoÁrbol = Arrays.asList("nodoOK", "nodoKO");

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
						libroOrigenÁrbol.getRoot().setExpanded(true);
						if (traducciónPO.getLibroDestinoDoc() != null)
							traducciónPO.generaPO();
						áreaTextoPO.setText(traducciónPO.obténContenidoPO());
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

		libroOrigenÁrbol.setCellFactory(treeView -> new TreeCell<NumberedSAXReader.LocationAwareElement>() {
			@Override
			public void updateItem(NumberedSAXReader.LocationAwareElement nodo, boolean vacío) {
				super.updateItem(nodo, vacío);
				getStyleClass().removeAll(estilosNodoÁrbol);

				if (nodo == null) {
					setText("");
					return;
				}

				if (vacío) {
					setText(nodo.toString());
				} else {
					setText(nodo.toString());
					String estiloNodoÁrbol = "nodoOK";
					if (nodo.isNodeWithErrors())
						estiloNodoÁrbol = "nodoKO";
					getStyleClass().add(estiloNodoÁrbol);
				}
			}
		});

		libroOrigenÁrbol.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent evento) {
				// Si se ha establecido el libro de destino, mostrar el nodo traducido
				// correspondiente al que se selecciona.
				if (traducciónPO.getLibroDestinoDoc() != null && evento.getClickCount() == 1) {
					NumberedSAXReader.LocationAwareElement nodo = libroOrigenÁrbol.getSelectionModel().getSelectedItem()
							.getValue();
					if (nodo.getRelatedNode() != null) {
						TreeItem<NumberedSAXReader.LocationAwareElement> nodoVisual = nodo.getRelatedNode()
								.getVisualNode();
						libroDestinoÁrbol.getSelectionModel().select(nodoVisual);
						libroDestinoÁrbol.scrollTo(libroDestinoÁrbol.getSelectionModel().getSelectedIndex());

						String patrónBúsquedaPO = "#: " + traducciónPO.getLibroOrigenFichero().getName() + ":"
								+ nodo.getLineNumber();
						int índice = áreaTextoPO.getText().indexOf(patrónBúsquedaPO);
						áreaTextoPO.selectRange(índice, índice + patrónBúsquedaPO.length());
					}
				}
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
						libroDestinoÁrbol.getRoot().setExpanded(true);
						if (traducciónPO.getLibroOrigenDoc() != null)
							traducciónPO.generaPO();
						áreaTextoPO.setText(traducciónPO.obténContenidoPO());
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
	 * 
	 * @param elementoRaízSubárbol
	 *            Elemento DOM raíz del subárbol construido en la presente llamada
	 *            de pila.
	 * @return Subárbol construido a partir de la raíz especificada.
	 */
	private TreeItem<NumberedSAXReader.LocationAwareElement> construyeÁrbolDesdeElementoDOM(
			Element elementoRaízSubárbol) {
		TreeItem<NumberedSAXReader.LocationAwareElement> elementoÁrbolVisual = new TreeItem<>(
				(NumberedSAXReader.LocationAwareElement) elementoRaízSubárbol);
		((NumberedSAXReader.LocationAwareElement) elementoRaízSubárbol).setVisualNode(elementoÁrbolVisual);
		for (Element nodoHijo : elementoRaízSubárbol.elements()) {
			elementoÁrbolVisual.getChildren().add(construyeÁrbolDesdeElementoDOM(nodoHijo));
		}
		if (elementoRaízSubárbol.getName().equals("info"))
			elementoÁrbolVisual.setExpanded(true);

		return elementoÁrbolVisual;
	}

	/**
	 * @return El árbol de nodos, JavaFX, con el contenido del documento DOM en el
	 *         idioma de origen.
	 */
	public TreeView<NumberedSAXReader.LocationAwareElement> getLibroOrigenÁrbol() {
		return libroOrigenÁrbol;
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
