package tech.skot.core

/**
 * Caractéristiques de l'exécution en cours, connues seulement au lancement de l'application.
 *
 * `debug` est posé côté Android par `SKEnvInitProvider`, avant `Application.onCreate()`. Sur les
 * autres plateformes, aucune détection n'est faite et la valeur reste `false` : c'est le cas des
 * tests JVM.
 */
object SKEnv {
    var debug: Boolean = false
}
