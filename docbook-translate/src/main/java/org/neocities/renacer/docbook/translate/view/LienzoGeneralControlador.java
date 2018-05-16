package org.neocities.renacer.docbook.translate.view;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.neocities.renacer.docbook.translate.TraducciónDocBookGUI;
import org.neocities.renacer.docbook.translate.model.FraseConTraducción;
import org.neocities.renacer.util.NumberedSAXReader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador del lienzo general del GUI del artefacto de soporte PO a
 * traducción.
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

	private TableView<FraseConTraducción> tablaFrases = new TableView<FraseConTraducción>();
	private ObservableList<FraseConTraducción> listaFrases = FXCollections.observableArrayList();

	private TraducciónDocBookGUI traducciónGUI;
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
						traducciónGUI.getTraducciónPO().estableceLibroOrigen(
								db.getString().replaceFirst("file://", "").replaceAll("\n|\r", ""));
						libroOrigenÁrbol.setRoot(construyeÁrbolDesdeElementoDOM(
								traducciónGUI.getTraducciónPO().getLibroOrigenDoc().getRootElement()));
						libroOrigenÁrbol.getRoot().setExpanded(true);
						if (traducciónGUI.getTraducciónPO().getLibroDestinoDoc() != null)
							traducciónGUI.getTraducciónPO().generaPO();
						áreaTextoPO.setText(traducciónGUI.getTraducciónPO().obténContenidoPO());
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
				if (traducciónGUI.getTraducciónPO().getLibroDestinoDoc() != null && evento.getClickCount() <= 2) {
					NumberedSAXReader.LocationAwareElement nodo = libroOrigenÁrbol.getSelectionModel().getSelectedItem()
							.getValue();
					if (nodo.getRelatedNode() != null) {
						TreeItem<NumberedSAXReader.LocationAwareElement> nodoVisual = nodo.getRelatedNode()
								.getVisualNode();
						libroDestinoÁrbol.getSelectionModel().select(nodoVisual);
						libroDestinoÁrbol.scrollTo(libroDestinoÁrbol.getSelectionModel().getSelectedIndex());

						String patrónBúsquedaPO = "#: "
								+ traducciónGUI.getTraducciónPO().getLibroOrigenFichero().getName() + ":"
								+ nodo.getLineNumber();
						int índiceDesde = áreaTextoPO.getText().indexOf(patrónBúsquedaPO);
						áreaTextoPO.selectRange(índiceDesde, índiceDesde + patrónBúsquedaPO.length());

						/*
						 * Visualización tabular de la traduccción de frases constituyentes del nodo.
						 */
						if (evento.getClickCount() == 2) {
							índiceDesde += patrónBúsquedaPO.length() + 1;
							int índiceHasta = áreaTextoPO.getText().indexOf("\n\n", índiceDesde);
							if (índiceHasta == -1)
								índiceHasta = áreaTextoPO.getText().length() - 1;
							StringTokenizer st = new StringTokenizer(
									áreaTextoPO.getText().substring(índiceDesde, índiceHasta), "\n");
							listaFrases.clear();
							while (st.hasMoreTokens()) {
								String fraseOrigen = st.nextToken(), fraseDestino = st.nextToken();

								listaFrases.add(new FraseConTraducción(
										fraseOrigen.substring(fraseOrigen.indexOf('"') + 1, fraseOrigen.length() - 1),
										fraseDestino.substring(fraseDestino.indexOf('"') + 1,
												fraseDestino.length() - 1)));
							}
							visualizaFrasesDelNodo();
						}
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
						traducciónGUI.getTraducciónPO().estableceLibroDestino(
								db.getString().replaceFirst("file://", "").replaceAll("\n|\r", ""));
						libroDestinoÁrbol.setRoot(construyeÁrbolDesdeElementoDOM(
								traducciónGUI.getTraducciónPO().getLibroDestinoDoc().getRootElement()));
						libroDestinoÁrbol.getRoot().setExpanded(true);
						if (traducciónGUI.getTraducciónPO().getLibroOrigenDoc() != null)
							traducciónGUI.getTraducciónPO().generaPO();
						áreaTextoPO.setText(traducciónGUI.getTraducciónPO().obténContenidoPO());
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
	 * Visualización del diálogo de frases constituyentes del nodo traducible.
	 */
	@SuppressWarnings("unchecked")
	private void visualizaFrasesDelNodo() {
		Stage diálogoFrases = new Stage();
		Scene escena = new Scene(new AnchorPane(), 650, 500);

		diálogoFrases.sizeToScene();
		diálogoFrases.setTitle("Frases del nodo");
		diálogoFrases.initModality(Modality.WINDOW_MODAL);
		diálogoFrases.initOwner(traducciónGUI.getEscenarioPrimario());

		TableColumn<FraseConTraducción, String> fraseOrigenCol = new TableColumn<>("Idioma Origen");
		fraseOrigenCol.setCellValueFactory(new PropertyValueFactory<>("fraseOrigen"));
		fraseOrigenCol.setCellFactory(tc -> {
			TableCell<FraseConTraducción, String> celda = new TableCell<>();
			Text texto = new Text();
			celda.setGraphic(texto);
			celda.setPrefHeight(Control.USE_COMPUTED_SIZE);
			texto.wrappingWidthProperty().bind(celda.widthProperty());
			texto.textProperty().bind(celda.itemProperty());
			return celda;
		});

		TableColumn<FraseConTraducción, String> fraseDestinoCol = new TableColumn<>("Idioma Destino");
		fraseDestinoCol.setCellValueFactory(new PropertyValueFactory<>("fraseDestino"));
		fraseDestinoCol.setCellFactory(fraseOrigenCol.getCellFactory());

		tablaFrases.setEditable(false);
		tablaFrases.setItems(listaFrases);
		tablaFrases.getColumns().clear();
		tablaFrases.getColumns().addAll(fraseOrigenCol, fraseDestinoCol);
		tablaFrases.setLayoutX(5);
		tablaFrases.setLayoutY(5);
		tablaFrases.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		AnchorPane.setTopAnchor(tablaFrases, 0.0);
		AnchorPane.setBottomAnchor(tablaFrases, 0.0);
		AnchorPane.setLeftAnchor(tablaFrases, 0.0);
		AnchorPane.setRightAnchor(tablaFrases, 0.0);

		((AnchorPane) escena.getRoot()).getChildren().clear();
		((AnchorPane) escena.getRoot()).getChildren().addAll(tablaFrases);
		diálogoFrases.setScene(escena);
		diálogoFrases.showAndWait();
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
