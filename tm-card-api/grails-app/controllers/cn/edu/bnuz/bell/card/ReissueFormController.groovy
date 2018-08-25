package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import cn.edu.bnuz.bell.workflow.Event
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证申请
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_WRITE")')
class ReissueFormController implements ServiceExceptionHandler {
    ReissueFormService reissueFormService
    ReissueReviewerService reissueReviewerService

    def index(String studentId) {
        renderJson reissueFormService.list(studentId)
    }

    def show(String studentId, Long id) {
        renderJson reissueFormService.getFormForShow(studentId, id)
    }

    def create(String studentId) {
        renderJson reissueFormService.getFormForCreate(studentId)
    }

    def save(String studentId) {
        def cmd = new CardReissueFormCommand()
        bindData(cmd, request.JSON)
        def form = reissueFormService.create(studentId, cmd)
        renderJson([id: form.id])
    }

    def edit(String studentId, Long id) {
        renderJson reissueFormService.getFormForEdit(studentId, id)
    }

    def update(String studentId, Long id) {
        def cmd = new CardReissueFormCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        reissueFormService.update(studentId, cmd)
        renderOk()
    }

    def delete(String studentId, Long id) {
        reissueFormService.delete(studentId, id)
        renderOk()
    }

    def patch(String studentId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                reissueFormService.submit(studentId, cmd)
                renderOk()
                break
            default:
                throw new BadRequestException()
        }
    }

    def notice() {
        renderJson reissueFormService.getNotice()
    }

    def settings() {
        renderJson reissueFormService.getSettings()
    }

    def picture(String studentId) {
        if (!reissueFormService.pictureExists(studentId)) {
            renderJson( [
                    warning: reissueFormService.getNoPictureWarning()
            ])
        } else {
            renderOk()
        }
    }

    def approvers(String studentId, Long reissueFormId) {
        renderJson reissueReviewerService.getApprovers()
    }
}
