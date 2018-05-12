package org.neocities.renacer.docbook.translate;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.DocumentException;
import org.neocities.renacer.docbook.translate.model.TraducciónDocBook;

/**
 * Interfaz de Línea de Comandos del artefacto de generación de ficheros de descripción,
 * de traducción, de libros estructurados según el estándar DocBook v5.x.
 *  
 * @author avega
 */
public class TraducciónDocBookCLI {

	private final static Logger BITÁCORA = Logger.getLogger(TraducciónDocBookCLI.class.getName());

	/**
	 * @param args
	 *            Lista de ficheros XML, contenedores de libros, a analizar: libro en el
	 *            idioma de origen y en el de destino; fichero de traducción; solicitud de
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
}
