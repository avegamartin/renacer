/**
 * Clase generadora de ficheros PO, de descripción de traducción de libros
 * estructurados según subconjuntos de esquemas definidos por el estándar
 * DocBook v5.x.
 * 
 * @author avega
 * 
 */
package org.neocities.renacer.docbook.translate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
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
public class TraducciónDocBook {

	private File libroOrigenFichero = null, libroDestinoFichero = null;
	private Document libroOrigenDoc = null, libroDestinoDoc = null;
	private static Properties config = new Properties();
	private final static Logger BITÁCORA = Logger.getLogger(TraducciónDocBook.class.getName());

	/**
	 * Construcción del objeto, y carga de su configuración de entorno.
	 */
	public TraducciónDocBook() {
		try {
			config.load(new InputStreamReader(
					this.getClass().getClassLoader().getResourceAsStream("org.neocities.renacer.docbook.translate.cfg"),
					"UTF-8"));
		} catch (IOException ioe) {
			BITÁCORA.log(Level.SEVERE, "Imposible cargar la parametrización inicial: ", ioe);
			ioe.printStackTrace();
		}
	}

	/**
	 * @param args
	 *            Lista de ficheros XML, contenedores de libros, a analizar.
	 */
	public static void main(String[] args) {
		TraducciónDocBook generador = new TraducciónDocBook();
		try {
			Document libro = generador
					.analizaXMLDocBook(generador.getClass().getClassLoader().getResourceAsStream(args[0]));
			System.out.println("Codificación empleada:" + libro.getXMLEncoding());

		} catch (DocumentException e) {
			System.err.println("Se ha producido una excepción al procesar el fichero:\n" + e);
			e.printStackTrace();
		}
	}

	/**
	 * Establecimiento del libro de origen: el escrito en el idioma original del que se traduce.
	 * 
	 * @param rutaLibroOrigen
	 *            Ruta donde está ubicado el libro de origen.
	 */
	public void estableceLibroOrigen(String rutaLibroOrigen) {
		libroOrigenFichero = new File(rutaLibroOrigen);
		try {
			libroOrigenDoc = analizaXMLDocBook(new FileInputStream(libroOrigenFichero));
		} catch (FileNotFoundException fnfe) {
			BITÁCORA.log(Level.SEVERE, "Fichero de libro en el idioma de origen no encontrado: ", fnfe);
			fnfe.printStackTrace();
		} catch (DocumentException de) {
			BITÁCORA.log(Level.SEVERE, "Error al efectuar en análisis XML del libro en el idioma de origen: ", de);
			de.printStackTrace();
		}
	}
	
	/**
	 * Establecimiento del libro de destino: el escrito en el idioma al que se traduce.
	 * 
	 * @param rutaLibroDestino
	 *            Ruta donde está ubicado el libro de destino.
	 */
	public void estableceLibroDestino(String rutaLibroDestino) {
		libroDestinoFichero = new File(rutaLibroDestino);
		try {
			libroDestinoDoc = analizaXMLDocBook(new FileInputStream(libroDestinoFichero));
		} catch (FileNotFoundException fnfe) {
			BITÁCORA.log(Level.SEVERE, "Fichero de libro en el idioma de destino no encontrado: ", fnfe);
			fnfe.printStackTrace();
		} catch (DocumentException de) {
			BITÁCORA.log(Level.SEVERE, "Error al efectuar en análisis XML del libro en el idioma de destino: ", de);
			de.printStackTrace();
		}
	}

	/**
	 * @return the libroOrigenDoc
	 */
	public Document getLibroOrigenDoc() {
		return libroOrigenDoc;
	}

	/**
	 * @return the libroDestinoDoc
	 */
	public Document getLibroDestinoDoc() {
		return libroDestinoDoc;
	}

	/**
	 * Análisis de ficheros XML contenedores de libros esquematizados según el
	 * estándar DocBook, y conversión de los mismos a un apropiado árbol de objetos
	 * Java, según el API DOM.
	 * 
	 * @param is
	 *            Flujo de entrada con un fichero XML conteniendo un libro según el
	 *            esquema DocBook.
	 * @return Documento XML contenido en el flujo de entrada pasado como parámetro.
	 * @throws DocumentException
	 */
	private Document analizaXMLDocBook(InputStream is) throws DocumentException {
		SAXReader lector = new SAXReader();
		Document libroAnalizado = lector.read(is);
		return libroAnalizado;
	}

	public void iteraElementos(Element elementoRaíz, String nombreNodo) throws DocumentException {
		// Iteramos sobre los elementos hijos del raíz
		for (Iterator<Element> it = elementoRaíz.elementIterator(); it.hasNext();) {
			Element elemento = it.next();
			System.out.println(elemento.toString());
			for (Iterator<Element> it2 = elemento.elementIterator(nombreNodo); it2.hasNext();) {
				Element elementoNivel2 = it2.next();
				System.out.println("  >> " + elementoNivel2.toString());
			}
		}
	}

	/**
	 * Obtención del nombre del libro con el idioma de origen de la traducción.
	 * 
	 * @param libro
	 *            Documento contenedor de libro del que se quiere obtener el título.
	 * @return Nombre del libro en el idioma de origen.
	 */
	public String obténTítuloLibro(Document libro) {
//		 libroOrigen.selectSingleNode("/*[name()='book']/*[name()='info']/*[name()='title']");
		Node nodo = libro.selectSingleNode(xpathSinNS("/book/info/title"));
//		Node nodo = libro.getRootElement().element("info").element("title");
		return nodo.getText();
	}
	
	/**
	 * Conversión de una expresión XPath para buscar en un XML sin espacio de nombres, en otra que permite hacerlo
	 * aun dándose uno.
	 * 
	 * @param xpathOriginal	Expresión XPath apropiada para búsquedas sin espacio de nombres.
	 * @return
	 */
	private String xpathSinNS(String xpathOriginal) {
		StringBuilder sb = new StringBuilder(64);
		
		for (String nodo : xpathOriginal.split("/")) {
			if (nodo.length() == 0) continue;
			sb.append("/*[name()='");
			sb.append(nodo);
			sb.append("']");
		}
		
		return sb.toString();
	}

	/**
	 * Generación del fichero de traducción PO.
	 */
	public void generaFicheroPO() {
		PrintWriter escritorPO;
		try {
			escritorPO = new PrintWriter("fichero.po", "UTF-8");

			/*
			 * Creación de la cabecera del fichero.
			 */
			escritorPO.println(String.format(config.getProperty("po.cabecera.línea1"),
					this.obténTítuloLibro(libroOrigenDoc)));
			for (int i = 2; i <= 8; i++)
				escritorPO.println(config.getProperty("po.cabecera.línea" + i));
			escritorPO.println(String.format(config.getProperty("po.cabecera.línea9"),
					this.obténTítuloLibro(libroOrigenDoc)));
			escritorPO.println(String.format(config.getProperty("po.cabecera.línea10"), new Date()));
			for (int i = 11; i <= 18; i++)
				escritorPO.println(config.getProperty("po.cabecera.línea" + i));

			/*
			 * Creación de las líneas descriptoras del título y su traducción.
			 */
			escritorPO.println(config.getProperty("po.título.línea1"));
			escritorPO.println(String.format(config.getProperty("po.título.línea2"), libroOrigenFichero.getName(), 4));

			escritorPO.close();

		} catch (FileNotFoundException fnfe) {
			BITÁCORA.log(Level.SEVERE, "Error al intentar escribir en el fichero PO: ", fnfe);
			fnfe.printStackTrace();
		} catch (UnsupportedEncodingException uee) {
			BITÁCORA.log(Level.SEVERE, "La codificación de caracteres, especificada para el fichero PO, no está soportada: ", uee);
			uee.printStackTrace();
		}
	}
}
