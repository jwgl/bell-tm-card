package cn.edu.bnuz.bell.card

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 查看补办学生证申请
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_APPROVE")')
class ReissueController {
    def show() { }
}
