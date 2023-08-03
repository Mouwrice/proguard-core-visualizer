package ui.fileview

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import viewmodel.FilesViewModel

/**
 * Build a branch of the tree and recursively call itself for all children.
 * Returns a list of all the NodeStates from the current branch.
 */
fun buildTreeBranch(
    viewModel: FilesViewModel,
    packageState: PackageState,
    indentation: Dp,
    registerChange: (PackageState) -> Unit,
): List<NodeState> {
    val nodeStates = mutableListOf<NodeState>()

    // Display the root node
    val root = NodeState(
        packageState.name,
        indentation,
        if (packageState.expanded) IconMode.Open else IconMode.Closed,
        // If the entry is a file, you can close it
        closeCallback = if (packageState.isFileEntry) {
            {
                viewModel.closeFile(packageState.path.path)
            }
        } else {
            null
        },
        onClick = {
            registerChange(packageState.toggleExpanded())
        },
    )

    nodeStates.add(root)

    // If expanded, show children
    if (packageState.expanded) {
        // Display all subpackages
        packageState.subPackages.forEach { (subPackageName, subPackage) ->
            val nodes = buildTreeBranch(viewModel, subPackage, indentation + 12.dp) {
                registerChange(
                    PackageState(
                        packageState.name,
                        expanded = true,
                        packageState.path,
                        packageState.isFileEntry,
                        packageState.subPackages.plus(Pair(subPackageName, it)).toSortedMap(),
                        packageState.containingClasses,
                        packageState.clazz,
                    ),
                )
            }
            nodeStates.addAll(nodes)
        }

        // Display all containing classes
        packageState.containingClasses.forEach { (subPackageName, subPackage) ->
            val nodes = buildTreeBranch(viewModel, subPackage, indentation + 12.dp) {
                registerChange(
                    PackageState(
                        packageState.name,
                        expanded = true,
                        packageState.path,
                        packageState.isFileEntry,
                        packageState.subPackages,
                        packageState.containingClasses.plus(Pair(subPackageName, it)).toSortedMap(),
                        packageState.clazz,
                    ),
                )
            }
            nodeStates.addAll(nodes)
        }

        // Display the methods of the current class (the above 2 maps should be empty in this case)
        packageState.clazz?.let { ownClazz ->
            ownClazz.methodMap.forEach { (_, method) ->
                val node = NodeState(
                    method.name,
                    indentation + 12.dp,
                    // Go through name since selection of a method that need to be evaluated will change the method instance
                    iconMode = if (viewModel.curPath?.path == packageState.path.path && viewModel.curClazz?.name == ownClazz.name && viewModel.curMethod?.name == method.name) {
                        IconMode.Selected
                    } else {
                        IconMode.Unselected
                    },
                    onClick = {
                        viewModel.curPath = packageState.path
                        viewModel.curClazz = ownClazz
                        viewModel.curMethod = method
                    },
                )
                nodeStates.add(node)
            }
        }
    }

    return nodeStates
}
