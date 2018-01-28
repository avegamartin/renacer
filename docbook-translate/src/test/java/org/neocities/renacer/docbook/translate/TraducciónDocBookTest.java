/**
 * 
 */
package org.neocities.renacer.docbook.translate;

import junit.framework.TestCase;

/**
 * @author avega
 *
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
	 * {@link org.neocities.renacer.docbook.translate.TraducciónDocBook#analizaXMLDocBook(java.net.URL)}.
	 */
	public void testAnalizaXMLDocBook() {
		TraducciónDocBook traducción = new TraducciónDocBook();
		traducción.estableceLibroOrigen("src/test/resources/examples/book-en-2_GB.xml");
		traducción.estableceLibroDestino("src/test/resources/examples/book-es-2_ES.xml");
		assertEquals(traducción.getLibroOrigenDoc().getXMLEncoding(), "UTF-8");
		assertEquals("Leadership in Theological Education Volume 1", traducción.obténTítuloLibro(traducción.getLibroOrigenDoc()));
		traducción.generaFicheroPO();
	}
}
