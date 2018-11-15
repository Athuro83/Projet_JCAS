package fr.esisar.compilation.verif;

import fr.esisar.compilation.global.src.*;

/**
 * Cette classe permet de réaliser la vérification et la décoration 
 * de l'arbre abstrait d'un programme.
 */
public class Verif {

	private Environ env; // L'environnement des identificateurs

	/**
	 * Constructeur.
	 */
	public Verif() {
		env = new Environ();
	}

	/**
	 * Vérifie les contraintes contextuelles du programme correspondant à 
	 * l'arbre abstrait a, qui est décoré et enrichi. 
	 * Les contraintes contextuelles sont décrites 
	 * dans Context.txt.
	 * En cas d'erreur contextuelle, un message d'erreur est affiché et 
	 * l'exception ErreurVerif est levée.
	 */
	public void verifierDecorer(Arbre a) throws ErreurVerif {
		verifier_PROGRAMME(a);
	}

	/**
	 * Initialisation de l'environnement avec les identificateurs prédéfinis.
	 */
	private void initialiserEnv() {
		Defn def;
		// integer
		def = Defn.creationType(Type.Integer);
		def.setGenre(Genre.PredefInteger);
		env.enrichir("integer", def);
		//boolean
		def = Defn.creationType(Type.Boolean);
		def.setGenre(Genre.PredefBoolean);
		env.enrichir("boolean", def);
		//real
		def = Defn.creationType(Type.Real);
		def.setGenre(Genre.PredefReal);
		env.enrichir("real", def);
		//false
		def = Defn.creationConstBoolean(false);
		def.setGenre(Genre.PredefFalse);
		env.enrichir("false", def);
		//true
		def = Defn.creationConstBoolean(true);
		def.setGenre(Genre.PredefTrue);
		env.enrichir("true", def);
		//max_int
		def = Defn.creationConstInteger(Integer.MAX_VALUE);
		def.setGenre(Genre.PredefMaxInt);
		env.enrichir("max_int", def);

		// ------------
		// DONE
		// ------------
	}

	/**************************************************************************
	 * PROGRAMME
	 **************************************************************************/
	private void verifier_PROGRAMME(Arbre a) throws ErreurVerif {
		initialiserEnv();
		verifier_LISTE_DECL(a.getFils1());
		verifier_LISTE_INST(a.getFils2());
	}

	/**************************************************************************
	 * LISTE_DECL
	 * 
	 * Vérifie une liste de déclarations.
	 * Examine les déclarations dans l'ordre dans lequel elles sont faîtes dans
	 * le programme.
	 **************************************************************************/
	private void verifier_LISTE_DECL(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		
		case Vide:
			Decor dec_vide = new Decor();
			dec_vide.setInfoCode(0);
			a.setDecor(dec_vide);
			break;
			
		case ListeDecl:
			verifier_LISTE_DECL(a.getFils1());
			verifier_DECL(a.getFils2());
			Decor dec = new Decor();
			/*InfoCode contient ici le nombre de cases mémoires nécéssitées pour faire les déclarations*/
			dec.setInfoCode(a.getFils1().getDecor().getInfoCode()+a.getFils2().getDecor().getInfoCode());
			a.setDecor(dec);
			
			break;
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_DECL");
		}
	}

	/**************************************************************************
	 * DECL
	 * 
	 * Vérifier une déclaration.
	 * Vérifie d'abord la règle TYPE pour pouvoir remonter le type et décorer
	 * les identificateurs rencontrés dans la LISTE_IDENT.
	 **************************************************************************/
	private void verifier_DECL(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Decl:
			Type type_decl = verifier_TYPE(a.getFils2());
			verifier_LISTE_IDENT(a.getFils1(), type_decl);
			Decor dec = new Decor();
			/*InfoCode contient ici le nombre de cases mémoires nécéssitées*/
			dec.setInfoCode(a.getFils1().getDecor().getInfoCode());
			a.setDecor(dec);
			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_DECL");
		}
	}

	/**************************************************************************
	 * LISTE_IDENT
	 * 
	 * Vérifier une liste d'identificateurs.
	 * Examine les identificateurs dans l'ordre dans lequel ils sont déclarés.
	 **************************************************************************/
	private void verifier_LISTE_IDENT(Arbre a, Type type) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Vide:
			Decor dec_vide = new Decor();
			dec_vide.setInfoCode(0);
			a.setDecor(dec_vide);
			
			break;
			
		case ListeIdent:
			verifier_LISTE_IDENT(a.getFils1(), type);
			verifier_IDENT(a.getFils2(), type, true);
			Decor dec = new Decor();
			/*InfoCode contient ici le nombre de cases mémoires nécéssitées*/
			dec.setInfoCode(a.getFils1().getDecor().getInfoCode()+a.getFils2().getDecor().getInfoCode());
			a.setDecor(dec);
			break;
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_IDENT");
		}
	}
	
	/**************************************************************************
	 * IDENT
	 * 
	 * Vérifier et décorer un identificateur.
	 * Le comportement varie selon que l'identificateur se trouve dans la partie
	 * "déclaration" (règle LISTE_DECL) ou dans la partie "instructions"
	 * (règle LISTE_INST).
	 **************************************************************************/
	private void verifier_IDENT(Arbre a, Type t, boolean estDECL) throws ErreurVerif {
		
		if(estDECL) {
			/* On vérifie que l'identificateur n'est pas déjà utilisé */
			if(env.chercher(a.getChaine()) != null) {
				/* ERREUR : Identificateur déjà utilisé */
				ErreurContext.ErreurDejaDeclare.leverErreurContext(null, a.getNumLigne());
			}
			
			/* On enrichit l'environnement avec ce nouvel identificateur */
			Defn def_ident = Defn.creationVar(t);
			env.enrichir(a.getChaine(), def_ident);
			
			/* On décore le noeud avec ces informations */
			Decor dec = new Decor(def_ident);
			/*La déclaration va occuper une case mémoire : on utilise InfoCode pour stocker cette information*/
			dec.setInfoCode(1);
			a.setDecor(dec);
		}
		else {
			/* On vérifie que l'identificateur est bien déclaré */
			Defn def;
			if((def = env.chercher(a.getChaine())) == null) {
				/* ERREUR CONTEXTE : Identificateur non déclaré ! */
				ErreurContext.ErreurPasDeclare.leverErreurContext(null, a.getNumLigne());
			}
			
			/* On décore l'identificateur avec sa Defn et son Type */
			Decor dec = new Decor(def, def.getType()) ;
			// on utilise un registre pour enregistrer la valeur dans un ident 
			dec.setInfoCode(1);
			a.setDecor(dec);
		}
	}
	
	/**************************************************************************
	 * TYPE
	 * 
	 * Vérifier un type et le retourner sous forme d'objet Type.
	 **************************************************************************/
	private Type verifier_TYPE(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		/* Dans le cas d'un identificateur, vérifier que c'est bien un identificateur de type */
		case Ident:

			Defn def;
			if((def = env.chercher(a.getChaine())) == null) {
				/* ERREUR CONTEXTE : Identificateur non déclaré */
				ErreurContext.ErreurPasDeclare.leverErreurContext(null, a.getNumLigne());
			}
			else if(def.getNature() != NatureDefn.Type) {
				/* ERREUR : Pas un identificateur de type */
				ErreurContext.ErreurIdentificateurTypeNonReconnu.leverErreurContext(null, a.getNumLigne());
			}
			
			/* Retourner le type de l'identificateur */
			return def.getType();
			
		/* Dans le cas d'un intervalle, on retourne le type d'intervalle */
		case Intervalle:
			return verifier_TYPE_INTERVALLE(a);
			
		/* Dans le cas d'un tableau, il faut vérifier l'intervalle et renvoyer le type du tableau */
		case Tableau:
			Type type_inter = verifier_TYPE_INTERVALLE(a.getFils1());
			Type type_elem = verifier_TYPE(a.getFils2());
			
			return Type.creationArray(type_inter, type_elem);

		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_TYPE");
		}
	}
	
	/**************************************************************************
	 * TYPE_INTERVALLE
	 * 
	 * Vérifier la déclaration d'un intervalle et renvoyer le Type correspondant.
	 **************************************************************************/
	private Type verifier_TYPE_INTERVALLE(Arbre a) throws ErreurVerif {

		/* Vérification grammaticale */
		int b_inf = verifier_EXP_CONST(a.getFils1());
		int b_sup = verifier_EXP_CONST(a.getFils2());
		
		return Type.creationInterval(b_inf, b_sup);
	}

	/**************************************************************************
	 * EXP_CONST
	 * 
	 * Vérifier une expression constante et renvoyer sa valeur numérique.
	 **************************************************************************/
	private int verifier_EXP_CONST(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		/* Dans le cas d'un identificateur, il faut vérifier que c'est un identificateur constant de type entier */
		case Ident:
			Defn def;
			if((def = env.chercher(a.getChaine())) == null) {
				/* ERREUR : Identificateur non déclaré */
				ErreurContext.ErreurPasDeclare.leverErreurContext(null, a.getNumLigne());
			}
			else if(def.getNature() != NatureDefn.ConstInteger) {
				/* ERREUR : Pas un identificateur de constante entière */
				ErreurContext.ErreurType.leverErreurContext(null, a.getNumLigne());
			}
			
			return def.getValeurInteger();
			
		/* Dans le cas d'un entier on retourne simlement la valeur */
		case Entier:
			return a.getEntier();
		
		case PlusUnaire:
			return verifier_EXP_CONST(a.getFils1());

		case MoinsUnaire:
			return verifier_EXP_CONST(a.getFils1());

		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_EXP_CONST");
		}
	}

	/**************************************************************************
	 * LISTE_INST
	 * 
	 * Vérifier une liste d'instruction.
	 * Examine les instructions dans l'ordre du programme.
	 **************************************************************************/
	void verifier_LISTE_INST(Arbre a) throws ErreurVerif {
		switch (a.getNoeud()) {
		
		case Vide: 
			break;
			
		case ListeInst:
			verifier_LISTE_INST(a.getFils1()); 
			verifier_INST(a.getFils2());
			break;
			
		default	: 
			throw new ErreurInterneVerif( "Arbre incorrect dans verifier_LISTE_INST")  ; 
		} 
	}

	/**************************************************************************
	 * INST
	 * 
	 * Vérifier une instruction.
	 **************************************************************************/
	private void verifier_INST(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		
		case Nop:
			break;
			
		case Affect:
			Defn def_var_affect;
			/* Vérification de la grammaire */
			def_var_affect = verifier_PLACE(a.getFils1(), true);
			verifier_EXP(a.getFils2());
			
			/* Vérification du contexte */
			/* - On affecte bien une variable */
			if(def_var_affect.getNature() != NatureDefn.Var) {
				/* ERREUR CONTEXTE : On essaye d'affecter autre chose qu'une variable */
				//TODO : Nouvelle erreur ?
				throw new ErreurInterneVerif("Le membre de gauche de l'affectation n'est pas une variable (ligne " + a.getNumLigne() + ")");
			}
			/* - L'affectation respecte le typage */
			ResultatAffectCompatible res_affect = ReglesTypage.affectCompatible(getTypeNoeud(a.getFils1()), getTypeNoeud(a.getFils2()));
			if(!res_affect.getOk()) {
				/* ERREUR CONTEXTE : Affectation illégale ! */
				ErreurContext.ErreurAffectation.leverErreurContext(null, a.getNumLigne());
			}
			
			if(res_affect.getConv2()) {
				/* Ajout d'un noeud conversion */
				addNoeudConv(a, 2);
			}
			
			/* Décoration du noeud avec le type */
			Decor decAff = new Decor(getTypeNoeud(a.getFils1())) ;
			decAff.setInfoCode(a.getFils2().getDecor().getInfoCode());
			//System.out.println("Infocode ajouté : " + a.getFils1().getDecor().getInfoCode() + " et " + a.getFils2().getDecor().getInfoCode());
			a.setDecor(decAff);			
			break;
			
		case Pour:
			/* Vérification de la grammaire */
			verifier_PAS(a.getFils1());
			verifier_LISTE_INST(a.getFils2());
			break;
			
		case TantQue:
			/* Vérification de la grammaire */
			verifier_EXP(a.getFils1());
			verifier_LISTE_INST(a.getFils2());
			
			/* Vérification du contexte */
			/* - Le type de l'expression doit être booléen */
			if(getTypeNoeud(a.getFils2()) != Type.Boolean) {
				/* ERREUR CONTEXTE : Condition de type non booléen */
				//TODO : Nouvelle erreur ?
				throw new ErreurInterneVerif("La condition n'est pas un booléen (ligne " + a.getNumLigne() + ")");
			}
			break;
			
		case Si:
			/* Vérification de la grammaire */
			verifier_EXP(a.getFils1());
			verifier_LISTE_INST(a.getFils2());
			verifier_LISTE_INST(a.getFils3());
			
			/* Vérification du contexte */
			/* - L'expression doit être de type booléen */
			if(getTypeNoeud(a.getFils1()) != Type.Boolean) {
				/* ERREUR CONTEXTE : Condition de type non booléen */
				//TODO : Nouvelle erreur ?
				throw new ErreurInterneVerif("La condition n'est pas un booléen (ligne " + a.getNumLigne() + ")");
			}
			break;
			
		case Lecture:
			Defn def_var_lue;
			def_var_lue = verifier_PLACE(a.getFils1(), false);
			
			/* Vérifier que la variable est de type Real ou Integer */
			if(def_var_lue.getType() != Type.Real && def_var_lue.getType() != Type.Integer) {
				/* ERREUR CONTEXTE : Tentative de lecture d'une variable de type invalide */
				ErreurContext.ErreurVariableRead.leverErreurContext(null, a.getNumLigne());
			}
			break;
			
		case Ecriture:
			/* Vérification de la grammaire */
			verifier_LISTE_EXP(a.getFils1());
			break;

		case Ligne:
			break;
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_INST");
		}
	}
	
	/**************************************************************************
	 * PLACE
	 * 
	 * Vérifier la règle PLACE, et retourner la def de l'identificateur associé.
	 **************************************************************************/
	private Defn verifier_PLACE(Arbre a, boolean setDecor) throws ErreurVerif {
		switch(a.getNoeud()) {
		
		case Ident:
			/* On vérifie et décore l'identificateur */
			verifier_IDENT(a, null, false);
			/* On retourne la nature de l'identificateur */
			return getDefNoeud(a);
			
		case Index:
			/* On fait remonter la def du tableau */
			return verifier_INDEX(a, setDecor);
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_PLACE");
		}
	}

	/**************************************************************************
	 * LISTE_EXP
	 * 
	 * Vérifier une liste d'expression.
	 * S'assure en particulier que le type des expressions est compatible avec
	 * l'écriture.
	 **************************************************************************/
	private void verifier_LISTE_EXP(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		
		case Vide:
			break;
			
		case ListeExp:
			/* Vérifier la grammaire de l'expression et son type :
			 * 		Seulement des types String, Real et Interval */
			verifier_EXP(a.getFils2());
			Type type_exp = getTypeNoeud(a.getFils2());
			
			if(type_exp != Type.String && type_exp != Type.Real && type_exp != Type.Integer) {
				/* ERREUR CONTEXTE : On essaye d'écrire autre chose qu'une chaîne, un entier ou un réel */
				ErreurContext.ErreurExpressionWrite.leverErreurContext(null, a.getNumLigne());
			}
			
			/* Continuer avec la suite de la liste */
			verifier_LISTE_EXP(a.getFils1());
			break;
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_EXP");
		}
	}
	
	/**************************************************************************
	 * INDEX
	 * 
	 * Vérification de l'indexation d'un tableau, et retour de la def de
	 * l'identificateur associé au tableau indéxé.
	 **************************************************************************/
	private Defn verifier_INDEX(Arbre a, boolean setDecor) throws ErreurVerif{
		
		Defn def_place;
		/* Vérifier la grammaire */
		def_place = verifier_PLACE(a.getFils1(), setDecor);
		verifier_EXP(a.getFils2());
		
		/* Vérifier le contexte */
		ResultatBinaireCompatible res_index = ReglesTypage.binaireCompatible(a.getNoeud(), getTypeNoeud(a.getFils1()), getTypeNoeud(a.getFils2()));
		if(!res_index.getOk()) {
			/* ERREUR : Indexation illégale ! */
			//TODO: ajout erreur ?
			throw new ErreurInterneVerif("Indexation illégale (ligne " + a.getNumLigne() + ")");
		}
		
		/* Décorer le noeud si nécessaire */
		if(setDecor) {
			Decor dec = new Decor(res_index.getTypeRes()) ; 
			dec.setInfoCode(a.getFils1().getDecor().getInfoCode()+a.getFils2().getDecor().getInfoCode()); // finalement on le calcule déja dans ident
			a.setDecor(dec);
		}
		
		/* Faire remonter la def du tableau */
		return def_place;
	}

	/**************************************************************************
	 * EXP
	 **************************************************************************/
	private Type verifier_EXP(Arbre a) throws ErreurVerif {
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
			/* Vérification de la grammaire */
			verifier_EXP(a.getFils1());
			verifier_EXP(a.getFils2());
			
			/* Vérification du contexte */
			ResultatBinaireCompatible res_binaire = ReglesTypage.binaireCompatible(a.getNoeud(), getTypeNoeud(a.getFils1()), getTypeNoeud(a.getFils2()));
			if(!res_binaire.getOk()) {
				/* ERREUR: Opération binaire illégale ! */
				System.out.println("Erreur flag 1 !\n Type gauche :" + getTypeNoeud(a.getFils1()) + "\n Type droite :" + getTypeNoeud(a.getFils2()) + "Noeud : " + a.getNoeud());
				ErreurContext.ErreurType.leverErreurContext(null, a.getNumLigne());
			}
			
			/* Ajout de noeuds Conversion si nécessaire */
			if(res_binaire.getConv1()) {
				addNoeudConv(a, 1);
			}
			else if(res_binaire.getConv2()) {
				addNoeudConv(a, 2);
			}
			
			/* Décor du noeud */
			Decor decb = new Decor(res_binaire.getTypeRes()) ; 
			// le nombre de registre est la somme des registres utilises par le fils1 et le fils2 
			decb.setInfoCode(a.getFils1().getDecor().getInfoCode() + a.getFils2().getDecor().getInfoCode());
			a.setDecor(decb);
			return res_binaire.getTypeRes();

			
		case PlusUnaire:
		case MoinsUnaire:
		case Non:
			/* Vérifier la grammaire */
			verifier_EXP(a.getFils1());
			
			/* Vérifier le contexte */
			ResultatUnaireCompatible res_unaire = ReglesTypage.unaireCompatible(a.getNoeud(), getTypeNoeud(a.getFils1()));
			
			if(!res_unaire.getOk()) {
				/* ERREUR: Opération unaire illégale ! */
				ErreurContext.ErreurType.leverErreurContext(null, a.getNumLigne());
			}
			
			/* Décoration du noeud */
			Decor decu = new Decor(res_unaire.getTypeRes()) ; 
			decu.setInfoCode(a.getFils1().getDecor().getInfoCode());
			a.setDecor(decu);
			return res_unaire.getTypeRes();

			
		case Index:
			verifier_INDEX(a, true);
			return null;
			
		case Conversion:
			/* Vérifier la grammaire */
			verifier_EXP(a.getFils1());
			
			/* Décorer le noeud */
			Decor decConv = new Decor(Type.Real);
			//On ajoute un registre pour enregistrer la conversion 
			decConv.setInfoCode(a.getFils1().getDecor().getInfoCode()+1);
			a.setDecor(decConv);
			return Type.Real;
			
		case Entier:
			//System.out.println("Entier");
			Decor decEnt = new Decor(Type.Integer) ; 
			decEnt.setInfoCode(0);
			a.setDecor(decEnt);
			return Type.Integer;
			
		case Reel:
			Decor decReal = new Decor(Type.Real);
			decReal.setInfoCode(0);
			a.setDecor(decReal);
			return Type.Real;
			
		case Chaine:
			Decor decChaine = new Decor(Type.String);
			decChaine.setInfoCode(0);
			a.setDecor(decChaine); 
			return Type.String; //on est pas sûrs
			
		case Ident:
			verifier_IDENT(a, null, false);
			return a.getDecor().getType();
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_EXP");
		}
	}

	/**************************************************************************
	 * PAS
	 * 
	 * Vérifier le pas.
	 * Vérifier également le contexte.
	 **************************************************************************/
	private void verifier_PAS(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		
		case Increment:
		case Decrement:
			
			/* Vérification de la grammaire */
			verifier_IDENT(a.getFils1(), null, false);
			verifier_EXP(a.getFils2());
			verifier_EXP(a.getFils3());
			
			/* Vérification du contexte */
			/* - C'est bien une variable qui contrôle la boucle */
			if(getDefNoeud(a.getFils1()).getNature() != NatureDefn.Var) {
				/* ERREUR CONTEXTE : Pas un identificateur de variable */
				//TODO : Nouvelle erreur ?
				throw new ErreurInterneVerif("Pas un identificateur de variable (ligne " + a.getNumLigne() + ")");
			}
			
			/* - La variable et les expressions doivent être de type Integer */
			for(int i = 1 ; i <= 3 ; i++) {
				if(getTypeNoeud(a.getFils(i)) != Type.Integer) {
					/* ERREUR CONTEXTE : Mauvais typage dans le pas */
					//TODO : Nouvelle erreur ?
					throw new ErreurInterneVerif("Mauvais typage dans le pas (ligne " + a.getNumLigne() + ")");
				}
			}
			break;
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_PAS");
		}
	}
	
	// Autres fonctions
	
	/**************************************************************************
	 * getTypeNoeud
	 * 
	 * Retourne le type du noeud, en se basant sur son décor
	 **************************************************************************/
	private Type getTypeNoeud(Arbre a) {
		
		Decor dec;
		if((dec = a.getDecor()) == null) {
			/* ERREUR INTERNE : Pas de décor ! */
			throw new ErreurInterneVerif("Pas de décor sur noeud " + a + " (ligne " + a.getNumLigne() + ")");
		}
		else if(dec.getType() == null) {
			/* ERREUR INTERNE : Pas de type dans le décor ! */
			throw new ErreurInterneVerif("Pas de type dans le décor du noeud " + a + " (ligne " + a.getNumLigne() + ")");
		}
		
		return dec.getType();
	}
	
	/**************************************************************************
	 * getNatureNoeud
	 * 
	 * Retourne la nature du noeud, en se basant sur son décor
	 * Le noeud visé doit être un identificateur
	 **************************************************************************/
	private Defn getDefNoeud(Arbre a) {
	
		Decor dec;
		if((dec = a.getDecor()) == null) {
			/* ERREUR INTERNE : Pas de décor ! */
			throw new ErreurInterneVerif("Pas de décor sur noeud " + a + " (ligne " + a.getNumLigne() + ")");
		}
		else if(dec.getDefn() == null) {
			/* ERREUR INTERNE : Pas de defn dans le décor ! */
			throw new ErreurInterneVerif("Pas de defn dans le décor du noeud " + a + " (ligne " + a.getNumLigne() + ")");
		}
		
		return dec.getDefn();
	}
	
	/**************************************************************************
	 * addNoeudConv
	 * 
	 * Ajoute un noeud Convertion entre le noeud a et son fils d'indice numFils
	 **************************************************************************/
	private void addNoeudConv(Arbre a, int numFils) {
		/* Créer le noeud */
		Arbre conv = Arbre.creation1(Noeud.Conversion, a.getFils(numFils), a.getNumLigne());
		/* Le décorer */
		Decor decConv = new Decor(Type.Real);
		decConv.setInfoCode( a.getFils(numFils).getDecor().getInfoCode());
		conv.setDecor(decConv);
		/* Le positionner */
		a.setFils(numFils, conv);
	}
}
