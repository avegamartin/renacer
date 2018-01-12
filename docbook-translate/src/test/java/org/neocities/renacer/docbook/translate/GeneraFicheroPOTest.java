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
public class GeneraFicheroPOTest extends TestCase {

	/**
	 * @param name
	 */
	public GeneraFicheroPOTest(String name) {
		super(name);
	}

	/**
	 * Test method for
	 * {@link org.neocities.renacer.docbook.translate.GeneraFicheroPO#analizaXMLDocBook(java.net.URL)}.
	 */
	public void testAnalizaXMLDocBook() {
		GeneraFicheroPO generador = new GeneraFicheroPO();
		try {
			Document libro = generador.analizaXMLDocBook(
					generador.getClass().getClassLoader().getResourceAsStream("examples/book-en_GB.xml"));
			assertEquals(libro.getXMLEncoding(), "UTF-8");
			generador.iteraElementos(libro.getRootElement());
			System.out.println("Título de libro: " + generador.obténTítuloLibroOrigen());
		} catch (DocumentException e) {
			e.printStackTrace();
			assertTrue("Se ha producido una excepción al procesar el fichero:\n" + e, false);
		}
	}
}
