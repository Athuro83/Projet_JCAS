package fr.esisar.compilation.verif;

import fr.esisar.compilation.global.src.*;

/**
 * Classe chargée de tester les méthode de la classe ReglesTypage.
 */
public class VerifReglesTypage {

	private Noeud noeudUnaires[];
	private Noeud noeudBinaires[];
	private Type types[];
	
	public VerifReglesTypage() {
		
		/* Initialisation des tableaux de tests */
		this.noeudUnaires = new Noeud[] {Noeud.MoinsUnaire, Noeud.Non, Noeud.PlusUnaire};
		this.noeudBinaires = new Noeud[] {Noeud.DivReel, Noeud.Et, Noeud.Ou, Noeud.Inf,
										  Noeud.InfEgal, Noeud.NonEgal, Noeud.Sup, Noeud.SupEgal,
										  Noeud.Egal, Noeud.Plus, Noeud.Moins, Noeud.Mult,
										  Noeud.Quotient};
		this.types = new Type[] {Type.Boolean, Type.Integer, Type.Real, Type.String};

	}
	
	public static void main(String[] args) throws ErreurVerif {

		VerifReglesTypage verificateur = new VerifReglesTypage();
		
		/* Test de l'affectation (hors tableau) */
		System.out.println("== Test affectCompatible ==\n");
		ResultatAffectCompatible resultAffet;
		
		for(Type t1 : verificateur.types) {
			for(Type t2 : verificateur.types) {
				resultAffet = ReglesTypage.affectCompatible(t1, t2);
				System.out.println("> " + t1 + " := " + t2 + "\n"
								 + "| ok : " + resultAffet.getOk() + "\n"
								 + "| conv2 : " + resultAffet.getConv2() + "\n");
			}
		}
		
		/* Test des opérations unaires */
		System.out.println("== Test unaireCompatible ==\n");
		ResultatUnaireCompatible resultUnaire;
		
		for(Noeud n : verificateur.noeudUnaires) {
			for(Type t : verificateur.types) {
				resultUnaire = ReglesTypage.unaireCompatible(n, t);
				System.out.println("> " + n + " " + t + "\n"
						 + "| ok : " + resultUnaire.getOk() + "\n"
						 + "| typeRes : " + resultUnaire.getTypeRes() + "\n");

			}
		}
		
		/* Test des opérations binaires */
		System.out.println("== Test binaireCompatible ==\n");
		ResultatBinaireCompatible resultBinaire;

		for(Noeud n : verificateur.noeudBinaires) {
			for(Type t1 : verificateur.types) {
				for(Type t2 : verificateur.types) {
					resultBinaire = ReglesTypage.binaireCompatible(n, t1, t2);
					System.out.println("> " + t1 + " " + n + " " + t2 + "\n"
							 + "| ok : " + resultBinaire.getOk() + "\n"
							 + "| conv1 : " + resultBinaire.getConv1() + "\n"
							 + "| conv2 : " + resultBinaire.getConv2() + "\n"
							 + "| typeRes : " + resultBinaire.getTypeRes() + "\n");

				}
			}
		}
		
		ErreurContext.ErreurAffectationBooleanAutre.leverErreurContext(null, 2);
	}

}
