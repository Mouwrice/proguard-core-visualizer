package ui.fileview

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.LoadedClass
import data.LoadedPath
import viewmodel.FilesViewModel
import java.util.*

// Can be either a package or a class
data class PackageState(
    val name: String,
    val expanded: Boolean,
    val path: LoadedPath,
    val isFileEntry: Boolean,

    // Need to make distinction because of for example:
    // data.BranchTargetRecord.ErrorRecord
    // data.BranchTargetRecord
    val subPackages: SortedMap<String, PackageState>,
    val containingClasses: SortedMap<String, PackageState>,

    val clazz: LoadedClass?,
) {
    /**
     * Collapse this Package and all children.
     */
    private fun collapseRecursive(): PackageState {
        return PackageState(
            name,
            false,
            path,
            isFileEntry,
            subPackages.mapValues { it.value.collapseRecursive() }.toSortedMap(),
            containingClasses.mapValues { it.value.collapseRecursive() }.toSortedMap(),
            clazz,
        )
    }

    /**
     * Toggle expanded state, always makes sure children are collapsed.
     */
    private fun toggleExpanded(): PackageState {
        return PackageState(
            name,
            !expanded,
            path,
            isFileEntry,
            subPackages.mapValues { it.value.collapseRecursive() }.toSortedMap(),
            containingClasses.mapValues { it.value.collapseRecursive() }.toSortedMap(),
            clazz,
        )
    }

    /**
     * Build a branch of the tree and recursively call itself for all children.
     * Returns a list of all the NodeStates from the current branch.
     *
     * @param searchQuery If not empty, only show nodes that match the query. Or branches that contain nodes that match the query.
     */
    fun buildTreeBranch(
        viewModel: FilesViewModel,
        searchQuery: String,
        indentation: Dp,
        registerChange: (PackageState) -> Unit,
    ): List<NodeState> {
        // Display the root node
        val root = NodeState(
            name,
            indentation,
            if (expanded) IconMode.Open else IconMode.Closed,
            // If the entry is a file, you can close it
            closeCallback = if (isFileEntry) {
                {
                    viewModel.closeFile(path.path)
                }
            } else {
                null
            },
            onClick = {
                registerChange(toggleExpanded())
            },
        )

        val childPackages = mutableListOf<NodeState>()
        val childClasses = mutableListOf<NodeState>()
        val childMethods = mutableListOf<NodeState>()

        // If expanded or if the search query is not empty
        // Go through all children
        if (expanded || searchQuery != "") {
            // All subpackages
            subPackages.forEach { (subPackageName, subPackage) ->
                childPackages.addAll(
                    subPackage.buildTreeBranch(viewModel, searchQuery, indentation + 12.dp) {
                        registerChange(
                            PackageState(
                                name,
                                expanded,
                                path,
                                isFileEntry,
                                subPackages.plus(Pair(subPackageName, it)).toSortedMap(),
                                containingClasses,
                                clazz,
                            ),
                        )
                    },
                )
            }

            // All containing classes
            containingClasses.forEach { (subPackageName, subPackage) ->
                childClasses.addAll(
                    subPackage.buildTreeBranch(viewModel, searchQuery, indentation + 12.dp) {
                        registerChange(
                            PackageState(
                                name,
                                expanded,
                                path,
                                isFileEntry,
                                subPackages,
                                containingClasses.plus(Pair(subPackageName, it)).toSortedMap(),
                                clazz,
                            ),
                        )
                    },
                )
            }
        }

        // Display the methods of the current class (the above 2 maps should be empty in this case)
        clazz?.let { ownClazz ->
            ownClazz.methodMap.forEach { (_, method) ->
                // Show all methods if the search query is empty
                // or only show methods that match the search query
                if (searchQuery == "" || method.name.contains(searchQuery.toRegex())) {
                    childMethods.add(
                        NodeState(
                            method.name,
                            indentation + 12.dp,
                            // Go through name since selection of a method that need to be evaluated will change the method instance
                            iconMode = if (viewModel.curPath?.path == path.path && viewModel.curClazz?.name == ownClazz.name && viewModel.curMethod?.name == method.name) {
                                IconMode.Selected
                            } else {
                                IconMode.Unselected
                            },
                            onClick = {
                                viewModel.curPath = path
                                viewModel.curClazz = ownClazz
                                viewModel.curMethod = method
                            },
                        ),
                    )
                }
            }
        }

        // There are no children to display
        if (childPackages.isEmpty() && childClasses.isEmpty() && childMethods.isEmpty() && searchQuery != "") {
            // The node is hidden if it has no children to display and does not contain the search query
            if (!name.contains(searchQuery.toRegex())) {
                return emptyList()
            }
        }

        return listOf(root) + childPackages + childClasses + childMethods
    }

    companion object {
        /**
         * Helper method to create a map of [PackageState]s from a LoadedPath.
         */
        fun sortByPackage(
            path: LoadedPath,
            classes: Map<String, LoadedClass>,
            classState: Map<String, PackageState>?,
        ): SortedMap<String, PackageState> {
            /**
             * Inner helper method for recursion.
             * Groups together all classes by their packages.
             */
            fun inner(
                classes: List<Pair<List<String>, LoadedClass>>,
                classState: Map<String, PackageState>?,
            ): SortedMap<String, PackageState> {
                // Classes is a list of to be handled classes, a pair of what packages remain and the class itself
                return classes.groupBy { it.first[0] }.map { (packageName, entries) ->
                    // All entries share the first element of their name list
                    Pair(
                        packageName,
                        PackageState(
                            packageName,
                            classState?.get(packageName)?.expanded ?: false,
                            path,
                            false,
                            inner(
                                entries
                                    // If the child is a package, it has the current package, at least one next package and in the end, a class.
                                    .filter { it.first.size > 2 }
                                    .map {
                                        Pair(
                                            it.first.slice(IntRange(1, it.first.size - 1)),
                                            it.second,
                                        )
                                    },
                                classState?.get(packageName)?.subPackages,
                            ),
                            inner(
                                entries
                                    // If the child is a class, it has the current package, followed by the class name.
                                    .filter { it.first.size == 2 }
                                    .map {
                                        Pair(
                                            it.first.slice(IntRange(1, it.first.size - 1)),
                                            it.second,
                                        )
                                    },
                                classState?.get(packageName)?.containingClasses,
                            ),

                            // The current "package" is not followed, this is not a package, but a class, put it in.
                            entries
                                .filter { it.first.size == 1 }.getOrNull(0)?.second,
                        ),
                    )
                }.toMap().toSortedMap()
            }
            return inner(classes.map { Pair(it.key.split("/"), it.value) }, classState)
        }
    }
}
