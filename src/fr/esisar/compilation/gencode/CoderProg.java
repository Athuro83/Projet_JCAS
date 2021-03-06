package fr.esisar.compilation.gencode;

import java.util.HashMap;

import fr.esisar.compilation.global.src.Arbre;
import fr.esisar.compilation.global.src.Decor;
import fr.esisar.compilation.global.src.Defn;
import fr.esisar.compilation.global.src.NatureType;
import fr.esisar.compilation.global.src.Noeud;
import fr.esisar.compilation.global.src.Type;
import fr.esisar.compilation.global.src3.ErreurInst;
import fr.esisar.compilation.global.src3.ErreurOperande;
import fr.esisar.compilation.global.src3.Etiq;
import fr.esisar.compilation.global.src3.Inst;
import fr.esisar.compilation.global.src3.Operande;
import fr.esisar.compilation.global.src3.Operation;
import fr.esisar.compilation.global.src3.Prog;
import fr.esisar.compilation.global.src3.Registre;

public class CoderProg {

	//  HashMap representant l'etat des registres : true -> Libre / false -> Utilise
	private HashMap<Registre, Boolean> etatRegistre = new HashMap<Registre, Boolean>();

	// Tableau contenant tous les registres
	private Registre tabRegistre[] = {Registre.R0, 
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
	private HashMap<String, Integer> offsetID = new HashMap<String, Integer>();

	// Integer permettant d'avoir un repere dans l'espace de la pile alloue aux variables
	// Il pointe sur la premiere case vide
	private Integer repereOffset = 1;

	/* Garde en mémoire l'offset du premier mot vide de la pile par rapport à la base de la pile */
	private int freeWordStackOffset = 1;

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
		initialiserRegistre();
		coder_PROGRAMME(a);
		Prog.ajouter(Inst.creation0(Operation.HALT));
		/* Coder les exceptions */
		Prog.ajouterComment("Erreurs machine abstraite");
		coder_ERR_STACK_OV();
		coder_ERR_ADD_OV();
		coder_ERR_READ_OV();
		coder_ERR_INDEX_OV();
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
		Decor dec = a.getDecor();
		if(dec == null) {
			throw new RuntimeException("Pas de décor sur le noeud racine LISTE_DECL");
		}

		/* Récupérer la taille des variables globales */
		int mem_size =  dec.getInfoCode();
		/* Tester si l'espace est suffisant dans la pile */
		testerPile(mem_size);

		/* Réserver l'espace pour les variables */
		Prog.ajouter(Inst.creation1(Operation.ADDSP, Operande.creationOpEntier(mem_size)));
		this.freeWordStackOffset += mem_size;
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
		Operande op;
		Registre r;
		int offset_tmp = 0;
		boolean affect_tmp = false;
		
		switch(a.getNoeud()) {

		case Nop:
			break;

		case Affect:
			Registre r2 = null;
			int offset_tmp1 = 0,
				offset_tmp2 = 0;
			int nb_tmp = 0;
			int offset;
			
			/* On tente de réserver un registre */
			if(resteRegistre())	{
				r = allouerRegistre();
			}
			else {
				r = Registre.R0;
				offset_tmp = allouerTemp(r);
				affect_tmp = true;
			}
			
			/* On évalue notre expression dans r1 */
			coder_EXP(a.getFils2(), r);
			
			/* On récupère l'opérande de notre ident */
			op = coder_PLACE(a.getFils1());
			
			/* Charger la valeur dans la pile */
			Prog.ajouter(Inst.creation2(Operation.STORE, Operande.opDirect(r), op));
			
			switch(nb_tmp) {
			
				case 0:
					/* Libérer les 2 registres */
					libererRegistre(r);
					if(r2 != null) libererRegistre(r2);
					break;

				case 1:
					/* On a réussi à allouer r1 */
					libererRegistre(r);
					/* Il faut rétablir r2 */
					Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(offset_tmp2, Registre.GB), Operande.opDirect(r2)));
					libererTemp();
					break;
					
				case 2:
					/* Il faut rétablir r1 et r2 */
					Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(offset_tmp1, Registre.GB), Operande.opDirect(r)));
					libererTemp();
					Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(offset_tmp2, Registre.GB), Operande.opDirect(r2)));
					libererTemp();
					break;
			}
			break;

		case Pour:
			/* Déclaration des étiquettes */
			Etiq etiqFOR = Etiq.nouvelle("FOR") ; 
			Etiq etiqCOND = Etiq.nouvelle("FOR_COND") ; 

			Registre rdebut = allouerRegistre();
			Registre rfin = allouerRegistre();

			/* Calcul des bornes */
			coder_EXP(a.getFils1().getFils2(), rdebut);
			coder_EXP(a.getFils1().getFils3(), rfin);

			/* Récupération de l'identificateur de boucle */
			op = coder_PLACE(a.getFils1().getFils1());

			/* Début de la boucle */
			Prog.ajouter(Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiqCOND)));
			Prog.ajouter(etiqFOR, "Etiquette pour le début du POUR"); // FOR :

			/* Les instructions de la boucle */
			coder_LISTE_INST(a.getFils2());
			
			/* Evaluer le type de la boucle (Inc. ou Dec.) */
			if(a.getFils1().getNoeud().equals(Noeud.Increment)) {		
				
				/* Incrémentation du compteur */
				Prog.ajouter(Inst.creation2(Operation.ADD, Operande.creationOpEntier(1), Operande.opDirect(rdebut))); // ADD R , 1
				coder_TEST_ADD_OV();

				/* Etiquette condition */
				Prog.ajouter(etiqCOND, "Bloc condition du POUR");
				
				/* Sauvegarde de la valeur dans l'identificateur de boucle */
				Prog.ajouter(Inst.creation2(Operation.STORE, Operande.opDirect(rdebut), op));

				/* Comparaison et saut */
				Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(rfin), Operande.opDirect(rdebut)), "Test de la condition de la boucle incrémentale");
				Prog.ajouter(Inst.creation1(Operation.BLT, Operande.creationOpEtiq(etiqFOR)), "Branchement vers le début de la boucle"); // B
				coder_LISTE_INST(a.getFils2()); // coder la liste d'instruction

			}else if(a.getFils1().getNoeud().equals(Noeud.Decrement)) {

				/* Décrémentation du compteur */
				Prog.ajouter(Inst.creation2(Operation.SUB, Operande.creationOpEntier(1), Operande.opDirect(rdebut))); // ADD R , 1
				coder_TEST_ADD_OV();

				/* Etiquette condition */
				Prog.ajouter(etiqCOND, "Bloc condition du POUR");

				/* Sauvegarde de la valeur dans l'identificateur de boucle */
				Prog.ajouter(Inst.creation2(Operation.STORE, Operande.opDirect(rdebut), op));

				/* Comparaison et saut */
				Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(rfin), Operande.opDirect(rdebut)), "Test de la condition de la boucle décrémentale");
				Prog.ajouter(Inst.creation1(Operation.BGT, Operande.creationOpEtiq(etiqFOR)), "Branchement vers le début de la boucle"); // B
				coder_LISTE_INST(a.getFils2()); // coder la liste d'instruction
			}

			libererRegistre(rdebut);
			libererRegistre(rfin);

			break;

		case TantQue:
			Prog.ajouter("Bloc 'Tant que'");
			
			/* Déclaration des étiquettes */
			Etiq e_cond = Etiq.nouvelle("cond_TQ");
			Etiq e_deb = Etiq.nouvelle("debut_TQ");
			
			/* Branchement sur la condition */
			Prog.ajouter(Inst.creation1(Operation.BRA, Operande.creationOpEtiq(e_cond)), "Saut vers la condition de la boucle");
			
			/* Bloc d'instructions de la boucle */
			Prog.ajouter(e_deb);
			coder_LISTE_INST(a.getFils2());
			
			/* Bloc condition de la boucle */
			Prog.ajouter(e_cond);
			coder_COND(a.getFils1(), true, e_deb);
			break;

		case Si:
			
			if(!a.getFils3().getNoeud().equals(Noeud.Vide)) {
				Etiq etiqSinon = Etiq.nouvelle("E_sinon");
				Etiq etiqFin = Etiq.nouvelle("E_fin");
				
				coder_COND(a.getFils1(), false, etiqSinon);
				coder_LISTE_INST(a.getFils2());
				Prog.ajouter(Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiqFin)));
				Prog.ajouter(etiqSinon);
				coder_LISTE_INST(a.getFils3());
				Prog.ajouter(etiqFin);
			}
			
			else {
				Etiq etiqFin = Etiq.nouvelle("E_fin");
				
				coder_COND(a.getFils1(), false, etiqFin);
				coder_LISTE_INST(a.getFils2());
				Prog.ajouter(etiqFin);
				
			}
			
			break;

		case Lecture:
			/* Récupérer l'opérande de l'identificateur */
			op = coder_PLACE(a.getFils1());

			/* Récupérer le type de l'identificateur */
			Decor dec;
			if( (dec = a.getFils1().getDecor()) == null ) {
				throw new RuntimeException("lecture n'a pas de decor");
			}
			Type t ; 
			if((t = dec.getType())== null) {
				throw new RuntimeException("le decor n'a pas de type");
			}
			
			/* Faire une sauvegarde du registre R1 */
			offset_tmp = allouerTemp(Registre.R1);

			/* Lire correctement la valeur */
			if(t.equals(Type.Integer)) {
				Prog.ajouter(Inst.creation0(Operation.RINT), "Lecture d'entier");
			}
			else {
				Prog.ajouter(Inst.creation0(Operation.RFLOAT), "Lecture de reel");
			}
			
			/* Test de débordement */
			Prog.ajouter(Inst.creation1(Operation.BOV, Operande.creationOpEtiq(Etiq.lEtiq("ERR_READ_OV"))));
			/* Charger la valeur en pile */
			Prog.ajouter(Inst.creation2(Operation.STORE, Operande.R1, op));

			/* Rétablir la valeur de R1 */
			Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(offset_tmp, Registre.GB), Operande.R1));

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
	 * COND
	 **************************************************************************/
	private void coder_COND(Arbre a, boolean comparaison, Etiq etiquette) {
		
		Registre r1;
		Registre r2;
		// Entier permettant de compter le nombre de variables temporaires que l'on va devoir creer
		int allocTemp = 0;
		// Entier permettant de garder en memoire l'offset de la premiere variable temporaire
		int offsetTemp1 = 0;
		// Entier permettant de garder en memoire l'offset de la deuxieme variable temporaire
		int offsetTemp2 = 0;
		
		// On teste s'il reste 1 registre de libre
		if(resteRegistre()) {
			r1 = allouerRegistre();
			// On teste s'il reste un 2eme registre de libre
			if(resteRegistre()) {
				r2 = allouerRegistre();
			}
			// S'il n'y a plus de registre on alloue une variable temporaire
			else {
				offsetTemp1 = allouerTemp(Registre.R1);
				allocTemp = 1;
				r2 = Registre.R1;			
			}
		}
		// S'il n'y a plus de registre on alloue 2 variables temporaires
		else {
			offsetTemp1 = allouerTemp(Registre.R1);
			r1 = Registre.R1;
			offsetTemp2 = allouerTemp(Registre.R2);
			r2 = Registre.R2;			
			allocTemp = 2;
		}
		
		switch(a.getNoeud()) {
		/* Pour le cas Et et Ou on cree des etiquettes pour faire les branchements selon la methode paresseuse
		 * Une fois l'etiquette créée on évalue les différentes conditions de part et d'autre de l'operateur
		 */
		case Et:
			if(comparaison) {
				Etiq etiqFinEt = Etiq.nouvelle("E_fin");
				
				coder_COND(a.getFils1(), !comparaison, etiqFinEt);
				coder_COND(a.getFils2(), comparaison, etiquette);
				
				Prog.ajouter(etiqFinEt);
			}
			else {
				coder_COND(a.getFils1(), comparaison, etiquette);
				coder_COND(a.getFils2(), comparaison, etiquette);
			}
			
			break;
		case Ou:
			if(comparaison) {
				coder_COND(a.getFils1(), comparaison, etiquette);
				coder_COND(a.getFils2(), comparaison, etiquette);
			}
			else {
				Etiq etiqFinOu = Etiq.nouvelle("E_fin");

				coder_COND(a.getFils1(), !comparaison, etiqFinOu);
				coder_COND(a.getFils2(), comparaison, etiquette);
				
				Prog.ajouter(etiqFinOu);

			}
			
			break;
		/* Pour les tests ci-dessous, on évalue les expressions de part et d'autre de l'operateur 
		 * ensuite on fait le test de comparaison et on fait le branchement correspondant
		 */
		case Sup:
			coder_EXP(a.getFils1(), r1);
			coder_EXP(a.getFils2(), r2);
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)));
			if(comparaison) {
				Prog.ajouter(Inst.creation1(Operation.BGT, Operande.creationOpEtiq(etiquette)));
			}
			else {
				Prog.ajouter(Inst.creation1(Operation.BLE, Operande.creationOpEtiq(etiquette)));
			}
			break;
		case Inf:
			coder_EXP(a.getFils1(), r1);
			coder_EXP(a.getFils2(), r2);
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)));
			if(comparaison) {
				Prog.ajouter(Inst.creation1(Operation.BLT, Operande.creationOpEtiq(etiquette)));
			}
			else {
				Prog.ajouter(Inst.creation1(Operation.BGE, Operande.creationOpEtiq(etiquette)));
			}
			break;
		case InfEgal:
			coder_EXP(a.getFils1(), r1);
			coder_EXP(a.getFils2(), r2);
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)));
			if(comparaison) {
				Prog.ajouter(Inst.creation1(Operation.BLE, Operande.creationOpEtiq(etiquette)));
			}
			else {
				Prog.ajouter(Inst.creation1(Operation.BGT, Operande.creationOpEtiq(etiquette)));
			}
			break;
		case SupEgal:
			coder_EXP(a.getFils1(), r1);
			coder_EXP(a.getFils2(), r2);
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)));
			if(comparaison) {
				Prog.ajouter(Inst.creation1(Operation.BGE, Operande.creationOpEtiq(etiquette)));
			}
			else {
				Prog.ajouter(Inst.creation1(Operation.BLT, Operande.creationOpEtiq(etiquette)));
			}
			break;
		case Egal:
			coder_EXP(a.getFils1(), r1);
			coder_EXP(a.getFils2(), r2);
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)));
			if(comparaison) {
				Prog.ajouter(Inst.creation1(Operation.BEQ, Operande.creationOpEtiq(etiquette)));
			}
			else {
				Prog.ajouter(Inst.creation1(Operation.BNE, Operande.creationOpEtiq(etiquette)));
			}
			break;
		case NonEgal:
			coder_EXP(a.getFils1(), r1);
			coder_EXP(a.getFils2(), r2);
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)));
			if(comparaison) {
				Prog.ajouter(Inst.creation1(Operation.BNE, Operande.creationOpEtiq(etiquette)));
			}
			else {
				Prog.ajouter(Inst.creation1(Operation.BEQ, Operande.creationOpEtiq(etiquette)));
			}
			break;
		// On appelle coder_COND avec la condition et l'inverse du saut
		case Non:
			coder_COND(a.getFils1(), !comparaison, etiquette);	
		/* Si l'on est sur un noeud ident on a 3 cas possibles :
		 * Soit on est sur un noeud True ou False, soit on est sur un identifiant
		 * Dans ce dernier cas, on va chercher la valeur de l'identifiant que l'on stocke dans R1 pour ensuite le comparer a 0
		 * On fait enfin le branchement correspondant
		 */
				break;
		case Ident:
			switch(a.getChaine()) {
			case "true":
				if(comparaison) {
					Prog.ajouter(Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiquette)));

				}
				break;
			case "false":
				if(!comparaison) {
					Prog.ajouter(Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiquette)));

				}
				break;
			default:
				Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(getOffset(a.getChaine()), Registre.GB), Operande.opDirect(r1)));
				Prog.ajouter(Inst.creation2(Operation.CMP, Operande.creationOpEntier(0), Operande.opDirect(r1)));

				if(comparaison) {
					Prog.ajouter(Inst.creation1(Operation.BNE, Operande.creationOpEtiq(etiquette)));
				}
				else {
					Prog.ajouter(Inst.creation1(Operation.BEQ, Operande.creationOpEtiq(etiquette)));
				}
				break;
			}
		default:
			break;
		}
		/* On libere les registres alloués ou les variables temporaires allouées
		 * Pour cela on switch sur le nombre d'allocations temporaires
		 */
		switch(allocTemp) {
			case 0:
				libererRegistre(r1);
				libererRegistre(r2);
				break;
			case 1:
				Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(offsetTemp1, Registre.GB), Operande.R1));
				libererTemp();
				libererRegistre(r1);
				break;
			case 2:
				Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(offsetTemp2, Registre.GB), Operande.R2));
				Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(offsetTemp1, Registre.GB), Operande.R1));
				libererTemp();
				libererTemp();		
				break;
		}
	}
	
	/**************************************************************************
	 * PLACE
	 **************************************************************************/
	private Operande coder_PLACE(Arbre a)  {
		Operande op_gauche, op_ret;
		Registre r;
		boolean alloc_tmp = false;
		int offset_tmp = 0;
		
		switch(a.getNoeud()) {

		case Ident:
			// Si l'ID n'existe pas, on l'alloue dans la hashmap
			if(!testerID(a.getChaine())) {
				allouerOffsetID(a.getChaine(), a);	
			}
			
			// On retourne l'opérande associé
			return Operande.creationOpIndirect(getOffset(a.getChaine()), Registre.GB);

		case Index:
			/* Récupérer l'opérande de notre fils gauche */
			op_gauche = coder_PLACE(a.getFils1());
			
			/* On alloue un registre */
			if(resteRegistre()) {
				r = allouerRegistre();
			}
			else {
				r = Registre.R0;
				offset_tmp = allouerTemp(r);
				alloc_tmp = true;
			}
			
			/* On calcule l'index dans r */
			coder_EXP(a.getFils2(), r);
			/* Tester un débordement d'index */
			Type t_inter = getArrayInterval(a.getFils1());
			testerIndex(r, t_inter);
			/* Calculer l'adresse de la valeur */
			Prog.ajouter(Inst.creation2(Operation.SUB, Operande.creationOpEntier(t_inter.getBorneInf()), Operande.opDirect(r)), "Décallage de " + t_inter.getBorneInf() + " pour obtenir l'adresse");
			
			/* Création de l'opérande */
			op_ret = Operande.creationOpIndexe(op_gauche.getDeplacement(), Registre.GB, r);
			
			/* Libération du registre */
			if(alloc_tmp) {
				Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpIndirect(offset_tmp, Registre.GB), Operande.opDirect(r)));
			}
			else {
				libererRegistre(r);
			}
			
			/* On renvoie l'offset du début du tableau */
			return op_ret;

		default:
			break;
			//throw new ErreurInterneVerif("Arbre incorrect dans verifier_PLACE");
		}
		
		return null;
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
		/* Variables */
		Registre r;
		Decor dec;
		int offset;
		boolean R1_save = false;
		
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
			/* Récupération de l'identificateur */
			Operande ident = coder_PLACE(a);
			
			/* Sauvegarde du registre R1 */
			Prog.ajouter(Inst.creation1(Operation.PUSH, Operande.R1), "Sauvegarde de la valeur de R1");

			/* Charger la valeur dans R1 */
			Prog.ajouter(Inst.creation2(Operation.LOAD, ident, Operande.R1));

			/* Afficher correctement l'identificateur */
			// Si le type est un Integer 
			if(a.getDecor().getType() == Type.Integer) {
				Prog.ajouter(Inst.creation0(Operation.WINT), "Ecrire valeur de "+a.getChaine());
			}
			// Si le type est un Real
			else if (a.getDecor().getType() == Type.Real) {
				Prog.ajouter(Inst.creation0(Operation.WFLOAT), "Ecrire valeur de " + a.getChaine());
			}
			// Si le type est une String ou un Boolean on jette une exception
			else {
				throw new ErreurInst("L'affichage n'est pas possible pour ce type.");
			}
			
			/* Rétablir la valeur de R1 */
			Prog.ajouter(Inst.creation1(Operation.POP, Operande.R1), "Rétablir la valeur de R1");
			break;
			

		default:
			/* Expressions qui demandent un calcul avant affichage */
			Prog.ajouter("Calcul de l'expression avant affichage");

			/* Calculer l'expression */
			r = allouerRegistre();
			coder_EXP(a, r);

			/* Charger la valeur dans le registre d'affichage */
			if(r != Registre.R1) {
				/* Sauvegarder le contenu de R1 dans la pile */
				testerPile(1);
				Prog.ajouter(Inst.creation1(Operation.PUSH, Operande.R1), "Sauvegarde de R1");
				R1_save = true;
				/* Charger la valeur à afficher dans R1 */
				Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.opDirect(r), Operande.opDirect(Registre.R1)), "Charger la valeur de " + r + " dans le registre d'affichage R1");
			}

			/* Afficher correctement l'expression */
			dec = a.getDecor();
			if(dec.getType() == Type.Integer) {
				Prog.ajouter(Inst.creation0(Operation.WINT));
			}
			else {
				Prog.ajouter(Inst.creation0(Operation.WFLOAT));
			}

			/* Rétablir la valeur de R1 si besoin */
			if(R1_save) {
				Prog.ajouter(Inst.creation1(Operation.POP, Operande.R1), "Rétablir la valeur de R1");
			}
			
			/* Libérer le registre */
			libererRegistre(r);
		}
	}

	private void coder_EXP(Arbre a, Registre r)  {
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
				Registre r1, r2;
				int offset;
				
				switch(a.getNoeud()) {

				case Plus:
					oper = Operation.ADD;
					break;

				case Moins:
					oper = Operation.SUB;
					break;

				case Mult:
					oper = Operation.MUL;
					break;

				case Quotient:
				case DivReel:
					oper = Operation.DIV;
					break;
				case Egal :
					// On alloue 2 registres pour les 2 expressions a comparer
					r1 = allouerRegistre();
					r2 = allouerRegistre();
					// On calcule les valeurs des expressions
					coder_EXP(a.getFils1(), r1);
					coder_EXP(a.getFils2(), r2);
					// On insere la comparaison qui permet de mettre a jour les flags
					Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)), "Comparaison");
					// On tcheck le flag EQ qui a ete mis a jour par le CMP pour assigner la valeur dans le registre r
					Prog.ajouter(Inst.creation1(Operation.SEQ, Operande.opDirect(r)), "Controle du flag EQ");
					libererRegistre(r1);
					libererRegistre(r2);
					return;
				case Inf :
					// On alloue 2 registres pour les 2 expressions a comparer
					r1 = allouerRegistre();
					r2 = allouerRegistre();
					// On calcule les valeurs des expressions
					coder_EXP(a.getFils1(), r1);
					coder_EXP(a.getFils2(), r2);
					// On insere la comparaison qui permet de mettre a jour les flags
					Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)), "Comparaison");
					// On tcheck le flag LT qui a ete mis a jour par le CMP pour assigner la valeur dans le registre r
					Prog.ajouter(Inst.creation1(Operation.SLT, Operande.opDirect(r)), "Controle du flag LT");
					libererRegistre(r1);
					libererRegistre(r2);
					return;
				case SupEgal :
					// On alloue 2 registres pour les 2 expressions a comparer
					r1 = allouerRegistre();
					r2 = allouerRegistre();
					// On calcule les valeurs des expressions
					coder_EXP(a.getFils1(), r1);
					coder_EXP(a.getFils2(), r2);
					// On insere la comparaison qui permet de mettre a jour les flags
					Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)), "Comparaison");
					// On tcheck le flag GE qui a ete mis a jour par le CMP pour assigner la valeur dans le registre r
					Prog.ajouter(Inst.creation1(Operation.SGE, Operande.opDirect(r)), "Controle du flag GE");
					libererRegistre(r1);
					libererRegistre(r2);
					return;
				case InfEgal :
					// On alloue 2 registres pour les 2 expressions a comparer
					r1 = allouerRegistre();
					r2 = allouerRegistre();
					// On calcule les valeurs des expressions
					coder_EXP(a.getFils1(), r1);
					coder_EXP(a.getFils2(), r2);
					// On insere la comparaison qui permet de mettre a jour les flags
					Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)), "Comparaison");
					// On tcheck le flag LE qui a ete mis a jour par le CMP pour assigner la valeur dans le registre r
					Prog.ajouter(Inst.creation1(Operation.SLE, Operande.opDirect(r)), "Controle du flag LE");
					libererRegistre(r1);
					libererRegistre(r2);
					return;
				case Sup :
					// On alloue 2 registres pour les 2 expressions a comparer
					r1 = allouerRegistre();
					r2 = allouerRegistre();
					// On calcule les valeurs des expressions
					coder_EXP(a.getFils1(), r1);
					coder_EXP(a.getFils2(), r2);
					// On insere la comparaison qui permet de mettre a jour les flags
					Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)), "Comparaison");
					// On tcheck le flag GT qui a ete mis a jour par le CMP pour assigner la valeur dans le registre r
					Prog.ajouter(Inst.creation1(Operation.SGT, Operande.opDirect(r)), "Controle du flag GT");
					libererRegistre(r1);
					libererRegistre(r2);
					return;
				case NonEgal :
					// On alloue 2 registres pour les 2 expressions a comparer
					r1 = allouerRegistre();
					r2 = allouerRegistre();
					// On calcule les valeurs des expressions
					coder_EXP(a.getFils1(), r1);
					coder_EXP(a.getFils2(), r2);
					// On insere la comparaison qui permet de mettre a jour les flags
					Prog.ajouter(Inst.creation2(Operation.CMP, Operande.opDirect(r2), Operande.opDirect(r1)), "Comparaison");
					// On tcheck le flag NE qui a ete mis a jour par le CMP pour assigner la valeur dans le registre r
					Prog.ajouter(Inst.creation1(Operation.SNE, Operande.opDirect(r)), "Controle du flag NE");
					libererRegistre(r1);
					libererRegistre(r2);
					return;
					
				case Index:
					/* Récupérer l'adresse du tableau */
					Operande op = coder_PLACE(a);
					
					/* Charger la valeur dans le registre */
					Prog.ajouter(Inst.creation2(Operation.LOAD, op, Operande.opDirect(r)));
					return;

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
					Prog.ajouter("Cas compliqué : Allocation d'un registre");
					/* Il nous faut un registre supplémentaire ! */
					if(resteRegistre()) {
						Prog.ajouter("Il reste des registres : évaluation gauche -> droite");
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
						Prog.ajouter("Il ne reste plus de registres, évaluation droite -> gauche");
						/* Evaluer l'expression de gauche */
						coder_EXP(a.getFils2(), r);

						/* Stocker le résultat dans une variable temporaire */
						int offsetTmp = allouerTemp(r);

						/* Evaluer l'expression de droite */
						coder_EXP(a.getFils1(), r);

						/* Evaluer l'expression du noeud */
						Prog.ajouter(Inst.creation2(oper, Operande.creationOpIndirect(offsetTmp, Registre.GB), Operande.opDirect(r)));

						/* Libérer la variable temporaire */
						libererTemp();
					}
				}
			}
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
	
	/* Détecte si un index dépasse les limites de son tableau */
	private void testerIndex(Registre r_index, Type t_intervalle) {
		Prog.ajouter("Test de débordement d'index");
		/* Test de la borne inf */
		Prog.ajouter(Inst.creation2(Operation.CMP, Operande.creationOpEntier(t_intervalle.getBorneInf()), Operande.opDirect(r_index)), "Test de la borne inf : " + t_intervalle.getBorneInf());
		Prog.ajouter(Inst.creation1(Operation.BLT, Operande.creationOpEtiq(Etiq.lEtiq("ERR_INDEX_OV"))));
		/* Test de la borne sup */
		Prog.ajouter(Inst.creation2(Operation.CMP, Operande.creationOpEntier(t_intervalle.getBorneSup()), Operande.opDirect(r_index)), "Test de la borne sup : " + t_intervalle.getBorneSup());
		Prog.ajouter(Inst.creation1(Operation.BGT, Operande.creationOpEtiq(Etiq.lEtiq("ERR_INDEX_OV"))));

	}

	// Fonctions utilitaires
	
	private Type getArrayInterval(Arbre a) throws RuntimeException {
		
		/* On vérifie que l'on a bien un tableau */
		Type t = a.getDecor().getType();
		if(t.getNature() != NatureType.Array) {
			throw new RuntimeException("Pas un tableau !");
		}
		
		/* On récupère et renvoie l'intervalle */
		return t.getIndice();
	}

	private Operande generateOperande(Arbre a) {
		switch (a.getNoeud()) {

		case Entier:
			return Operande.creationOpEntier(a.getEntier());

		case Reel:
			return Operande.creationOpReel(a.getReel());

		case Ident:
			/* Tester si l'offset a été alloué */
			if(!testerID(a.getChaine())) {
				allouerOffsetID(a.getChaine(), a);
			}
			
			/* Chercher où est stocké la valeur de l'identificateur */
			int offset = getOffset(a.getChaine());

			/* Retourner l'opérande correspondant */
			return Operande.creationOpIndirect(offset, Registre.GB);

		default:
			return null;
		}
	}

	// Cette fonction détermine si un noeud de l'arbre est ou non une feuille
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
	private void allouerOffsetID(String id, Arbre arbreID) {
		offsetID.put(id, repereOffset);
		repereOffset += arbreID.getDecor().getType().getTaille();
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
	// Elle retourne l'offset auquel se trouve la variable temporaire et met à jour le pointeur de pile
	private int allouerTemp(Registre R) {
		/* Tester si il reste de la place dans la pile */
		testerPile(1);

		/* Sauvegarder la valeur du registre dans la pile */
		Prog.ajouter(Inst.creation2(Operation.STORE, Operande.opDirect(R), Operande.creationOpIndirect(this.freeWordStackOffset, Registre.GB)), "Stocker la valeur de " + R + " dans la pile (offset " + this.freeWordStackOffset + ")");

		/* Mettre à jour le pointeur de pile */
		Prog.ajouter(Inst.creation1(Operation.ADDSP, Operande.creationOpEntier(1)));
		this.freeWordStackOffset += 1;

		/* Retourner l'offset de la variable temporaire */
		return this.freeWordStackOffset - 1;
	}

	// Cette fonction permet de liberer la place dans la pile allouee a une variable temporaire
	private void libererTemp() {

		/* Mettre à jour le pointeur de pile */
		this.freeWordStackOffset -= 1;
		Prog.ajouter(Inst.creation1(Operation.SUBSP, Operande.creationOpEntier(1)), "Libération de la variable temporaire (offset " + this.freeWordStackOffset + ")");
	}

	// Fonctions d'implémentation des erreurs

	private void coder_ERR_STACK_OV() {

		Prog.ajouter(Etiq.lEtiq("ERR_STACK_OV"), "Erreur : Dépassement de la capacité de la pile");
		Prog.ajouter(Inst.creation1(Operation.WSTR, Operande.creationOpChaine("ERREUR : Limite de pile depassee")));
		Prog.ajouter(Inst.creation0(Operation.HALT));
	}

	private void coder_ERR_ADD_OV() {

		Prog.ajouter(Etiq.lEtiq("ERR_ADD_OV"), "Erreur : Dépassement après opération arithmétique");
		Prog.ajouter(Inst.creation1(Operation.WSTR, Operande.creationOpChaine("ERREUR : Overflow apres operation arithmetique")));
		Prog.ajouter(Inst.creation0(Operation.HALT));
	}

	private void coder_ERR_READ_OV() {

		Prog.ajouter(Etiq.lEtiq("ERR_READ_OV"), "Erreur : Dépassement après une lecture");
		Prog.ajouter(Inst.creation1(Operation.WSTR, Operande.creationOpChaine("ERREUR : Overflow apres une lecture")));
		Prog.ajouter(Inst.creation0(Operation.HALT));
	}
	
	private void coder_ERR_INDEX_OV( ) {
		Prog.ajouter(Etiq.lEtiq("ERR_INDEX_OV"), "Erreur : Index hors des limites du tableau");
		Prog.ajouter(Inst.creation1(Operation.WSTR, Operande.creationOpChaine("ERREUR : Index en dehors des limites du tableau")));
		Prog.ajouter(Inst.creation0(Operation.HALT));
	}
}
