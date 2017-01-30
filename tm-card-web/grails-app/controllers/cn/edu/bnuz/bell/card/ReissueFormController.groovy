package cn.edu.bnuz.bell.card

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 学生补办学生证申请。
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_WRITE")')
class ReissueFormController {
    def index() { }
}
