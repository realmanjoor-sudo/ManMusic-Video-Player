package com.manmusic.videoplayer.data.media

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.manmusic.videoplayer.data.room.SongEntity

sealed class RingtoneResult {
    data class Success(val typeName: String) : RingtoneResult()
    data class PermissionRequired(val intent: Intent) : RingtoneResult()
    data class Error(val message: String) : RingtoneResult()
}

object RingtoneHelper {
    fun setAsRingtoneOrAlarm(context: Context, song: SongEntity, isAlarm: Boolean = false): RingtoneResult {
        val type = if (isAlarm) RingtoneManager.TYPE_ALARM else RingtoneManager.TYPE_RINGTONE
        val typeLabel = if (isAlarm) "Alarm Sound" else "Phone Ringtone"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return RingtoneResult.PermissionRequired(intent)
        }

        return try {
            val uri = Uri.parse(song.contentUri)
            RingtoneManager.setActualDefaultRingtoneUri(context, type, uri)
            RingtoneResult.Success(typeLabel)
        } catch (e: Exception) {
            RingtoneResult.Error("Failed to apply: ${e.localizedMessage}")
        }
    }
}