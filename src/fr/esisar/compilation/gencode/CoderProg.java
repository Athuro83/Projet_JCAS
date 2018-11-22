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
	
	//  HashMap representant l'etat des registres : true -> Libre / false -> Utilise
	HashMap<Registre, Boolean> etatRegistre = new HashMap<Registre, Boolean>();
	// Tableau contenant tous les registres
	Registre tabRegistre[] = {Registre.R0, 
							  Registre.R1, 
							  Registre.R2, 
							  Registre.R3, 
							  Registre.R4, 
							  Registre.R5, 
							  Registre.R6, 
							  Registre.R7, 
							  Registre.R8, 
							  Registre.R9, 
							  Registre.R10, 
							  Registre.R11, 
							  Registre.R12, 
							  Registre.R13, 
							  Registre.R14, 
							  Registre.R15} ;
	
	// HashMap permettant d'associer un offset a un ID
	HashMap<String, Integer> offsetID = new HashMap<String, Integer>();
	// Integer permettant d'avoir un repere dans l'espace de la pile alloue aux variables
	// Il pointe sur la premiere case vide
	Integer repereOffset = 0;
	
	public void coderProgramme(Arbre a) throws ErreurInst, ErreurOperande {
		/* Coder le programme */
		Prog.ajouterComment("Programme JCAS");
		initialiserRegistre();
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
		testerPile(mem_size);
		
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
			coder_EXP(a.getFils2(), Registre.R1);
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
	private void coder_PLACE(Arbre a)  {
		switch(a.getNoeud()) {
		
		case Ident:
			if(testerID(a.getChaine())) {
				offsetID.get(a.getChaine());
			}
			else {
				allouerOffsetID(a.getChaine(), a.getDecor().getType().getTaille());			
			}
			break;
			
		case Index:
			break;
			
		default:
			break;
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
	private Defn coder_INDEX(Arbre a, boolean setDecor) {

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
				Prog.ajouter("Calcul de l'expression avant affichage");
				/* Réserver le registre R1 */
				//TODO
				
				/* Calculer l'expression dans R1 */
				coder_EXP(a, Registre.R1);
				
				/* Afficher correctement l'expression */
				Decor dec = a.getDecor();
				if(dec.getType() == Type.Integer) {
					Prog.ajouter(Inst.creation0(Operation.WINT));
				}
				else {
					Prog.ajouter(Inst.creation0(Operation.WFLOAT));
				}
		}
	}
	
	private Type coder_EXP(Arbre a, Registre r)  {
		/* On vérifie si l'on est sur une feuille de l'arbre */
		if(isLeaf(a)) {
			/* Charger la valeur dans le registre */
			Prog.ajouter("Cas d'une feuille");
			Prog.ajouter(Inst.creation2(Operation.LOAD, generateOperande(a), Operande.opDirect(r)));
		}
		else {
			
			if(a.getArite() == 1) {
				/* On évalue l'expression sous-jacente puis on applique le noeud */
				Prog.ajouter("Cas d'un noeud d'arité 1");
				coder_EXP(a.getFils1(), r);
				
				switch(a.getNoeud()) {
				
					case PlusUnaire:
						/* Ne rien faire */
						break;
						
					case MoinsUnaire:
						Prog.ajouter(Inst.creation2(Operation.OPP, Operande.opDirect(r), Operande.opDirect(r)), "Moins unaire");
						break;
						
					case Non:
						/* Pas encore bien sûr */
						break;
						
					case Conversion:
						Prog.ajouter(Inst.creation2(Operation.FLOAT, Operande.opDirect(r), Operande.opDirect(r)), "Convertion entier -> flottant");
				}
			}
			else {
				Prog.ajouter("Cas d'un noeud d'arité 2");
				
				/* Créer l'opération */
				Operation oper = null;
				switch(a.getNoeud()) {
				
				case Plus:
					oper = Operation.ADD;
					//Prog.ajouter(Inst.creation2(Operation.ADD, oper_gauche, Operande.opDirect(r)), "Addition");
					break;
					
				case Moins:
					oper = Operation.SUB;
					//Prog.ajouter(Inst.creation2(Operation.SUB, oper_gauche, Operande.opDirect(r)), "Soustraction");
					break;
					
				case Mult:
					oper = Operation.MUL;
					//Prog.ajouter(Inst.creation2(Operation.MUL, oper_gauche, Operande.opDirect(r)), "Multiplication");
					break;
					
				case Quotient:
				case DivReel:
					oper = Operation.DIV;
					//Prog.ajouter(Inst.creation2(Operation.DIV, oper_gauche, Operande.opDirect(r)), "Division");
					break;
				}

				/* Vérifier si l'expression de droite est une feuille */
				if(isLeaf(a.getFils2())) {
					Prog.ajouter("Cas simple");
					/* Calculer l'expression de droite */
					coder_EXP(a.getFils1(), r);
					
					/* Ajouter la feuille de gauche */
					Operande oper_gauche = generateOperande(a.getFils2());
					
					Prog.ajouter(Inst.creation2(oper, oper_gauche, Operande.opDirect(r)));
					
					/* Vérification d'un éventuel débordement */
					coder_TEST_ADD_OV();
				}
				else {
					Prog.ajouter("Cas compliqué : Alloc d'un registre");
					/* Il nous faut un registre supplémentaire ! */
					if(resteRegistre()) {
						Prog.ajouter("Il reste des registres");
						/* Evaluer la sous-expression de gauche */
						coder_EXP(a.getFils1(), r);
						
						/* Allouer un registre pour l'expression de droite */
						Registre rd = allouerRegistre();
						
						/* Evaluer la sous-expression de droite */
						coder_EXP(a.getFils2(), rd);
						
						/* Coder l'opération */
						Prog.ajouter(Inst.creation2(oper, Operande.opDirect(rd), Operande.opDirect(r)));
						
						/* Libérer le registre */
						libererRegistre(rd);
					}
					else {
						Prog.ajouter("Il ne reste plus de registres, aled");
						//TODO : Affect temporaire
					}
				}
			}
		}
		
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
	private void coder_PAS(Arbre a)  {
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

	
	
	// Fonction de détection des erreurs
	
	// Détecte les débordements arithmétiques
	private void coder_TEST_ADD_OV() {
		
		Prog.ajouter(Inst.creation1(Operation.BOV, Operande.creationOpEtiq(Etiq.lEtiq("ERR_ADD_OV"))), "Test de débordement arithmétique");
	}
	
	// Cette fonction permet de tester si la place dans la pile est suffisante
	private void testerPile(int mem_size) {
		/* Tester si l'espace est suffisant dans la pile */
		Prog.ajouter(Inst.creation1(Operation.TSTO, Operande.creationOpEntier(mem_size)), "Test de la pile pour " + mem_size + " case(s) mémoire");
		Prog.ajouter(Inst.creation1(Operation.BOV, Operande.creationOpEtiq(Etiq.lEtiq("ERR_STACK_OV"))));
	}
	
	// Fonctions utilitaires 
	
	private Operande generateOperande(Arbre a) {
		switch (a.getNoeud()) {
		
			case Entier:
				return Operande.creationOpEntier(a.getEntier());
				
			case Reel:
				return Operande.creationOpReel(a.getReel());

			case Ident:
				/* Chercher où est stocké la valeur de l'identificateur */
				int offset = 1 /*getOffset(a.getChaine())*/;
				
				/* Retourner l'opérande correspondant */
				return Operande.creationOpIndirect(offset, Registre.GB);
				
			default:
				return null;
		}
	}
	
	private boolean isLeaf(Arbre a) {
		return a.getArite() == 0;
	}
	
	// Cette fonction initialise l'etat de tous les registres
	private void initialiserRegistre() {
		for(Registre R : tabRegistre)
		{
			etatRegistre.put(R, true);
		}
	}
	
	// Cette foncton permet d'allouer le premier registre libre
	// S'il n'y en a pas, elle renvoie null;
	private Registre allouerRegistre() {
		for(int i=0 ; i < 16 ; i++)
		{
			if((boolean) etatRegistre.get(tabRegistre[i]))
			{
				etatRegistre.put(tabRegistre[i], false);
				return tabRegistre[i];
			}
		}
		return null;
	}
	
	// Cette fonction permet de liberer le registre placer en argument
	private void libererRegistre(Registre R) {
		etatRegistre.replace(R, true);
	}
	
	// Cette fonction permet de tester s'il reste au moins un registre libre
	// true -> un registre est libre / false -> tous les registres sont occupes
	private boolean resteRegistre() {
		for(Registre R : tabRegistre) {
			if(etatRegistre.get(R)) {
				return true;
			}
		}
		return false;
	}
	
	// Cette fonction permet de stocker l'offset correspondant à l'ID
	private void allouerOffsetID(String id, int sizeID) {
		offsetID.put(id, repereOffset);
		repereOffset += sizeID;
	}
	
	// Cette fonction permet d'avoir l'offset associe a l'id en argument
	private int getOffset(String id) {
		return offsetID.get(id);
	}
	
	// Cette fonction teste si l'id en argument est deja declare dans la hashmap
	// true -> l'id existe / false -> l'id n'est pas dans la hashmap
	private boolean testerID(String id) {
		return offsetID.containsKey(id);
	}
	
	// Cette fonction permet d'allouer une place en pile pour une varibale temporaire
	private void allouerTemp(Registre R) {
		testerPile(1);
		Prog.ajouter(Inst.creation1(Operation.PUSH, Operande.opDirect(R)), "Allocation d'une variable temporaire");
	}
	
	// Cette fonction permet de liberer la place dans la pile allouee a une variable temporaire
	private void libererTemp(Registre R) {
		Prog.ajouter(Inst.creation1(Operation.POP, Operande.opDirect(R)), "Liberation de la pile et suppression de la variable temporaire");
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
