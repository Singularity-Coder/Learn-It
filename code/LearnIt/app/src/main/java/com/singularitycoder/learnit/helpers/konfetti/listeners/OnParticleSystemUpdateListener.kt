package com.singularitycoder.learnit.helpers.konfetti.listeners

import com.singularitycoder.learnit.helpers.konfetti.core.Party
import com.singularitycoder.learnit.helpers.konfetti.KonfettiView

/**
 * Created by dionsegijn on 5/31/17.
 */
interface OnParticleSystemUpdateListener {
    fun onParticleSystemStarted(
        view: KonfettiView,
        party: Party,
        activeSystems: Int,
    )

    fun onParticleSystemEnded(
        view: KonfettiView,
        party: Party,
        activeSystems: Int,
    )
}
