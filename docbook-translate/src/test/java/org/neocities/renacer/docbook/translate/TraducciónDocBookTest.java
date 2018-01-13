/**
 * 
 */
package org.neocities.renacer.docbook.translate;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import junit.framework.TestCase;

/**
 * @author antonio
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
		assertEquals(traducción.getLibroOrigenDoc().getXMLEncoding(), "UTF-8");
		assertEquals(traducción.obténTítuloLibro(traducción.getLibroOrigenDoc()), "Excellence in Theological Educational");
		traducción.generaFicheroPO();
	}
}
