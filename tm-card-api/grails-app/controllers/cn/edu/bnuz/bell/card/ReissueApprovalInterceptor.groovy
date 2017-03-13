package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.http.HttpStatus

class ReissueApprovalInterceptor {
    SecurityService securityService

    boolean before() {
        if (params.approverId != securityService.userId) {
            render(status: HttpStatus.FORBIDDEN)
            return false
        } else {
            return true
        }
    }

    boolean after() { true }

    void afterView() {}
}
