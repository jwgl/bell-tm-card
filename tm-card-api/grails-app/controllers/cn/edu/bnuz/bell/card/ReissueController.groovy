package cn.edu.bnuz.bell.card

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证查看
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_APPROVE")')
class ReissueController {
    ReissueService reissueService

    def show(Long id) {
        renderJson reissueService.getForm(id)
    }
}
