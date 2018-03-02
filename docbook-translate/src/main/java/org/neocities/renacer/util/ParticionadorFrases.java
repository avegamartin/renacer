package org.neocities.renacer.util;

import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Clase particionadora de cadenas de texto según tokens delimitadores,
 * posibilitando el tener dos niveles de prioridad.
 * 
 * @author avega
 */
public class ParticionadorFrases {

	private final short LONGITUD_MÍNIMA_FRASE = 5;
	private String caracDelimitadores = null;

	/**
	 * @return Conjunto de caracteres que consideramos delimitan frases.
	 */
	public String getCaracDelimitadores() {
		return caracDelimitadores;
	}

	/**
	 * @param caracDelimitadores
	 *            Conjunto de caracteres que consideramos delimitan frases.
	 */
	public void setCaracDelimitadores(String caracDelimitadores) {
		this.caracDelimitadores = caracDelimitadores;
	}

	/**
	 * Método para el particionado de una cadena de texto en sus frases componentes.
	 * <p>
	 * Si en el conjunto de delimitadores se dan paréntesis, o la apertura de
	 * etiquetas XML, se estipula que toda la cadena encerrada entre dichos
	 * delimitadores sea considerada una sola frase, aunque en su interior se dé
	 * algún otro delimitador.
	 * 
	 * @param cadenaOriginal
	 *            Cadena original que se pretende particionar en sus frases
	 *            componentes.
	 * @return Conjunto de frases en las que se particiona la cadena original,
	 *         incluidos los delimitadores.
	 */
	public String[] obténFrases(String cadenaOriginal) {
		StringTokenizer st = new StringTokenizer(cadenaOriginal, caracDelimitadores, true);
		LinkedList<String> listaFrases = new LinkedList<String>();
		String delimitador = "<nulo>", delimitadorAnterior = "<nulo>";
		int nivelesDelimXML = 0; // Niveles de delimitadores de etiquetas XML
		boolean enÁmbitoParéntesis = false, seHaCerradoEtiqueta = false;

		listaFrases.clear();

		// Descomposición de elementos traducibles en las frases que los componen.
		while (st.hasMoreTokens()) {
			String frase = st.nextToken();
			if (caracDelimitadores.indexOf(frase) == -1) {
				delimitador = st.hasMoreTokens() ? st.nextToken() : "";
				frase += delimitador;
			} else {
				delimitador = frase; // Dos delimitadores seguidos
			}

			if (frase.startsWith("/") && delimitadorAnterior.equals("<")) { // Cierre de etiqueta XML
				nivelesDelimXML--;
				seHaCerradoEtiqueta = true;
			} else {
				seHaCerradoEtiqueta = false;
			}

			/*
			 * Aplicación de criterios para decidir si concatenar la frase actual a la
			 * anterior: 1. Frase dentro de un par de etiquetas XML; 2. frase entre
			 * paréntesis; 3. frase actual demasiado corta; 4. frase anterior demasiado
			 * corta.
			 */
			if (listaFrases.size() > 0 && (nivelesDelimXML > 0 || enÁmbitoParéntesis || delimitadorAnterior.equals(")")
					|| frase.length() < LONGITUD_MÍNIMA_FRASE
					|| listaFrases.getLast().length() < LONGITUD_MÍNIMA_FRASE)) {
				listaFrases.addLast(listaFrases.removeLast() + frase);
				if (seHaCerradoEtiqueta)
					nivelesDelimXML--;
			} else {
				listaFrases.addLast(frase);
			}

			switch (delimitador) {
				case "<":
					nivelesDelimXML++;
					break;
				case "(":
					enÁmbitoParéntesis = true;
					break;
				case ")":
					enÁmbitoParéntesis = false;
					break;
			}

			delimitadorAnterior = delimitador;
		}

		return listaFrases.toArray(new String[0]);
	}
}
