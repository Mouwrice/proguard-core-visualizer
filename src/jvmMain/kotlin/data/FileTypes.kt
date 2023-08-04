package data

/**
 * Enum class to keep track of the file types that are supported.
 * Some file types can only be read, others can only be written in the editor.
 */
enum class FileTypes(val extension: String, val canRead: Boolean, val canWrite: Boolean) {
    AAR("aar", true, false),
    APK("apk", true, false),
    CLASS("class", true, false),
    DEX("dex", true, false),
    JAR("jar", true, false),
    JSON("json", true, true),
    ZIP("zip", true, false),

    // Following specs: https://github.com/Guardsquare/proguard-assembler/blob/master/docs/md/specification.md
    JBC("jbc", false, true),
}
