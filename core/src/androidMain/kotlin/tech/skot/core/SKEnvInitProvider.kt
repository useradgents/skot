package tech.skot.core

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.net.Uri

/**
 * Pose [SKEnv.debug] au plus tôt.
 *
 * Android crée les ContentProviders avant `Application.onCreate()`, la valeur est donc déjà
 * disponible pour tout le code d'initialisation de l'application — y compris celui qui s'exécute
 * avant la construction de l'injecteur.
 *
 * `FLAG_DEBUGGABLE` est lu plutôt que `BuildConfig.DEBUG` : `buildConfig` est désactivé par défaut
 * depuis AGP 8, la classe `BuildConfig` peut donc ne pas exister.
 */
class SKEnvInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        SKEnv.debug = context?.applicationInfo?.let { it.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0 } == true
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
