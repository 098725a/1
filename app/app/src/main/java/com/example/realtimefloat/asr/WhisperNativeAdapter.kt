init {
    try {
        System.loadLibrary("native-lib")
        nativeLoaded = true
    } catch (t: Throwable) {
        // NDK/native not available on this build - fall back to stub mode
        Log.w("WhisperNativeAdapter", " stub mode", t)
        nativeLoaded = false
    }
}

// Native method declarations (only used if nativeLoaded == true)
private external fun nativeInit(modelPath: String): Boolean
private external fun nativeFeedAudio(pcm: ShortArray, len: Int)
private external fun nativeGetResult(): String?
private external fun nativeStop()

// Simple stub state for no-native environment
private var stubBuffer = StringBuilder()
private var running = false

override fun start() {
    if (nativeLoaded) {
        // native init should be called by higher-level code with model path
    } else {
        running = true
        stubBuffer.clear()
    }
}

override fun stop() {
    if (nativeLoaded) {
        nativeStop()
    } else {
        running = false
    }
}

override fun feedAudio(pcm: ShortArray) {
    if (nativeLoaded) {
        nativeFeedAudio(pcm, pcm.size)
    } else {
        // stub: append fake text when receiving audio to allow UI flow tests
        if (running) {
            stubBuffer.append("[audio ").append(pcm.size).append("] ")
        }
    }
}

fun getResult(): String? {
    return if (nativeLoaded) {
        nativeGetResult()
    } else {
        val s = stubBuffer.toString()
        if (s.isEmpty()) null else s
    }
}
