package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 审核补办学生证申请
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_CHECK")')
class ReissueReviewController {
    ReissueReviewService reissueReviewService
    SecurityService securityService

    def index(String reviewerId) {
        def status = State.valueOf(params.status)
        def offset = params.int("offset") ?: 0
        def max = params.int("max") ?: (params.int("offset") ? 20 : Integer.MAX_VALUE)
        def forms = reissueReviewService.findAllByStatus(status, offset, max)
        def counts = reissueReviewService.getCountsByStatus()
        renderJson([counts: counts, forms: forms])
    }

    def show(String reviewerId, Long reissueReviewId, String id) {
        if (id == 'undefined') {
            renderJson reissueReviewService.getFormForReview(reviewerId, reissueReviewId)
        } else {
            renderJson reissueReviewService.getFormForReview(reviewerId, reissueReviewId, UUID.fromString(id))
        }
    }

    def patch(Long reissueReviewId, String id, String op) {
        def userId = securityService.userId
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = reissueReviewId
                reissueReviewService.accept(cmd, userId, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = reissueReviewId
                reissueReviewService.reject(cmd, userId, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        renderOk()
    }
}
