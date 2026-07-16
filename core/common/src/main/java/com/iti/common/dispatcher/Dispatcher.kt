package com.iti.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val careerPilotDispatcher: CareerPilotDispatchers)

enum class CareerPilotDispatchers {
    Default,
    IO,
}
