package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证审批
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_APPROVE")')
class ReissueApprovalController {
    ReissueApprovalService reissueApprovalService

    def index(String approverId, ListCommand cmd) {
        renderJson reissueApprovalService.list(approverId, cmd)
    }

    def show(String approverId, Long reissueApprovalId, String id, String type) {
        ListType listType = Enum.valueOf(ListType, type)
        if (id == 'undefined') {
            renderJson reissueApprovalService.getFormForReview(approverId, reissueApprovalId, listType)
        } else {
            renderJson reissueApprovalService.getFormForReview(approverId, reissueApprovalId, listType, UUID.fromString(id))
        }
    }

    def patch(String approverId, Long reissueApprovalId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = reissueApprovalId
                reissueApprovalService.accept(cmd, approverId, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = reissueApprovalId
                reissueApprovalService.reject(cmd, approverId, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(approverId, reissueApprovalId, id, 'todo')
    }
}
