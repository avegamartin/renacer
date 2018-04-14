package org.neocities.renacer.docbook.translate;

import java.io.FileNotFoundException;
import java.util.logging.Level;

import org.dom4j.DocumentException;
import org.neocities.renacer.docbook.translate.model.TraducciónDocBook;

import junit.framework.TestCase;

/**
 * Clase testeadora de la de traducción de documentos contenedores de libros.
 * 
 * @author avega
 */
public class TraducciónDocBookTest extends TestCase {

	/**
	 * @param name
	 */
	public TraducciónDocBookTest(String name) {
		super(name);
	}

	/**
	 * Test method for
	 * {@link org.neocities.renacer.docbook.translate.model.TraducciónDocBook#analizaXMLDocBook(java.net.URL)}.
	 */
	public void testAnalizaXMLDocBook() {
		TraducciónDocBook traducción = new TraducciónDocBook();
		try {
			traducción.estableceLibroOrigen("src/test/resources/examples/book-en_GB.xml");
			traducción.estableceLibroDestino("src/test/resources/examples/book-es_ES.xml");
			traducción.estableceFicheroPO("src/test/resources/examples/fichero.po");
			assertEquals(traducción.getLibroOrigenDoc().getXMLEncoding(), "UTF-8");
			assertEquals("Excellence in Theological Educational", traducción.obténTítuloLibro(traducción.getLibroOrigenDoc()));
			traducción.generaFicheroPO();
		} catch (FileNotFoundException | DocumentException e) {
			System.err.println("Abortando el proceso de generación del fichero PO...");
			System.exit(1);
		}
	}
}
