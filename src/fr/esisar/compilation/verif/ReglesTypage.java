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
	   				
	   				//result.setOk(false);
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
	   
	   ResultatBinaireCompatible result = new ResultatBinaireCompatible();
	   /* On assume par défaut que la conversion n'est pas nécessaire */
	   result.setConv1(false);
	   result.setConv2(false);
	   
	   	switch(noeud){
	   		
	   		case Et:
	   		case Ou:
	   			/* Résultat de type booléen si les deux opérandes sont booléens */
	   			if(t1.getNature() != NatureType.Boolean){
	   				/* ERREUR : Op1 invalide */
	   				result.setOk(false);
	   				result.setConv1(true);
	   				break;
	   			}
	   			else if(t2.getNature() != NatureType.Boolean){
	   				/* ERREUR : Op2 invalide */
	   				result.setOk(false);
	   				result.setConv2(true);
	   			}
	   			
	   			result.setOk(true);
	   			result.setTypeRes(Type.Boolean);
	   			break;
	   
	   		case Egal:
	   		case Inf:
	   		case Sup:
	   		case NonEgal:
	   		case SupEgal:
	   		case InfEgal:
	   			/* Le résultat est booléen */
				result.setTypeRes(Type.Boolean);

	   			/* On commence par examiner le type du premier opérande */
	   			switch(t1.getNature()){
	   			
	   				case Interval:
	   					/* Types autorisés pour l'opérande 2 : Interval ou Real */
	   					switch(t2.getNature()){
	   					
	   						case Real:
	   							/* Conversion nécessaire de l'Interval en Real */
	   							result.setConv1(true);
		   					case Interval:
		   						result.setOk(true);
		   						break;
		   						
	   						default:
	   							/* ERREUR : Op2 invalide */
	   							result.setOk(false);
	   							result.setConv2(true);
	   					}
	   					break;
	   					
	   				case Real:
	   					/* Types autorisés pour l'opérande 2 : Interval ou Real */
	   					switch(t2.getNature()){
	   					
	   						case Interval:
	   							/* Conversion nécessaire de l'Interval en Real */
	   							result.setConv2(true);
		   					case Real:
		   						result.setOk(true);
		   						break;
		   						
	   						default:
	   							/* ERREUR : Op2 invalide */
	   							result.setOk(false);
	   							result.setConv2(true);
	   					}
	   					break;
	   					
	   				default:
	   					/* ERREUR : Op1 invalide */
	   					result.setOk(false);
	   					result.setConv1(true);
	   			}
	   			break;
	   			
	   		case Plus:
	   		case Moins:
	   		case Mult:
	   			/* On commence par examiner le type du premier opérande */
	   			switch(t1.getNature()){
	   				
	   				case Interval:
	   					/* Types autorisés pour l'opérande 2 : Interval ou Real */
	   					switch(t2.getNature()){
	   						
		   					case Interval:
		   						/* Les deux opérandes sont de type Interval : Sortie de type Interval */
		   						result.setOk(true);
		   						result.setTypeRes(Type.Integer);
		   						break;
		   						
		   					case Real:
		   						/* Conversion de l'opérande 1 nécessaire */
		   						result.setConv1(true);
		   						result.setOk(true);
		   						result.setTypeRes(Type.Real);
		   						break;
		   						
		   					default:
		   						/* ERREUR : Op2 invalide */
		   						result.setOk(false);
		   						result.setConv2(true);
	   					}
	   					break;
	   					
	   				case Real:
	   					/* Types autorisés pour l'opérande 2 : Interval ou Real */
	   					switch(t2.getNature()){
	   						
		   					case Interval:
		   						/* Conversion de l'opérande 2 nécessaire */
		   						result.setConv2(true);
		   					case Real:
		   						result.setOk(true);
		   						result.setTypeRes(Type.Real);
		   						break;
		   						
		   					default:
		   						/* ERREUR : Op2 invalide */
		   						result.setOk(false);
		   						result.setConv2(true);
	   					}
	   					break;
	   					
	   				default:
	   					/* ERREUR : Op1 invalide */
	   					result.setOk(false);
	   					result.setConv1(true);
	   			}
	   			break;
	   			
	   		case Quotient:
	   		case Reste:
	   			/* Opérations entre opérandes de type Interval seulement */
	   			if(t1.getNature() != NatureType.Interval){
	   				/* ERREUR : Op1 invalide */
	   				result.setOk(false);
	   				result.setConv1(true);
	   				break;
	   			}
	   			else if(t2.getNature() != NatureType.Interval){
	   				/* ERREUR : Op2 invalide */
	   				result.setOk(false);
	   				result.setConv2(true);
	   				break;
	   			}
	   			
	   			result.setOk(true);
	   			result.setTypeRes(Type.Integer);
	   			break;
	   			
	   		case DivReel:
	   			/* Le résultat est un réel */
	   			result.setTypeRes(Type.Real);
	   			
	   			/* On commence par examiner le type du premier opérande */
	   			switch(t1.getNature()) {
	   			
	   				case Interval:
	   					/* On examine le type de l'opérande 2 */
	   					switch(t2.getNature()) {
	   					  							
	   						case Real:
	   							/* Conversion de l'opérande 1 nécessaire */
	   							result.setConv1(true);
	   						case Interval:
	   							result.setOk(true);
	   							break;
	   							
	   						default:
	   							/* Erreur sur l'opérande 2 */
	   							result.setOk(false);
	   							result.setConv2(true);
	   					}
	   					break;
	   					
	   				case Real:
	   					/* On examine le type de l'opérande 2 */
	   					switch(t2.getNature()) {
	   					
	   						case Interval:
	   							/* Conversion de l'opérande 2 nécessaire */
	   							result.setConv2(true);
	   						case Real:
	   							result.setOk(true);
	   							break;
	   							
	   						default:
	   							/* Erreur sur l'opérande 2 */
	   							result.setOk(false);
	   							result.setConv2(true);
	   					}

	   					break;
	   					
	   				default:
	   					/* Erreur sur l'opérande 1 */
	   					result.setOk(false);
	   					result.setConv1(true);
	   			}
	   			break;
	   			
	   		case Index:
	   			/* Cas de l'indexation d'un tableau */
	   			if(t1.getNature() != NatureType.Array || t1.getIndice().getNature() != NatureType.Interval) {
	   				/* Erreur sur l'opérande 1 */
	   				result.setOk(false);
	   				result.setConv1(true);
	   			}
	   			else {
	   				if(t2.getNature() != NatureType.Interval) {
	   					/* Erreur sur l'opérande 2 */
	   					result.setOk(false);
	   					result.setConv2(true);
	   				}
	   				else {
	   					/* Indexation correcte : le type du résultat correspond au type du tableau */
	   					result.setOk(true);
	   					result.setTypeRes(t1.getElement());
	   				}
	   			}
	   			break;
	   			
	   		default:
	   			System.out.println("Unknow node");
	   	}
	   	
	   	return result;
   }

   /**
    * Teste si le type t est compatible pour l'opération binaire représentée 
    * dans noeud.
    */
   static ResultatUnaireCompatible unaireCompatible
         (Noeud noeud, Type t) {
	   
	   	ResultatUnaireCompatible result = new ResultatUnaireCompatible();

	   	/* On commence par identifier le type du Noeud */
	   	switch(noeud) {
	  	
	   		case Non:
	   			/* Le résultat est de type booléen */
	   			result.setTypeRes(Type.Boolean);
	   			/* L'opérande doit être de type booléen */
	   			if(t.getNature() == NatureType.Boolean) {
	   				result.setOk(true);
	   			}
	   			else {
	   				/* Erreur sur le type de l'opérande */
	   			}
	   			break;
	   			
	   		case PlusUnaire:
	   		case MoinsUnaire:
	   			/* On examine le type de l'opérande */
	   			switch(t.getNature()) {
	   			
	   				case Interval:
	   					/* Le résultat est de type booléen */
	   					result.setTypeRes(Type.Integer);
	   				case Real:
	   					/* Le résultat est de type réel */
	   					result.setTypeRes(Type.Real);
	   					result.setOk(true);
	   					break;
	   					
	   				default:
	   					/* Erreur sur le type de l'opérande */
	   					
	   			}
	   			break;
	   			
	   		default:
	   			System.out.println("Unknow node");
	   	}
	   
	   	return result;
   }
         
}

