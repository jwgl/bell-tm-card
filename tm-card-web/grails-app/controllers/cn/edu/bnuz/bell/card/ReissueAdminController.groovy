package cn.edu.bnuz.bell.card

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证申请（管理员）。
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_CHECK")')
class ReissueAdminController {

    def index() {}

    def show() {}
}
