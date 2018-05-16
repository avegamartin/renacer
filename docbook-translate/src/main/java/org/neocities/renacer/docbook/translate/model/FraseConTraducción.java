package org.neocities.renacer.docbook.translate.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Frase con traducción, que constituyen un componente de un nodo traducible de
 * un libro estructurado según el estándar DocBook v5.x.
 * 
 * @author avega
 */
public class FraseConTraducción {

	private SimpleStringProperty fraseOrigen;
	private SimpleStringProperty fraseDestino;

	/**
	 * Constructor con los datos iniciales del modelo.
	 * 
	 * @param fraseOrigen
	 *            Frase en el idioma de origen.
	 * @param fraseDestino
	 *            Frase en el idioma de destino.
	 */
	public FraseConTraducción(String fraseOrigen, String fraseDestino) {
		this.fraseOrigen = new SimpleStringProperty(fraseOrigen);
		this.fraseDestino = new SimpleStringProperty(fraseDestino);
	}

	/**
	 * @return the fraseOrigen
	 */
	public String getFraseOrigen() {
		return fraseOrigen.get();
	}

	/**
	 * @param fraseOrigen
	 *            the fraseOrigen to set
	 */
	public void setFraseOrigen(String fraseOrigen) {
		this.fraseOrigen.set(fraseOrigen);
	}

	/**
	 * @return the fraseDestino
	 */
	public String getFraseDestino() {
		return fraseDestino.get();
	}

	/**
	 * @param fraseDestino
	 *            the fraseDestino to set
	 */
	public void setFraseDestino(String fraseDestino) {
		this.fraseDestino.set(fraseDestino);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fraseOrigen.get() + " -> " + fraseDestino.get();
	}
}
