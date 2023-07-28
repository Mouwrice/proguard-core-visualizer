package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.CodeAttributeRecord
import java.nio.file.Path

class File(val path: Path, codeAttributes: List<CodeAttributeRecord> = emptyList()) {
    var codeAttributeViewModels by mutableStateOf(codeAttributes.map { CodeAttributeViewModel(it) })
}
