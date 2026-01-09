package com.codeboy.mediafacerkotlin.utils
import android.content.Context
import android.content.SharedPreferences
import com.codeboy.mediafacer.models.AudioContent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MusicDataUtil(val context: Context) {

    private val PREFS_FILENAME = "com.codeboy.mediafacerkotlin.userPrefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    private val LAST_PLAYLIST = "PlayerList"
    var lastPlaylist :String?
        get() = prefs.getString(LAST_PLAYLIST, "")
        set(value) = prefs.edit().putString(LAST_PLAYLIST, value).apply()

    fun getLastPlaylist(): ArrayList<AudioContent>{
        var serializedPlaylist: ArrayList<AudioContent> = ArrayList()
        val gson = Gson()
        val json: String = lastPlaylist ?: ""
        gson.fromJson<ArrayList<AudioContent>>(json, object : TypeToken<ArrayList<AudioContent>>(){}.type)
            .also { serializedPlaylist = it?: ArrayList() }
        return serializedPlaylist
    }

    fun saveLastPlaylist(playlist: ArrayList<AudioContent>){
        val gson = Gson()
        val playlistJson: String = gson.toJson(playlist)
        lastPlaylist = playlistJson
    }
}