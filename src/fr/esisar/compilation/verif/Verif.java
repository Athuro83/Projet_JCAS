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
		//false
		def = Defn.creationType(Type.Boolean);
		def.setGenre(Genre.PredefFalse);
		env.enrichir("false", def);
		//true
		def = Defn.creationType(Type.Boolean);
		def.setGenre(Genre.PredefTrue);
		env.enrichir("true", def);
		//max_int
		def = Defn.creationType(Type.Integer);
		def.setGenre(Genre.PredefMaxInt);
		env.enrichir("max_int", def);
		//real
		def = Defn.creationType(Type.Real);
		def.setGenre(Genre.PredefReal);
		env.enrichir("real", def);

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


	private void verifier_DECL(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Decl:
			verifier_LISTE_IDENT(a, a.getFils1());
			verifier_TYPE(a.getFils2());
			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_DECL");
		}
	}

	private void verifier_LISTE_IDENT(Arbre aDecl, Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Vide:
			break;
		case ListeIdent:
			verifier_LISTE_IDENT(aDecl, a.getFils1());
			verifier_IDENT_decl(aDecl, a.getFils2());
			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_LISTE_IDENT");
		}
	}

	private Type getTypeDecl(Arbre a) throws ErreurVerif{
			switch(a.getNoeud()) {
			case Ident:
				if(a.getChaine().toLowerCase().equals("real")){
					return Type.Real;
				}
				else if(a.getChaine().toLowerCase().equals("boolean")){
					return Type.Boolean;
				}
				else if(a.getChaine().toLowerCase().equals("string")){
					return Type.String;
				}
				else if(a.getChaine().toLowerCase().equals("integer")){
					return Type.Integer;
				}else {
					ErreurContext e = ErreurContext.ErreurIdentificateurTypeNonReconnu;
					e.leverErreurContext(null, a.getNumLigne());
				}
			case Tableau:
				return(Type.creationArray(Type.creationInterval(a.getFils1().getFils1().getEntier(), a.getFils1().getFils2().getEntier()), getTypeDecl(a.getFils2())));
			case Intervalle:
				return Type.creationInterval(a.getFils1().getEntier(), a.getFils2().getEntier());
				
			default :
				throw new ErreurInterneVerif("Arbre incorrecte dans le getTypeDecl" );
			}
		
	}
	
	private void verifier_IDENT_decl(Arbre aDecl, Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Ident:
			if(env.chercher(a.getChaine()) == null ) {
				Defn def = Defn.creationVar(getTypeDecl(aDecl.getFils2()));
				env.enrichir(a.getChaine().toLowerCase(), def);
			}
			else {
				ErreurContext e = ErreurContext.ErreurDejaDeclare;
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
			a.setDecor(new Decor (Defn.creationVar(getTypeDecl(aDecl.getFils2()))));
			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_IDENT");
		}
	}
	
	private void verifier_TYPE(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Ident:
				if( ! (a.getChaine().toLowerCase().equals("integer") 
						||a.getChaine().toLowerCase().equals("boolean")
						||a.getChaine().toLowerCase().equals("real") ) ) {
					ErreurContext e = ErreurContext.ErreurNatureIDENT;
					e.leverErreurContext(null, a.getNumLigne());
				}
			break;
		case Intervalle:
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			if(a.getFils1().getNoeud()!=Noeud.Entier || a.getFils2().getNoeud()!=Noeud.Entier ) {
				ErreurContext e = ErreurContext.ErreurType;
				e.leverErreurContext(null, a.getNumLigne());
			}
			break;
		case Tableau:
			verifier_TYPE_INTERVALLE(a.getFils1());
			verifier_TYPE(a.getFils2());

			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_TYPE");
		}
	}
	private void verifier_TYPE_INTERVALLE(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Intervalle:
			verifier_EXP_CONST(a.getFils1());
			verifier_EXP_CONST(a.getFils2());
			break;
		default:
			throw new ErreurInterneVerif("Arbre incorrect dans verifier_TYPE_INTERVALLE");
		}
	}

	private void verifier_EXP_CONST(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Ident:

			break;
		case Entier:

			break;
		case PlusUnaire:
			verifier_EXP_CONST(a.getFils1());

			break;
		case MoinsUnaire:
			verifier_EXP_CONST(a.getFils1());

			break;
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
			break	; 
		case ListeInst:
			verifier_LISTE_INST(a.getFils1()); 
			verifier_INST(a.getFils2());
			break	;
		default	: 
			throw new ErreurInterneVerif( "Arbre incorrect dans verifier_LISTE_INST")  ; 
		} 
	}

	private void verifier_INST(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Nop:
			break;
		case Affect:
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
			verifier_PLACE(a.getFils1());
			verifier_EXP(a.getFils2());
			
			Type t1,t2;
			Decor dec = a.getFils2().getDecor();
			
			/* Déterminer le type de la PLACE */
			dec = a.getFils1().getDecor();
			if(dec != null) {
				t1 = dec.getType();
				if(t1 == null) t1 = dec.getDefn().getType();
			}
			else {
				switch(a.getFils1().getNoeud()) {
				
					case Entier:
						t1 = Type.Integer;
						break;
						
					case Reel:
						t1 = Type.Real;
						break;
						
					case Chaine:
						t1 = Type.String;
						break;
						
					default:
						throw new ErreurInterneVerif( "Noeud sans décor dans verifier_INST")  ; 
				}
			}

			
			/* Déterminer le type de l'EXP */
			dec = a.getFils2().getDecor();
			if(dec != null) {
				t2 = dec.getType();
				if(t2 == null) t2 = dec.getDefn().getType();
			}
			else {
				switch(a.getFils2().getNoeud()) {
				
					case Entier:
						t2 = Type.Integer;
						break;
						
					case Reel:
						t2 = Type.Real;
						break;
						
					case Chaine:
						t2 = Type.String;
						break;
						
					default:
						throw new ErreurInterneVerif( "Noeud sans décor dans verifier_INST")  ; 
				}
			}
			
			//System.out.println("Type :" + t2);
			ResultatAffectCompatible rac = ReglesTypage.affectCompatible(t1, t2);
			//System.out.println("Vérif contexte :" + rac);
			if(!rac.getOk()) {
				ErreurContext e = ErreurContext.ErreurAffectation;
				e.leverErreurContext(null, a.getNumLigne());
			}
			a.setDecor(new Decor(t1));
			//System.out.println("Décor en place !");
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

	private void verifier_PLACE(Arbre a) throws ErreurVerif {
		switch(a.getNoeud()) {
		case Ident:
			Defn def;
			if((def = env.chercher(a.getChaine())) == null ) {
				ErreurContext e = ErreurContext.ErreurPasDeclare;
				e.leverErreurContext(null, a.getNumLigne());
			}
			a.setDecor(new Decor (def));
			/*
			if( a.getChaine().toLowerCase().equals("integer") 
					||a.getChaine().toLowerCase().equals("boolean")
					||a.getChaine().toLowerCase().equals("false")
					||a.getChaine().toLowerCase().equals("true")
					||a.getChaine().toLowerCase().equals("max_int")
					||a.getChaine().toLowerCase().equals("real") )  {
				ErreurContext e = ErreurContext.ErreurNatureIDENT;
				e.leverErreurContext(null, a.getNumLigne());
			}
			*/
			break;
		case Index:
			if(a.getArite()!=2) {
				ErreurContext e = ErreurContext.ErreurArite;
				e.leverErreurContext(null, a.getNumLigne());
			}
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
		case Et :
		case Ou :
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
			
		case Egal :
		case Inf :
		case SupEgal :
		case InfEgal :
		case Sup :
		case NonEgal :
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
			
		case Plus:
		case Moins:
		case Mult:
		case DivReel:
		case Reste:
		case Quotient:
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
			
		case PlusUnaire:
		case MoinsUnaire:
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

		case Non:
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
		case Index:
			return verifier_INDEX(a);
		case Conversion:
			a.setDecor(new Decor(Type.Real));
			return Type.Real;
		case Entier:
			a.setDecor(new Decor(Type.Integer));
			return Type.Integer;
		case Reel:
			a.setDecor(new Decor(Type.Real));
			return Type.Real;
		case Chaine:
			a.setDecor(new Decor(Type.String));
			return Type.String; //on est pas sûrs
		case Ident:
			if(env.chercher(a.getChaine()) == null ) {
				ErreurContext e = ErreurContext.ErreurPasDeclare;
				e.leverErreurContext(null, a.getNumLigne());
			}
			Defn defn = env.chercher(a.getChaine().toLowerCase());
			a.setDecor(new Decor(defn));
			return(defn.getType());
			
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





	// ------------------------------------------------------------------------
	// COMPLETER les operations de vérifications et de décoration pour toutes 
	// les constructions d'arbres
	// ------------------------------------------------------------------------

}
