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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.neocities.renacer.util.NumberedSAXReader;

/**
 * @author avega
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
	 *            Lista de ficheros XML, contenedores de libros, a analizar: idioma
	 *            de origen y de destino.
	 */
	public static void main(String[] args) {
		TraducciónDocBook traducción = new TraducciónDocBook();
		try {
			Document libro = traducción
					.analizaXMLDocBook(traducción.getClass().getClassLoader().getResourceAsStream(args[0]));
			System.out.println("Codificación empleada:" + libro.getXMLEncoding());

		} catch (DocumentException e) {
			System.err.println("Se ha producido una excepción al procesar el fichero:\n" + e);
			e.printStackTrace();
		}
	}

	/**
	 * Establecimiento del libro de origen: el escrito en el idioma original del que
	 * se traduce.
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
	 * Establecimiento del libro de destino: el escrito en el idioma al que se
	 * traduce.
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
	 * estándar DocBook, y conversión de los mismos a un árbol de objetos Java
	 * apropiado.
	 * 
	 * @param is
	 *            Flujo de entrada con un fichero XML conteniendo un libro según el
	 *            esquema DocBook.
	 * @return Documento XML contenido en el flujo de entrada pasado como parámetro.
	 * @throws DocumentException
	 */
	private Document analizaXMLDocBook(InputStream is) throws DocumentException {
		SAXReader lector = new NumberedSAXReader();
		Document libroAnalizado = lector.read(is);
		return libroAnalizado;
	}

	/**
	 * Traducción de los títulos y párrafos encontrados a partir de la raíz de un
	 * subárbol dado.
	 * 
	 * @param escritorPO
	 *            Escritor del fichero resultado de la traducción (fichero PO).
	 * @param element
	 *            Nodo elemento raíz del subárbol.
	 */
	public void traduceSubárbol(PrintWriter escritorPO, Element element) {
		Node nodoOrigen = null, nodoDestino = null;
		StringTokenizer stNodoOrigen, stNodoDestino = null;
		String tokenOrigen = null, tokenDestino = null;

		for (int i = 0, númNodos = element.nodeCount(); i < númNodos; i++) {
			nodoOrigen = element.node(i);

			if (nodoOrigen.getNodeType() == Node.ELEMENT_NODE
					&& (nodoOrigen.getName().equals("title") || nodoOrigen.getName().equals("para"))) { // Requiere
																										// traducción
				escritorPO.println(config.getProperty("po.cuerpo.línea0"));
				escritorPO.println(String.format(config.getProperty("po.cuerpo.línea1"),
						expresaRutaComoNodos(xPathSinNS(nodoOrigen.getPath()))));
				escritorPO.println(String.format(config.getProperty("po.cuerpo.línea2"), libroOrigenFichero.getName(),
						((NumberedSAXReader.LocationAwareElement) nodoOrigen).getLineNumber()));

				nodoDestino = libroDestinoDoc.selectSingleNode(nodoOrigen.getUniquePath());
				stNodoOrigen = new StringTokenizer(nodoOrigen.getText(), ".?!", true);
				if (nodoDestino != null) {
					stNodoDestino = new StringTokenizer(nodoDestino.getText(), ".?!", true);
				} else {
					stNodoDestino = null;
				}

				while (stNodoOrigen.hasMoreTokens()) {
					tokenOrigen = stNodoOrigen.nextToken();
					if (stNodoDestino.hasMoreElements()) {
						tokenDestino = stNodoDestino.nextToken();
					} else {
						tokenDestino = null;
					}
					if (tokenOrigen.equals(" ") || tokenDestino == null || tokenDestino.equals(" ")) {
						continue;
					}

					escritorPO.println(String.format(config.getProperty("po.cuerpo.línea3"),
							tokenOrigen + (stNodoOrigen.hasMoreTokens() ? stNodoOrigen.nextToken() : "")));
					escritorPO.println(String.format(config.getProperty("po.cuerpo.línea4"),
							nodoDestino != null
									? tokenDestino + (stNodoDestino.hasMoreTokens() ? stNodoDestino.nextToken() : "")
									: "[*** Sin traducción ***]"));
				}
			}

			if (nodoOrigen instanceof Element) {
				traduceSubárbol(escritorPO, (Element) nodoOrigen);
			}
		}
	}

	/**
	 * Obtención del título del libro que se pasa como argumento.
	 * 
	 * @param libro
	 *            Documento contenedor de libro del que se quiere obtener el título.
	 * @return Título del libro que se pasa como argumento.
	 */
	public String obténTítuloLibro(Document libro) {
		// Node nodo = libro.getRootElement().element("info").element("title");
		return this.obténTextoContenidoNodo(libro, "/book/info/title");
	}

	/**
	 * Obtención del subtítulo del libro que se pasa como argumento.
	 * 
	 * @param libro
	 *            Documento contenedor de libro del que se quiere obtener el
	 *            subtítulo.
	 * @return Subtítulo del libro que se pasa como argumento.
	 */
	public String obténSubtítuloLibro(Document libro) {
		return this.obténTextoContenidoNodo(libro, "/book/info/subtitle");
	}

	/**
	 * Obtención del texto contenido en el elemento especificado, perteneciente al
	 * libro que se pasa como argumento.
	 * 
	 * @param libro
	 *            Documento contenedor de libro al que pertenece el elemento.
	 * @param xPathElemento
	 *            Expresión XPath para localización del elemento del que se quiere
	 *            obtener el texto contenido.
	 * @return Texto contenido en el elemento especificado, del libro que se pasa
	 *         como argumento.
	 */
	private String obténTextoContenidoNodo(Document libro, String xPathElemento) {
		return libro.selectSingleNode(xPathConNS(xPathElemento)).getText();
	}

	/**
	 * Obtención de la representación textual XML del contenido del elemento
	 * especificado, perteneciente al libro que se pasa como argumento.
	 * 
	 * @param libro
	 *            Documento contenedor de libro al que pertenece el elemento.
	 * @param xPathElemento
	 *            Expresión XPath para localización del elemento del que se quiere
	 *            obtener la representación textual XML del contenido.
	 * @return Representación textual XML del contenido en el elemento especificado,
	 *         del libro que se pasa como argumento.
	 */
	private String obténXMLContenidoNodo(Document libro, String xPathElemento) {
		return compactaCadenaXML(libro.selectSingleNode(xPathConNS(xPathElemento)).asXML());
	}

	/**
	 * Obtención del número de línea donde se localiza, dentro el fichero XML
	 * contenedor del libro que constituye el primer argumento, el elemento
	 * especificado en el segundo argumento.
	 * 
	 * @param libro
	 *            Libro donde consta el elemento del que se quiere obtener el número
	 *            de línea.
	 * @param xPathElemento
	 *            Expresión XPath para localización del elemento del que se quiere
	 *            obtener el número de línea.
	 * @return Número de línea donde está localizado el elemento, dentro del libro.
	 */
	public int obténLíneaElementoLibro(Document libro, String xPathElemento) {
		return ((NumberedSAXReader.LocationAwareElement) libro.selectSingleNode(xPathConNS(xPathElemento)))
				.getLineNumber();
	}

	/**
	 * Conversión de una expresión XPath para buscar en un XML sin espacio de
	 * nombres, en otra que permite hacerlo aun dándose uno.
	 * 
	 * @param xPathOriginal
	 *            Expresión XPath apropiada para búsquedas sin espacio de nombres.
	 * @return Expresión XPath que permite la búsqueda independientemente de espacio
	 *         de nombre alguno.
	 */
	private String xPathConNS(String xPathOriginal) {
		StringBuilder sb = new StringBuilder(64);

		for (String nodo : xPathOriginal.split("/")) {
			if (nodo.length() == 0)
				continue;
			sb.append("/*[name()='").append(nodo).append("']");
		}

		return sb.toString();
	}

	/**
	 * Conversión de una expresión XPath para buscar en un XML independientemente de
	 * espacios de nombres, en otra que sólo permite hacerlo en ausencia de ellos.
	 * 
	 * @param xPathOriginal
	 *            Expresión XPath apropiada para búsquedas con espacio de nombres.
	 * @return Expresión XPath que permite la búsqueda sólo en caso de no
	 *         especificar espacios de nombres.
	 */
	private String xPathSinNS(String xPathOriginal) {
		return xPathOriginal.replaceAll("\\*\\[name\\(\\)='", "").replaceAll("'\\]", "");
	}

	/**
	 * Conversión de una expresión XPath para buscar elementos XML, en una secuencia
	 * de nodos en notación de elementos XML clásica.
	 * 
	 * @param xPathOriginal
	 *            Expresión XPath de dirección de un elemento XML.
	 * @return Ruta XPath convertida en una secuencia de nodos (elementos) XML.
	 */
	private String expresaRutaComoNodos(String xPathOriginal) {
		StringBuilder sb = new StringBuilder(64);

		for (String nodo : xPathOriginal.split("/")) {
			if (nodo.length() == 0)
				continue;
			sb.append("<").append(nodo).append(">");
		}

		return sb.toString();
	}

	/**
	 * Compactación de una cadena XML, eliminando saltos de línea y tabuladores.
	 * 
	 * @param cadenaXMLOriginal
	 *            Cadena XML original a compactar.
	 * @return Resultado de la compactación.
	 */
	private String compactaCadenaXML(String cadenaXMLOriginal) {
		return cadenaXMLOriginal.replaceAll("\n|\r|\t", "");
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
			escritorPO.println(
					String.format(config.getProperty("po.cabecera.línea1"), this.obténTítuloLibro(libroOrigenDoc)));
			for (int i = 2; i <= 8; i++)
				escritorPO.println(config.getProperty("po.cabecera.línea" + i));
			escritorPO.println(
					String.format(config.getProperty("po.cabecera.línea9"), this.obténTítuloLibro(libroOrigenDoc)));
			escritorPO.println(String.format(config.getProperty("po.cabecera.línea10"), new Date()));
			for (int i = 11; i <= 17; i++)
				escritorPO.println(config.getProperty("po.cabecera.línea" + i));

			/*
			 * Creación de las líneas descriptoras del título y su traducción.
			 */
			escritorPO.println(config.getProperty("po.cuerpo.línea0"));
			escritorPO.println(String.format(config.getProperty("po.cuerpo.línea1"), "<book><info><title>"));
			escritorPO.println(String.format(config.getProperty("po.cuerpo.línea2"), libroOrigenFichero.getName(),
					this.obténLíneaElementoLibro(libroOrigenDoc, "/book/info/title")));
			escritorPO.println(
					String.format(config.getProperty("po.cuerpo.línea3"), this.obténTítuloLibro(libroOrigenDoc)));
			escritorPO.println(
					String.format(config.getProperty("po.cuerpo.línea4"), this.obténTítuloLibro(libroDestinoDoc)));

			/*
			 * Creación de las líneas descriptoras del subtítulo y su traducción.
			 */
			escritorPO.println(config.getProperty("po.cuerpo.línea0"));
			escritorPO.println(String.format(config.getProperty("po.cuerpo.línea1"), "<book><info><subtitle>"));
			escritorPO.println(String.format(config.getProperty("po.cuerpo.línea2"), libroOrigenFichero.getName(),
					this.obténLíneaElementoLibro(libroOrigenDoc, "/book/info/subtitle")));
			escritorPO.println(
					String.format(config.getProperty("po.cuerpo.línea3"), this.obténSubtítuloLibro(libroOrigenDoc)));
			escritorPO.println(
					String.format(config.getProperty("po.cuerpo.línea4"), this.obténSubtítuloLibro(libroDestinoDoc)));

			/*
			 * Creación de las líneas descriptoras del autor y su traducción, si están
			 * especificados.
			 */
			if (libroOrigenDoc.selectSingleNode(xPathConNS("/book/info/author")) != null) {
				escritorPO.println(config.getProperty("po.cuerpo.línea0"));
				escritorPO.println(String.format(config.getProperty("po.cuerpo.línea1"), "<book><info><author>"));
				escritorPO.println(String.format(config.getProperty("po.cuerpo.línea2"), libroOrigenFichero.getName(),
						this.obténLíneaElementoLibro(libroOrigenDoc, "/book/info/author")));
				escritorPO.println(String.format(config.getProperty("po.cuerpo.línea3"),
						this.obténXMLContenidoNodo(libroOrigenDoc, "/book/info/author")));
				escritorPO.println(String.format(config.getProperty("po.cuerpo.línea4"),
						this.obténXMLContenidoNodo(libroDestinoDoc, "/book/info/author")));
			}
			/*
			 * Creación de las líneas descriptoras del prefacio y su traducción.
			 */
			this.traduceSubárbol(escritorPO, libroOrigenDoc.getRootElement().element("preface"));

			escritorPO.close();

		} catch (FileNotFoundException fnfe) {
			BITÁCORA.log(Level.SEVERE, "Error al intentar escribir en el fichero PO: ", fnfe);
			fnfe.printStackTrace();
		} catch (UnsupportedEncodingException uee) {
			BITÁCORA.log(Level.SEVERE,
					"La codificación de caracteres, especificada para el fichero PO, no está soportada: ", uee);
			uee.printStackTrace();
		}
	}
}
