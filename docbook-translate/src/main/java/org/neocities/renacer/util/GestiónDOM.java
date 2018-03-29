package org.neocities.renacer.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Utilidades para la gestión de árboles DOM.
 * 
 * @author avega
 */
public final class GestiónDOM {
	
	private GestiónDOM() {
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
	public static String obténTextoContenidoNodo(Document libro, String xPathElemento) {
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
	public static String obténXMLContenidoNodo(Document libro, String xPathElemento) {
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
	public static String xPathConNS(String xPathOriginal) {
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
	public static String xPathSinNS(String xPathOriginal) {
		return xPathOriginal.replaceAll("\\*\\[name\\(\\)='", "").replaceAll("'\\]", "");
	}

	/**
	 * Compactación de una cadena XML, eliminando saltos de línea y tabuladores.
	 * 
	 * @param cadenaXMLOriginal
	 *            Cadena XML original a compactar.
	 * @return Resultado de la compactación.
	 */
	public static String compactaCadenaXML(String cadenaXMLOriginal) {
		return cadenaXMLOriginal.replaceAll("\n|\r|\t", "");
	}

	/**
	 * Conversión de una expresión XPath para buscar elementos XML, en una secuencia
	 * de nodos en notación de elementos XML clásica.
	 * 
	 * @param xPathOriginal
	 *            Expresión XPath de dirección de un elemento XML.
	 * @return Ruta XPath convertida en una secuencia de nodos (elementos) XML.
	 */
	public static String expresaRutaComoNodos(String xPathOriginal) {
		StringBuilder sb = new StringBuilder(64);

		for (String nodo : xPathOriginal.split("/")) {
			if (nodo.length() == 0)
				continue;
			sb.append("<").append(nodo).append(">");
		}

		return sb.toString();
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
	public static String obténCódigoMD5(String cadena) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(cadena.getBytes());
		byte[] digestión = md.digest();
		BigInteger bi = new BigInteger(1, digestión);
		return bi.toString(16);
	}
}
