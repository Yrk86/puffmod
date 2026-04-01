Plan du Mod "Façade Légit" (HUD/UX uniquement)

Objectifs
- Proposer un mod utilitaire crédible et autonome : keystrokes, CPS, FPS, ping, reach réel (lecture seulement), armor status.
- Offrir une configuration propre via un bouton “Mod Settings” dans le menu Échap.
- Éviter toute trace ou mention de fonctionnalités cachées/cheat (non couvertes ici).

Fonctionnalités HUD
- Keystrokes : affichage des touches ZQSD + clics, couleurs configurables, opacité et taille ajustables.
- CPS : clics gauche/droit séparés, fenêtre de calcul sur 1s.
- FPS & Ping : chiffres simples avec suffixes, option de couleur dynamique (vert/jaune/rouge).
- Reach Display : valeur de portée réelle (lecture côté client), arrondie à 2 décimales.
- Armor/Item : durabilité et nom de l’item tenu, option d’icônes.
- Layout : position ancrable (haut/bas, gauche/droite), marge personnalisable, toggle individuel par widget.

Menus et configuration
- Menu Échap : bouton “Mod Settings” ouvrant un écran dédié HUD (style Forge/Fabric).
- Écran de config : sliders (taille, opacité), pickers (couleurs), boutons toggle par module HUD.
- Keybinds documentés : uniquement pour ouvrir le menu HUD; aucun bind caché.
- Persistance : sauvegarde/chargement des options dans un fichier config clair (ex: configs/puff-hud.json).

Techniques d’implémentation
- Rendu : utiliser les événements standards (RenderGameOverlayEvent) et la font renderer vanilla.
- Mesures : calcul du CPS via timestamp des clics; ping via NetHandlerPlayClient; reach via distance du raytrace client.
- Utils : helpers de rendu (boîtes arrondies simples, bords, ombres discrètes) dans un util RenderUtils.
- Performance : éviter l’allocation par frame; réutiliser objets (StringBuilder, Vec3) si besoin.

Structure recommandée
src/main/java/com/client/utility/
├── hud/          (keystrokes, cps, fps, ping, reach, armor)
├── menu/         (écran Mod Settings, composants UI)
├── util/         (RenderUtils, math, couleurs, profil config)
└── core/         (module de base, gestion des settings)
    ├── legit/        (modules HUD/UX uniquement)
    ├── combat/       (modules non legit liés au combat)
    ├── movement/     (modules non legit de déplacement)
    └── impl/         (profils/configs, keybinds, glue)
└── Mixins/            (pour injecter le code discrètement)

Checklist minimale
- HUD visible en solo et multi, sans crash.
- Chaque widget activable/désactivable, positions sauvegardées.
- Bouton “Mod Settings” présent et fonctionnel dans le menu Échap.
- Aucune référence ou dépendance à des fonctionnalités non-légit.
