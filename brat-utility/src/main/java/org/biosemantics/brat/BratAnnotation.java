/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biosemantics.brat;

import java.io.Serializable;

/**
 * 
 * @author bhsingh
 */
public interface BratAnnotation extends Serializable {
	BratAnnotationType getType();

	String getEntity();

	String getId();
	
	String getFileName();
	
	String[] toStringArray();
}
