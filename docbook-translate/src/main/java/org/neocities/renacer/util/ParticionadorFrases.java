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

	private String[] delimPrincipales = null;
	private String caracDelimSecundarios = null;

	private final short LONGITUD_MÍNIMA_FRASE = 5;

	/**
	 * @return the delimPrincipales
	 */
	public String[] getDelimPrincipales() {
		return delimPrincipales;
	}

	/**
	 * @param delimPrincipales
	 *            Delimitadores principales a establecer, por pares de apertura y
	 *            cierre.
	 */
	public void setDelimPrincipales(String[] delimPrincipales) throws IllegalArgumentException {
		this.delimPrincipales = delimPrincipales;

		if ((this.delimPrincipales.length % 2) != 0)
			throw new IllegalArgumentException("Los delimitadores principales han de ser especificados por pares.");
	}

	/**
	 * @return the caracDelimSecundarios
	 */
	public String getCaracDelimSecundarios() {
		return caracDelimSecundarios;
	}

	/**
	 * @param caracDelimSecundarios
	 *            Cadena de caracteres con aquellos que constituyen el conjunto de
	 *            los secundarios.
	 */
	public void setCaracDelimSecundarios(String caracDelimSecundarios) {
		this.caracDelimSecundarios = caracDelimSecundarios;
	}

	/**
	 * Método para el particionado de una cadena de texto, en sus frases
	 * componentes, según una jerarquía de delimitadores de dos niveles:
	 * <p>
	 * <ol>
	 * <li>Delimitadores principales: vector de cadenas de texto que, cuando se dan
	 * en la cadena particionándose, delimitan una frase completa, no
	 * interpretándose los delimitadores secundarios dentro de ellas. Se espera que
	 * las cadenas delimitadoras vengan especificadas por pares; la cadena de
	 * apertura seguida de la cadena de cierre.</li>
	 * <li>Caracteres delimitadores secundarios: conjunto de caracteres que, si
	 * cualquiera de ellos se da dentro de la cadena particionándose, delimitan una
	 * frase completa; a menos que dichos caracteres se den dentro de una subcadena
	 * confinada entre delimitadores principales.</li>
	 * </ol>
	 * 
	 * @param cadenaOriginal
	 *            Cadena original que se pretende particionar en sus frases
	 *            componentes.
	 * @return Conjunto de frases en las que se particiona la cadena original,
	 *         incluidos los delimitadores.
	 */
	public String[] obténFrases(String cadenaOriginal) {
		StringBuilder sb = new StringBuilder(cadenaOriginal);
		StringTokenizer stSecundarios = null;
		LinkedList<String> listaFrases = new LinkedList<String>();
		int posCurAnálisis = 0; // Posición del cursor de análisis

		listaFrases.clear();

		while (sb.length() > 0) {
			int idxDelimPrincipal;
			for (idxDelimPrincipal = 0; idxDelimPrincipal < delimPrincipales.length - 1; idxDelimPrincipal += 2) {
				posCurAnálisis = sb.indexOf(delimPrincipales[idxDelimPrincipal]);
				if (posCurAnálisis != -1)
					break;
			}

			if (posCurAnálisis == -1)
				posCurAnálisis = sb.length(); // Cadena estándar, sin delimintadores principales

			if (posCurAnálisis > 0) {
				stSecundarios = new StringTokenizer(cadenaOriginal.substring(0, posCurAnálisis), caracDelimSecundarios,
						true);

				/*
				 * Particionado de frase estándar
				 */
				while (stSecundarios.hasMoreTokens()) {
					String token = stSecundarios.nextToken() // Incluido token separador
							+ (stSecundarios.hasMoreTokens() ? stSecundarios.nextToken() : "");

					/*
					 * Si la frase es demasiado corta para ser considerada por separado; se
					 * concatena a la anterior.
					 */
					if (token.length() < LONGITUD_MÍNIMA_FRASE && listaFrases.size() > 0) {
						listaFrases.addLast(listaFrases.removeLast() + token);
					} else {
						listaFrases.addLast(token);
					}
				}

				sb.delete(0, posCurAnálisis);
			}

			if (idxDelimPrincipal < delimPrincipales.length) { // Se dio un delimitador especial
				int inicioCierre = sb.indexOf(delimPrincipales[idxDelimPrincipal + 1]); // Segundo miembro del par
				if (inicioCierre == -1)
					throw new IndexOutOfBoundsException("La subcadena no está correctamente delimitada: falta cierre.");
				listaFrases.addLast(sb.substring(0, inicioCierre + delimPrincipales[idxDelimPrincipal + 1].length()));
				sb.delete(0, inicioCierre + delimPrincipales[idxDelimPrincipal + 1].length());
			}
		}

		return listaFrases.toArray(new String[0]);
	}
}
