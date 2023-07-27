package data

import com.google.gson.Gson

data class StateTracker(val codeAttributes: List<CodeAttributeRecord>) {
    companion object {
        /**
         * Tries to parse the json file at the given path
         * into a StateTracker.
         */
        fun fromJson(json: String): StateTracker {
            return Gson().fromJson(json, StateTracker::class.java)
        }
    }
}
