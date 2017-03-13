package cn.edu.bnuz.bell.card

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证审批。
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_APPROVE")')
class ReissueApprovalController {
    def index() {}
}
