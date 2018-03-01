package org.neocities.renacer.docbook.translate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.neocities.renacer.util.MutableInteger;
import org.neocities.renacer.util.NumberedSAXReader;
import org.neocities.renacer.util.ParticionadorFrases;

/**
 * Clase generadora de ficheros PO, de descripción de traducción de libros
 * estructurados según subconjuntos de esquemas definidos por el estándar
 * DocBook v5.x.
 * 
 * @author avega
 */
public class TraducciónDocBook {

	private File libroOrigenFichero = null, libroDestinoFichero = null, ficheroPO = null;
	private Document libroOrigenDoc = null, libroDestinoDoc = null;
	private static Properties config = new Properties();
	private HashMap<String, MutableInteger> mapaCadenas = new HashMap<String, MutableInteger>();
	private ParticionadorFrases particionador = new ParticionadorFrases();
	private final static String TEXTO_NODO_NULO = "[*** Nulo ***]",
			TEXTO_NODO_SIN_TRADUCCIÓN = "[*** Sin traducción ***]";
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
	 *            de origen y de destino; fichero de traducción; solicitud de
	 *            mostrado de estructuras.
	 */
	public static void main(String[] args) {
		TraducciónDocBook traducción = new TraducciónDocBook();
		CommandLineParser analizadorLC = new DefaultParser();

		/*
		 * Establecimiento de opciones de la línea de comandos.
		 */
		Options opciones = new Options();
		opciones.addRequiredOption("o", "original", true, "Fichero XML con el documento en el idioma original.");
		opciones.addRequiredOption("t", "traducido", true, "Fichero XML con el documento en el idioma traducido.");
		opciones.addOption("p", "po", true, "Fichero de traducción PO.");
		opciones.addOption("e", "estructura", false,
				"Muestra la estructura de nodos traducibles de los ficheros de documento.");
		opciones.addOption("n", "numera", false, "Muestra los números de línea donde aparecen los nodos");

		/*
		 * Procesamiento y uso de las opciones de la línea de comandos.
		 */
		try {
			CommandLine lc = analizadorLC.parse(opciones, args);

			if (!(lc.hasOption("po") || lc.hasOption("estructura")))
				throw new ParseException("Es necesario especificar al menos una de las opciones -p o -e");

			traducción.estableceLibroOrigen(lc.getOptionValue("original"));
			traducción.estableceLibroDestino(lc.getOptionValue("traducido"));

			if (lc.hasOption("po")) {
				traducción.estableceFicheroPO(lc.getOptionValue("po"));
				traducción.generaFicheroPO();
			}

			if (lc.hasOption("estructura"))
				traducción.generaFicherosEstructura(lc.hasOption("numera"));

		} catch (FileNotFoundException | DocumentException e) {
			BITÁCORA.log(Level.SEVERE, "Abortando el proceso de generación del fichero PO...");
			System.exit(1);
		} catch (ParseException pe) {
			BITÁCORA.log(Level.SEVERE, "Parámetros de invocación incorrectos: ", pe);
			HelpFormatter formateadorLC = new HelpFormatter();
			formateadorLC.printHelp("TraducciónDocBook", opciones);
			System.exit(1);
		}
	}

	/**
	 * Establecimiento del libro de origen: el escrito en el idioma original del que
	 * se traduce.
	 * 
	 * @param rutaLibroOrigen
	 *            Ruta donde está ubicado el libro de origen.
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 */
	public void estableceLibroOrigen(String rutaLibroOrigen) throws FileNotFoundException, DocumentException {
		libroOrigenFichero = new File(rutaLibroOrigen);

		try {
			libroOrigenDoc = analizaXMLDocBook(new FileInputStream(libroOrigenFichero));
		} catch (FileNotFoundException fnfe) {
			BITÁCORA.log(Level.SEVERE, "Fichero de libro en el idioma de origen no encontrado: ", fnfe);
			fnfe.printStackTrace();
			throw fnfe;
		} catch (DocumentException de) {
			BITÁCORA.log(Level.SEVERE, "Error al efectuar en análisis XML del libro en el idioma de origen: ", de);
			de.printStackTrace();
			throw de;
		}
	}

	/**
	 * Establecimiento del libro de destino: el escrito en el idioma al que se
	 * traduce.
	 * 
	 * @param rutaLibroDestino
	 *            Ruta donde está ubicado el libro de destino.
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 */
	public void estableceLibroDestino(String rutaLibroDestino) throws FileNotFoundException, DocumentException {
		libroDestinoFichero = new File(rutaLibroDestino);

		try {
			libroDestinoDoc = analizaXMLDocBook(new FileInputStream(libroDestinoFichero));
		} catch (FileNotFoundException fnfe) {
			BITÁCORA.log(Level.SEVERE, "Fichero de libro en el idioma de destino no encontrado: ", fnfe);
			fnfe.printStackTrace();
			throw fnfe;
		} catch (DocumentException de) {
			BITÁCORA.log(Level.SEVERE, "Error al efectuar en análisis XML del libro en el idioma de destino: ", de);
			de.printStackTrace();
			throw de;
		}
	}

	/**
	 * Establecimiento del fichero PO, de traducción del idioma de origen al de
	 * destino.
	 * 
	 * @param rutaFicheroPO
	 *            Ruta donde será ubicado el fichero PO.
	 */
	public void estableceFicheroPO(String rutaFicheroPO) {
		ficheroPO = new File(rutaFicheroPO);
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
	 * @param elementoRaízSubárbol
	 *            Nodo elemento raíz del subárbol.
	 * @throws NoSuchAlgorithmException
	 */
	public void traduceSubárbol(PrintWriter escritorPO, Element elementoRaízSubárbol) throws NoSuchAlgorithmException {
		Node nodoDestino = null;

		// Navegación por las ramas del subárbol.
		for (Node nodoOrigen : elementoRaízSubárbol.content()) {
			// Comprobación de nodos que requieren traducción.
			if (nodoOrigen.getNodeType() == Node.ELEMENT_NODE
					&& (nodoOrigen.getName().equals("para") || nodoOrigen.getName().endsWith("title")
							|| nodoOrigen.getName().equals("author") || nodoOrigen.getName().equals("biblioentry"))) {
				escritorPO.println(config.getProperty("po.cuerpo.línea0"));
				escritorPO.println(String.format(config.getProperty("po.cuerpo.línea1"),
						expresaRutaComoNodos(xPathSinNS(nodoOrigen.getPath()))));
				escritorPO.println(String.format(config.getProperty("po.cuerpo.línea2"), libroOrigenFichero.getName(),
						((NumberedSAXReader.LocationAwareElement) nodoOrigen).getLineNumber()));

				nodoDestino = libroDestinoDoc.selectSingleNode(nodoOrigen.getUniquePath());

				String textoNodoOrigen = compactaCadenaXML(nodoOrigen.asXML());
				if (!textoNodoOrigen.endsWith("/>")) {
					textoNodoOrigen = textoNodoOrigen
							.substring(textoNodoOrigen.indexOf(">") + 1, textoNodoOrigen.lastIndexOf("<")).trim();
				} else {
					textoNodoOrigen = TEXTO_NODO_NULO;
				}

				String textoNodoDestino = null;
				if (nodoDestino != null) {
					textoNodoDestino = compactaCadenaXML(nodoDestino.asXML());
					if (!textoNodoDestino.endsWith("/>")) {
						textoNodoDestino = textoNodoDestino
								.substring(textoNodoDestino.indexOf(">") + 1, textoNodoDestino.lastIndexOf("<")).trim();
					} else {
						textoNodoDestino = TEXTO_NODO_NULO;
					}
				} else {
					textoNodoDestino = TEXTO_NODO_SIN_TRADUCCIÓN;
				}

				String[] listaFrasesOrigen = particionador.obténFrases(textoNodoOrigen),
						listaFrasesDestino = particionador.obténFrases(textoNodoDestino);

				for (int j = 0; j < listaFrasesOrigen.length; j++) {
					String fraseOrigen = listaFrasesOrigen[j], md5FraseOrigen = obténCódigoMD5(fraseOrigen);

					int númRepeticiones = obténRepeticionesDeCadena(md5FraseOrigen);
					if (númRepeticiones > 1) {
						escritorPO.println(String.format(config.getProperty("po.cuerpo.línea.contexto"),
								md5FraseOrigen + "-" + númRepeticiones));
					}

					escritorPO.println(String.format(config.getProperty("po.cuerpo.línea3"), listaFrasesOrigen[j]));
					try {
						escritorPO
								.println(String.format(config.getProperty("po.cuerpo.línea4"), listaFrasesDestino[j]));
					} catch (IndexOutOfBoundsException ioobe) {
						escritorPO.println(
								String.format(config.getProperty("po.cuerpo.línea4"), TEXTO_NODO_SIN_TRADUCCIÓN));
					}
				}

				continue; // No escaneamos sub-nodos de los nodos traducibles.
			}

			if (nodoOrigen instanceof Element) {
				traduceSubárbol(escritorPO, (Element) nodoOrigen);
			}
		}
	}

	/**
	 * Análisis de ficheros XML contenedores de libros esquematizados según el
	 * estándar DocBook; y generación de una representación de la estructura de
	 * nodos que se dan en los mismos, en ficheros con el mismo nombre que los
	 * originales, pero con extensión "txt".
	 * 
	 * @param escritorFE
	 *            Escritor del fichero donde escribir la estructura de nodos.
	 * @param elementoRaízSubárbol
	 *            Nodo elemento raíz del subárbol.
	 * @param numera
	 *            ¿Se quiere mostrar los números de línea donde aparecen los nodos?
	 * @throws NoSuchAlgorithmException
	 */
	public void muestraEstructuraSubárbol(PrintWriter escritorFE, Element elementoRaízSubárbol, boolean numera) {
		// Navegación por las ramas del subárbol.
		for (Node nodoOrigen : elementoRaízSubárbol.content()) {
			// Comprobación de nodos que requieren traducción.
			if (nodoOrigen.getNodeType() == Node.ELEMENT_NODE
					&& (nodoOrigen.getName().equals("para") || nodoOrigen.getName().endsWith("title")
							|| nodoOrigen.getName().equals("author") || nodoOrigen.getName().equals("biblioentry"))) {
				escritorFE.println(expresaRutaComoNodos(xPathSinNS(nodoOrigen.getPath()))
						+ (numera ? ":" + ((NumberedSAXReader.LocationAwareElement) nodoOrigen).getLineNumber() : ""));
				continue; // No escaneamos sub-nodos de los nodos traducibles.
			}

			if (nodoOrigen instanceof Element) {
				muestraEstructuraSubárbol(escritorFE, (Element) nodoOrigen, numera);
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
		Node nodo = libro.selectSingleNode(xPathConNS(xPathElemento));
		return nodo != null ? nodo.getText() : "<nulo>";
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
	 * Obtención del código MD5 correspondiente a una cadena de texto.
	 * 
	 * @param cadena
	 *            Cadena de texto semilla para generar el código MD5.
	 * @return Código MD5 correspondiente a la cadena de texto pasada como
	 *         parámetro.
	 * @throws NoSuchAlgorithmException
	 */
	private String obténCódigoMD5(String cadena) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(cadena.getBytes());
		byte[] digestión = md.digest();
		BigInteger bi = new BigInteger(1, digestión);
		return bi.toString(16);
	}

	/**
	 * Obtención del número de repeticiones con las que aparece una cadena dada,
	 * dentro de un mapa de cadenas mantenida por la clase.
	 * 
	 * @param cadena
	 *            Cadena de la que se quiere obtener el número de repeticiones.
	 * @return Número de repeticiones de la cadena dentro del mapa mantenido por la
	 *         clase.
	 */
	private int obténRepeticionesDeCadena(String cadena) {
		MutableInteger mi = mapaCadenas.get(cadena);
		if (mi == null) {
			mi = new MutableInteger(0);
			mapaCadenas.put(cadena, mi);
		}
		return mi.inc();
	}

	/**
	 * Generación del fichero de traducción PO.
	 */
	public void generaFicheroPO() {
		PrintWriter escritorPO = null;

		try {
			particionador.setCaracDelimitadores(".?!<()");
			escritorPO = new PrintWriter(ficheroPO, "UTF-8");

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
			 * Creación de las líneas descriptoras de los contenidos de libro y su
			 * traducción.
			 */
			for (Element nodoPrimerNivel : libroOrigenDoc.getRootElement().elements()) {
				this.traduceSubárbol(escritorPO, nodoPrimerNivel);
			}

		} catch (FileNotFoundException fnfe) {
			BITÁCORA.log(Level.SEVERE, "Error al intentar escribir en el fichero PO: ", fnfe);
			fnfe.printStackTrace();
		} catch (UnsupportedEncodingException uee) {
			BITÁCORA.log(Level.SEVERE,
					"La codificación de caracteres, especificada para el fichero PO, no está soportada: ", uee);
			uee.printStackTrace();
		} catch (NoSuchAlgorithmException nsae) {
			BITÁCORA.log(Level.SEVERE, "Error de generación de códigos MD5 para procesado de documentos: ", nsae);
			nsae.printStackTrace();
		} finally {
			escritorPO.close();
		}
	}

	/**
	 * Generación del los ficheros descriptores de estructura de los de traducción.
	 *
	 * @param numera
	 *            ¿Se quiere mostrar los números de línea donde aparecen los nodos?
	 */
	public void generaFicherosEstructura(boolean numera) {
		PrintWriter escritorEstructura = null;

		try {
			escritorEstructura = new PrintWriter(libroOrigenFichero.getCanonicalPath().split("\\.")[0] + ".txt",
					"UTF-8");
			for (Element nodoPrimerNivel : libroOrigenDoc.getRootElement().elements()) {
				this.muestraEstructuraSubárbol(escritorEstructura, nodoPrimerNivel, numera);
			}
			escritorEstructura.close();

			escritorEstructura = new PrintWriter(libroDestinoFichero.getCanonicalPath().split("\\.")[0] + ".txt",
					"UTF-8");
			for (Element nodoPrimerNivel : libroDestinoDoc.getRootElement().elements()) {
				this.muestraEstructuraSubárbol(escritorEstructura, nodoPrimerNivel, numera);
			}
		} catch (IOException ioe) {
			BITÁCORA.log(Level.SEVERE, "Error de lectura/escritura al intentar abrir el fichero de estructura: ", ioe);
			ioe.printStackTrace();
		} finally {
			escritorEstructura.close();
		}
	}
}
