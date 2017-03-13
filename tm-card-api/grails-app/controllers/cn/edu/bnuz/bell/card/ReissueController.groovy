package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.workflow.State
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证查看
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_APPROVE")')
class ReissueController {
    ReissueService reissueService

    def index() {
        def status = State.valueOf(params.status)
        def forms = reissueService.findAllByStatus(status)
        renderJson([forms: forms])
    }

    def show(Long id) {
        renderJson reissueService.getForm(id)
    }
}
