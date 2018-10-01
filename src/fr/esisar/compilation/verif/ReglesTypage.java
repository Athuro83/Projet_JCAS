package fr.esisar.compilation.verif;

import fr.esisar.compilation.global.src.*;

/**
 * La classe ReglesTypage permet de définir les différentes règles 
 * de typage du langage JCas.
 */

public class ReglesTypage {

   /**
    * Teste si le type t1 et le type t2 sont compatibles pour l'affectation, 
    * c'est à dire si on peut affecter un objet de t2 à un objet de type t1.
    */

   static ResultatAffectCompatible affectCompatible(Type t1, Type t2) {
	   
		ResultatAffectCompatible result = new ResultatAffectCompatible();
		/* On assume par défaut que la conversion n'est pas nécessaire */
		result.setConv2(false);
	   
		/* Etude du type t1 */
		switch(t1.getNature()){
	   
	   		case Boolean:
	   			/* Seul un booléen peut être assigné à une variable booléenne */
	   			if(t2.getNature() == NatureType.Boolean){
	   				result.setOk(true);
	   			}
	   			else{
	   				result.setOk(false);
	   			}
	   			break;
	   			
	   		case Real:
	   			/* On peut affecter un réel à une variable réelle */
	   			if(t2.getNature() == NatureType.Real){
	   				result.setOk(true);
	   			}
	   			/* On peut affecter un intervalle à une variable réelle */
	   			else if(t2.getNature() == NatureType.Interval){
	   				result.setOk(true);
	   				/* Mais il faut convertir */
	   				result.setConv2(true);
	   			}
	   			break;
	   		
	   		case Interval:
	   			/* Seul un intervalle peut être affecté à une variable de type intervalle */
	   			if(t2.getNature() == NatureType.Interval){
	   				result.setOk(true);
	   			}
	   			else{
	   				result.setOk(false);
	   			}
	   			break;
	   			
	   		case Array:
	   			/* Seul un tableau peut être assigné à une variable de type tableau */
	   			if(t2.getNature() == NatureType.Array){
	   				Type i1 = t1.getIndice();
	   				Type i2 = t2.getIndice();
	   				/* On vérifie que les indices sont de type intervalle, avec les même bornes */
	   				if( i1.getNature() == NatureType.Interval && i2.getNature() == NatureType.Interval &&
	   					i1.getBorneInf() == i2.getBorneInf() && i1.getBorneSup() == i2.getBorneSup()){
	   					/* On vérifie que les éléments sont bien compatibles pour l'affectation */
	   					result = ReglesTypage.affectCompatible(t1.getElement(), t2.getElement());
	   				}
	   				else{
	   					result.setOk(false);
	   				}
	   			}
	   			else{
	   				result.setOk(false);
	   			}
	   			break;
	   			
	   		case String:
	   			/* L'affectation de chaîne de caractères est interdite en JCas */
	   			result.setOk(false);
	   			break;
		}
	   
		return result; 
}

   /**
    * Teste si le type t1 et le type t2 sont compatible pour l'opération 
    * binaire représentée dans noeud.
    */

   static ResultatBinaireCompatible binaireCompatible
      (Noeud noeud, Type t1, Type t2) {
      return null;
   }

   /**
    * Teste si le type t est compatible pour l'opération binaire représentée 
    * dans noeud.
    */
   static ResultatUnaireCompatible unaireCompatible
         (Noeud noeud, Type t) {
      return null;
   }
         
}

