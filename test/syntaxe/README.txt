Description des differents fichiers test :


* Dichotomie_FACTEUR.cas
  ce test contient l'erreur syntaxique pour la regle FACTEUR a la ligne 13

*Dichotomie_INST.cas
  ce test contient l'erreur syntaxique pour la regle INST a la ligne 13

*Dichotomie_LISTE_EXP
  ce fichier teste l'erreur syntaxique pour la regle LISTE_EXP a la ligne 42

*Dicotomie_LISTE_INST
  ce fichier teste l'erreur sntaxique de la regle LISTE_EXP a la ligne 23

*Dichotomie_PLACE
  ce fichier contient une erreur syntaxique pour la regle PLACE a la ligne 30

* testListDecl.cas :
	Au lieu de finir sur un point virgule on a deux points ce qui provoque bien une erreur de syntaxe ligne 2.

* testDecl.cas : 
	Au lieu d'etre separe par deux points, la list_idf et le type sont separes par un point virgule ce qui provoque bien une erreur de syntaxe ligne 4.

* testListIdf.cas :
	A la place d'une virgule entre la liste_idf et l'idf, on a un point virgule ce qui provoque bien une erreur de syntaxe ligne 4.

* testType.cas :
	A la place d'avoir un crochet ouvert ligne 2, on a une parenthese ouvrante ce qui provoque bien une erreur de syntaxe ligne 2.

* testTypeIntervalle.cas :
	A la place d'avoir un double_point on a une virgule ce qui provoque une erreur de syntaxe ligne 2. 

* testConstante.cas :
	A la ligne 11 on a place un '*' devant le 2 ce qui est faux syntaxiquement car les 3 choix possibles sont un '+', un '-' ou rien. Ce qui provoque bien une erreur
syntaxique a la ligne 11.

