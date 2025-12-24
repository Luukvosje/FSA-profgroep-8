import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.IOException

object PhotoUtils {

    fun getRequiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
            else -> arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    fun hasAllPermissions(context: Context): Boolean {
        val hasCamera =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        val hasGallery =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                        ) == PackageManager.PERMISSION_GRANTED
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

        return hasCamera
    }

    fun createImageUri(context: Context): Uri? {
        return try {
            val timeStamp = createTimestamp()
            val imageFileName = "CarImage_${timeStamp}.jpg"
            val storageDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(storageDir, imageFileName)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            val file = File(
                context.cacheDir,
                "upload_${System.currentTimeMillis()}.jpg"
            )

            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createTimestamp(): String {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "%04d%02d%02d_%02d%02d%02d".format(
            now.year, now.month.number, now.day,
            now.hour, now.minute, now.second
        )
    }
}
