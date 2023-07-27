package data

import com.google.gson.Gson
import java.io.BufferedReader
import java.nio.file.Path
import kotlin.io.path.reader

data class StateTracker(val codeAttributes: List<CodeAttributeRecord>) {
    companion object {
        /**
         * Tries to parse the json file at the given path
         * into a StateTracker.
         */
        fun fromJson(json: Path): StateTracker {
            return Gson().fromJson(BufferedReader(json.reader()), StateTracker::class.java)
        }

        /**
         * Parse from a json string.
         */
        fun fromJson(json: String): StateTracker {
            return Gson().fromJson(json, StateTracker::class.java)
        }
    }
}
