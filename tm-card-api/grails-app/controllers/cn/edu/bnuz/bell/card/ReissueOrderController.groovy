package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.workflow.State
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证订单
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_APPROVE")')
class ReissueOrderController implements ServiceExceptionHandler {
    SecurityService securityService
    ReissueOrderService reissueOrderService

    def index() {
        def offset = params.int('offset') ?: 0
        def max = params.int('max') ?: 10
        renderCount reissueOrderService.listCount()
        renderJson reissueOrderService.list(offset, max)
    }

    def show(Long id) {
        renderJson reissueOrderService.getInfo(id)
    }

    def save() {
        def userId = securityService.userId
        def cmd = new CardReissueOrderCommand()
        bindData cmd, request.JSON
        def order = reissueOrderService.create(userId, cmd)
        renderJson([id: order.id])
    }

    def edit(Long id) {
        renderJson reissueOrderService.getInfo(id)
    }

    def update(Long id) {
        def userId = securityService.userId
        def cmd = new CardReissueOrderCommand()
        bindData cmd, request.JSON
        cmd.id = id
        reissueOrderService.update(userId, cmd)
        renderOk()
    }

    def delete(Long id) {
        reissueOrderService.delete(id)
        renderOk()
    }

    def patch(Long id) {
        def userId = securityService.userId
        def status
        switch (request.JSON.type as String) {
            case 'RECEIVE':
                status = reissueOrderService.receive(userId, id, request.JSON.formId as Long, request.JSON.received as boolean)
                break
            case 'RECEIVE_ALL':
                status = reissueOrderService.receiveAll(userId, id, request.JSON.received as boolean)
                break
            default:
                throw new BadRequestException()
        }
        renderJson([status: status])
    }

    def unorderedForms() {
        renderJson([forms: reissueOrderService.getUnorderedForms()])
    }


}
