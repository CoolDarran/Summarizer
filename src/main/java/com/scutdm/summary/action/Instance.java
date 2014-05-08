package com.scutdm.summary.action;

import edu.mit.jwi.IDictionary;
import edu.sussex.nlp.jws.JWS;

public class Instance {
	public static final String dir = "./WordNet";
	public static final JWS    WS = new JWS(dir, "2.1");
	public static IDictionary dict;
	public static IDictionary getDict() {
		return dict; 
	}
	public static void setDict(IDictionary dict) {
		Instance.dict = dict;
	}
}
