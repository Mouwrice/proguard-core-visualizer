package data

enum class FileTypes(val extension: String) {
    AAR("aar"),
    APK("apk"),
    CLASS("class"),
    DEX("dex"),
    JAR("jar"),
    JSON("json"),
    ZIP("zip"),

    // Following specs: https://github.com/Guardsquare/proguard-assembler/blob/master/docs/md/specification.md
    JBC("jbc"),
}
