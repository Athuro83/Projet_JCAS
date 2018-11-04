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
	
	/*ErreurOperandeNonBooleen,
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
	ErreurOperandeNonNumerique,
	ErreurOperande1NonNumerique,
	ErreurOperande2NonNumerique,*/
	
	//condensé en :
	ErreurArite,
	ErreurDejaDeclare,
	ErreurIdentificateurTypeNonReconnu,
	ErreurType,
	ErreurNatureIDENT,
	ErreurPasDeclare,
	ErreurIntervalNonCompatible,
	ErreurAffectationNumeriqueAutre,
	ErreurAffectationBooleanAutre,
	ErreurAffectationArrayAutre,
	ErreurAffectationArrayIntervalsDifferents,
	ErreurAffectationArrayTypesElementsIncompatibles,
	ErreurAffectation,
	ErreurVariablesBoucleNonInterval,
	ErreurVariableRead,
	ErreurExpressionWrite,
	ErreurIndicesInverse,
    ErreurNonRepertoriee;

    void leverErreurContext(String s, int numLigne) throws ErreurVerif {
    	
       System.err.println("Erreur contextuelle : ");
       
       switch (this) {
       
       
       case ErreurIdentificateurTypeNonReconnu:
    	   System.err.println("L'identificateur de type dans la déclaration n'est pas reconnu");
    	   break;
       case ErreurDejaDeclare: 
    	   System.err.println("La variable a déja été déclarée");
    	   break;
       case ErreurArite :
    	   System.err.println("Le nombre de fils ne correspond pas");
    	   break;  	   
       case ErreurType :
    	   System.err.println("Le type ne correspond pas");
    	   break;
       case ErreurPasDeclare :
    	   System.err.println("L'identificateur n'a pas été déclaré");
    	   break;
       case ErreurNatureIDENT :
    	   System.err.println("La nature de l'identificateur ne correspond pas");
    	   break;
       case ErreurIntervalNonCompatible :
    	   System.err.println("L'Interval doit etre defini par deux Interval ");
    	   break;
       case ErreurAffectationNumeriqueAutre :
    	   System.err.println("L'<expression> devrait etre un Interval ou un Real ");
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
       case ErreurAffectation :
    	   System.err.println("L'affectation est incorrecte");
    	   break;
       case ErreurVariableRead :
    	   System.err.println("La <place> de l'instruction read devraient etre un Real ou un Interval");
    	   break;
       case ErreurIndicesInverse :
    	   System.err.println("Les indices de l'incrémentation ne correspondent pas");
    	   break;
       case ErreurExpressionWrite :
    	   System.err.println("Les expressions devraient etre de type Real, Interval ou String");
    	   break;
    	   
       default:
           System.err.println("Non repertoriee");
           
           
       }
       System.err.println(" ... ligne " + numLigne);
       throw new ErreurVerif();
    }

}