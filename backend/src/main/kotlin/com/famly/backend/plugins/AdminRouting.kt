package com.famly.backend.plugins

import com.famly.backend.admin.configureAdminWebRouting
import com.famly.backend.services.AdminService
import com.famly.backend.services.AuthService
import com.famly.backend.services.HouseholdService
import com.famly.backend.services.SubscriptionService
import io.ktor.server.routing.*

fun Route.configureAdminRouting(
    authService: AuthService,
    adminService: AdminService,
    subscriptionService: SubscriptionService,
    householdService: HouseholdService,
) {
    route("/admin") {
        configureAdminWebRouting(
            authService = authService,
            adminService = adminService,
            householdService = householdService,
            subscriptionService = subscriptionService,
        )
    }
}
