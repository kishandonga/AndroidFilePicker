package com.kdonga.filepicker.utility

enum class SizeUnit(private val inBytes: Long) {
    B(1),
    KB(SizeUnit.BYTES),
    MB(SizeUnit.BYTES * SizeUnit.BYTES),
    GB(SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES),
    TB(SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES);

    fun inBytes(): Long {
        return inBytes
    }

    companion object {
        private const val BYTES: Long = 1024

        fun formatFileSize(size: Long): String {
            return if (size < 1024) {
                String.format("%d B", size)
            } else if (size < 1024 * 1024) {
                String.format("%.1f KB", size / 1024.0f)
            } else if (size < 1024 * 1024 * 1024) {
                String.format("%.1f MB", size.toFloat() / 1024.0f / 1024.0f)
            } else {
                String.format("%.1f GB", size.toFloat() / 1024.0f / 1024.0f / 1024.0f)
            }
        }
    }
}
