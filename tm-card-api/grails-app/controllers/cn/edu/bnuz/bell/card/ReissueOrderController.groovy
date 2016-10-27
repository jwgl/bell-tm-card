package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证订单（管理员）
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_CHECK")')
class ReissueOrderController implements ServiceExceptionHandler {
    SecurityService securityService
    ReissueOrderService reissueOrderService

    def index() {
        renderJson reissueOrderService.getAll()
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
        switch (request.JSON.type) {
            case 'RECEIVE':
                status = reissueOrderService.receive(userId, id, request.JSON.formId, request.JSON.received)
                break
            case 'RECEIVE_ALL':
                status = reissueOrderService.receiveAll(userId, id, request.JSON.received)
                break
            default:
                throw new BadRequestException()
        }
        renderJson([status: status])
    }
}
