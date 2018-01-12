/**
 * Clase generadora de ficheros PO, de descripción de traducción de libros
 * estructurados según subconjuntos de esquemas definidos por el estándar
 * DocBook v5.x.
 * 
 * @author avega
 * 
 */
package org.neocities.renacer.docbook.translate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * @author antonio
 *
 */
public class GeneraFicheroPO {

	private Document libroOrigen = null;
	private static Properties config = new Properties();
	private final static Logger BITÁCORA = Logger.getLogger(GeneraFicheroPO.class.getName());
	
	/**
	 * Construcción del objeto, y carga de su configuración de entorno.
	 */
	public GeneraFicheroPO() {
		try {
			config.load(this.getClass().getClassLoader().getResourceAsStream("org.neocities.renacer.docbook.translate.cfg"));
		} catch (IOException ioe) {
			BITÁCORA.log(Level.SEVERE, "Imposible cargar la parametrización inicial: ", ioe);
			ioe.printStackTrace();
		}
	}
	
	/**
	 * @param args	Lista de ficheros XML, contenedores de libros, a analizar.
	 */
	public static void main(String[] args) {
		GeneraFicheroPO generador = new GeneraFicheroPO();
		try {
			Document libro = generador.analizaXMLDocBook(generador.getClass().getClassLoader().getResourceAsStream(args[0]));
			System.out.println("Codificación empleada:" + libro.getXMLEncoding());
			
		} catch (DocumentException e) {
			System.err.println("Se ha producido una excepción al procesar el fichero:\n" + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Análisis de ficheros XML contenedores de libros esquematizados según el estándar DocBook, y conversión de los mismos
	 * a un apropiado árbol de objetos Java, según el API DOM.
	 * 
	 * @param is	Flujo de entrada con un fichero XML conteniendo un libro según el esquema DocBook.
	 * @return Documento XML contenido en el flujo de entrada pasado como parámetro.
	 * @throws DocumentException
	 */
	public Document analizaXMLDocBook(InputStream is) throws DocumentException {
		SAXReader lector = new SAXReader();
		libroOrigen = lector.read(is);
		return libroOrigen;
	}
	
	public void iteraElementos(Element elementoRaíz) throws DocumentException {
		// Iteramos sobre los elementos hijos del raíz
		for (Iterator<Element> it = elementoRaíz.elementIterator(); it.hasNext();) {
			Element elemento = it.next();
			System.out.println(elemento.toString());
			for (Iterator<Element> it2 = elemento.elementIterator("para"); it2.hasNext();) {
				Element elementoNivel2 = it2.next();
				System.out.println("  >> " + elementoNivel2.toString());
			}
		}
	}
	
	public String obténTítuloLibroOrigen() {
//		Node nodo = libroOrigen.selectSingleNode("/*[name()='book']/*[name()='info']/*[name()='title']");
//		Node nodo = libroOrigen.selectSingleNode("/book/info/title");
		Node nodo = libroOrigen.getRootElement().element("info").element("title");
		return nodo.getText() + " [" + nodo.getPath() + "]";
	}
}
