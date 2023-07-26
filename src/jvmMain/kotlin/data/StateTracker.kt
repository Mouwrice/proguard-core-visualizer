package data

import com.google.gson.Gson

data class StateTracker(val codeAttributes: List<CodeAttributeRecord>) {
    companion object {
        /**
         * Tries to parse json into a StateTracker.
         */
        fun fromJson(json: String): StateTracker {
            return Gson().fromJson(json, StateTracker::class.java)
        }
    }
}
