package app.synco.shizuku

class ShizukuInstall(
    private val hasPackage: (String) -> Boolean,
    private val hasProvider: (String) -> Boolean,
) {
    fun isInstalled(binderAlive: Boolean): Boolean = when {
        binderAlive -> true
        PACKAGES.any { safely { hasPackage(it) } } -> true
        else -> AUTHORITIES.any { safely { hasProvider(it) } }
    }

    private fun safely(probe: () -> Boolean): Boolean = runCatching(probe).getOrDefault(false)

    companion object {
        val PACKAGES = listOf(
            "moe.shizuku.privileged.api",
            "moe.shizuku.manager",
        )

        val AUTHORITIES = listOf(
            "moe.shizuku.privileged.api.shizuku",
            "moe.shizuku.manager.shizuku",
        )
    }
}
