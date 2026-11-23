# PHP Dollar Highlight

Plugin PhpStorm qui colore **uniquement le symbole `$`** des variables PHP, indépendamment du nom de la variable.
Le fichier n'est jamais modifié : seul l'affichage change. Le reste de la coloration du thème est conservé.

```php
$user = $repository->find($id);     // seuls les « $ » sont colorés (défaut #C792EA)
$this->service->execute($_POST);
$$dynamic = ${$other};              // les deux « $ » de $$x et le « $ » de ${ sont colorés
$text = 'Le prix est de $100';      // chaîne littérale : non colorée
// $commentaire                     // commentaire : non coloré
```

## Approche technique

Dans le lexer PHP de PhpStorm, `$nom` est **un seul token** `PhpTokenTypes.VARIABLE` ; `$$x` et `${` produisent
des tokens `DOLLAR` / `DOLLAR_LBRACE` sous un `Variable`. Le plugin utilise donc un `Annotator` silencieux
(`newSilentAnnotation(INFORMATION)`) qui pose un `TextAttributesKey` sur un `TextRange` d'**un seul caractère**.

- Fonctionne par token (feuille PSI) : coût O(1) par élément, aucun parcours du document, incrémental par nature.
- L'attribut est *foreground seul*, sans clé de repli : gras/italique/fond du thème restent intacts.
- Les `$` des commentaires et chaînes littérales ne sont jamais des tokens `VARIABLE` → jamais colorés.
- Aucune API interne ni dépréciée (vérifié avec le Plugin Verifier).

Pourquoi pas un `SyntaxHighlighter` : celui de PHP est fourni par PhpStorm et ne peut pas être étendu pour scinder un token.

## Versions prises en charge

PhpStorm **2026.2 (build 262)** et supérieures (`sinceBuild = 262`, sans `untilBuild`).
Testé/vérifié uniquement avec le build **PS-262.12573.41**. Les versions antérieures ne sont pas supportées (JBR 25 requis par la plateforme).

## Prérequis

- JDK 25 (PhpStorm 2026.2 embarque JBR 25).
- Gradle 9+ (le wrapper `./gradlew` fournit Gradle 9.9.2).

## Compilation

```bash
# Avec un PhpStorm installé (aucun téléchargement du SDK) :
./gradlew buildPlugin -PlocalIde=/chemin/vers/PhpStorm
# Ou en téléchargeant PhpStorm automatiquement :
./gradlew buildPlugin

./gradlew test             # tests automatisés
./gradlew verifyPlugin     # JetBrains Plugin Verifier
```

Le ZIP est généré dans `build/distributions/php-dollar-highlight-0.1.0.zip`.
Si votre `~/.gradle/gradle.properties` force un autre JDK : ajoutez `-Dorg.gradle.java.home=/usr/lib/jvm/java-25-openjdk-amd64`.

## Installation

`Settings → Plugins → ⚙ → Install Plugin from Disk…` → choisir le ZIP → redémarrer si l'IDE le demande
(le Verifier indique que le plugin est rechargeable sans redémarrage).

## Configuration

- **Couleur depuis l'éditeur** : placez le curseur juste avant un `$`, `Alt+Entrée` → *Change the color of the $ sign…* → choisissez la couleur ; appliquée immédiatement et mémorisée. Cette couleur **prend le dessus** sur celle du schéma ; `Settings → Tools → PHP Dollar Highlight → Reset custom color` revient au schéma. (Le schéma intégré, ex. Darcula, est en lecture seule : l'API publique ne permet pas de le modifier proprement, d'où ce réglage propre au plugin.)
- **Couleur via le schéma** : `Settings → Editor → Color Scheme → PHP Dollar Highlight` (aperçu intégré). « Restore defaults »
  du schéma rétablit `#C792EA`. Valeurs par défaut fournies pour les thèmes Default et Darcula ; appliquées sans redémarrage.
- **Activer/désactiver** : `Settings → Tools → PHP Dollar Highlight` (persistant, appliqué sans redémarrage).

Choix : la page `PHP` du Color Scheme est fournie par PhpStorm et n'est pas extensible par API publique ; une page
sœur est donc utilisée pour la couleur, et une page Tools pour l'interrupteur (un `ColorSettingsPage` n'a pas de case à cocher).

## Désinstallation

`Settings → Plugins → Installed → PHP Dollar Highlight → Uninstall`.

## Tests

`DollarAnnotatorTest` (15 tests, sur le vrai moteur de highlighting PHP) : variables locales, paramètres, propriétés,
superglobales, `$this`, variables dynamiques, chaînes, regex, commentaires, chaînes interpolées, activation/désactivation,
couleur personnalisée, nom de variable intact. Chaque test vérifie que le `TextRange` fait exactement 1 caractère `$`.

## Limitations connues

- Dans les chaînes interpolées, `"$name"`, `"{$obj->p}"` et `"${name}"` sont colorés (ce sont de vraies variables) ;
  un `$` échappé (`\$`) ne l'est pas.
- Les `$` des commentaires PHPDoc ne sont pas colorés (testé). Heredoc/nowdoc : comportement non couvert par les tests.
- Non testé manuellement dans l'interface graphique de PhpStorm ; seuls les tests automatisés et le Plugin Verifier ont été exécutés.
