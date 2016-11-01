package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证申请（管理员）
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_CHECK")')
class ReissueAdminController {
    ReissueAdminService reissueAdminService
    SecurityService securityService

    def index() {
        def status = State.valueOf(params.status)
        // 当参数中没有offset和max时，表示不分页
        def offset = params.int("offset") ?: 0
        def max = params.int("max") ?: (params.int("offset") ? 20 : Integer.MAX_VALUE)
        def forms = reissueAdminService.findAllByStatus(status, offset, max)
        def counts = reissueAdminService.getCountsByStatus()
        renderJson([counts: counts, forms: forms])
    }

    def show(Long id) {
        renderJson reissueAdminService.getFormInfo(securityService.userId, id);
    }

    def patch(Long reissueAdminId, String id, String op) {
        def userId = securityService.userId
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = reissueAdminId
                reissueAdminService.accept(cmd, userId, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = reissueAdminId
                reissueAdminService.reject(cmd, userId, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        renderOk()
    }
}
