package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.workflow.State
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 查看补办学生证申请
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_CHECK")')
class ReissueController {
    ReissueService reissueService
    ReissueFormService reissueFormService

    def index() {
        def status = State.valueOf(params.status)
        def forms = reissueService.findAllByStatus(status)
        renderJson([forms: forms])
    }

    def show(Long id) {
        renderJson reissueFormService.getFormInfo(id)
    }
}
