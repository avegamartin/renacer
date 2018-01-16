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
		traducción.estableceLibroOrigen("src/test/resources/examples/book-en_GB.xml");
		traducción.estableceLibroDestino("src/test/resources/examples/book-es_ES.xml");
		assertEquals(traducción.getLibroOrigenDoc().getXMLEncoding(), "UTF-8");
		assertEquals(traducción.obténTítuloLibro(traducción.getLibroOrigenDoc()), "Excellence in Theological Educational");
		traducción.generaFicheroPO();
	}
}
