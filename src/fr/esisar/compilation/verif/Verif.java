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
	 **************************************************************************/
	private void verifier_LISTE_DECL(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Vide:
			break;
		case ListeDecl:
			verifier_LISTE_DECL(a.getFils1());
			verifier_DECL(a.getFils2());
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
	 * les identificateurs rencontrés dans la LISTE_IDENT
	 **************************************************************************/
	private void verifier_DECL(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Decl:
			Type type_decl = verifier_TYPE(a.getFils2());
			verifier_LISTE_IDENT(a.getFils1(), type_decl);
			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_DECL");
		}
	}

	/**************************************************************************
	 * LISTE_IDENT
	 **************************************************************************/
	private void verifier_LISTE_IDENT(Arbre a, Type type) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Vide:
			break;
			
		case ListeIdent:
			verifier_LISTE_IDENT(a.getFils1(), type);
			verifier_IDENT(a.getFils2(), type, true);
			break;
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_IDENT");
		}
	}
	
	/**************************************************************************
	 * IDENT
	 * 
	 * Vérifier et décorer un identificateur
	 **************************************************************************/
	private void verifier_IDENT(Arbre a, Type t, boolean estDECL) throws ErreurVerif {
		
		if(estDECL) {
			/* On vérifie que l'identificateur n'est pas déjà utilisé */
			if(env.chercher(a.getChaine()) != null) {
				/* ERREUR : Identificateur déjà utilisé */
				ErreurContext.ErreurDejaDeclare.leverErreurContext(null, a.getNumLigne());
			}
			
			/* On enrichie l'environnement avec ce nouvel identificateur */
			Defn def_ident = Defn.creationVar(t);
			env.enrichir(a.getChaine(), def_ident);
			
			/* On décore le noeud avec ces informations */
			a.setDecor(new Decor(def_ident));
		}
		else {
			/* On vérifie que l'identificateur est bien déclaré */
			Defn def;
			if((def = env.chercher(a.getChaine())) == null) {
				/* ERREUR : Identificateur non déclaré ! */
				ErreurContext.ErreurNonRepertoriee.leverErreurContext(null, a.getNumLigne());
			}
			
			/* On décore l'identificateur avec sa Defn et son Type */
			a.setDecor(new Decor(def, def.getType()));
		}
	}
	
	/**************************************************************************
	 * TYPE
	 * 
	 * Vérifier un type et le retourner sous forme d'objet Type
	 **************************************************************************/
	private Type verifier_TYPE(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		/* Dans le cas d'un identificateur, vérifier que c'est bien un identificateur de type */
		case Ident:

			Defn def;
			if((def = env.chercher(a.getChaine())) == null) {
				/* ERREUR : Identificateur non déclaré */
				ErreurContext.ErreurNonRepertoriee.leverErreurContext(null, a.getNumLigne());
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
	 **************************************************************************/
	private Type verifier_TYPE_INTERVALLE(Arbre a) throws ErreurVerif {

		int b_inf = verifier_EXP_CONST(a.getFils1());
		int b_sup = verifier_EXP_CONST(a.getFils2());
		
		return Type.creationInterval(b_inf, b_sup);
	}

	/**************************************************************************
	 * EXP_CONST
	 **************************************************************************/
	private int verifier_EXP_CONST(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		/* Dans le cas d'un identificateur, il faut vérifier que c'est un identificateur constant de type entier */
		case Ident:
			Defn def;
			if((def = env.chercher(a.getChaine())) == null) {
				/* ERREUR : Identificateur non déclaré */
				ErreurContext.ErreurNonRepertoriee.leverErreurContext(null, a.getNumLigne());
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
	 **************************************************************************/
	void verifier_LISTE_INST(Arbre a) throws ErreurVerif {
		switch (a.getNoeud()) {
		
		case Vide: 
			break;
			
		case ListeInst:
			verifier_INST(a.getFils2());
			verifier_LISTE_INST(a.getFils1()); 
			break;
			
		default	: 
			throw new ErreurInterneVerif( "Arbre incorrect dans verifier_LISTE_INST")  ; 
		} 
	}

	/**************************************************************************
	 * INST
	 **************************************************************************/
	private void verifier_INST(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		
		case Nop:
			break;
			
		case Affect:
			/* Vérification de la grammaire */
			verifier_PLACE(a.getFils1());
			verifier_EXP(a.getFils2());
			
			/* Vérification du contexte */
			ResultatAffectCompatible res_affect = ReglesTypage.affectCompatible(getTypeNoeud(a.getFils1()), getTypeNoeud(a.getFils2()));
			if(!res_affect.getOk()) {
				/* ERREUR : Affectation illégale ! */
				ErreurContext.ErreurAffectation.leverErreurContext(null, a.getNumLigne());
			}
			
			//System.out.println("Conversion");
			if(res_affect.getConv2()) {
				/* Ajout d'un noeud conversion */
				addNoeudConv(a, 2);
			}
			
			/* Décoration du noeud avec le type */
			a.setDecor(new Decor(getTypeNoeud(a.getFils1())));
			
//			/*if(a.getArite()!=2) {
//				ErreurContext e = ErreurContext.ErreurArite;
//				e.leverErreurContext(null, a.getNumLigne());
//			}*/
//			verifier_PLACE(a.getFils1());
//			verifier_EXP(a.getFils2());
//			
//			Type t1,t2;
//			Decor dec = a.getFils2().getDecor();
//			
//			/* Déterminer le type de la PLACE */
//			dec = a.getFils1().getDecor();
//			if(dec != null) {
//				t1 = dec.getType();
//				if(t1 == null) t1 = dec.getDefn().getType();
//			}
//			else {
//				switch(a.getFils1().getNoeud()) {
//				
//					case Entier:
//						t1 = Type.Integer;
//						break;
//						
//					case Reel:
//						t1 = Type.Real;
//						break;
//						
//					case Chaine:
//						t1 = Type.String;
//						break;
//						
//					default:
//						throw new ErreurInterneVerif( "Noeud sans décor dans verifier_INST")  ; 
//				}
//			}
//
//			
//			/* Déterminer le type de l'EXP */
//			dec = a.getFils2().getDecor();
//			if(dec != null) {
//				t2 = dec.getType();
//				if(t2 == null) t2 = dec.getDefn().getType();
//			}
//			else {
//				switch(a.getFils2().getNoeud()) {
//				
//					case Entier:
//						t2 = Type.Integer;
//						break;
//						
//					case Reel:
//						t2 = Type.Real;
//						break;
//						
//					case Chaine:
//						t2 = Type.String;
//						break;
//						
//					default:
//						throw new ErreurInterneVerif( "Noeud sans décor dans verifier_INST")  ; 
//				}
//			}
//			
//			//System.out.println("Type :" + t2);
//			ResultatAffectCompatible rac = ReglesTypage.affectCompatible(t1, t2);
//			//System.out.println("Vérif contexte :" + rac);
//			if(!rac.getOk()) {
//				ErreurContext e = ErreurContext.ErreurAffectation;
//				e.leverErreurContext(null, a.getNumLigne());
//			}
//			a.setDecor(new Decor(t1));
//			//System.out.println("Décor en place !");
			break;
			
		case Pour:
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			verifier_PAS(a.getFils1());
			verifier_LISTE_INST(a.getFils2());
			break;
		case TantQue:
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			verifier_EXP(a.getFils1());
			verifier_LISTE_INST(a.getFils2());
			if(a.getFils1().getDecor().getType()!=Type.Boolean) {
				ErreurContext e = ErreurContext.ErreurType;
				e.leverErreurContext(null, a.getNumLigne());
			}
			break;
		case Si:
			if(a.getArite()==3) {
				verifier_EXP(a.getFils1());
				verifier_LISTE_INST(a.getFils2());
				verifier_LISTE_INST(a.getFils3());
				if(a.getFils1().getDecor().getType()!=Type.Boolean) {
					ErreurContext e = ErreurContext.ErreurType;
					e.leverErreurContext(null, a.getNumLigne());
				}
			}
			else if(a.getArite()==2){
				verifier_EXP(a.getFils1());
				verifier_LISTE_INST(a.getFils2());
				if(a.getFils1().getDecor().getType()!=Type.Boolean) {
					ErreurContext e = ErreurContext.ErreurType;
					e.leverErreurContext(null, a.getNumLigne());
				}
			}
			else {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			break;
		case Lecture:
			if(a.getArite()!=1) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			verifier_PLACE(a.getFils1());
			if(a.getFils1().getDecor().getType()!=Type.Integer || a.getFils1().getDecor().getType()!=Type.Real) {
				ErreurContext e = ErreurContext.ErreurType;
				e.leverErreurContext(null, a.getNumLigne());
			}
			break;
		case Ecriture:
			if(a.getArite()!=1) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			verifier_LISTE_EXP(a.getFils1());
			break;

		case Ligne:

			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_INST");
		}
	}


	private void verifier_IDENT_util(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Ident:
			if(env.chercher(a.getChaine()) == null ) {
				ErreurContext e = ErreurContext.ErreurPasDeclare;
				e.leverErreurContext(null, a.getNumLigne());
			}
			if( a.getChaine().toLowerCase().equals("integer") 
					||a.getChaine().toLowerCase().equals("boolean")
					||a.getChaine().toLowerCase().equals("false")
					||a.getChaine().toLowerCase().equals("true")
					||a.getChaine().toLowerCase().equals("max_int")
					||a.getChaine().toLowerCase().equals("real") )  {
				ErreurContext e = ErreurContext.ErreurNatureIDENT;
				e.leverErreurContext(null, a.getNumLigne());
			}
			Defn defn = env.chercher(a.getChaine().toLowerCase());
			a.setDecor(new Decor(defn));
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_IDENT");
		}
	}
	
	/**************************************************************************
	 * PLACE
	 **************************************************************************/
	private void verifier_PLACE(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		
		case Ident:
			/* On vérifie et décore l'identificateur */
			verifier_IDENT(a, null, false);
			break;
			
		case Index:
			verifier_PLACE(a.getFils1());
			verifier_EXP(a.getFils2());
			break;
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_PLACE");
		}
	}

	private void verifier_LISTE_EXP(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Vide:
			break;
		case ListeExp:
			verifier_LISTE_EXP(a.getFils1());
			Type type = verifier_EXP(a.getFils2());
			if(type!=Type.Integer && type != Type.Real && type != Type.String) {
				ErreurContext e = ErreurContext.ErreurType;
				e.leverErreurContext(null, a.getNumLigne());
			}
			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_EXP");
		}
	}
	
	
	private Type verifier_INDEX(Arbre a) throws ErreurVerif{
		switch(a.getNoeud()) {
		case Index:
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			return verifier_INDEX(a.getFils1()).getElement();
		case Ident:
			a.setDecor(new Decor(Defn.creationVar(env.chercher(a.getChaine().toLowerCase()).getType())));
			return(env.chercher(a.getChaine().toLowerCase()).getType());
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_INDEX");
		}
		
	}

	private Type verifier_EXP(Arbre a) throws ErreurVerif {
		ResultatBinaireCompatible RBC;
		ResultatUnaireCompatible RUC;
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
			a.setDecor(new Decor(res_binaire.getTypeRes()));
			return res_binaire.getTypeRes();
			
			/*
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			RBC = ReglesTypage.binaireCompatible(a.getNoeud(), verifier_EXP(a.getFils1()), verifier_EXP(a.getFils2()));
			if(RBC.getOk()) {
				a.setDecor(new Decor(RBC.getTypeRes()));
				return(RBC.getTypeRes());
			}
			else {
				ErreurContext e = ErreurContext.ErreurAffectationBooleanAutre;
				e.leverErreurContext(null, a.getNumLigne());
				return(RBC.getTypeRes());
			}
			*/
			
			/*
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			RBC = ReglesTypage.binaireCompatible(a.getNoeud(),verifier_EXP(a.getFils1()), verifier_EXP(a.getFils2()));
			if(RBC.getOk()) {
			
				if(RBC.getConv1()) {
					Arbre conversion = Arbre.creation1(Noeud.Conversion, a.getFils1(), a.getNumLigne());
					a.setFils1(conversion);
					conversion.setDecor(new Decor(Type.Real));
					a.setDecor(new Decor(RBC.getTypeRes()));
					return(RBC.getTypeRes());
				}
				else if (RBC.getConv2()) {
					Arbre conversion = Arbre.creation1(Noeud.Conversion, a.getFils2(), a.getNumLigne());
					a.setFils2(conversion);
					conversion.setDecor(new Decor(Type.Real));
					a.setDecor(new Decor(RBC.getTypeRes()));
					return(RBC.getTypeRes());
				}
			}
			else {
				ErreurContext e = ErreurContext.ErreurAffectationBooleanAutre;
				e.leverErreurContext(null, a.getNumLigne());
				return(RBC.getTypeRes());
			}
			*/

			/*
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			RBC = ReglesTypage.binaireCompatible(a.getNoeud(), verifier_EXP(a.getFils1()), verifier_EXP(a.getFils2()));
			if(RBC.getOk()) {
				a.setDecor(new Decor(RBC.getTypeRes()));
				return(RBC.getTypeRes());
			}
			else if(RBC.getConv1()) {
				Arbre conversion = Arbre.creation1(Noeud.Conversion, a.getFils1(), a.getNumLigne());
				a.setFils1(conversion);
				conversion.setDecor(new Decor(Type.Real));
				a.setDecor(new Decor(RBC.getTypeRes()));
				return(RBC.getTypeRes());
			}
			else if (RBC.getConv2()) {
				Arbre conversion = Arbre.creation1(Noeud.Conversion, a.getFils2(), a.getNumLigne());
				a.setFils2(conversion);
				conversion.setDecor(new Decor(Type.Real));
				a.setDecor(new Decor(RBC.getTypeRes()));
				return(RBC.getTypeRes());
			}
			else {
				ErreurContext e = ErreurContext.ErreurAffectationNumeriqueAutre;
				e.leverErreurContext(null, a.getNumLigne());
				return(RBC.getTypeRes());
			}
			*/
			
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
			a.setDecor(new Decor(res_unaire.getTypeRes()));
			return res_unaire.getTypeRes();
			
			/*
			if(a.getArite()!=1) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			RUC = ReglesTypage.unaireCompatible(a.getNoeud(),verifier_EXP(a.getFils(1)));
			if(RUC.getOk()) {
				a.setDecor(new Decor(RUC.getTypeRes()));
				return(RUC.getTypeRes());
			}
			else {
				ErreurContext e = ErreurContext.ErreurAffectationNumeriqueAutre;
				e.leverErreurContext(null, a.getNumLigne());
				return(RUC.getTypeRes());
			}
			*/

			/*
			if(a.getArite()!=1) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			RUC = ReglesTypage.unaireCompatible(a.getNoeud(), verifier_EXP(a.getFils(1)));
			if(RUC.getOk()) {
				a.setDecor(new Decor(RUC.getTypeRes()));
				return(RUC.getTypeRes());
			}
			else {
				ErreurContext e = ErreurContext.ErreurAffectationBooleanAutre;
				e.leverErreurContext(null, a.getNumLigne());
				return(RUC.getTypeRes());
			}
			*/
			
		case Index:
			return verifier_INDEX(a);
			
		case Conversion:
			/* Vérifier la grammaire */
			verifier_EXP(a.getFils1());
			
			/* Décorer le noeud */
			a.setDecor(new Decor(Type.Real));
			return Type.Real;
			
		case Entier:
			//System.out.println("Entier");
			a.setDecor(new Decor(Type.Integer));
			return Type.Integer;
			
		case Reel:
			a.setDecor(new Decor(Type.Real));
			return Type.Real;
			
		case Chaine:
			a.setDecor(new Decor(Type.String));
			return Type.String; //on est pas sûrs
			
		case Ident:
			/*if(env.chercher(a.getChaine()) == null ) {
				ErreurContext e = ErreurContext.ErreurPasDeclare;
				e.leverErreurContext(null, a.getNumLigne());
			}
			Defn defn = env.chercher(a.getChaine().toLowerCase());
			a.setDecor(new Decor(defn));
			*/
			verifier_IDENT(a, null, false);
			return a.getDecor().getType();
			
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_EXP");
		}
	}


	private void verifier_PAS(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Increment:
			if(a.getArite()!=3) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
				}
			verifier_IDENT_util(a.getFils1());
			verifier_EXP(a.getFils2());
			verifier_EXP(a.getFils3());
			if(a.getFils1().getDecor().getType()!=Type.Integer || a.getFils2().getDecor().getType()!=Type.Integer 
					|| a.getFils3().getDecor().getType()!=Type.Integer) {
				ErreurContext e = ErreurContext.ErreurType;
				e.leverErreurContext(null, a.getNumLigne());
			}
			if(a.getFils2().getEntier()>a.getFils3().getEntier()) {
				ErreurContext e = ErreurContext.ErreurIndicesInverse;
				e.leverErreurContext(null, a.getNumLigne());
			}
			break;
		case Decrement:
			if(a.getArite()!=3) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			verifier_IDENT_util(a.getFils1());
			verifier_EXP(a.getFils2());
			verifier_EXP(a.getFils3());
			if(a.getFils1().getDecor().getType()!=Type.Integer || a.getFils2().getDecor().getType()!=Type.Integer 
					|| a.getFils3().getDecor().getType()!=Type.Integer) {
				ErreurContext e = ErreurContext.ErreurType;
				e.leverErreurContext(null, a.getNumLigne());
			}
			if(a.getFils2().getEntier()<a.getFils3().getEntier()) {
				ErreurContext e = ErreurContext.ErreurIndicesInverse;
				e.leverErreurContext(null, a.getNumLigne());
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
			/* ERREUR : Pas de décor ! */
			throw new ErreurInterneVerif("Pas de décor sur noeud " + a + " (ligne " + a.getNumLigne() + ")");
		}
		else if(dec.getType() == null) {
			/* ERREUR : Pas de type dans le décor ! */
			throw new ErreurInterneVerif("Pas de type dans le décor du noeud " + a + " (ligne " + a.getNumLigne() + ")");
		}
		
		return dec.getType();
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
		conv.setDecor(new Decor(Type.Real));
		/* Le positionner */
		a.setFils(numFils, conv);
	}
}
