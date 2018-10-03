/**
 * Type énuméré pour les erreurs contextuelles.
 * Ce type énuméré définit toutes les erreurs contextuelles possibles et 
 * permet l'affichage des messages d'erreurs pour la passe 2.
 */

// -------------------------------------------------------------------------
// A COMPLETER, avec les différents types d'erreur et les messages d'erreurs 
// correspondants
// -------------------------------------------------------------------------

package fr.esisar.compilation.verif;

public enum ErreurContext {
	
	ErreurOperandeNonBooleen,
	ErreurOperande1NonBooleen,
	ErreurOperande2NonBooleen,
	ErreurOperandeNonReal,
	ErreurOperande1NonReal,
	ErreurOperande2NonReal,
	ErreurOperandeNonString,
	ErreurOperande1NonString,
	ErreurOperande2NonString,
	ErreurOperandeNonInterval,
	ErreurOperande1NonInterval,
	ErreurOperande2NonInterval,
	ErreurOperandeNonArray,
	ErreurOperande1NonArray,
	ErreurOperande2NonArray,
	ErreurIntervalNonCompatible,
	ErreurAffectationIntervalAutre,
	ErreurAffectationRealAutre,
	ErreurAffectationBooleanAutre,
	ErreurAffectationArrayAutre,
	ErreurAffectationArrayIntervalsDifferents,
	ErreurAffectationArrayTypesElementsIncompatibles,
	ErreurVariablesBoucleNonInterval,
	ErreurVariableRead,
	ErreurExpressionWrite,
    ErreurNonRepertoriee;

    void leverErreurContext(String s, int numLigne) throws ErreurVerif {
    	
       System.err.println("Erreur contextuelle : ");
       
       switch (this) {
       
       
       case ErreurOperandeNonBooleen :
    	   System.err.println("L'operande devrait etre un Booleen");
    	   break;
       case ErreurOperande1NonBooleen :
    	   System.err.println("L'operande 1 devrait etre un Booleen");
    	   break;
       case ErreurOperande2NonBooleen :
    	   System.err.println("L'operande 2 devrait etre un Booleen");
    	   break;
       case ErreurOperandeNonReal :
    	   System.err.println("L'operande devrait etre un Real");
    	   break;
       case ErreurOperande1NonReal :
    	   System.err.println("L'operande 1 devrait etre un Real");
    	   break;
       case ErreurOperande2NonReal :
    	   System.err.println("L'operande 2 devrait etre un Real");
    	   break;
       case ErreurOperandeNonString :
    	   System.err.println("L'operande devrait etre un String");
    	   break;
       case ErreurOperande1NonString :
    	   System.err.println("L'operande 1 devrait etre un String");
    	   break;
       case ErreurOperande2NonString :
    	   System.err.println("L'operande 2 devrait etre un String");
    	   break;
       case ErreurOperandeNonInterval :
    	   System.err.println("L'operande devrait etre un Interval");
    	   break;
       case ErreurOperande1NonInterval :
    	   System.err.println("L'operande 1 devrait etre un Interval");
    	   break;
       case ErreurOperande2NonInterval :
    	   System.err.println("L'operande 2 devrait etre un Interval");
    	   break;
       case ErreurOperandeNonArray :
    	   System.err.println("L'operande devrait etre un Array");
    	   break;
       case ErreurOperande1NonArray :
    	   System.err.println("L'operande 1 devrait etre un Array");
    	   break;
       case ErreurOperande2NonArray :
    	   System.err.println("L'operande 2 devrait etre un Array");
    	   break;
       case ErreurIntervalNonCompatible :
    	   System.err.println("L'Interval doit etre defini par deux Interval ");
    	   break;
       case ErreurAffectationIntervalAutre :
    	   System.err.println("L'<expression> devrait etre un Interval ");
    	   break;
       case ErreurAffectationRealAutre :
    	   System.err.println("L'<expression> devrait etre un Real ou un Interval ");
    	   break;
       case ErreurAffectationBooleanAutre :
    	   System.err.println("L'<expression> devrait etre un Boolean ");
    	   break;
       case ErreurAffectationArrayAutre :
    	   System.err.println("L'<expression> devrait etre un Array");
    	   break;
       case ErreurAffectationArrayIntervalsDifferents :
    	   System.err.println("Les indices de la <place> et de l'<expression> devraient etre des Interval de memes bornes");
    	   break;
       case ErreurAffectationArrayTypesElementsIncompatibles :
    	   System.err.println("Les elements de l'Array doivent etre compatibles avec le type declare");
    	   break;
       case ErreurVariablesBoucleNonInterval :
    	   System.err.println("La variable de controle et les expressions devraient etre des Interval");
    	   break;
       case ErreurVariableRead :
    	   System.err.println("La <place> de l'instruction read devraient etre un Real ou un Interval");
    	   break;
       case ErreurExpressionWrite :
    	   System.err.println("Les expressions devraient etre de type Real, Interva ou String");
    	   break;
    	   
       default:
           System.err.println("Non repertoriee");
           
           
       }
       System.err.println(" ... ligne " + numLigne);
       throw new ErreurVerif();
    }

}




