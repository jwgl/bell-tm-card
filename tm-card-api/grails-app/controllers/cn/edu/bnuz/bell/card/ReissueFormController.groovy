package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import cn.edu.bnuz.bell.workflow.Event
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证申请（学生）
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_WRITE")')
class ReissueFormController implements ServiceExceptionHandler {
    ReissueFormService reissueFormService

    def index(String userId) {
        Student student = Student.findById(userId)
        if (student == null) {
            throw new NotFoundException()
        }

        renderJson([
                student: [
                        id      : student.id,
                        atSchool: student.atSchool,
                        picture : reissueFormService.pictureExists(student.id)
                ],
                forms  : reissueFormService.getForms(student.id)
        ])
    }

    def show(String userId, Long id) {
        renderJson reissueFormService.getFormForShow(userId, id)
    }

    def create(String userId) {
        renderJson reissueFormService.getFormForCreate(userId)
    }

    def save(String userId) {
        def form = reissueFormService.create(userId, request.JSON.reason as String)
        renderJson([id: form.id])
    }

    def edit(String userId, Long id) {
        renderJson reissueFormService.getFormForEdit(userId, id)
    }

    def update(String userId, Long id) {
        reissueFormService.update(userId, id, request.JSON.reason as String)
        renderOk()
    }

    def delete(String userId, Long id) {
        reissueFormService.delete(userId, id)
        renderOk()
    }

    def patch(String userId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                reissueFormService.submit(userId, cmd)
                renderOk()
                break
            default:
                throw new BadRequestException()
        }
    }

    def checkers() {
        renderJson reissueFormService.getCheckers()
    }
}
