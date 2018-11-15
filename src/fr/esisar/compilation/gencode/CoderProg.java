package fr.esisar.compilation.gencode;

import java.util.HashMap;
import java.util.Map;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

public class CoderProg {

	/**
	 * Parcours l'arbre abstrait décoré et génère le code pour machine abstraite
	 * associé.
	 * @param a
	 * @throws ErreurInst
	 * @throws ErreurOperande
	 */
	public void coderProgramme(Arbre a) throws ErreurInst, ErreurOperande {
		/* Coder le programme */
		Prog.ajouterComment("Programme JCAS");
		coder_PROGRAMME(a);
		Prog.ajouter(Inst.creation0(Operation.HALT));
		/* Coder les exceptions */
		Prog.ajouterComment("Erreurs machine abstraite");
		coder_ERR_STACK_OV();
		coder_ERR_ADD_OV();
	}
	
	
	/**************************************************************************
	 * PROGRAMME
	 **************************************************************************/
	private void coder_PROGRAMME(Arbre a)  {
		Prog.ajouterComment("Allocation des variables globales");
		coder_LISTE_DECL(a.getFils1());
		Prog.ajouterComment("Lecture du programme");
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

		/* Récupérer la valeur d'infoCode */
		/*Decor dec = a.getDecor();
		if(dec == null) {
			throw new RuntimeException("Pas de décor sur le noeud racine LISTE_DECL");
		}*/
		
		/* Récupérer la taille des variables globales */
		int mem_size =  2/*dec.getInfoCode()*/;
		
		/* Tester si l'espace est suffisant dans la pile */
		Prog.ajouter(Inst.creation1(Operation.TSTO, Operande.creationOpEntier(mem_size)), "Test de la pile pour " + mem_size + " cases mémoire");
		Prog.ajouter(Inst.creation1(Operation.BOV, Operande.creationOpEtiq(Etiq.lEtiq("ERR_STACK_OV"))));
		
		/* Réserver l'espace pour les variables */
		Prog.ajouter(Inst.creation1(Operation.ADDSP, Operande.creationOpEntier(mem_size)));
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
			afficher_LISTE_EXP(a.getFils1());
			break;

		case Ligne:
			Prog.ajouter(Inst.creation0(Operation.WNL), "Saut de ligne");
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
	private void afficher_LISTE_EXP(Arbre a)  {
		switch(a.getNoeud()) {
		
		case Vide:
			break;
			
		case ListeExp:
			afficher_LISTE_EXP(a.getFils1());
			afficher_EXP(a.getFils2());
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
	
	/**
	 * Afficher_EXP
	 * @param a
	 * 
	 * Affiche à l'écran l'expression
	 */
	private void afficher_EXP(Arbre a) {
		switch(a.getNoeud()) {
		
			case Entier:
				afficher_INT(a.getEntier());
				break;
				
			case Reel:
				afficher_FLOAT(a.getReel());
				break;
				
			case Chaine:
				Prog.ajouter(Inst.creation1(Operation.WSTR, Operande.creationOpChaine(a.getChaine())), "Ecrire chaine: " + a.getChaine());
				break;
				
			case Ident:
				/* Vérifier la nature de l'identificateur */
				
				/* Afficher la valeur de l'identificateur */
				break;
				
			default:
				/* Expressions qui demandent un calcul avant affichage */

		}
	}
	
	private Type coder_EXP(Arbre a, Registre r)  {
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
	
	// Fonctions d'affichage
	
	private void afficher_INT(int i) {
		/* Charger l'entier dans le registre R1 */
		Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpEntier(i), Operande.R1));
		Prog.ajouter(Inst.creation0(Operation.WINT), "Ecrire entier: " + i);
	}
	
	private void afficher_FLOAT(float f) {
		/* Charger le réel dans le registre R1 */
		Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpReel(f), Operande.R1));
		Prog.ajouter(Inst.creation0(Operation.WFLOAT), "Ecrire reel: " + f);
	}
	
	// Fonctions d'implémentation des erreurs
	
	private void coder_ERR_STACK_OV() {
		
		Prog.ajouter(Etiq.lEtiq("ERR_STACK_OV"), "Erreur : Dépassement de la capacité de la pile");
		Prog.ajouter(Inst.creation1(Operation.WSTR, Operande.creationOpChaine("Limite de pile depassee")));
		Prog.ajouter(Inst.creation0(Operation.HALT));
	}
	
	private void coder_ERR_ADD_OV() {
		
		Prog.ajouter(Etiq.lEtiq("ERR_ADD_OV"), "Erreur : Dépassement après opération arithmétique");
		Prog.ajouter(Inst.creation1(Operation.WSTR, Operande.creationOpChaine("Overflow apres operation arithmetique")));
		Prog.ajouter(Inst.creation0(Operation.HALT));
	}
}
