package fr.esisar.compilation.global.src3;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.verif.ErreurContext;
import fr.esisar.compilation.verif.ErreurInterneVerif;
import fr.esisar.compilation.verif.ErreurVerif;
import fr.esisar.compilation.verif.ReglesTypage;
import fr.esisar.compilation.verif.ResultatAffectCompatible;
import fr.esisar.compilation.verif.ResultatBinaireCompatible;
import fr.esisar.compilation.verif.ResultatUnaireCompatible;

public class CoderProg {

	/**
	 * Parcours l'arbre abstrait décoré et génère le code pour machine abstraite
	 * associé.
	 * @param a
	 * @throws ErreurInst
	 * @throws ErreurOperande
	 */
	public void coderProgramme(Arbre a) throws ErreurInst, ErreurOperande {
		coder_PROGRAMME(a);
	}
	
	
	/**************************************************************************
	 * PROGRAMME
	 **************************************************************************/
	private void coder_PROGRAMME(Arbre a)  {
		coder_LISTE_DECL(a.getFils1());
		coder_LISTE_INST(a.getFils2());
	}

	/**************************************************************************
	 * LISTE_DECL
	 * 
	 * Vérifie une liste de déclarations.
	 * Examine les déclarations dans l'ordre dans lequel elles sont faîtes dans
	 * le programme.
	 **************************************************************************/
	private void coder_LISTE_DECL(Arbre a)  {
		switch(a.getNoeud()) {
		
		case Vide:
			break;
			
		case ListeDecl:
			coder_LISTE_DECL(a.getFils1());
			coder_DECL(a.getFils2());
			break;
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_DECL");
		}
	}

	/**************************************************************************
	 * DECL
	 **************************************************************************/
	private void coder_DECL(Arbre a)  {
		switch(a.getNoeud()) {
		case Decl:
			Type type_decl = verifier_TYPE(a.getFils2());
			coder_LISTE_IDENT(a.getFils1(), type_decl);
			break;
		default:
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_DECL");
		}
	}

	/**************************************************************************
	 * LISTE_IDENT
	 **************************************************************************/
	private void coder_LISTE_IDENT(Arbre a, Type type)  {
		switch(a.getNoeud()) {
		case Vide:
			break;
			
		case ListeIdent:
			coder_LISTE_IDENT(a.getFils1(), type);
			coder_IDENT(a.getFils2(), type, true);
			break;
			
		default:
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_IDENT");
		}
	}
	
	/**************************************************************************
	 * IDENT
	 **************************************************************************/
	private void coder_IDENT(Arbre a, Type t, boolean estDECL)  {
		
		if(estDECL) {

		}
		else {

		}
	}
	
	/**************************************************************************
	 * TYPE
	 **************************************************************************/
	private Type verifier_TYPE(Arbre a)  {
		switch(a.getNoeud()) {
		/* Dans le cas d'un identificateur, vérifier que c'est bien un identificateur de type */
		case Ident:
			return null;
			
		/* Dans le cas d'un intervalle, on retourne le type d'intervalle */
		case Intervalle:
			return null;
			
		/* Dans le cas d'un tableau, il faut vérifier l'intervalle et renvoyer le type du tableau */
		case Tableau:
			return null;

		default:
			return null;
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_TYPE");
		}
	}
	
	/**************************************************************************
	 * TYPE_INTERVALLE
	 * 
	 * Vérifier la déclaration d'un intervalle et renvoyer le Type correspondant.
	 **************************************************************************/
	private Type coder_TYPE_INTERVALLE(Arbre a)  {

		/* Vérification grammaticale */
		int b_inf = coder_EXP_CONST(a.getFils1());
		int b_sup = coder_EXP_CONST(a.getFils2());
		
		return Type.creationInterval(b_inf, b_sup);
	}

	/**************************************************************************
	 * EXP_CONST
	 **************************************************************************/
	private int coder_EXP_CONST(Arbre a)  {
		switch(a.getNoeud()) {
		/* Dans le cas d'un identificateur, il faut vérifier que c'est un identificateur constant de type entier */
		case Ident:
			return 0;
			
		/* Dans le cas d'un entier on retourne simlement la valeur */
		case Entier:
			return a.getEntier();
		
		case PlusUnaire:
			return coder_EXP_CONST(a.getFils1());

		case MoinsUnaire:
			return coder_EXP_CONST(a.getFils1());

		default:
			return 0;
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_EXP_CONST");
		}
	}

	/**************************************************************************
	 * LISTE_INST
	 **************************************************************************/
	private void coder_LISTE_INST(Arbre a)  {
		switch (a.getNoeud()) {
		
		case Vide: 
			break;
			
		case ListeInst:
			coder_LISTE_INST(a.getFils1()); 
			coder_INST(a.getFils2());
			break;
			
		default	: 
			//throw new ErreurInterneVerif( "Arbre incorrect dans verifier_LISTE_INST")  ; 
		} 
	}

	/**************************************************************************
	 * INST
	 **************************************************************************/
	private void coder_INST(Arbre a)  {
		switch(a.getNoeud()) {
		
		case Nop:
			break;
			
		case Affect:			
			break;
			
		case Pour:
			break;
			
		case TantQue:
			break;
			
		case Si:
			break;
			
		case Lecture:
			break;
			
		case Ecriture:
			break;

		case Ligne:
			break;
			
		default:
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_INST");
		}
	}
	
	/**************************************************************************
	 * PLACE
	 **************************************************************************/
	private Defn verifier_PLACE(Arbre a, boolean setDecor)  {
		switch(a.getNoeud()) {
		
		case Ident:
			return null;
			
		case Index:
			return verifier_INDEX(a, setDecor);
			
		default:
			return null;
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_PLACE");
		}
	}

	/**************************************************************************
	 * LISTE_EXP
	 **************************************************************************/
	private void coder_LISTE_EXP(Arbre a)  {
		switch(a.getNoeud()) {
		
		case Vide:
			break;
			
		case ListeExp:
			coder_EXP(a.getFils2());
			coder_LISTE_EXP(a.getFils1());
			break;
			
		default:
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_EXP");
		}
	}
	
	/**************************************************************************
	 * INDEX
	 **************************************************************************/
	private Defn verifier_INDEX(Arbre a, boolean setDecor) {

		return null;
	}

	/**************************************************************************
	 * EXP
	 **************************************************************************/
	private Type coder_EXP(Arbre a)  {
		switch(a.getNoeud()) {
		
		/* Tous les opérateurs binaires */
		case Et :
		case Ou :
		case Plus:
		case Moins:
		case Mult:
		case DivReel:
		case Reste:
		case Quotient:
		case Egal :
		case Inf :
		case SupEgal :
		case InfEgal :
		case Sup :
		case NonEgal :
			return null;

			
		case PlusUnaire:
		case MoinsUnaire:
		case Non:
			return null;

			
		case Index:
			return null;
			
		case Conversion:
			return null;
			
		case Entier:
			return null;
			
		case Reel:
			return null;
			
		case Chaine:
			return null;
			
		case Ident:
			return null;
			
		default:
			return null;
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_EXP");
		}
	}

	/**************************************************************************
	 * PAS
	 **************************************************************************/
	private void verifier_PAS(Arbre a)  {
		switch(a.getNoeud()) {
		
		case Increment:
		case Decrement:
			break;
			
		default:
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_PAS");
		}
	}
}
